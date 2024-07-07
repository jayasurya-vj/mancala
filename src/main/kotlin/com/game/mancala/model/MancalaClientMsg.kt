package com.game.mancala.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MancalaClientMsg(
    val gameId: String?,
    @JsonProperty(required = true)
    val player: Turn,
    @JsonProperty(required = true)
    val type: ClientStatus,
    @JsonProperty(required = true)
    val selectedPitIndex: Int,
    @JsonProperty(required = true)
    val multiScreen: Boolean
)
