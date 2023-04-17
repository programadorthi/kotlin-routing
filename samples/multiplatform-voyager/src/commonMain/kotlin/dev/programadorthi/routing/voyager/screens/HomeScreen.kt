package dev.programadorthi.routing.voyager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.programadorthi.routing.core.push
import dev.programadorthi.routing.core.pushNamed
import dev.programadorthi.routing.voyager.VoyagerLocalRouter
import dev.programadorthi.routing.voyager.model.ViewModel
import dev.programadorthi.routing.voyager.routes.loadRoutes
import dev.programadorthi.routing.voyager.routes.unLoadRoutes

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val router = VoyagerLocalRouter.currentOrThrow
        val signedIn by remember { ViewModel.signedIn }

        // Simulate loading routes on demand
        DisposableEffect(key1 = Unit) {
            router.loadRoutes()

            onDispose {
                router.unLoadRoutes()
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Welcome to Home Screen")
            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Click below to push ${if (signedIn) "Logout" else "Login"} Screen by path")
            Button(onClick = {
                router.push(path = "/${if (signedIn) "logout" else "login"}")
            }) {
                Text(text = "PUSH ${if (signedIn) "LOGOUT" else "LOGIN"} SCREEN")
            }
            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Click below to push Catalog Screen by name")
            Button(onClick = {
                router.pushNamed(name = "catalog")
            }) {
                Text(text = "PUSH CATALOG SCREEN")
            }
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}