package dev.programadorthi.routing.voyager.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.core.pushNamed
import dev.programadorthi.routing.voyager.VoyagerLocalRouter
import dev.programadorthi.routing.voyager.model.ViewModel
import dev.programadorthi.routing.voyager.routes.loadRoutes
import dev.programadorthi.routing.voyager.routes.unLoadRoutes

class CatalogScreen : Screen {

    @Composable
    override fun Content() {
        val router = VoyagerLocalRouter.currentOrThrow
        val cart = remember { ViewModel.cart }

        // Reloading routes after Home Screen disposable
        DisposableEffect(key1 = Unit) {
            router.loadRoutes()

            onDispose {
                router.unLoadRoutes()
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
            ) {
                itemsIndexed(items = ViewModel.colors) { index, color ->
                    Row {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Item #${index + 1}",
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = {
                            val other = cart[index]
                            if (other == null) {
                                cart[index] = color
                            } else {
                                cart.remove(index)
                            }
                        }) {
                            Text(text = if (cart[index] == null) "ADD" else "REMOVE")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    if (index != ViewModel.colors.lastIndex) {
                        Divider()
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = {
                router.pushNamed(name = "cart")
            }) {
                Text(text = "PUSH CART SCREEN")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = {
                router.pop()
            }) {
                Text(text = "POP CATALOG SCREEN")
            }
        }
    }

}