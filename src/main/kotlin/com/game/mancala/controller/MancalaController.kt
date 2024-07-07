package com.game.mancala.controller

import com.game.mancala.model.Game
import com.game.mancala.model.MancalaClientMsg
import com.game.mancala.service.MancalaService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/mancala")
class MancalaController(val mancalaService: MancalaService){

    val games = mutableListOf<Game>();

    @GetMapping("/games")
    fun games() = games

//    @GetMapping("/start")
//    fun start(): Game {
//        val newGame = Game(
//            pitsA= intArrayOf(NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_MANCALA_INITIAL_STONES),
//            pitsB= intArrayOf(9,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_INITIAL_STONES,NUM_MANCALA_INITIAL_STONES),
//            turn=Turn.A,
//            playerA="playerA",
//            playerB = "playerB",
//            full=true,
//            singleScreen = true,
//            type=GameStatus.InProgress
//            )
//        games.add(newGame);
//        return newGame;
//    }


    @PostMapping("/play/{gameId}")
    fun play(@RequestBody mancalaClientMsg: MancalaClientMsg, @PathVariable gameId: String): Game {

        val currentGame = games.find{it->it.gameId==gameId} ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        println("Current game Before Round - $currentGame")

//        if(currentGame.turn == mancalaClientMsg.player && mancalaClientMsg.selectedPitIndex >= 0 && mancalaClientMsg.selectedPitIndex <= 5) {
//            return mancalaService.play(request, currentGame)
            return currentGame
//        }else{
//            println("Error : Not Allowed - Please provide correct turn or pit")
//            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
//        }
    }
}