package com.game.mancala.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.game.mancala.constants.MancalaConstants
import com.game.mancala.constants.MancalaConstants.INVALID_REQUEST
import com.game.mancala.model.*
import com.game.mancala.service.MancalaService
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler


@Controller
class WebsocketController(private val mancalaService: MancalaService) : TextWebSocketHandler() {

    val initialPits = intArrayOf(
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_MANCALA_INITIAL_STONES)

    val gamesList = mutableListOf<Game>()
    val gameRoomList = mutableListOf<GameRoom>()
    var mapper = jacksonObjectMapper()

    val sessionList = mutableListOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) = println("web socket connection established for session Id  ${session.id}")


    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) = println("web socket connection closed for session Id  ${session.id}")

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val receivedMessage = message.payload as String
        try {
            val mancalaClientMsg: MancalaClientMsg = mapper.readValue(receivedMessage)
            println("MancalaClientMsg Received : $mancalaClientMsg")

            when (mancalaClientMsg.type) {
                ClientStatus.Start -> {
                    startGame(mancalaClientMsg, session)
                }

                ClientStatus.InProgress -> {
                    playGame(mancalaClientMsg, session)
                }

                else -> {
                    emit(session, error = INVALID_REQUEST)
                    println("$INVALID_REQUEST - Invalid Status")
                }
            }
        }catch (e:Exception){
            println(e.message)
            println(e.stackTrace)
            emit(session, error = INVALID_REQUEST)
            println("$INVALID_REQUEST - Exception Occured")
        }

    }



    fun emit(session: WebSocketSession, game: Game?=null, error: String?=null) =
        session.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(
            MancalaServerMsg(game=game,
                  error = error,
                sessionId = session.id
        ))))

    fun broadcast(game: Game){
        val gameRoom = gameRoomList.find{it -> it.gameId==game.gameId}
        if (gameRoom != null) {
            emit(gameRoom.playerA, game)
            if(gameRoom.playerB!=null && gameRoom.multiScreen) emit(gameRoom.playerB!!, game)
        }
    }

    fun startGame(mancalaClientMsg:MancalaClientMsg, session: WebSocketSession){
            if(mancalaClientMsg.gameId==null || mancalaClientMsg.gameId==""){
                createGame(mancalaClientMsg,session)
            }else{
                addPlayer(mancalaClientMsg,session)
            }
    }

    //use one if instead of many
    fun createGame(mancalaClientMsg:MancalaClientMsg, session: WebSocketSession){
        val newGame = Game(
            pitsA= initialPits.copyOf(),
            pitsB= initialPits.copyOf(),
            status = if(mancalaClientMsg.multiScreen) GameStatus.Waiting else GameStatus.InProgress,
            turn = if(mancalaClientMsg.multiScreen) null else Turn.A
        )
        gamesList.add(newGame);
        val newGameRoom = GameRoom(
            gameId = newGame.gameId,
            playerA = session,
            playerB =  if(mancalaClientMsg.multiScreen) null else session,
            full = !mancalaClientMsg.multiScreen,
            multiScreen = mancalaClientMsg.multiScreen
        )
        gameRoomList.add(newGameRoom)

        broadcast(newGame)
    }

    fun addPlayer(mancalaClientMsg:MancalaClientMsg, session: WebSocketSession){
        val currentGameRoom = gameRoomList.find{it -> it.gameId==mancalaClientMsg.gameId}
        val currentGame  = gamesList.find{ it -> it.gameId==mancalaClientMsg.gameId}
        if(currentGameRoom==null || currentGame==null){
            emit(session,error=INVALID_REQUEST)
            println("$INVALID_REQUEST - game or gameRoom null")
        }else{
            if(!currentGameRoom.full && currentGameRoom.playerB==null && currentGameRoom.multiScreen){
                currentGameRoom.playerB = session
                currentGameRoom.full = true

                currentGame.turn = Turn.A
                currentGame.status = GameStatus.InProgress

                broadcast(currentGame)
            }else{
                emit(session,currentGame,INVALID_REQUEST)
                println("$INVALID_REQUEST - Both Player exist. send Inprogress requests")
            }

        }
    }

    fun isAuthorizedTurn(currentGame:Game, currentGameRoom:GameRoom, playerSessionId:String, player:Turn, selectedPitIndex:Int):Boolean{
        return (selectedPitIndex in 0..5) &&
                if(currentGame.turn==player){
                       (currentGame.turn==Turn.A && playerSessionId ==  currentGameRoom.playerA.id) ||
                       (currentGame.turn==Turn.B && playerSessionId == currentGameRoom.playerB!!.id)
                }else{
                    false
                }
    }

    fun playGame(mancalaClientMsg:MancalaClientMsg, session: WebSocketSession){
        val currentGame = gamesList.find{it->it.gameId==mancalaClientMsg.gameId}
        val currentGameRoom = gameRoomList.find{it -> it.gameId==mancalaClientMsg.gameId}

        if (currentGame == null || currentGameRoom==null || currentGameRoom.playerB==null) {
            emit(session,currentGame,INVALID_REQUEST)
            println("$INVALID_REQUEST - game/gameRoom/playerB null")
        }else{

            if(isAuthorizedTurn(currentGame,currentGameRoom, session.id, mancalaClientMsg.player, mancalaClientMsg.selectedPitIndex)){

                mancalaService.play(mancalaClientMsg, currentGame)

                broadcast(currentGame)

                if(currentGame.gameOver || currentGame.status == GameStatus.End){
                    gamesList-currentGame
                    gameRoomList-currentGameRoom
                }
            }else{
                emit(session,currentGame,INVALID_REQUEST)
                println("$INVALID_REQUEST - Player mismatch or wrong pit")
            }
        }
    }


}