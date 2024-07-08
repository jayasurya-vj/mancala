package com.game.mancala.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameTest {

    @Test
    fun `test game initialization`() {
        val pitsA = intArrayOf(4, 4, 4, 4, 4, 4)
        val pitsB = intArrayOf(4, 4, 4, 4, 4, 4)
        val game = Game(pitsA = pitsA, pitsB = pitsB)

        assertNotNull(game.gameId)
        assertArrayEquals(pitsA, game.pitsA)
        assertArrayEquals(pitsB, game.pitsB)
        assertNull(game.turn)
        assertFalse(game.gameOver)
        assertNull(game.winner)
        assertNull(game.status)
    }

    @Test
    fun `test gameOver when pits are not empty`() {
        val pitsA = intArrayOf(4, 4, 4, 4, 4, 4)
        val pitsB = intArrayOf(4, 4, 4, 4, 4, 4)
        val game = Game(pitsA = pitsA, pitsB = pitsB)

        assertFalse(game.gameOver())
    }

    @Test
    fun `test gameOver when one side pits are empty`() {
        val pitsA = intArrayOf(0, 0, 0, 0, 0, 0)
        val pitsB = intArrayOf(4, 4, 4, 4, 4, 4)
        val game = Game(pitsA = pitsA, pitsB = pitsB)

        assertTrue(game.gameOver())
    }

    @Test
    fun `test checkWinner when game is not over`() {
        val pitsA = intArrayOf(4, 4, 4, 4, 4, 4)
        val pitsB = intArrayOf(4, 4, 4, 4, 4, 4)
        val game = Game(pitsA = pitsA, pitsB = pitsB)

        assertNull(game.checkWinner())
    }

    @Test
    fun `test checkWinner when player A wins`() {
        val pitsA = intArrayOf(0, 0, 0, 0, 0, 0)
        val pitsB = intArrayOf(1, 1, 1, 1, 1, 1)
        val game = Game(pitsA = pitsA, pitsB = pitsB, gameOver=true)

        assertEquals(Winner.B, game.checkWinner())
    }

    @Test
    fun `test checkWinner when player B wins`() {
        val pitsA = intArrayOf(1, 1, 1, 1, 1, 1)
        val pitsB = intArrayOf(0, 0, 0, 0, 0, 0)
        val game = Game(pitsA = pitsA, pitsB = pitsB, gameOver=true)

        assertEquals(Winner.A, game.checkWinner())
    }

    @Test
    fun `test checkWinner when it is a draw`() {
        val pitsA = intArrayOf(0, 0, 0, 0, 0, 0)
        val pitsB = intArrayOf(0, 0, 0, 0, 0, 0)
        val game = Game(pitsA = pitsA, pitsB = pitsB, gameOver=true)

        assertEquals(Winner.Draw, game.checkWinner())
    }
}