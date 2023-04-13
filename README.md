<h1 align="center">
    Kotlin Routing
</h1>

A multiplatform, extensible and independent "navigation" routing library powered by Ktor.
Create routing independent and extend to what you need.

```kotlin
val router = routing {
    route(path = "/login") {
        // handle {} is generic for any routing event (pop, push, replace, ...)
        handle {
            val application = call.application
            val routeMethod = call.routeMethod (RouteMethod.Pop, RouteMethod.Push, RouteMethod.Replace)
            val uri = call.uri
            val attributes = call.attributes // Always empty by default
            val parameters = call.parameters // All in one (path parameters ({...}) + routing parameters (push(parameters = ...)) + query parameters ("/path?q=search&name=routing"))
            
            // Here is your time
            // Are you on Android? Do your Intent or finish your Activity
            // Are you on Desktop? Use your Stack based navigation and navigate
            // Are you on iOS? Do your navigation like using UINavigationController
            // Are you on Web? Push, replace or pop your history state
        }
    }
}

// And to route do:
router.push(path = "/login")
```

## Declaring routes

> Based on [Ktor Routing](https://ktor.io/docs/routing-in-ktor.html)

```kotlin
val router = routing {
    // name is optional and used to named navigation
    route(path = "/path", name = "path") {
        handle {
            // handle any routing action to this route (pop, push, replace, ...)
        }
    }

    // name is optional and used to named navigation
    route(path = "/path2", name = "path2") {
        push {
            // handle push to this route only
        }
        replace {
            // handle replace to this route only
        }
        pop {
            // handle pop to this route only
        }
    }

    // handle is short version to combine route { handle{} }
    handle(path = "/path3", name = "path3") {
        // handle any routing action to this route (pop, push, replace, ...)
    }

    // push is short version to combine route { push{} }
    push(path = "/path4", name = "path4") {
        // handle push to this route only
    }

    // push is short version to combine route { replace{} }
    replace(path = "/path5", name = "path5") {
        // handle replace to this route only
    }

    // pop is short version to combine route { pop{} }
    // pop can not be named
    pop(path = "/path6") {
        // handle pop to this route only
    }

    // If you need redirect one route to another do:
    route|handle|push|replace|pop(...) {
        redirectToPath(path = "/path-destination")
        // or
        redirectToName(name = "destination-name")
    }

    // If you need a route Regex based do:
    route|handle|push|replace|pop(path = Regex()) {
        // ...
    }
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
    pathParameters = parametersOf("id", "456"), // It will be used to do a replace in the path
    parameters = parametersOf("number", "123"),
)

// Replacing or replacing all works the same as push but 'replace' instead push :D
router.replace(...)
router.replaceAll(...) // WARNING: replaceAll() behaves the same as replace(). See next steps below

// Popping the last pushed or replaced route
router.pop()

// Popping the last pushed or replaced route with parameters
router.pop(parameters = parametersOf("number", "123"))
```

## Type-safe routing

> Based on [Ktor Type-safe routing](https://ktor.io/docs/type-safe-routing.html)
>
> First you have to put module `resources` as a dependency

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

    push<Articles.New> {
        // handle push to this route only
    }

    replace<Articles> {
        // handle replace to this route only
    }

    // There is no use case for type-safe pop handler. Maybe in the future?
}

// Pushing a typed route
router.push(Articles.New())

// Replacing a typed route
router.replace(Articles())

// Pushing or Replacing a typed route with parameters
router.push|replace(Articles.Id(id = 123))

// Popping the last pushed or replaced route
router.pop()
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
            // exception handled during push, replace or pop routing
        }
    }
    
    push(path = "/path") {
        throw IllegalArgumentException("simulating an exception thrown on routing")
    }
}

// Pushing to simulate
router.push(path = "/path")
```

## Next steps

[ ] - Helper functions for each platform (Android, iOS, Web, Desktop, ...)

[ ] - Implement replaceAll handlers

[ ] - Support query parameters from external URI as Deep Link, Browser URL, etc

[ ] - More plugins like Session, CallLogging, etc

[ ] - Samples
