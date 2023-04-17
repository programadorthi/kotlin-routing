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
    route(path = "/login") {
        handle {
          // Handle the call to the routing "/login"
        }
    }
}

// And to call login routing...
router.execute(
    call = MyCustomApplicationCall(
        routeMethod = MyCustomRouteMethod(),
        uri = "/login"
    )
)
```

## Core-stack module

This module is a core extension providing Stack based behaviors (push, pop, replace, ...)
So, if you need stack behaviors and avoid creating your custom ApplicationCall? It is for you.

> Keep reading to see how use stack routing

## Defining routes
> Based on [Ktor Routing](https://ktor.io/docs/routing-in-ktor.html)

```kotlin
val router = routing {
    route(path = "/path") {
        // ...
    }
}

```
It's also possible to define a name to navigate instead of using the path value.
```Kotlin
route(path = "/path", name = "path") {
    // ...
}
```

[Type-safe](https://github.com/programadorthi/kotlin-routing/edit/main/README.md#type-safe-routing) navigation is also supported.

## Handling routes
Since you defined your routes, it's time to handle them. Use the `handle` block to do that.
```kotlin
val router = routing {
    route(path = "/path") {
        handle {
            // Handle any call to the "/path" route
        }
    }
}  
```
Using core-stack module, it's possible to define an action for each StackRouteMethod available
```Kotlin
(StackRouteMethod.Pop, StackRouteMethod.Push, StackRouteMethod.Replace, StackRouteMethod.ReplaceAll)

route(path = "/path2") {
    push { }   // handle push to this route only
    replace { }  // handle replace to this route only
    replaceAll { }  // handle replaceAll to this route only
    pop { }  // handle pop to this route only
}
```

### Handling route short version

```Kotlin
handle(path = "/path3", name = "path3") {

}
push(path = "/path4", name = "path4") {

}
replace(path = "/path5", name = "path5") {

}
replaceAll(path = "/path6", name = "path6") {

}
// Pop can not be named
pop(path = "/path7") {

}
```
### Getting route detail 
Use `call` inside of handle block or any `StackRouteMethod` (core-stack) to get all details available of a route that was called

```Kotlin
val application = call.application
val routeMethod = call.routeMethod
val uri = call.uri
val attributes = call.attributes
val parameters = call.parameters // routing parameters (see Routing routes) plus query parameters when provided
```

### Redirecting route
Using handle, any RouteMethod or short version you can redirect one route to another

```Kotlin
route|handle|push|replace|replaceAll|pop(...) {
    redirectToPath(path = "/path-destination")
    // or
    redirectToName(name = "destination-name")
}
```
### Regex route
You can also create a route using regex.

```Kotlin
route|handle|push|replace|replaceAll|pop(path = Regex(...)) {
    // ...
}
```
## Routing routes

> There is no behavior in Ktor for that

```kotlin
val router = routing {
    // ...
}

// Pushing a path
router.push(path = "/path")

// Pushing a path with parameters
router.push(path = "/path", parameters = parametersOf("number", "123"))

// Pushing a name
router.pushNamed(name = "name")

// Pushing a name with parameters
router.pushNamed(name = "name", parameters = parametersOf("number", "123"))

// Pushing a name with parameters and path parameters ("/path/{id}")
router.pushNamed(
    name = "name",
    parameters = parametersOf("id", "456"), // It will be used to replace {id} path parameter
)

// Replacing or replacing all works the same as push but 'replace' instead push :D
router.replace(...)
router.replaceAll(...)

// Popping the last pushed or replaced route
router.pop()

// Popping the last pushed or replaced route with parameters
router.pop(parameters = parametersOf("number", "123"))
```

## Type-safe routing

> Based on [Ktor Type-safe routing](https://ktor.io/docs/type-safe-routing.html)
>
> First you have to put module `resources` as a dependency
> There is a `resources-stack` module with stack behaviors

```kotlin

@Resource("/articles")
class Articles {

    @Resource("new")
    class New(val parent: Articles = Articles())

    @Resource("{id}")
    class Id(val parent: Articles = Articles(), val id: Long) {

        @Resource("edit")
        class Edit(val parent: Id)
    }
}

val router = routing {
    install(Resources)

    handle<Articles> {
        // handle any call to Articles
    }
}

// And do:
router.execute(Articles())
```

## Exception routing handler

> Based on [Ktor Status pages](https://ktor.io/docs/status-pages.html)
>
> First you have to put module `status-pages` as a dependency

```kotlin
val router = routing {
    install(StatusPages) {
        // Catch any exception (change to be specific if you need)
        exception<Throwable> { call, cause ->
            // exception handled
        }
    }

    handle(path = "/path") {
        throw IllegalArgumentException("simulating an exception thrown on routing")
    }
}

// Pushing to simulate
router.execute(
    call = MyCustomApplicationCall(
        routeMethod = MyCustomRouteMethod(),
        uri = "/path"
    )
)
```

## Voyager module

A [Voyager](https://github.com/adrielcafe/voyager/) extension to do navigation using routes.
See unit tests for more details.

## Next steps

[ ] - Helper modules for native platform navigation (Android, iOS, Web, Desktop, ...)

[ ] - Deep Link support by platform

[ ] - More plugins like Session, CallLogging, etc

[ ] - Status-pages redirect support
