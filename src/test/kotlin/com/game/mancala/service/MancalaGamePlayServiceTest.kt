package com.game.mancala.service

import com.game.mancala.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@ExtendWith(MockitoExtension::class)
class MancalaGamePlayServiceTest {

    @InjectMocks
    lateinit var mancalaGamePlayService: MancalaGamePlayService

    lateinit var game: Game

    @BeforeEach
    fun setUp() {
        game = Game(
            pitsA = intArrayOf(4, 4, 4, 4, 4, 4, 0),
            pitsB = intArrayOf(4, 4, 4, 4, 4, 4, 0),
            turn = Turn.A
        )
    }

    @Test
    fun `test play with Player A for valid Turn and Pits`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game.gameId,
            player = Turn.A,
            type = ClientStatus.InProgress,
            selectedPitIndex = 0,
            multiScreen = false
        )

        val updatedGame = mancalaGamePlayService.play(mancalaClientMsg, game)

        assertEquals(0, updatedGame.pitsA[0])
        assertEquals(5, updatedGame.pitsA[1])
        assertEquals(Turn.B, updatedGame.turn)
    }

    @Test
    fun `test play with Player A empty pit selection`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game.gameId,
            player = Turn.A,
            type = ClientStatus.InProgress,
            selectedPitIndex = 6,
            multiScreen = false
        )

        val exception = assertThrows(ResponseStatusException::class.java) {
            mancalaGamePlayService.play(mancalaClientMsg, game)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun `test updatePits correctly distributes stones`() {
        val lastIndex = mancalaGamePlayService.updatePits(game.pitsA, game.pitsB, 0)

        assertArrayEquals(intArrayOf(0, 5, 5, 5, 5, 4, 0), game.pitsA)
        assertArrayEquals(intArrayOf(4, 4, 4, 4, 4, 4, 0), game.pitsB)
        assertEquals(4, lastIndex)
    }

    @Test
    fun `test updatePits to return lastIndexIfCurrentPlayerPits=null`() {
        val lastIndex = mancalaGamePlayService.updatePits(game.pitsA, game.pitsB, 5)

        assertArrayEquals(intArrayOf(4, 4, 4, 4, 4, 0, 1), game.pitsA)
        assertArrayEquals(intArrayOf(5, 5, 5, 4, 4, 4, 0), game.pitsB)
        assertEquals(null, lastIndex)
    }

    @Test
    fun `test captureStonesIfOwnEmptyLastPit with eligible capture`() {
        game.pitsA = intArrayOf(0, 0, 0, 1, 0, 0, 4)
        game.pitsB = intArrayOf(4, 4, 4, 0, 4, 4, 0)

        mancalaGamePlayService.captureStonesIfOwnEmptyLastPit(game.pitsA, game.pitsB, 3)

        assertArrayEquals(intArrayOf(0, 0, 0, 0, 0, 0, 9), game.pitsA)
        assertArrayEquals(intArrayOf(4, 4, 0, 0, 4, 4, 0), game.pitsB)
    }

    @Test
    fun `test checkNextTurn switches turn correctly`() {
        mancalaGamePlayService.checkNextTurn(game, 5)

        assertEquals(Turn.B, game.turn)
    }

    @Test
    fun `test checkNextTurn game over`() {
        game.pitsA = intArrayOf(0, 0, 0, 0, 0, 0, 10)
        game.pitsB = intArrayOf(4, 4, 4, 4, 4, 4, 0)

        mancalaGamePlayService.checkNextTurn(game, null)

        assertNull(game.turn)
        assertTrue(game.gameOver)
        assertEquals(Winner.B, game.winner)
        assertEquals(GameStatus.End, game.status)
    }
    @Test
    fun `test play with Player B for valid Turn and Pits`() {
        game.turn = Turn.B
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game.gameId,
            player = Turn.B,
            type = ClientStatus.InProgress,
            selectedPitIndex = 0,
            multiScreen = false
        )

        val updatedGame = mancalaGamePlayService.play(mancalaClientMsg, game)

        assertEquals(0, updatedGame.pitsB[0])
        assertEquals(5, updatedGame.pitsB[1])
        assertEquals(Turn.A, updatedGame.turn)
    }

    @Test
    fun `test play with Player B turn with empty pit selection`() {
        game.turn = Turn.B
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game.gameId,
            player = Turn.B,
            type = ClientStatus.InProgress,
            selectedPitIndex = 6,
            multiScreen = false
        )

        val exception = assertThrows(ResponseStatusException::class.java) {
            mancalaGamePlayService.play(mancalaClientMsg, game)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

}
