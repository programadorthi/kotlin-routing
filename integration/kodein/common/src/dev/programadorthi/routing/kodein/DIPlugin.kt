package dev.programadorthi.routing.kodein

import dev.programadorthi.routing.core.application.Application
import dev.programadorthi.routing.core.application.ApplicationCallPipeline
import dev.programadorthi.routing.core.application.BaseApplicationPlugin
import dev.programadorthi.routing.core.application.install
import dev.programadorthi.routing.kodein.DIPlugin.Plugin
import io.ktor.util.AttributeKey
import org.kodein.di.DI
import org.kodein.di.bindInstance

/**
 * Ktor [Plugin] that provide a global [DI] container
 * that would be accessible from everywhere in the Ktor application
 */
public class DIPlugin {
    /**
     * Configure the [DI] container then put it in the [Application.attributes],
     * thus it would be easily accessible (e.g. [Application.di]
     */
    internal companion object Plugin :
        BaseApplicationPlugin<ApplicationCallPipeline, DI.MainBuilder, DIPlugin> {
        override val key: AttributeKey<DIPlugin> = AttributeKey("DIPlugin")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: DI.MainBuilder.() -> Unit,
        ): DIPlugin {
            pipeline.attributes.put(KodeinDIKey, DI.lazy { configure() })

            return DIPlugin()
        }
    }
}

/**
 * Installs a [DIPlugin] feature for the this [Application] and runs a [configuration] script on it
 *
 * @throws [dev.programadorthi.routing.core.application.DuplicatePluginException] if the plugin has already been installed.
 */
public fun Application.di(configuration: DI.MainBuilder.() -> Unit): DIPlugin =
    install(DIPlugin) {
        bindInstance { this@di }
        configuration()
    }
