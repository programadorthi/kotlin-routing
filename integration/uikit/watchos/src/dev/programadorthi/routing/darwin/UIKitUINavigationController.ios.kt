package dev.programadorthi.routing.darwin

import platform.Foundation.NSCoder
import platform.WatchKit.WKInterfaceController

// All credits to: https://stackoverflow.com/a/50191172

public actual class UIKitUINavigationController actual constructor(coder: NSCoder) : WKInterfaceController() {
    private val controllers = mutableListOf<WKInterfaceController>()

    private val topController: WKInterfaceController?
        get() = controllers.lastOrNull()

    public actual fun viewControllers(): List<UIKitUIViewController> {
        return controllers.toList()
    }

    public actual fun popViewControllerAnimated(animated: Boolean): UIKitUIViewController? {
        return controllers.removeLastOrNull()
    }

    public actual fun popToViewController(
        viewController: UIKitUIViewController,
        animated: Boolean,
    ): List<UIKitUIViewController>? {
        controllers.removeAll { it == viewController }
        return controllers
    }

    public actual fun pushViewController(
        viewController: UIKitUIViewController,
        animated: Boolean,
    ) {
        controllers += viewController
    }

    public actual fun setViewControllers(
        viewControllers: List<UIKitUIViewController>,
        animated: Boolean,
    ) {
        controllers.clear()
        controllers += viewControllers
    }
}
