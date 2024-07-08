package com.game.mancala.model
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MancalaServerMsgTest {


    @Test
    fun `test creation with all fields`() {
        val game = Game(pitsA = intArrayOf(0,0,0,0,0,0,0),pitsB = intArrayOf(0,0,0,0,0,0,0))
        val serverMsg = MancalaServerMsg("session3", game, "An error occurred")

        assertEquals("session3", serverMsg.sessionId)
        assertEquals(game, serverMsg.game)
        assertEquals("An error occurred", serverMsg.error)
    }

    @Test
    fun `test creation with only sessionId`() {
        val serverMsg = MancalaServerMsg("session4")

        assertEquals("session4", serverMsg.sessionId)
        assertEquals(null, serverMsg.game)
        assertEquals(null, serverMsg.error)
    }
}