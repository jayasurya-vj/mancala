package com.game.mancala.model

import org.springframework.web.socket.WebSocketSession

data class GameRoom(val gameId: String,
                    var playerA: WebSocketSession,
                    var playerB: WebSocketSession?,
                    var full: Boolean,
                    var multiScreen: Boolean)
