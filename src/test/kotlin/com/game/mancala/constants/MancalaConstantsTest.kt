package com.game.mancala.constants

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MancalaConstantsTest {

    @Test
    fun `test NUM_INITIAL_STONES`() {
        assertEquals(6, MancalaConstants.NUM_INITIAL_STONES)
    }

    @Test
    fun `test NUM_MANCALA_INITIAL_STONES`() {
        assertEquals(0, MancalaConstants.NUM_MANCALA_INITIAL_STONES)
    }

    @Test
    fun `test NUM_PITS`() {
        assertEquals(7, MancalaConstants.NUM_PITS)
    }

    @Test
    fun `test NUM_PITS_WITHOUT_MANCALA`() {
        assertEquals(6, MancalaConstants.NUM_PITS_WITHOUT_MANCALA)
    }

    @Test
    fun `test MANCALA_INDEX`() {
        assertEquals(6, MancalaConstants.MANCALA_INDEX)
    }

    @Test
    fun `test NUM_LAST_PIT_STONES_ELIGIBLE_FOR_CAPTURE`() {
        assertEquals(1, MancalaConstants.NUM_LAST_PIT_STONES_ELIGIBLE_FOR_CAPTURE)
    }

    @Test
    fun `test INVALID_REQUEST`() {
        assertEquals("Invalid Request", MancalaConstants.INVALID_REQUEST)
    }

    @Test
    fun `test METHOD_ENTRY`() {
        assertEquals("Entered into method - ", MancalaConstants.METHOD_ENTRY)
    }

    @Test
    fun `test METHOD_EXIT`() {
        assertEquals("Exiting from method - ", MancalaConstants.METHOD_EXIT)
    }
}
