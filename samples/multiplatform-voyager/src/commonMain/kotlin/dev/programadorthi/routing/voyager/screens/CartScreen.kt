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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.programadorthi.routing.core.pop
import dev.programadorthi.routing.voyager.VoyagerLocalRouter
import dev.programadorthi.routing.voyager.model.ViewModel

class CartScreen : Screen {

    @Composable
    override fun Content() {
        val router = VoyagerLocalRouter.currentOrThrow
        val cart = remember { ViewModel.cart }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
            ) {
                items(items = cart.keys.toList()) { index ->
                    Row {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(cart[index]!!)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Item #${index + 1}",
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = {
                            cart.remove(index)
                        }) {
                            Text(text = "REMOVE")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = {
                cart.clear()
            }) {
                Text(text = "CLEAR CART")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = {
                router.pop()
            }) {
                Text(text = "POP CART SCREEN")
            }
        }
    }

}