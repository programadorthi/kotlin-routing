package dev.programadorthi.routing.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.Path
import dev.programadorthi.routing.annotation.Route
import dev.programadorthi.routing.core.application.Application
import io.ktor.http.Parameters
import io.ktor.util.Attributes

public class RoutingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RoutingProcessor(codeGenerator = environment.codeGenerator)
    }
}

private class RoutingProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val ksFiles = mutableSetOf<KSFile>()
        val configureSpec = FunSpec
            .builder("configure")
            .addModifiers(KModifier.INTERNAL)
            .receiver(dev.programadorthi.routing.core.Route::class)

        resolver
            .getSymbolsWithAnnotation(Route::class.java.name)
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { func ->
                val qualifiedName = func.qualifiedName?.asString()
                check(func.functionKind == FunctionKind.TOP_LEVEL) {
                    "$qualifiedName fun must be a top level fun"
                }
                check(func.getVisibility() != Visibility.PRIVATE) {
                    "$qualifiedName fun must not be private"
                }
                func.containingFile?.let(ksFiles::add)
                func.wrapFunctionWithHandle(qualifiedName, configureSpec, resolver)
            }

        configureSpec
            .build()
            .generateFile(ksFiles = ksFiles)

        return emptyList()
    }

    @OptIn(KspExperimental::class)
    private fun KSFunctionDeclaration.wrapFunctionWithHandle(
        qualifiedName: String?,
        configureSpec: FunSpec.Builder,
        resolver: Resolver
    ) {
        val routeAnnotation = checkNotNull(getAnnotationsByType(Route::class).firstOrNull()) {
            "Invalid state because a @Route was not found to '$qualifiedName'"
        }
        val isRegexRoute = routeAnnotation.regex.isNotBlank()
        check(isRegexRoute || routeAnnotation.path.isNotBlank()) {
            "Using @Route a path or a regex is required"
        }
        check(!isRegexRoute || routeAnnotation.name.isBlank()) {
            "@Route with regex can't be named"
        }

        if (isRegexRoute) {
            if (routeAnnotation.method.isBlank()) {
                configureSpec
                    .beginControlFlow(
                        "%M(path = %T(%S))",
                        handle,
                        Regex::class,
                        routeAnnotation.regex
                    )
            } else {
                val template =
                    """%M(path = %T(%S), method = %M(value = "${routeAnnotation.method}"))"""
                configureSpec
                    .beginControlFlow(
                        template,
                        handle,
                        Regex::class,
                        routeAnnotation.regex,
                        routeMethod
                    )
            }
        } else {
            val named = when {
                routeAnnotation.name.isBlank() -> "name = null"
                else -> """name = "${routeAnnotation.name}""""
            }
            if (routeAnnotation.method.isBlank()) {
                configureSpec
                    .beginControlFlow("%M(path = %S, $named)", handle, routeAnnotation.path)
            } else {
                val template =
                    """%M(path = %S, $named, method = %M(value = "${routeAnnotation.method}"))"""
                configureSpec
                    .beginControlFlow(template, handle, routeAnnotation.path, routeMethod)
            }
        }

        val codeBlock = generateHandleBody(isRegexRoute, routeAnnotation, resolver, qualifiedName)

        configureSpec
            .addCode(codeBlock)
            .endControlFlow()
    }

    private fun KSFunctionDeclaration.generateHandleBody(
        isRegexRoute: Boolean,
        routeAnnotation: Route,
        resolver: Resolver,
        qualifiedName: String?
    ): CodeBlock {
        val funcMember = MemberName(packageName.asString(), simpleName.asString())
        val funcBuilder = CodeBlock.builder()
        val hasZeroOrOneParameter = parameters.size < 2
        if (hasZeroOrOneParameter) {
            funcBuilder.add(FUN_INVOKE_START, funcMember)
        } else {
            funcBuilder
                .addStatement(FUN_INVOKE_START, funcMember)
                .indent()
        }

        for (param in parameters) {
            check(param.isVararg.not()) {
                "Vararg is not supported as fun parameter"
            }
            var applied = param.tryApplyCallProperty(hasZeroOrOneParameter, resolver, funcBuilder)
            if (!applied) {
                applied = param.tryApplyBody(hasZeroOrOneParameter, funcBuilder)
            }
            if (!applied && !isRegexRoute) {
                applied = param.tryApplyTailCard(
                    routePath = routeAnnotation.path,
                    resolver = resolver,
                    hasZeroOrOneParameter = hasZeroOrOneParameter,
                    builder = funcBuilder,
                )
            }
            if (!applied) {
                param.tryApplyPath(
                    isRegexRoute = isRegexRoute,
                    routeAnnotation = routeAnnotation,
                    qualifiedName = qualifiedName,
                    resolver = resolver,
                    hasZeroOrOneParameter = hasZeroOrOneParameter,
                    builder = funcBuilder,
                )
            }
        }

        if (hasZeroOrOneParameter) {
            funcBuilder.addStatement(FUN_INVOKE_END)
        } else {
            funcBuilder
                .unindent()
                .addStatement(FUN_INVOKE_END)
        }

        return funcBuilder.build()
    }

    @OptIn(KspExperimental::class)
    private fun KSValueParameter.tryApplyPath(
        isRegexRoute: Boolean,
        routeAnnotation: Route,
        qualifiedName: String?,
        resolver: Resolver,
        hasZeroOrOneParameter: Boolean,
        builder: CodeBlock.Builder
    ) {
        val paramName = name?.asString()
        val customName = getAnnotationsByType(Path::class)
            .firstOrNull()
            ?.value
            ?: paramName
        val isRegex = isRegexRoute && routeAnnotation.regex.contains("(?<$customName>")
        val isOptional = !isRegex && routeAnnotation.path.contains("{$customName?}")
        val isRequired = !isRegex && routeAnnotation.path.contains("{$customName}")
        check(isRegex || isOptional || isRequired) {
            "'$qualifiedName' has parameter '$paramName' that is not declared as path parameter {$customName}"
        }
        val literal = when {
            isOptional -> resolver.builtIns.optionalParse(type.resolve())
            else -> resolver.builtIns.requiredParse(type.resolve())
        }
        when {
            hasZeroOrOneParameter -> builder.add(
                PATH_TEMPLATE,
                paramName,
                call,
                customName,
                literal
            )

            else -> builder.addStatement(PATH_TEMPLATE, paramName, call, customName, "$literal,")
        }
    }

    @OptIn(KspExperimental::class)
    private fun KSValueParameter.tryApplyTailCard(
        routePath: String,
        resolver: Resolver,
        hasZeroOrOneParameter: Boolean,
        builder: CodeBlock.Builder,
    ): Boolean {
        val paramName = name?.asString()
        val customName = getAnnotationsByType(Path::class)
            .firstOrNull()
            ?.value
            ?: paramName
        if (routePath.contains("{$customName...}").not()) {
            return false
        }
        val listDeclaration = checkNotNull(resolver.getClassDeclarationByName<List<*>>()) {
            "Class declaration not found to List<String>?"
        }
        val paramType = type.resolve()
        check(paramType.declaration == listDeclaration) {
            "TailCard parameter must be a List<String>?"
        }
        val genericArgument =
            checkNotNull(type.element?.typeArguments?.firstOrNull()?.type?.resolve()) {
                "No <String> type found at tailcard parameter"
            }
        check(genericArgument == resolver.builtIns.stringType) {
            "TailCard list item type must be non nullable String"
        }
        check(paramType.isMarkedNullable) {
            "TailCard list must be nullable as List<String>?"
        }

        when {
            hasZeroOrOneParameter -> builder.add(TAILCARD_TEMPLATE, paramName, call, customName, "")
            else -> builder.addStatement(TAILCARD_TEMPLATE, paramName, call, customName, ",")
        }

        return true
    }

    @OptIn(KspExperimental::class)
    private fun KSValueParameter.tryApplyBody(
        hasZeroOrOneParameter: Boolean,
        builder: CodeBlock.Builder,
    ): Boolean {
        if (getAnnotationsByType(Body::class).none()) {
            return false
        }
        val paramName = name?.asString()
        val paramType = type.resolve()
        val member = when {
            paramType.isMarkedNullable -> receiveNullable
            else -> receive
        }
        when {
            hasZeroOrOneParameter -> builder.add(BODY_TEMPLATE, paramName, call, member, "")
            else -> builder.addStatement(BODY_TEMPLATE, paramName, call, member, ",")
        }
        return true
    }

    private fun KSValueParameter.tryApplyCallProperty(
        hasZeroOrOneParameter: Boolean,
        resolver: Resolver,
        builder: CodeBlock.Builder,
    ): Boolean {
        val paramType = type.resolve()
        val propertyName = when (paramType.declaration) {
            resolver.getClassDeclarationByName<Application>() -> "application"
            resolver.getClassDeclarationByName<Parameters>() -> "parameters"
            resolver.getClassDeclarationByName<Attributes>() -> "attributes"
            else -> return false
        }
        val paramName = name?.asString()
        when {
            hasZeroOrOneParameter -> builder.add(CALL_PROPERTY_TEMPLATE, paramName, call, propertyName, "")
            else -> builder.addStatement(CALL_PROPERTY_TEMPLATE, paramName, call, propertyName, ",")
        }
        return true
    }

    private fun FunSpec.generateFile(ksFiles: Set<KSFile>) {
        FileSpec
            .builder(
                packageName = "dev.programadorthi.routing.generated",
                fileName = "ModuleRoutes"
            )
            .addFileComment("Generated by Kotlin Routing")
            .addFunction(this)
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                dependencies = Dependencies(false, *ksFiles.toTypedArray())
            )
    }

    private fun KSBuiltIns.optionalParse(paramType: KSType): String = when (paramType) {
        booleanType.makeNullable() -> "?.toBooleanOrNull()"
        byteType.makeNullable() -> "?.toByteOrNull()"
        charType.makeNullable() -> "?.firstOrNull()"
        doubleType.makeNullable() -> "?.toDoubleOrNull()"
        floatType.makeNullable() -> "?.toFloatOrNull()"
        intType.makeNullable() -> "?.toIntOrNull()"
        longType.makeNullable() -> "?.toLongOrNull()"
        shortType.makeNullable() -> "?.toShortOrNull()"
        stringType.makeNullable() -> ""
        else -> error("Path parameter must be primitive type only")
    }

    private fun KSBuiltIns.requiredParse(paramType: KSType): String = when (paramType) {
        booleanType -> "!!.toBoolean()"
        byteType -> "!!.toByte()"
        charType -> "!!.first()"
        doubleType -> "!!.toDouble()"
        floatType -> "!!.toFloat()"
        intType -> "!!.toInt()"
        longType -> "!!.toLong()"
        shortType -> "!!.toShort()"
        stringType -> "!!"
        else -> optionalParse(paramType)
    }

    private companion object {
        private val handle = MemberName("dev.programadorthi.routing.core", "handle")
        private val routeMethod = MemberName("dev.programadorthi.routing.core", "RouteMethod")
        private val call = MemberName("dev.programadorthi.routing.core.application", "call")
        private val receive = MemberName("dev.programadorthi.routing.core.application", "receive")
        private val receiveNullable =
            MemberName("dev.programadorthi.routing.core.application", "receiveNullable")

        private const val CALL_PROPERTY_TEMPLATE = """%L = %M.%L%L"""
        private const val BODY_TEMPLATE = "%L = %M.%M()%L"
        private const val FUN_INVOKE_END = ")"
        private const val FUN_INVOKE_START = "%M("
        private const val PATH_TEMPLATE = """%L = %M.parameters["%L"]%L"""
        private const val TAILCARD_TEMPLATE = """%L = %M.parameters.getAll("%L")%L"""
    }

}
