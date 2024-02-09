package dev.programadorthi.routing.darwin

import platform.AppKit.NSViewController
import platform.Foundation.NSCoder

// All credits to: https://stackoverflow.com/a/50191172

public actual class UIKitUINavigationController actual constructor(coder: NSCoder) : NSViewController(coder) {
    private val controllers = mutableListOf<NSViewController>()

    private val topController: NSViewController?
        get() = controllers.lastOrNull()

    public actual fun viewControllers(): List<UIKitUIViewController> = controllers.toList()

    public actual fun popViewControllerAnimated(animated: Boolean): UIKitUIViewController? = controllers.removeLastOrNull()

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
