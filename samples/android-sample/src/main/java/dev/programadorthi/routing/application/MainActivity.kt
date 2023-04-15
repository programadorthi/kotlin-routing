package dev.programadorthi.routing.application

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.programadorthi.routing.application.activity.ComposableActivity
import dev.programadorthi.routing.application.activity.HomeActivity
import dev.programadorthi.routing.application.ui.theme.RoutingApplicationTheme
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.pushNamed
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.route

// Simulating loading routes on demand. Useful for things like Dynamic Feature
private fun Route.loadHomeRoutes(context: Context) {
    route(path = "/home", name = "home") {
        push {
            context.startActivity(Intent(context, HomeActivity::class.java))
        }

        replace {
            context.currentActivity?.finish()
            context.startActivity(Intent(context, HomeActivity::class.java))
        }

        pop {
            // Will have a pop method when /home is the last path only
            context.currentActivity?.finish()
        }
    }
}

private fun Route.loadReplaceRoutes(context: Context) {
    route(path = "/replace", name = "replace") {
        push {
            val intent = Intent(context, ComposableActivity::class.java)
            call.parameters.forEach { key, values ->
                if (values.size <= 1) {
                    intent.putExtra(key, values.firstOrNull() ?: "")
                } else {
                    intent.putExtra(key, values.toTypedArray())
                }
            }
            context.startActivity(intent)
        }

        replace {
            context.currentActivity?.finish()
            val intent = Intent(context, ComposableActivity::class.java)
            call.parameters.forEach { key, values ->
                if (values.size <= 1) {
                    intent.putExtra(key, values.firstOrNull() ?: "")
                } else {
                    intent.putExtra(key, values.toTypedArray())
                }
            }
            context.startActivity(intent)
        }

        pop {
            // Will have a pop method when /home is the last path only
            context.currentActivity?.finish()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WARNING: putting routes with same name will throw an exception
        // You must be carefully about this and unregister them
        router.unregisterNamed(name = "home")
        router.unregisterNamed(name = "replace")
        router.loadHomeRoutes(this)
        router.loadReplaceRoutes(this)

        setContent {
            RoutingApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainComposable()
                }
            }
        }
    }
}

@Composable
fun MainComposable(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Welcome to routing sample",
            modifier = modifier
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Home and Composable activities routes loaded",
            modifier = modifier
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            context.router.pushNamed(name = "home")
        }) {
            Text(text = "GO TO HOME ACTIVITY")
        }
    }
}
