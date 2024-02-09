package dev.programadorthi.routing.darwin

import platform.Foundation.NSCoder

public expect class UIKitUINavigationController(coder: NSCoder) {
    public fun viewControllers(): List<UIKitUIViewController>

    public fun popViewControllerAnimated(animated: Boolean): UIKitUIViewController?

    public fun popToViewController(
        viewController: UIKitUIViewController,
        animated: Boolean,
    ): List<UIKitUIViewController>?

    public fun pushViewController(
        viewController: UIKitUIViewController,
        animated: Boolean,
    )

    public fun setViewControllers(
        viewControllers: List<UIKitUIViewController>,
        animated: Boolean,
    )
}
