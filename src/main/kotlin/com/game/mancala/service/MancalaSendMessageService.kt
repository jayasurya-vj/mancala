package com.game.mancala.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.game.mancala.constants.MancalaConstants
import com.game.mancala.model.Game
import com.game.mancala.model.GameRoom
import com.game.mancala.model.MancalaServerMsg
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Service
class MancalaSendMessageService {

    private val logger = LoggerFactory.getLogger(MancalaSendMessageService::class.java)

    fun emitMessage(session: WebSocketSession, game: Game?=null, error: String?=null){
        logger.info("${MancalaConstants.METHOD_ENTRY} emitMessage()")
        val textMessage = TextMessage(
            jacksonObjectMapper().writeValueAsString(
                MancalaServerMsg(game=game,
                    error = error,
                    sessionId = session.id
                )))
        session.sendMessage(textMessage)
        logger.info("Message sent - $textMessage; To session id - ${session.id}")
        logger.info("${MancalaConstants.METHOD_EXIT} emitMessage()")
    }

    fun broadcastMessage(game: Game, gameRoom:GameRoom){
        logger.info("${MancalaConstants.METHOD_ENTRY} broadcastMessage()")
        emitMessage(gameRoom.playerA, game)
        if(gameRoom.playerB!=null && gameRoom.multiScreen) emitMessage(gameRoom.playerB!!, game)
        logger.info("${MancalaConstants.METHOD_EXIT} broadcastMessage()")
    }
}