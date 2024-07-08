package com.game.mancala.model
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.socket.WebSocketSession

class GameRoomTest {

    @Test
    fun `test game room creation`() {
        val sessionIdA = "sessionA"
        val sessionIdB = "sessionB"

        val playerA = mockWebSocketSession(sessionIdA)
        val playerB = mockWebSocketSession(sessionIdB)

        val gameRoom = GameRoom("game1", playerA, playerB, false, true)

        assertEquals("game1", gameRoom.gameId)
        assertEquals(playerA, gameRoom.playerA)
        assertEquals(playerB, gameRoom.playerB)
        assertEquals(false, gameRoom.full)
        assertEquals(true, gameRoom.multiScreen)
    }

    private fun mockWebSocketSession(sessionId: String): WebSocketSession {
        return Mockito.mock(WebSocketSession::class.java).apply {
            Mockito.`when`(this.id).thenReturn(sessionId)
        }
    }
}