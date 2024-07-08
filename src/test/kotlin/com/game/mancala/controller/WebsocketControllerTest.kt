package com.game.mancala.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.game.mancala.constants.MancalaConstants.INVALID_REQUEST
import com.game.mancala.model.ClientStatus
import com.game.mancala.model.MancalaClientMsg
import com.game.mancala.model.Turn
import com.game.mancala.service.MancalaGameRegistryService
import com.game.mancala.service.MancalaSendMessageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

class WebsocketControllerTest {

    private lateinit var websocketController: WebsocketController
    private lateinit var mancalaGameRegistryService: MancalaGameRegistryService
    private lateinit var mancalaSendMessageService: MancalaSendMessageService
    private val mapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mancalaGameRegistryService = mock(MancalaGameRegistryService::class.java)
        mancalaSendMessageService = mock(MancalaSendMessageService::class.java)
        websocketController = WebsocketController(mancalaGameRegistryService, mancalaSendMessageService)
    }

    @Test
    fun `handleMessage should start game on ClientStatus Start`() {
        val session = mock(WebSocketSession::class.java)
        val mancalaClientMsg = MancalaClientMsg(
            gameId = null,
            player = Turn.A,
            type = ClientStatus.Start,
            selectedPitIndex = 0,
            multiScreen = false
        )

        val message = TextMessage(mapper.writeValueAsString(mancalaClientMsg))

        websocketController.handleMessage(session, message)

        verify(mancalaGameRegistryService).startGame(any(MancalaClientMsg::class.java) ?: mancalaClientMsg, eq(session)?:session)
    }

    @Test
    fun `handleMessage should play game on ClientStatus InProgress`() {
        val session = mock(WebSocketSession::class.java)
        val mancalaClientMsg = MancalaClientMsg(
            gameId = null,
            player = Turn.A,
            type = ClientStatus.InProgress,
            selectedPitIndex = 0,
            multiScreen = false
        )
        val message = TextMessage(mapper.writeValueAsString(mancalaClientMsg))

        websocketController.handleMessage(session, message)

        verify(mancalaGameRegistryService).playGame(any(MancalaClientMsg::class.java)?:mancalaClientMsg, eq(session)?:session)
    }

    @Test
    fun `handleMessage should emit error on invalid message`() {
        val session = mock(WebSocketSession::class.java)
        val message = TextMessage("invalid message")

        websocketController.handleMessage(session, message)

        verify(mancalaSendMessageService).emitMessage(eq(session)?:session, game=any(), error=eq(INVALID_REQUEST)?:INVALID_REQUEST)
    }

    @Test
    fun `afterConnectionEstablished logs correct message`() {
        val session = mock(WebSocketSession::class.java)
        `when`(session.id).thenReturn("123")

        websocketController.afterConnectionEstablished(session)

    }

    @Test
    fun `afterConnectionClosed logs correct message`() {
        val session = mock(WebSocketSession::class.java)
        `when`(session.id).thenReturn("123")
        val status = CloseStatus.NORMAL

        websocketController.afterConnectionClosed(session, status)
    }
}
