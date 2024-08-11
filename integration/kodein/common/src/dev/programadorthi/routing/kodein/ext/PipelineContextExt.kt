package dev.programadorthi.routing.kodein.ext

import dev.programadorthi.routing.core.application.ApplicationCall
import dev.programadorthi.routing.kodein.closestDI
import io.ktor.util.pipeline.PipelineContext
import org.kodein.di.DI
import org.kodein.di.DITrigger
import org.kodein.di.LazyDelegate
import org.kodein.di.On
import org.kodein.di.Typed
import org.kodein.di.constant
import org.kodein.di.diContext
import org.kodein.di.factory
import org.kodein.di.factoryOrNull
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.provider
import org.kodein.di.providerOrNull

//region Standard retrieving

/**
 * Gets a factory of `T` for the given argument type, return type and tag.
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the factory takes.
 * @param T The type of object the factory returns.
 * @param tag The bound tag, if any.
 * @return A factory.
 * @throws DI.NotFoundException if no factory was found.
 * @throws DI.DependencyLoopException When calling the factory function, if the instance construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.factory(tag: Any? = null): LazyDelegate<(A) -> T> =
    closestDI().factory(tag = tag)

/**
 * Gets a factory of `T` for the given argument type, return type and tag, or nul if none is found.
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the factory takes.
 * @param T The type of object the factory returns.
 * @param tag The bound tag, if any.
 * @return A factory, or null if no factory was found.
 * @throws DI.DependencyLoopException When calling the factory function, if the instance construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.factoryOrNull(
    tag: Any? = null,
): LazyDelegate<((A) -> T)?> = closestDI().factoryOrNull(tag = tag)

/**
 * Gets a provider of `T` for the given type and tag.
 *
 * T generics will be preserved!
 *
 * @param T The type of object the provider returns.
 * @param tag The bound tag, if any.
 * @return A provider.
 * @throws DI.NotFoundException if no provider was found.
 * @throws DI.DependencyLoopException When calling the provider function, if the instance construction triggered a dependency loop.
 */
public inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.provider(tag: Any? = null): LazyDelegate<() -> T> =
    closestDI().provider(tag = tag)

/**
 * Gets a provider of [T] for the given type and tag, curried from a factory that takes an argument [A].
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve with the returned provider.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return A provider of [T].
 * @throws DI.NotFoundException If no provider was found.
 * @throws DI.DependencyLoopException When calling the provider, if the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.provider(
    tag: Any? = null,
    arg: A,
): LazyDelegate<() -> T> = closestDI().provider(tag = tag, arg = arg)

/**
 * Gets a provider of [T] for the given type and tag, curried from a factory that takes an argument [A].
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve with the returned provider.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return A provider of [T].
 * @throws DI.NotFoundException If no provider was found.
 * @throws DI.DependencyLoopException When calling the provider, if the value construction triggered a dependency loop.
 */
public inline fun <A, reified T : Any> PipelineContext<*, ApplicationCall>.provider(
    tag: Any? = null,
    arg: Typed<A>,
): LazyDelegate<() -> T> = closestDI().provider(tag = tag, arg = arg)

/**
 * Gets a provider of [T] for the given type and tag, curried from a factory that takes an argument [A].
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve with the returned provider.
 * @param tag The bound tag, if any.
 * @param fArg A function that returns the argument that will be given to the factory when curried.
 * @return A provider of [T].
 * @throws DI.NotFoundException If no provider was found.
 * @throws DI.DependencyLoopException When calling the provider, if the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.provider(
    tag: Any? = null,
    noinline fArg: () -> A,
): LazyDelegate<() -> T> = closestDI().provider(tag = tag, fArg = fArg)

/**
 * Gets a provider of `T` for the given type and tag, or null if none is found.
 *
 * T generics will be preserved!
 *
 * @param T The type of object the provider returns.
 * @param tag The bound tag, if any.
 * @return A provider, or null if no provider was found.
 * @throws DI.DependencyLoopException When calling the provider function, if the instance construction triggered a dependency loop.
 */
public inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.providerOrNull(tag: Any? = null): LazyDelegate<(() -> T)?> =
    closestDI().providerOrNull(tag = tag)

/**
 * Gets a provider of [T] for the given type and tag, curried from a factory that takes an argument [A], or null if none is found.
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve with the returned provider.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return A provider of [T], or null if no factory was found.
 * @throws DI.DependencyLoopException When calling the provider, if the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.providerOrNull(
    tag: Any? = null,
    arg: A,
): LazyDelegate<(() -> T)?> = closestDI().providerOrNull(tag = tag, arg = arg)

/**
 * Gets a provider of [T] for the given type and tag, curried from a factory that takes an argument [A], or null if none is found.
 *
 * The argument type is extracted from the `Typed.type` of the argument.
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve with the returned provider.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return A provider of [T], or null if no factory was found.
 * @throws DI.DependencyLoopException When calling the provider, if the value construction triggered a dependency loop.
 */
public inline fun <A, reified T : Any> PipelineContext<*, ApplicationCall>.providerOrNull(
    tag: Any? = null,
    arg: Typed<A>,
): LazyDelegate<(() -> T)?> = closestDI().providerOrNull(tag = tag, arg = arg)

/**
 * Gets a provider of [T] for the given type and tag, curried from a factory that takes an argument [A], or null if none is found.
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve with the returned provider.
 * @param tag The bound tag, if any.
 * @param fArg A function that returns the argument that will be given to the factory when curried.
 * @return A provider of [T], or null if no factory was found.
 * @throws DI.DependencyLoopException When calling the provider, if the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.providerOrNull(
    tag: Any? = null,
    noinline fArg: () -> A,
): LazyDelegate<(() -> T)?> = closestDI().providerOrNull(tag = tag, fArg = fArg)

/**
 * Gets an instance of `T` for the given type and tag.
 *
 * T generics will be preserved!
 *
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @return An instance.
 * @throws DI.NotFoundException if no provider was found.
 * @throws DI.DependencyLoopException If the instance construction triggered a dependency loop.
 */
public inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.instance(tag: Any? = null): LazyDelegate<T> =
    closestDI().instance(tag = tag)

/**
 * Gets an instance of [T] for the given type and tag, curried from a factory that takes an argument [A].
 *
 * A & T generics will be preserved!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return An instance of [T].
 * @throws DI.NotFoundException If no provider was found.
 * @throws DI.DependencyLoopException If the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.instance(
    tag: Any? = null,
    arg: A,
): LazyDelegate<T> = closestDI().instance(tag = tag, arg = arg)

/**
 * Gets an instance of [T] for the given type and tag, curried from a factory that takes an argument [A].
 *
 * The argument type is extracted from the `Typed.type` of the argument.
 *
 * A & T generics will be erased!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return An instance of [T].
 * @throws DI.NotFoundException If no provider was found.
 * @throws DI.DependencyLoopException If the value construction triggered a dependency loop.
 */
public inline fun <A, reified T : Any> PipelineContext<*, ApplicationCall>.instance(
    tag: Any? = null,
    arg: Typed<A>,
): LazyDelegate<T> = closestDI().instance(tag = tag, arg = arg)

/**
 * Gets an instance of [T] for the given type and tag, curried from a factory that takes an argument [A].
 *
 * A & T generics will be erased!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @param fArg A function that returns the argument that will be given to the factory when curried.
 * @return An instance of [T].
 * @throws DI.NotFoundException If no provider was found.
 * @throws DI.DependencyLoopException If the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.instance(
    tag: Any? = null,
    noinline fArg: () -> A,
): LazyDelegate<T> = closestDI().instance(tag = tag, fArg = fArg)

/**
 * Gets an instance of `T` for the given type and tag, or null if none is found.
 *
 * T generics will be erased!
 *
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @return An instance, or null if no provider was found.
 * @throws DI.DependencyLoopException If the instance construction triggered a dependency loop.
 */
public inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.instanceOrNull(tag: Any? = null): LazyDelegate<T?> =
    closestDI().instanceOrNull(tag = tag)

/**
 * Gets an instance of [T] for the given type and tag, curried from a factory that takes an argument [A], or null if none is found.
 *
 * A & T generics will be erased!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return An instance of [T], or null if no factory was found.
 * @throws DI.DependencyLoopException If the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.instanceOrNull(
    tag: Any? = null,
    arg: A,
): LazyDelegate<T?> = closestDI().instanceOrNull(tag = tag, arg = arg)

/**
 * Gets an instance of [T] for the given type and tag, curried from a factory that takes an argument [A], or null if none is found.
 *
 * The argument type is extracted from the `Typed.type` of the argument.
 *
 * A & T generics will be erased!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @param arg The argument that will be given to the factory when curried.
 * @return An instance of [T], or null if no factory was found.
 * @throws DI.DependencyLoopException If the value construction triggered a dependency loop.
 */
public inline fun <A, reified T : Any> PipelineContext<*, ApplicationCall>.instanceOrNull(
    tag: Any? = null,
    arg: Typed<A>,
): LazyDelegate<T?> = closestDI().instanceOrNull(tag = tag, arg = arg)

/**
 * Gets an instance of [T] for the given type and tag, curried from a factory that takes an argument [A], or null if none is found.
 *
 * A & T generics will be erased!
 *
 * @param A The type of argument the curried factory takes.
 * @param T The type of object to retrieve.
 * @param tag The bound tag, if any.
 * @param fArg A function that returns the argument that will be given to the factory when curried.
 * @return An instance of [T], or null if no factory was found.
 * @throws DI.DependencyLoopException If the value construction triggered a dependency loop.
 */
public inline fun <reified A : Any, reified T : Any> PipelineContext<*, ApplicationCall>.instanceOrNull(
    tag: Any? = null,
    noinline fArg: () -> A,
): LazyDelegate<T?> = closestDI().instanceOrNull(tag = tag, fArg = fArg)

/**
 * Allows to create a new DI object with a context and/or a trigger set.
 *
 * @param context The new context of the new DI.
 * @param trigger The new trigger of the new DI.
 * @return A DI object that uses the same container as this one, but with its context and/or trigger changed.
 */
public inline fun <reified C : Any> PipelineContext<*, ApplicationCall>.on(
    context: C,
    trigger: DITrigger? = closestDI().diTrigger,
): DI = closestDI().On(diContext(context), trigger)

/**
 * Allows to create a new DI object with a context and/or a trigger set.
 *
 * @param getContext A function that gets the new context of the new DI.
 * @param trigger The new trigger of the new DI.
 * @return A DI object that uses the same container as this one, but with its context and/or trigger changed.
 */
public inline fun <reified C : Any> PipelineContext<*, ApplicationCall>.on(
    trigger: DITrigger? = closestDI().diTrigger,
    crossinline getContext: () -> C,
): DI = closestDI().On(diContext(getContext), trigger)

/**
 * Allows to create a new DI object with a trigger set.
 *
 * @param trigger The new trigger of the new DI.
 * @return A DI object that uses the same container as this one, but with its context and/or trigger changed.
 */
public fun PipelineContext<*, ApplicationCall>.on(trigger: DITrigger?): DI {
    val di = closestDI()
    return di.On(di.diContext, trigger)
}
//endregion

/**
 * Gets a constant of type [T] and tag whose tag is the name of the receiving property.
 *
 * T generics will be erased!
 *
 * @param T The type of object to retrieve.
 * @return An instance of [T].
 * @throws DI.NotFoundException If no provider was found.
 * @throws DI.DependencyLoopException If the value construction triggered a dependency loop.
 */
public inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.constant(): LazyDelegate<T> = closestDI().constant()
//endregion
