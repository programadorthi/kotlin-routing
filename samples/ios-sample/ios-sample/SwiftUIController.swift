//
//  SwiftUIController.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 08/02/24.
//

import SwiftUI

public final class SwiftUIController<Content : View>: UIHostingController<Content> {
    
    public init(
        title: String? = nil,
        @ViewBuilder content: () -> Content
    ) {
        super.init(rootView: content())
        super.title = title
    }
    
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}
