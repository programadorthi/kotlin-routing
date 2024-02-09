package dev.programadorthi.routing.darwin

import platform.Foundation.NSCoder
import platform.UIKit.UINavigationController

public actual class UIKitUINavigationController actual constructor(coder: NSCoder) :
    UINavigationController(coder) {
        public actual override fun viewControllers(): List<UIKitUIViewController> =
            super.viewControllers().map { it as UIKitUIViewController }

        public actual override fun popViewControllerAnimated(animated: Boolean): UIKitUIViewController? =
            super.popViewControllerAnimated(animated)

        public actual override fun popToViewController(
            viewController: UIKitUIViewController,
            animated: Boolean,
        ): List<UIKitUIViewController>? =
            super.popToViewController(
                viewController,
                animated,
            )?.map { it as UIKitUIViewController }

        public actual override fun pushViewController(
            viewController: UIKitUIViewController,
            animated: Boolean,
        ) {
            super.pushViewController(viewController, animated)
        }

        public actual fun setViewControllers(
            viewControllers: List<UIKitUIViewController>,
            animated: Boolean,
        ) {
            super.setViewControllers(viewControllers, animated)
        }
    }
