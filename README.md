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

> Keep reading to see what kotlin routing can provide

## Defining routes
> Based on [Ktor Routing](https://ktor.io/docs/routing-in-ktor.html)

All route definition provided by Ktor Routing is supported by Kotlin Routing.

```kotlin
val router = routing {
    route("/hello", RouteMethod.Empty) {
        handle {
            // Well, there is no respond* because we are not a server library
        }
    }
}
```

### Shortly version

```Kotlin
val router = routing {
    handle("/hello") {
        // Handle any call to the "/hello" route
    }
}
```

It's also possible to define a name to navigate instead of using the path value.
```Kotlin
route(path = "/hello", name = "hello") {
    // ...
}
```

[Type-safe](https://github.com/programadorthi/kotlin-routing/edit/main/README.md#type-safe-routing) navigation is also supported.

### Getting route detail 
Use `call` inside of handle block to get all route details available

```Kotlin
handle(path = "/path") {
    val application = call.application
    val routeMethod = call.routeMethod
    val name = call.name
    val uri = call.uri
    val attributes = call.attributes
    val parameters = call.parameters // routing parameters (see Routing routes) plus query parameters when provided
}
```

### Redirecting route
You can redirect from anywhere with an `ApplicationCall`:

```Kotlin
handle(...) {
    call.redirectToPath(path = "/path-destination")
    // or
    call.redirectToName(name = "destination-name")
}
```

## Routing routes

```kotlin
val router = routing {
    // ...
}

// Routing by uri
router.call(uri = "/path")

// Routing by uri with parameters
router.call(uri = "/path", parameters = parametersOf("number", listOf("123")))

// Routing by name
router.call(name = "name")

// Routing by name with parameters
router.call(name = "name", parameters = parametersOf("number", listOf("123")))

// Routing by a route method
router.call(uri = "/path", routeMethod = RouteMethod("custom name"))
```

## Type-safe routing (resources module)

> Based on [Ktor Type-safe routing](https://ktor.io/docs/type-safe-routing.html)

```kotlin
val router = routing {
    install(Resources)

    handle<Articles> {
        // handle any call to Articles
    }
}

// And do:
router.call(Articles())
```

## Exception routing handler (status-pages module)

> Based on [Ktor Status pages](https://ktor.io/docs/status-pages.html)

```kotlin
val router = routing {
    install(StatusPages) {
        // Catch any exception (change to be specific if you need)
        exception<Throwable> { call, cause ->
            // exception handled
        }
    }

    handle(path = "/hello") {
        throw IllegalArgumentException("simulating an exception thrown on routing")
    }
}

// And to simulate
router.call(uri = "/hello")
```

## Events module

An extension module to help working with events, using name instead of paths.
You can use it to sent or connect your event based system: Analytics, MVI, etc.

```kotlin
val router = routing {
    event(name = "event_name") {
        // Handle your event here
        call.redirectToEvent(name = "other_event_name") // If you need redirect from one to another
    }
}

// To emit events call:
router.emitEvent(
    name = "event_name",
    parameters = parametersOf(...),
)
```

## Nested Routing

With nested routing you can connect one `Routing` to another. It is good for projects that have routes on demand
as Android Dynamic Feature that each module has your own navigation and are loaded at runtime.
Checkout `RoutingTest` for more usages.

```kotlin
val parent = routing { }

val router = routing(
    rootPath = "/child",
    parent = parent,
) { }
```

## Compose Routing (compose module)

Are you using Compose Jetpack or Multiplatform? This module is for you.
Easily route any composable you have just doing:

```kotlin
val routing = routing { 
    composable(path = "/login") {
        // Your composable or any compose behavior here 
    }
}

@Composable
fun MyComposeApp() {
    Routing(routing = routing) {
        // Initial content
    }
}

// And in any place that have the routing instance call:
routing.call(uri = "/login")
```

## Other modules to interest

- `auth` - [Authentication and Authorization](https://ktor.io/docs/authentication.html)
- `call-logging` - [Call Logging](https://ktor.io/docs/call-logging.html)
- `sessions` - [Sessions](https://ktor.io/docs/sessions.html)

## Limitations

- Any type-safe behavior combined with Nested routing does not support navigation from parent to child using the Type. You have to use path routing.
```kotlin
@Resource("/endpoint")
class Endpoint

val parent = routing { }

val router = routing(
    rootPath = "/child",
    parent = parent,
) {
    handle<Endpoint> {
        // ...
    }
}

// IT WORKS
router.call(Endpoint())

// IT DOES NOT WORK
parent.call(Endpoint())

// IT WORKS
parent.call(uri = "/child/endpoint")
```
