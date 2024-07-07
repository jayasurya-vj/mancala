package com.game.mancala.model

import org.springframework.web.socket.WebSocketSession
import java.util.*


data class Game(
    val gameId: String = UUID.randomUUID().toString(),
    var pitsA: IntArray,
    var pitsB: IntArray,
    var turn:Turn?=null,
    var gameOver: Boolean = false,
    var winner: Winner? = null,
    var status: GameStatus? = null
) {
    private fun pitsEmpty(board:IntArray) :Boolean{
        for(i in 0..5){
            if(board[i]!=0) return false;
        }
        return true;
    }

    fun gameOver(): Boolean {
        return pitsEmpty(pitsA) || pitsEmpty(pitsB)
    }

    fun checkWinner(): Winner?{
        if(!gameOver) return null
        val pitsACount = pitsA.sum()
        val pitsBCount = pitsB.sum()
        if(pitsACount > pitsBCount) return Winner.A
        else if(pitsACount < pitsBCount) return Winner.B
        else if(pitsACount == pitsBCount) return Winner.Draw
        return null
    }
}





