//
//  MyLogger.swift
//  ios-sample
//
//  Created by Thiago dos Santos on 09/02/24.
//

import os
import UIKitShared

class MyLogger : UIKitShared.Logger {
    
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
