package dev.programadorthi.routing.application.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.programadorthi.routing.application.currentActivity
import dev.programadorthi.routing.application.router
import dev.programadorthi.routing.application.ui.theme.RoutingApplicationTheme
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.application.call
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.pushNamed
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.replaceAllNamed
import dev.programadorthi.routing.core.replaceNamed
import dev.programadorthi.routing.core.route
import io.ktor.http.parametersOf

// Simulating a stack based navigation as:
// Jetpack Navigation, Voyager, UINavigationController (ios), any desktop stack, any web stack, etc
private val stack = mutableStateListOf<@Composable () -> Unit>()

// Simulating compose navigation without having to crate another router
private fun Route.loadComposableRoutes(context: Context) {
    route(path = "/replace/list", name = "list") {
        push {
            stack += { List() }
        }

        replace {
            stack.removeLastOrNull()
            stack += { List() }
        }

        pop {
            if (stack.size <= 1) {
                context.currentActivity?.finish()
            } else {
                stack.removeLastOrNull()
            }
        }
    }

    route(path = "/replace/{id}", name = "view") {
        push {
            stack += { View(id = call.parameters["id"]) }
        }

        replace {
            stack.removeLastOrNull()
            stack += { View(id = call.parameters["id"]) }
        }

        pop {
            if (stack.size <= 1) {
                context.currentActivity?.finish()
            } else {
                stack.removeLastOrNull()
            }
        }
    }
}

class ComposableActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WARNING: putting routes with same name will throw an exception
        // You must be carefully about this and unregister them
        router.unregisterNamed(name = "list")
        router.unregisterNamed(name = "view")
        router.loadComposableRoutes(this)

        // Setting initial screen
        stack += { Main(extras = intent.extras) }

        setContent {
            RoutingApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val current = remember { stack }

                    current.last().invoke()
                }
            }
        }
    }
}

@Composable
fun Main(extras: Bundle?, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Text sent by home parameters: ${extras?.getString("text")}",
            modifier = modifier
        )
        Text(
            text = "Click in the button below to start Compose Navigation using the same router :D",
            modifier = modifier.padding(40.dp),
        )
        Button(onClick = {
            context.router.replaceNamed(name = "list")
        }) {
            Text(text = "USE ROUTER TO REPLACE THIS COMPOSABLE")
        }
    }
}

@Composable
fun List(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp),
        ) {
            items(count = 100) { index ->
                Text(
                    text = "Item #${index + 1}",
                    modifier = modifier
                        .clickable {
                            context.router.pushNamed(
                                name = "view",
                                parameters = parametersOf("id", "${index + 1}"),
                            )
                        }
                        .padding(10.dp)
                )
                if (index < 99) {
                    Divider()
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            context.router.pop()
        }) {
            Text(text = "POP THIS COMPOSABLE")
        }
    }
}

@Composable
fun View(id: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "You are seeing details to id: #$id",
            modifier = modifier
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            context.router.pop()
        }) {
            Text(text = "BACK TO THE LIST")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            context.router.replaceAllNamed(name = "main")
        }) {
            Text(text = "REPLACE COMPOSABLE NAVIGATION WITH MAIN ACTIVITY")
        }
    }
}
