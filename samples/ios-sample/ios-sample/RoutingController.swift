//
//  RoutingController.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 08/02/24.
//

import Foundation
import SwiftUI
import UIKitShared

public struct RoutingController {
    
    let prefersLargeTitles: Bool
    
    let router: Routing
    
    public init(
        router: Routing,
        prefersLargeTitles: Bool = false
    ) {
        self.router = router
        self.prefersLargeTitles = prefersLargeTitles
    }
}

extension RoutingController: UIViewControllerRepresentable {
    
    public func makeUIViewController(context _: Context) -> UINavigationController {
        let navigationController = UINavigationController()
        navigationController.navigationBar.prefersLargeTitles = prefersLargeTitles
        
        let rootController = SwiftUIController {
            ContentView(router: router)
        }
        
        router.install(plugin: UIKItPluginKt.UIKitPlugin) { setup in
            guard let config = setup as? UIKitPluginConfig else { return }
            config.navigationController = navigationController
        }
        
        navigationController.setViewControllers([rootController], animated: false)
        return navigationController
    }
    
    public func updateUIViewController(_ uiViewController: UINavigationController, context _: Context) {
        // no-op
    }
}
