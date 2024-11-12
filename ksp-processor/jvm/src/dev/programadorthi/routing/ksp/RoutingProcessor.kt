package dev.programadorthi.routing.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.programadorthi.routing.annotation.Route

public class RoutingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RoutingProcessor(logger = environment.logger)
    }
}

private class RoutingProcessor(
    private val logger: KSPLogger
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        resolver
            .getSymbolsWithAnnotation(Route::class.java.name)
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { func ->
                logger.warn(">>>> $func")
            }

        return emptyList()
    }

}
