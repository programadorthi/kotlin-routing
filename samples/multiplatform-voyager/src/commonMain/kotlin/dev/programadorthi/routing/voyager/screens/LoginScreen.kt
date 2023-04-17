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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.replaceAllNamed
import dev.programadorthi.routing.voyager.VoyagerLocalRouter
import dev.programadorthi.routing.voyager.model.ViewModel

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val router = VoyagerLocalRouter.currentOrThrow
        var signedIn by remember { ViewModel.signedIn }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Welcome to Login Screen")
            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Maybe here should have a Username input")
            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Maybe here should have a Password input")
            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Click below to replace all with Home Screen by name")
            Button(onClick = {
                signedIn = true
                router.replaceAllNamed(name = "home")
            }) {
                Text(text = "LOGIN AND REPLACE ALL WITH HOME SCREEN")
            }
            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Click below to pop Login Screen")
            Button(onClick = {
                router.pop()
            }) {
                Text(text = "POP LOGIN SCREEN")
            }
            Spacer(modifier = Modifier.height(15.dp))
        }
    }

}