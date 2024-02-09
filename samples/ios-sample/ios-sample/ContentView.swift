//
//  ContentView.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 07/02/24.
//

import SwiftUI
import UIKitShared

struct ContentView: View {
    
    var router: Routing?
    
    init(router: Routing? = nil) {
        self.router = router
    }

    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundColor(.accentColor)
            Text("Hello, world!")
            Text("I am the initial content")
        }
        .padding()
        .onTapGesture {
            router?.callUri(uri: "/login", routeMethod: RouteMethodCompanion().Push)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
