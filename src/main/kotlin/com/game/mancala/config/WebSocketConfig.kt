package com.game.mancala.config

import com.game.mancala.controller.WebsocketController
import com.game.mancala.service.MancalaGamePlayService
import com.game.mancala.service.MancalaGameRegistryService
import com.game.mancala.service.MancalaSendMessageService
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
class WebConfig : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(WebsocketController(mancalaGameRegistryService = MancalaGameRegistryService(
            MancalaGamePlayService(),MancalaSendMessageService()),
            mancalaSendMessageService= MancalaSendMessageService()),
            "/mancala").setAllowedOrigins("*")
    }
}