package com.game.mancala.config

import com.game.mancala.controller.WebsocketController
import com.game.mancala.service.MancalaService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean


@Configuration
@EnableWebSocket
class WebConfig : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(WebsocketController(mancalaService = MancalaService()), "/mancalaconnection").setAllowedOrigins("*")
    }
}