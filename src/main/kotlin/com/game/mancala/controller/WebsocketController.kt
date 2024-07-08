package com.game.mancala.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.game.mancala.constants.MancalaConstants.INVALID_REQUEST
import com.game.mancala.constants.MancalaConstants.METHOD_ENTRY
import com.game.mancala.constants.MancalaConstants.METHOD_EXIT
import com.game.mancala.model.*
import com.game.mancala.service.MancalaGameRegistryService
import com.game.mancala.service.MancalaSendMessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler


@Controller
class WebsocketController(private val mancalaGameRegistryService: MancalaGameRegistryService,
                          private val mancalaSendMessageService: MancalaSendMessageService) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(WebsocketController::class.java)

    var mapper = jacksonObjectMapper()


    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        logger.info("$METHOD_ENTRY handleMessage()")
        val receivedMessage = message.payload as String
        try {
            val mancalaClientMsg: MancalaClientMsg = mapper.readValue(receivedMessage)
            logger.info("MancalaClientMsg Received : $mancalaClientMsg")

            when (mancalaClientMsg.type) {
                ClientStatus.Start -> {
                    mancalaGameRegistryService.startGame(mancalaClientMsg, session)
                }

                ClientStatus.InProgress -> {
                    mancalaGameRegistryService.playGame(mancalaClientMsg, session)
                }

            }
        }catch (e:Exception){
            logger.error(e.message)
            mancalaSendMessageService.emitMessage(session, error = INVALID_REQUEST)
            logger.error("$INVALID_REQUEST - Exception Occured")
        }
        logger.info("$METHOD_EXIT handleMessage()")

    }


    override fun afterConnectionEstablished(session: WebSocketSession) = logger.info("web socket connection established for session Id  ${session.id}")


    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) = logger.info("web socket connection closed for session Id  ${session.id}")

}