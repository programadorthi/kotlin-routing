package dev.programadorthi.routing.application.activity

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
import dev.programadorthi.routing.application.router
import dev.programadorthi.routing.application.ui.theme.RoutingApplicationTheme
import io.ktor.http.parametersOf

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RoutingApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeComposable()
                }
            }
        }
    }
}

@Composable
fun HomeComposable(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Congratulations!!!",
            modifier = modifier
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "You arrived here pushing Home Activity",
            modifier = modifier
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            context.router.replaceNamed(
                name = "replace",
                parameters = parametersOf("text", "Routing is awesome!!!!")
            )
        }) {
            Text(text = "REPLACE WITH COMPOSABLE ACTIVITY")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            context.router.pop()
        }) {
            Text(text = "POP HOME ACTIVITY")
        }
    }
}
