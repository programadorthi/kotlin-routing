//
//  ios_sampleApp.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 07/02/24.
//

import SwiftUI
import UIKitShared

@main
struct ios_sampleApp: App {
    
    let router = Routing_iosKt.routing(
        rootPath: "/",
        parent: nil,
        logger: MyLogger(),
        developmentMode: true,
        configuration: { _ in }
    )

    var body: some Scene {
        WindowGroup {
            RoutingController(router: router)
            .onAppear {
                setupRoutes()
            }
        }
    }
    
    private func setupRoutes() {
        router.controller(path: "/home", animated: true) { _ in
            SwiftUIController {
                HomeView(router: router)
            }
        }
        
        router.controller(path: "/login", animated: true) { _ in
            SwiftUIController {
                LoginView(router: router)
            }
        }
        
        router.handle(path: "/home") { call in
            router.application_.logger?.debug(message: ">>>> home handle is called too: \(call)")
        }
        
        router.handle(path: "/login") { call in
            router.application_.logger?.debug(message: ">>>> login handle is called too: \(call)")
        }
    }
}
