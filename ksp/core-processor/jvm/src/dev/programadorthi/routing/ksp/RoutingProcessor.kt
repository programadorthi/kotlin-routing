package dev.programadorthi.routing.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.programadorthi.routing.annotation.Body
import dev.programadorthi.routing.annotation.Path
import dev.programadorthi.routing.annotation.Route

public class RoutingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RoutingProcessor(
            codeGenerator = environment.codeGenerator,
            options = environment.options,
            logger = environment.logger,
        )
    }
}

private class RoutingProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private var invoked = false

    private val fileName: String
        get() = (options[FLAG_ROUTING_MODULE_NAME] ?: "Module") + "Routes"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val ksFiles = mutableSetOf<KSFile>()
        val configureSpec = FunSpec
            .builder("configure")
            .addModifiers(KModifier.INTERNAL)
            .receiver(route)

        resolver
            .getSymbolsWithAnnotation(Route::class.java.name)
            .filterIsInstance<KSDeclaration>()
            .forEach { symbol ->
                symbol.transform(ksFiles, configureSpec, resolver)
            }

        configureSpec
            .build()
            .generateFile(ksFiles = ksFiles)

        return emptyList()
    }

    @OptIn(KspExperimental::class)
    private fun KSDeclaration.transform(
        ksFiles: MutableSet<KSFile>,
        configureSpec: FunSpec.Builder,
        resolver: Resolver
    ) {
        val qualifiedName = qualifiedName?.asString() ?: return
        logger.info(">>>> transforming: $qualifiedName")
        check(getVisibility() != Visibility.PRIVATE) {
            "$qualifiedName must not be private"
        }
        containingFile?.let(ksFiles::add)
        val routeAnnotation = checkNotNull(getAnnotationsByType(Route::class).firstOrNull()) {
            "Invalid state because is missing @Route to '$qualifiedName'"
        }
        when (this) {
            is KSFunctionDeclaration -> {
                check(functionKind == FunctionKind.TOP_LEVEL) {
                    "$qualifiedName must be a top level fun"
                }
                logger.info(">>>> transforming fun: $qualifiedName")
                wrapFunctionWithHandle(routeAnnotation, qualifiedName, configureSpec, resolver, null)
            }

            is KSClassDeclaration -> {
                check(classKind == ClassKind.OBJECT || classKind == ClassKind.CLASS) {
                    "$qualifiedName must be a class or object. ${classKind.type} is not supported"
                }
                check(
                    superTypes.any { type ->
                        type.resolve().declaration.qualifiedName?.asString() == VOYAGER_SCREEN_QUALIFIED_NAME
                    }
                ) {
                    "@Route can be applied to object or class that inherit from '$VOYAGER_SCREEN_QUALIFIED_NAME' only"
                }
                logger.info(">>>> transforming class: $qualifiedName")
                declarations
                    .filterIsInstance<KSFunctionDeclaration>()
                    .filter { func -> func.simpleName.asString() == CONSTRUCTOR_NAME }
                    .forEach { constructor ->
                        val annotation = constructor.getAnnotationsByType(Route::class).firstOrNull() ?: routeAnnotation
                        constructor.wrapFunctionWithHandle(
                            annotation,
                            qualifiedName,
                            configureSpec,
                            resolver,
                            classKind
                        )
                    }
            }

            else -> error("$qualifiedName is not supported. Class and top level fun are supported only")
        }
    }

    private fun KSFunctionDeclaration.wrapFunctionWithHandle(
        routeAnnotation: Route,
        qualifiedName: String,
        configureSpec: FunSpec.Builder,
        resolver: Resolver,
        classKind: ClassKind?,
    ) {
        val isRegexRoute = routeAnnotation.regex.isNotBlank()
        check(isRegexRoute || routeAnnotation.path.isNotBlank()) {
            "@Route requires a path or a regex"
        }
        check(!isRegexRoute || routeAnnotation.name.isBlank()) {
            "@Route having regex can't be named"
        }

        val isScreen = classKind != null
        val memberName = when {
            annotations.any { it.shortName.asString() == "Composable" } -> composable
            isScreen -> screen
            else -> handle
        }

        if (isRegexRoute) {
            check(!isScreen) {
                // TODO: Add regex support to composable handle
                "$qualifiedName has @Route(regex = ...) that cannot be applied to @Composable or Voyager Screen"
            }
            if (routeAnnotation.method.isBlank()) {
                configureSpec
                    .beginControlFlow(
                        "%M(path = %T(%S))",
                        memberName,
                        Regex::class,
                        routeAnnotation.regex
                    )
            } else {
                val template =
                    """%M(path = %T(%S), method = %M(value = "${routeAnnotation.method}"))"""
                configureSpec
                    .beginControlFlow(
                        template,
                        memberName,
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
            logger.info(">>>> transforming -> name: $named and member: $memberName")
            if (routeAnnotation.method.isBlank()) {
                configureSpec
                    .beginControlFlow("%M(path = %S, $named)", memberName, routeAnnotation.path)
            } else {
                val template =
                    """%M(path = %S, $named, method = %M(value = "${routeAnnotation.method}"))"""
                configureSpec
                    .beginControlFlow(template, memberName, routeAnnotation.path, routeMethod)
            }
        }

        val codeBlock = generateHandleBody(isRegexRoute, routeAnnotation, resolver, qualifiedName, classKind)

        configureSpec
            .addCode(codeBlock)
            .endControlFlow()
    }

    private fun KSFunctionDeclaration.generateHandleBody(
        isRegexRoute: Boolean,
        routeAnnotation: Route,
        resolver: Resolver,
        qualifiedName: String,
        classKind: ClassKind?,
    ): CodeBlock {
        val funcBuilder = CodeBlock.builder()
        val hasZeroOrOneParameter = parameters.size < 2
        val funName = simpleName.asString()
        val member: Any = when {
            classKind != null -> ClassName(packageName.asString(), qualifiedName.split(".").last())
            else -> MemberName(packageName.asString(), funName)
        }
        val template = when (classKind) {
            ClassKind.OBJECT -> FUN_TYPE_INVOKE
            ClassKind.CLASS -> FUN_TYPE_INVOKE_START
            else -> FUN_MEMBER_INVOKE_START
        }
        logger.info(">>>> fun name: $funName -> template: $template -> member: $member")
        when {
            classKind == ClassKind.OBJECT -> funcBuilder.addStatement(template, member)
            hasZeroOrOneParameter -> funcBuilder.add(template, member)
            else ->
                funcBuilder
                    .addStatement(template, member)
                    .indent()
        }

        for (param in parameters) {
            check(param.isVararg.not()) {
                "Vararg is not supported as fun parameter"
            }
            var applied = param.tryApplyCallProperty(hasZeroOrOneParameter, funcBuilder)
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

        if (classKind == ClassKind.OBJECT) {
            return funcBuilder.build()
        }

        if (hasZeroOrOneParameter.not()) {
            funcBuilder.unindent()
        }

        return funcBuilder
            .addStatement(FUN_INVOKE_END)
            .build()
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
        builder: CodeBlock.Builder,
    ): Boolean {
        val paramType = type.resolve()
        val paramName = name?.asString()
        val asTypeName = paramType.toTypeName()
        if (asTypeName == applicationCall) {
            when {
                hasZeroOrOneParameter -> builder.add(CALL_TEMPLATE, paramName, call, "")
                else -> builder.addStatement(CALL_TEMPLATE, paramName, call, ",")
            }
            return true
        }

        val propertyName = when (asTypeName) {
            application -> "application"
            parameters -> "parameters"
            attributes -> "attributes"
            else -> return false
        }
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
                fileName = fileName,
            )
            .addFileComment("Generated by Kotlin Routing")
            .addFunction(this)
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                dependencies = Dependencies(true, *ksFiles.toTypedArray())
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
        private val route = ClassName("dev.programadorthi.routing.core", "Route")
        private val application = ClassName("dev.programadorthi.routing.core.application", "Application")
        private val applicationCall = ClassName("dev.programadorthi.routing.core.application", "ApplicationCall")
        private val parameters = ClassName("io.ktor.http", "Parameters")
        private val attributes = ClassName("io.ktor.util", "Attributes")

        private val screen = MemberName("dev.programadorthi.routing.voyager", "screen")
        private val composable = MemberName("dev.programadorthi.routing.compose", "composable")
        private val handle = MemberName("dev.programadorthi.routing.core", "handle")
        private val routeMethod = MemberName("dev.programadorthi.routing.core", "RouteMethod")
        private val call = MemberName("dev.programadorthi.routing.core.application", "call")
        private val receive = MemberName("dev.programadorthi.routing.core.application", "receive")
        private val receiveNullable =
            MemberName("dev.programadorthi.routing.core.application", "receiveNullable")

        private const val CALL_TEMPLATE = """%L = %M%L"""
        private const val CALL_PROPERTY_TEMPLATE = """%L = %M.%L%L"""
        private const val BODY_TEMPLATE = "%L = %M.%M()%L"
        private const val FUN_INVOKE_END = ")"
        private const val FUN_MEMBER_INVOKE_START = "%M("
        private const val FUN_TYPE_INVOKE = "%T"
        private const val FUN_TYPE_INVOKE_START = "$FUN_TYPE_INVOKE("
        private const val PATH_TEMPLATE = """%L = %M.parameters["%L"]%L"""
        private const val TAILCARD_TEMPLATE = """%L = %M.parameters.getAll("%L")%L"""

        private const val FLAG_ROUTING_MODULE_NAME = "Routing_Module_Name"

        private const val VOYAGER_SCREEN_QUALIFIED_NAME = "cafe.adriel.voyager.core.screen.Screen"

        private const val CONSTRUCTOR_NAME = "<init>"
    }
}
