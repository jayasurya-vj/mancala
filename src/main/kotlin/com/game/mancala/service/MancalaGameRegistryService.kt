package com.game.mancala.service

import com.game.mancala.constants.MancalaConstants
import com.game.mancala.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

@Service
class MancalaGameRegistryService(private val mancalaGamePlayService: MancalaGamePlayService, private val mancalaSendMessageService: MancalaSendMessageService) {

    private val logger = LoggerFactory.getLogger(MancalaGameRegistryService::class.java)

    private val gamesList: MutableList<Game> = mutableListOf<Game>()
    private val gameRoomList = mutableListOf<GameRoom>()

    private val initialPits = intArrayOf(
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_INITIAL_STONES,
        MancalaConstants.NUM_MANCALA_INITIAL_STONES)

    private fun createGame(mancalaClientMsg: MancalaClientMsg, session: WebSocketSession){
        logger.info("${MancalaConstants.METHOD_ENTRY} createGame()")
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

        mancalaSendMessageService.broadcastMessage(newGame,newGameRoom)
        logger.info("${MancalaConstants.METHOD_EXIT} createGame()")
    }

    private fun addPlayer(mancalaClientMsg: MancalaClientMsg, session: WebSocketSession){
        logger.info("${MancalaConstants.METHOD_ENTRY} addPlayer()")
        val currentGameRoom = gameRoomList.find{it -> it.gameId==mancalaClientMsg.gameId}
        val currentGame  = gamesList.find{ it -> it.gameId==mancalaClientMsg.gameId}
        if(currentGameRoom==null || currentGame==null){
            mancalaSendMessageService.emitMessage(session,error= MancalaConstants.INVALID_REQUEST)
            logger.error("${MancalaConstants.INVALID_REQUEST} - game or gameRoom not found")
        }else{
            if(!currentGameRoom.full && currentGameRoom.playerB==null && currentGameRoom.multiScreen){
                currentGameRoom.playerB = session
                currentGameRoom.full = true

                currentGame.turn = Turn.A
                currentGame.status = GameStatus.InProgress

                mancalaSendMessageService.broadcastMessage(currentGame,currentGameRoom)
            }else{
                mancalaSendMessageService.emitMessage(session,currentGame, MancalaConstants.INVALID_REQUEST)
                logger.error("${MancalaConstants.INVALID_REQUEST} - Both Player already created. Please send InProgress requests")
            }

        }
        logger.info("${MancalaConstants.METHOD_EXIT} addPlayer()")
    }

    private fun isAuthorizedTurn(currentGame: Game, currentGameRoom: GameRoom, playerSessionId:String, player: Turn, selectedPitIndex:Int):Boolean{
        logger.info("${MancalaConstants.METHOD_ENTRY} isAuthorizedTurn()")
        return (selectedPitIndex in 0..5) &&
                if(currentGame.turn==player){
                    (currentGame.turn== Turn.A && playerSessionId ==  currentGameRoom.playerA.id) ||
                            (currentGame.turn== Turn.B && playerSessionId == currentGameRoom.playerB!!.id)
                }else{
                    false
                }
    }

    fun startGame(mancalaClientMsg:MancalaClientMsg, session: WebSocketSession){
        logger.info("${MancalaConstants.METHOD_ENTRY} startGame()")
        if(mancalaClientMsg.gameId==null || mancalaClientMsg.gameId==""){
            createGame(mancalaClientMsg,session)
        }else{
            addPlayer(mancalaClientMsg,session)
        }
        logger.info("${MancalaConstants.METHOD_ENTRY} startGame()")

    }

    fun playGame(mancalaClientMsg: MancalaClientMsg, session: WebSocketSession){
        logger.info("${MancalaConstants.METHOD_ENTRY} playGame()")
        val currentGame = gamesList.find{it->it.gameId==mancalaClientMsg.gameId}
        val currentGameRoom = gameRoomList.find{it -> it.gameId==mancalaClientMsg.gameId}

        if (currentGame == null || currentGameRoom==null || currentGameRoom.playerB==null) {
            mancalaSendMessageService.emitMessage(session,currentGame, MancalaConstants.INVALID_REQUEST)
            logger.error("${MancalaConstants.INVALID_REQUEST} - game/gameRoom/playerB null")
        }else{

            if(isAuthorizedTurn(currentGame,currentGameRoom, session.id, mancalaClientMsg.player, mancalaClientMsg.selectedPitIndex)){

                mancalaGamePlayService.play(mancalaClientMsg, currentGame)

                mancalaSendMessageService.broadcastMessage(currentGame,currentGameRoom)

                if(currentGame.gameOver || currentGame.status == GameStatus.End){
                    gamesList-currentGame
                    gameRoomList-currentGameRoom
                }
            }else{
                mancalaSendMessageService.emitMessage(session,currentGame, MancalaConstants.INVALID_REQUEST)
                logger.error("${MancalaConstants.INVALID_REQUEST} - Not an Authorized Turn")
            }
        }
        logger.info("${MancalaConstants.METHOD_EXIT} playGame()")
    }
}