package com.game.mancala.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.game.mancala.model.Game
import com.game.mancala.model.GameRoom
import com.game.mancala.model.MancalaServerMsg
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@ExtendWith(MockitoExtension::class)
class MancalaSendMessageServiceTest {

    @Mock
    lateinit var sessionA: WebSocketSession

    @Mock
    lateinit var sessionB: WebSocketSession

    @InjectMocks
    lateinit var mancalaSendMessageService: MancalaSendMessageService

    @Test
    fun `test emitMessage with game`() {
        val game = Game(
            pitsA = intArrayOf(4, 4, 4, 4, 4, 4),
            pitsB = intArrayOf(4, 4, 4, 4, 4, 4)
        )

        `when`(sessionA.id).thenReturn("sessionA")

        mancalaSendMessageService.emitMessage(sessionA, game)

        val expectedMsg = TextMessage(
            jacksonObjectMapper().writeValueAsString(
                MancalaServerMsg(
                    sessionId = "sessionA",
                    game = game,
                    error = null
                )
            )
        )

        verify(sessionA).sendMessage(expectedMsg)
    }

    @Test
    fun `test emitMessage with error`() {
        val error = "An error occurred"

        `when`(sessionA.id).thenReturn("sessionA")

        mancalaSendMessageService.emitMessage(sessionA, error = error)

        val expectedMsg = TextMessage(
            jacksonObjectMapper().writeValueAsString(
                MancalaServerMsg(
                    sessionId = "sessionA",
                    game = null,
                    error = error
                )
            )
        )

        verify(sessionA).sendMessage(expectedMsg)
    }

    @Test
    fun `test broadcastMessage with multiScreen true`() {
        val game = Game(
            pitsA = intArrayOf(4, 4, 4, 4, 4, 4),
            pitsB = intArrayOf(4, 4, 4, 4, 4, 4)
        )

        val gameRoom = GameRoom(
            gameId = "game1",
            playerA = sessionA,
            playerB = sessionB,
            full = true,
            multiScreen = true
        )

        `when`(sessionA.id).thenReturn("sessionA")
        `when`(sessionB.id).thenReturn("sessionB")

        mancalaSendMessageService.broadcastMessage(game, gameRoom)

        val expectedMsgA = TextMessage(
            jacksonObjectMapper().writeValueAsString(
                MancalaServerMsg(
                    sessionId = "sessionA",
                    game = game,
                    error = null
                )
            )
        )

        verify(sessionA).sendMessage(expectedMsgA)

        val expectedMsgB = TextMessage(
            jacksonObjectMapper().writeValueAsString(
                MancalaServerMsg(
                    sessionId = "sessionB",
                    game = game,
                    error = null
                )
            )
        )

        verify(sessionB).sendMessage(expectedMsgB)
    }

    @Test
    fun `test broadcastMessage with multiScreen false`() {
        val game = Game(
            pitsA = intArrayOf(4, 4, 4, 4, 4, 4),
            pitsB = intArrayOf(4, 4, 4, 4, 4, 4)
        )

        val gameRoom = GameRoom(
            gameId = "game1",
            playerA = sessionA,
            playerB = sessionB,
            full = true,
            multiScreen = false
        )

        `when`(sessionA.id).thenReturn("sessionA")

        mancalaSendMessageService.broadcastMessage(game, gameRoom)

        val expectedMsgA = TextMessage(
            jacksonObjectMapper().writeValueAsString(
                MancalaServerMsg(
                    sessionId = "sessionA",
                    game = game,
                    error = null
                )
            )
        )

        verify(sessionA).sendMessage(expectedMsgA)

        // PlayerB should not receive a message when multiScreen is false
        verify(sessionB, never()).sendMessage(any(TextMessage::class.java))
    }
}
