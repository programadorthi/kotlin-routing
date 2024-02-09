package dev.programadorthi.routing.darwin

import dev.programadorthi.routing.core.Routing

public fun Routing.popController(animated: Boolean = false) {
    popToController(viewController = null, animated = animated)
}

public fun Routing.popToController(
    viewController: UIKitUIViewController?,
    animated: Boolean = false,
) {
    val navigatorController = uiNavigationController

    if (viewController != null) {
        navigatorController.popToViewController(
            viewController = viewController,
            animated = animated,
        )
    } else {
        navigatorController.popViewControllerAnimated(animated = animated)
    }
}
