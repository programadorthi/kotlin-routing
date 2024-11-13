package dev.programadorthi.routing.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.programadorthi.routing.annotation.Path
import dev.programadorthi.routing.annotation.Route

public class RoutingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RoutingProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}

private class RoutingProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private var invoked = false

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val call = MemberName("dev.programadorthi.routing.core.application", "call")
        val handle = MemberName("dev.programadorthi.routing.core", "handle")

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

                check(func.packageName.asString().isNotBlank()) {
                    "Top level fun '$qualifiedName' must have a package"
                }

                val routeAnnotation = checkNotNull(func.getAnnotationsByType(Route::class).firstOrNull()) {
                    "Invalid state because a @Route was not found to '$qualifiedName'"
                }
                val isRegexRoute = routeAnnotation.regex.isNotBlank()
                check(isRegexRoute || routeAnnotation.path.isNotBlank()) {
                    "Using @Route a path or a regex is required"
                }
                check(!isRegexRoute || routeAnnotation.name.isBlank()) {
                    "@Route using regex can't be named"
                }

                val parameters = mutableListOf<String>()

                for (param in func.parameters) {
                    check(param.isVararg.not()) {
                        "Vararg is not supported as fun parameter"
                    }
                    val paramName = param.name?.asString()
                    val customName = param
                        .getAnnotationsByType(Path::class)
                        .firstOrNull()
                        ?.value
                        ?: paramName
                    val paramType = param.type.resolve()
                    if (!isRegexRoute && routeAnnotation.path.contains("{$customName...}")) {
                        val listDeclaration = checkNotNull(resolver.getClassDeclarationByName<List<*>>()) {
                            "Class declaration not found to List<String>?"
                        }
                        check(paramType.declaration == listDeclaration) {
                            "Tailcard parameter must be a List<String>?"
                        }
                        val genericArgument =
                            checkNotNull(param.type.element?.typeArguments?.firstOrNull()?.type?.resolve()) {
                                "No <String> type found at tailcard parameter"
                            }
                        check(genericArgument == resolver.builtIns.stringType) {
                            "Tailcard list items type must be non nullable String"
                        }
                        check(paramType.isMarkedNullable) {
                            "Tailcard list must be nullable as List<String>?"
                        }
                        parameters += """$paramName = %M.parameters.getAll("$customName")"""
                        continue
                    }

                    val isRegex = isRegexRoute && routeAnnotation.regex.contains("(?<$customName>")
                    val isOptional = !isRegex && routeAnnotation.path.contains("{$customName?}")
                    val isRequired = !isRegex && routeAnnotation.path.contains("{$customName}")
                    check(isRegex || isOptional || isRequired) {
                        "'$qualifiedName' has parameter '$paramName' that is not declared as path parameter {$customName}"
                    }
                    val parsed = """$paramName = %M.parameters["$customName"]"""
                    parameters += when {
                        isOptional -> optionalParse(paramType, resolver, parsed)
                        else -> requiredParse(paramType, resolver, parsed)
                    }
                }

                val calls = Array(size = parameters.size) { call }
                val params = parameters.joinToString(prefix = "(", postfix = ")") { "\n$it" }
                val named = when {
                    routeAnnotation.name.isBlank() -> "name = null"
                    else -> """name = "${routeAnnotation.name}""""
                }

                with(configureSpec) {
                    if (isRegexRoute) {
                        beginControlFlow("""%M(%T(%S))""", handle, Regex::class, routeAnnotation.regex)
                    } else {
                        beginControlFlow("""%M(path = %S, $named)""", handle, routeAnnotation.path)
                    }
                }.addStatement("""$qualifiedName$params""", *calls)
                    .endControlFlow()
            }

        FileSpec
            .builder(
                packageName = "dev.programadorthi.routing.generated",
                fileName = "ModuleRoutes"
            )
            .addFileComment("Generated by Kotlin Routing")
            .addFunction(configureSpec.build())
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                dependencies = Dependencies.ALL_FILES
            )

        return emptyList()
    }

    private fun optionalParse(
        paramType: KSType,
        resolver: Resolver,
        parsed: String
    ) = when (paramType) {
        resolver.builtIns.booleanType.makeNullable() -> "$parsed?.toBooleanOrNull()"
        resolver.builtIns.byteType.makeNullable() -> "$parsed?.toByteOrNull()"
        resolver.builtIns.charType.makeNullable() -> "$parsed?.firstOrNull()"
        resolver.builtIns.doubleType.makeNullable() -> "$parsed?.toDoubleOrNull()"
        resolver.builtIns.floatType.makeNullable() -> "$parsed?.toFloatOrNull()"
        resolver.builtIns.intType.makeNullable() -> "$parsed?.toIntOrNull()"
        resolver.builtIns.longType.makeNullable() -> "$parsed?.toLongOrNull()"
        resolver.builtIns.shortType.makeNullable() -> "$parsed?.toShortOrNull()"
        resolver.builtIns.stringType.makeNullable() -> parsed
        else -> error("Path parameters must be primitive type only")
    }

    private fun requiredParse(
        paramType: KSType,
        resolver: Resolver,
        parsed: String
    ) = when (paramType) {
        resolver.builtIns.booleanType -> "$parsed!!.toBoolean()"
        resolver.builtIns.byteType -> "$parsed!!.toByte()"
        resolver.builtIns.charType -> "$parsed!!.first()"
        resolver.builtIns.doubleType -> "$parsed!!.toDouble()"
        resolver.builtIns.floatType -> "$parsed!!.toFloat()"
        resolver.builtIns.intType -> "$parsed!!.toInt()"
        resolver.builtIns.longType -> "$parsed!!.toLong()"
        resolver.builtIns.shortType -> "$parsed!!.toShort()"
        resolver.builtIns.stringType -> "$parsed!!"
        else -> optionalParse(paramType, resolver, parsed)
    }

}
