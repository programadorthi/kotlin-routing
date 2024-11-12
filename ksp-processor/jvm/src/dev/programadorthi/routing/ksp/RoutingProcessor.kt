package dev.programadorthi.routing.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

public class RoutingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RoutingProcessor(logger = environment.logger)
    }
}

private class RoutingProcessor(
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allFiles = resolver.getAllFiles().map { it.fileName }
        logger.info("===== files =====")
        logger.info(allFiles.joinToString(separator = "\n"))
        logger.info("===== end =====")
        return emptyList()
    }

}
