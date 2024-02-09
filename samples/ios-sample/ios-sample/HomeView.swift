//
//  HomeView.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 09/02/24.
//

import SwiftUI
import UIKitShared

struct HomeView : View {
    
    let router: Routing
    
    init(router: Routing) {
        self.router = router
    }
    
    var body: some View {
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
