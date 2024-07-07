package com.game.mancala.model

    data class MancalaServerMsg(
    var sessionId: String,
    var game: Game? = null,
    var error: String? = null
)
