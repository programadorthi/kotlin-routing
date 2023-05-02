package dev.programadorthi.routing.application

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
import dev.programadorthi.routing.android.handle
import dev.programadorthi.routing.android.unregisterActivityForAllMethod
import dev.programadorthi.routing.application.activity.ComposableActivity
import dev.programadorthi.routing.application.activity.HomeActivity
import dev.programadorthi.routing.application.ui.theme.RoutingApplicationTheme
import dev.programadorthi.routing.core.Route
import dev.programadorthi.routing.core.pushNamed
import dev.programadorthi.routing.core.route

// Simulating loading routes on demand. Useful for things like Dynamic Feature
private fun Route.loadHomeRoutes() {
    handle<HomeActivity>(path = "/home", name = "home")
}

private fun Route.loadReplaceRoutes() {
    route(path = "/replace", name = "replace") {
        handle<ComposableActivity> {
            // No-op
            // But to be a sub-route you need to provide a empty lambda :D
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WARNING: putting routes with same name will throw an exception
        // You must be carefully about this and unregister them
        router.unregisterNamed(name = "home")

        // WARNING: typed activity navigation is unique too
        // So you must unregister the activity to each RouteMethod or unregister for all
//        router.unregisterActivityForMethod(HomeActivity::class, StackRouteMethod.Push)
        router.unregisterActivityForAllMethod(HomeActivity::class)

        // Here you can unregister the type and the name in a unique function call
        router.unregisterActivityForAllMethod(ComposableActivity::class, name = "replace")

        router.loadHomeRoutes()
        router.loadReplaceRoutes()

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
