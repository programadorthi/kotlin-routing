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

## Integration modules

> These kind of modules are inspirations showing how do you own integration with the target framework.

### Compose Routing (compose module)

> This module is just for study or simple compose application.
> I recommend use Voyager module for more robust application.

Are you using Jetpack or Multiplatform Compose Runtime only? This module is for you.
Easily route any composable you have just doing:

```kotlin
val routing = routing {
    composable(path = "/login") {
        // Your composable or any compose behavior here 
        call.popped // True if it was popped
        val result = call.popResult<T>() // To get the pop result after pop one composable
        val typedValue = call.resource<T>() // To get the type-safe navigated value
    }
}

@Composable
fun MyComposeApp() {
    Routing(routing = routing, initial = {
        // Initial content
        LocalRouting.current // Available inside compositions to do routing
    })
}

// And in any place that have the routing instance call:
routing.push(path = "/login")

val lastPoppedCall = routing.poppedCall() // The call that was popped after call `routing.pop()`
val result = lastPoppedCall?.popResult<T>() // To get the result after call `routing.pop(result = T)`
```

### Compose Animation (compose-animation module)

> This module is just for study or simple compose application.
> I recommend use Voyager module for more robust application.
> At the moment Compose Multiplatform Animation (not the routing module) has limited targets and it 
> is not available to all routing targets

Are you using Jetpack or Multiplatform Compose that requires animation? This module is for you.
Easily route any composable you have just doing:

```kotlin
val routing = routing { 
    // You can override global behaviors to each composable
    composable(
        path = "/login",
        enterTransition = {...},
        exitTransition = {...},
        popEnterTransition = {...},
        popExitTransition = {...},
    ) {
        // Your composable or any compose behavior here 
        call.animatedVisibilityScope // If you need do something during animation
    }
}

@Composable
fun MyComposeApp() {
    Routing(
        routing = routing,
        enterTransition = {...},    // on enter next composable in forward direction
        exitTransition = {...},     // on exit current composable in forward direction
        popEnterTransition = {...}, // on enter previous composable in backward direction
        popExitTransition = {...},  // on exit current composable in backward direction
        initial = {
        // Initial animated content
    })
}

// And in any place that have the routing instance call:
routing.push(path = "/login")
```

> The kotlin-routing author is not expert in Compose Animation. So, yes, the behavior here is close
> to [Navigation with Compose](https://developer.android.com/jetpack/compose/navigation) and will help people that come from it.

### Web Routing (javascript module still in development)

> Are you building a DOM application? This module is for you.

```kotlin
val routing = routing {
    jsRoute(path = "/page1") {
        // create and return your DOM Element
    }
    jsRoute(path = "/page2") {
        // create and return your DOM Element
    }
}

fun main() {
    render(
        routing = routing,
        root = document.getElementById("root") ?: document.create.div(),
        initial = document.create.h1 {
            +"I am the initial content"
            onClickFunction = {
                routing.push(path = "/page1")
            }
        }
    )
}

// And in any place that have the routing instance call:
routing.push(path = "/page2")
```

### Voyager Routing (voyager module)

> Are you building a Voyager application and need routing support? This module is for you.

```kotlin
val routing = routing { 
    screen(path = "/page1") {
        // create and return your Screen instance
    }
}

@Composable
fun App() {
    VoyagerRouting(
        routing = routing,
        initialScreen = SplashScreen() // The first screen rendered
        ... // Any other Voyager related config
    )
}

// And in any place that have the routing instance call:
routing.push(path = "/page1")
```

Voyager is a screen based library. So the result put in a pop call is passed to the screen and not
to the composition. And here it is different from `compose` module. To get the result after a pop call 
do the previous screen implement `VoyagerRoutingPopResult<T>`. Its `onResult` function will be called 
on any successfully `pop()` or `popUntil()` to the previous screen.
