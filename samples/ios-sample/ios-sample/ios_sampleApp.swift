//
//  ios_sampleApp.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 07/02/24.
//

import os
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
                router.controller(path: "/home", animated: true) { call in
                    router.application_.logger?.debug(message: ">>>> home controller: \(call)")
                    return SwiftUIController {
                        VStack {
                            Text("Hello, Home! Go to Login")
                                .onTapGesture {
                                    router.callUri(uri: "/login", routeMethod: RouteMethodCompanion().Push)
                                }
                            Text("Pop Home!")
                                .onTapGesture {
                                    router.popController(animated: true)
                                }
                        }
                    }
                }
                
                router.controller(path: "/login", animated: true) { call in
                    router.application_.logger?.debug(message: ">>>> login controller: \(call)")
                    return SwiftUIController {
                        VStack {
                            Text("Hello, Login! Go to Home")
                                .onTapGesture {
                                    router.callUri(uri: "/home", routeMethod: RouteMethodCompanion().ReplaceAll)
                                }
                            Text("Pop Login!")
                                .onTapGesture {
                                    router.popController(animated: true)
                                }
                        }
                    }
                }
                
                router.handle(path: "/home") { call in
                    router.application_.logger?.debug(message: ">>>> home handle: \(call)")
                }
                
                router.handle(path: "/login") { call in
                    router.application_.logger?.debug(message: ">>>> login handle: \(call)")
                }
            }
        }
    }
}

private class MyLogger : UIKitShared.Logger {
    
    private let logger = Logger()
    
    var level: UIKitShared.LogLevel = .trace
    
    init() {}
    
    func debug(message: String) {
        logger.debug("\(message)")
    }
    
    func debug(message: String, cause: KotlinThrowable) {
        logger.debug("\(message) -> \(cause)")
    }
    
    func error(message: String) {
        logger.error("\(message)")
    }
    
    func error(message: String, cause: KotlinThrowable) {
        logger.error("\(message) -> \(cause)")
    }
    
    func info(message: String) {
        logger.info("\(message)")
    }
    
    func info(message: String, cause: KotlinThrowable) {
        logger.info("\(message) -> \(cause)")
    }
    
    func trace(message: String) {
        logger.trace("\(message)")
    }
    
    func trace(message: String, cause: KotlinThrowable) {
        logger.trace("\(message) -> \(cause)")
    }
    
    func warn(message: String) {
        logger.warning("\(message)")
    }
    
    func warn(message: String, cause: KotlinThrowable) {
        logger.warning("\(message) -> \(cause)")
    }
}
