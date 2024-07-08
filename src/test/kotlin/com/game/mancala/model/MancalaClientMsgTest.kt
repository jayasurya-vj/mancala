package com.game.mancala.model
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MancalaClientMsgTest {

    @Test
    fun `test MancalaClientMsg creation`() {
        val originalMsg = MancalaClientMsg(
            gameId = "game1",
            player = Turn.A,
            type = ClientStatus.Start,
            selectedPitIndex = 3,
            multiScreen = true
        )

        assertEquals("game1", originalMsg.gameId)
        assertEquals(Turn.A, originalMsg.player)
        assertEquals(ClientStatus.Start, originalMsg.type)
        assertEquals(3, originalMsg.selectedPitIndex)
        assertEquals(true, originalMsg.multiScreen)

    }
}