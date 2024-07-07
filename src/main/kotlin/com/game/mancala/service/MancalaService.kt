package com.game.mancala.service



import com.game.mancala.constants.MancalaConstants.MANCALA_INDEX
import com.game.mancala.constants.MancalaConstants.NUM_LAST_PIT_STONES_ELIGIBLE_FOR_CAPTURE
import com.game.mancala.constants.MancalaConstants.NUM_PITS
import com.game.mancala.constants.MancalaConstants.NUM_PITS_WITHOUT_MANCALA
import com.game.mancala.model.Game
import com.game.mancala.model.GameStatus
import com.game.mancala.model.MancalaClientMsg
import com.game.mancala.model.Turn
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException


@Service
class MancalaService {


    val oppPitPairIndexes: IntArray = intArrayOf(5, 4, 3, 2, 1, 0);

    fun handleRequest(){

    }

    fun play(mancalaClientMsg: MancalaClientMsg, currentGame:Game): Game {
            var currentPlayerPits: IntArray
            var opponentPlayerPits: IntArray
            var lastIndexIfCurrentPlayerPits : Int? = null

            if(currentGame.turn == Turn.A){
              println("Turn - Player A")
              currentPlayerPits= currentGame.pitsA
              opponentPlayerPits= currentGame.pitsB

            }else {
              println("Turn - Player B")
              currentPlayerPits= currentGame.pitsB
              opponentPlayerPits= currentGame.pitsA
            }

            //change position
            if(currentPlayerPits[mancalaClientMsg.selectedPitIndex]<=0){
                println("Error : Not Allowed - Please select a non empty pit")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }

            lastIndexIfCurrentPlayerPits = updatePits(currentPlayerPits,opponentPlayerPits,mancalaClientMsg.selectedPitIndex)

            println("After Turn: curr - ${currentPlayerPits.contentToString()} , opp - ${opponentPlayerPits.contentToString()}")

            captureStonesIfOwnEmptyLastPit(currentPlayerPits,opponentPlayerPits,lastIndexIfCurrentPlayerPits)

            println("After Capture: curr - ${currentPlayerPits.contentToString()} , opp - ${opponentPlayerPits.contentToString()}")

            checkNextTurn(currentGame,lastIndexIfCurrentPlayerPits)

            println("After the round: A - $currentGame.pitsA , B - $currentGame.pitsB")

             println("Current game After Round - $currentGame")

            return currentGame;
    }


    fun updatePits(currentPlayerPits:IntArray, opponentPlayerPits:IntArray, selectedPitIndex: Int ): Int? {
        var stonesInHand:Int = currentPlayerPits[selectedPitIndex]
        currentPlayerPits[selectedPitIndex] = 0;

        var index =  selectedPitIndex + 1;
        var lastIndexIfCurrentPlayerPits : Int? = null;


        while (stonesInHand > 0) {

            //sow in current player pits
            while(stonesInHand > 0 && index < NUM_PITS
            ) {
                stonesInHand--;
                currentPlayerPits[index]++;
                index++;
            }

            if (stonesInHand == 0) {
                lastIndexIfCurrentPlayerPits = index - 1;
                break;
            } else {
                index = 0;
            }

            //sow in opponent player pits
            while(stonesInHand > 0 && index < NUM_PITS_WITHOUT_MANCALA){
                stonesInHand--;
                opponentPlayerPits[index]++;
                index++;
            }

            if (stonesInHand != 0) {
                index = 0;
            }
        }

        return lastIndexIfCurrentPlayerPits;
    }


    fun captureStonesIfOwnEmptyLastPit(currentPlayerPits:IntArray, opponentPlayerPits:IntArray, lastIndexIfCurrentPlayerPits:Int?){

        if(lastIndexIfCurrentPlayerPits!=null && lastIndexIfCurrentPlayerPits!=MANCALA_INDEX &&
            currentPlayerPits[lastIndexIfCurrentPlayerPits] == NUM_LAST_PIT_STONES_ELIGIBLE_FOR_CAPTURE){

            val oppIndex = oppPitPairIndexes[lastIndexIfCurrentPlayerPits]

            if(opponentPlayerPits[oppIndex]!=0){
                currentPlayerPits[MANCALA_INDEX] += opponentPlayerPits[oppIndex] + NUM_LAST_PIT_STONES_ELIGIBLE_FOR_CAPTURE
                opponentPlayerPits[oppIndex] = 0
                currentPlayerPits[lastIndexIfCurrentPlayerPits] = 0

            }

        }

    }


    fun checkNextTurn(currentGame:Game, lastIndexIfCurrentPlayerPits:Int?){
        if(currentGame.gameOver()) {
            currentGame.turn = null
            currentGame.gameOver = true
            currentGame.winner = currentGame.checkWinner()
            currentGame.status = GameStatus.End
        }else{
            if(lastIndexIfCurrentPlayerPits==null || lastIndexIfCurrentPlayerPits!=MANCALA_INDEX){
                currentGame.turn = if(currentGame.turn==Turn.A) Turn.B else Turn.A
            }
        }
    }




}