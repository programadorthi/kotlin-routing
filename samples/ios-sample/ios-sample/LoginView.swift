//
//  LoginView.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 09/02/24.
//

import SwiftUI
import UIKitShared

struct LoginView : View {
    
    let router: Routing
    
    init(router: Routing) {
        self.router = router
    }
    
    var body: some View {
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
