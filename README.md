<h1 align="center">
    Kotlin Routing
</h1>

A multiplatform, extensible, and independent routing library powered by Ktor.
Create routing independently and extend it to what you need.

## Core module

The core module is the Ktor routing engine modified to be "server" and "client".
It is abstract and ready to extend.
Using core module you can:

```kotlin
val router = routing {
    route(path = "/hello") {
        handle {
          // Handle the call to the routing "/login"
        }
    }
}

// And to call login routing...
router.call(uri = "/hello")
```

For more information check the [Wiki pages](https://github.com/programadorthi/kotlin-routing/wiki).
