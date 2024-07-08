package com.game.mancala.service

import com.game.mancala.constants.MancalaConstants
import com.game.mancala.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.socket.WebSocketSession
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@ExtendWith(MockitoExtension::class)
class MancalaGameRegistryServiceTest {

    @Mock
    private lateinit var mancalaGamePlayService: MancalaGamePlayService

    @Mock
    private lateinit var mancalaSendMessageService: MancalaSendMessageService

    @InjectMocks
    private lateinit var mancalaGameRegistryService: MancalaGameRegistryService

    private lateinit var sessionA: WebSocketSession
    private lateinit var sessionB: WebSocketSession

    private lateinit var game: Game
    private lateinit var gameRoom: GameRoom

    private lateinit var game1: Game
    private lateinit var gameRoom1: GameRoom

    @BeforeEach
    fun setUp() {
        sessionA = mock(WebSocketSession::class.java)
        sessionB = mock(WebSocketSession::class.java)

        game = Game(
            gameId = "testGame",
            pitsA = intArrayOf(4, 4, 4, 4, 4, 4, 0),
            pitsB = intArrayOf(4, 4, 4, 4, 4, 4, 0),
            turn = Turn.B
        )
        game1 = Game(
            gameId = "testGame1",
            pitsA = intArrayOf(4, 4, 4, 4, 4, 4, 0),
            pitsB = intArrayOf(4, 4, 4, 4, 4, 4, 0),
            turn = Turn.A
        )
        gameRoom = GameRoom(
            gameId = game.gameId,
            playerA = sessionA,
            playerB = sessionB,
            full = true,
            multiScreen = true
        )
        gameRoom1 = GameRoom(
            gameId = game1.gameId,
            playerA = sessionA,
            playerB = null,
            full = false,
            multiScreen = true
        )

        // Access private fields using reflection
        val gamesListField = MancalaGameRegistryService::class.memberProperties.find { it.name == "gamesList" }
        gamesListField?.let {
            it.isAccessible = true
            val gamesList = it.get(mancalaGameRegistryService) as MutableList<Game>
            gamesList.add(game)
            gamesList.add(game1)
        }

        val gameRoomListField = MancalaGameRegistryService::class.memberProperties.find { it.name == "gameRoomList" }
        gameRoomListField?.let {
            it.isAccessible = true
            val gameRoomList = it.get(mancalaGameRegistryService) as MutableList<GameRoom>
            gameRoomList.add(gameRoom)
            gameRoomList.add(gameRoom1)
        }
    }

    @Test
    fun `test startGame for new game creation`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = null,
            player = Turn.A,
            type = ClientStatus.Start,
            selectedPitIndex = 0,
            multiScreen = true
        )

        mancalaGameRegistryService.startGame(mancalaClientMsg, sessionA)

        // Verify that the game and game room were created and broadcasted
        verify(mancalaSendMessageService).broadcastMessage(any()?:game, any()?:gameRoom)
        verifyNoMoreInteractions(mancalaSendMessageService)
    }


    @Test
    fun `test startGame for adding player to existing game`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game1.gameId,
            player = Turn.B,
            type = ClientStatus.Start,
            selectedPitIndex = 0,
            multiScreen = true
        )

        mancalaGameRegistryService.startGame(mancalaClientMsg, sessionB)

        // Verify that the player was added to the existing game room and broadcasted
        verify(mancalaSendMessageService).broadcastMessage(any()?:game1, any()?:gameRoom1)
        verifyNoMoreInteractions(mancalaSendMessageService)
    }

    @Test
    fun `test startGame for adding player to a Game Room which is full`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game.gameId,
            player = Turn.B,
            type = ClientStatus.Start,
            selectedPitIndex = 0,
            multiScreen = true
        )

        mancalaGameRegistryService.startGame(mancalaClientMsg, sessionB)

        // Verify that the player was added to the existing game room and broadcasted
        verify(mancalaSendMessageService).emitMessage(sessionB, game, MancalaConstants.INVALID_REQUEST)
        verifyNoMoreInteractions(mancalaSendMessageService)
        verifyNoInteractions(mancalaGamePlayService)
    }

    @Test
    fun `test startGame for adding player to an invalid gameRoom`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = "InvalidID",
            player = Turn.B,
            type = ClientStatus.Start,
            selectedPitIndex = 0,
            multiScreen = true
        )

        mancalaGameRegistryService.startGame(mancalaClientMsg, sessionB)

        // Verify that the player was added to the existing game room and broadcasted
        verify(mancalaSendMessageService).emitMessage(sessionB, error=MancalaConstants.INVALID_REQUEST)
        verifyNoMoreInteractions(mancalaSendMessageService)
        verifyNoInteractions(mancalaGamePlayService)
    }

    @Test
    fun `test playGame with Player B's valid turn`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game.gameId,
            player = Turn.B,
            type = ClientStatus.InProgress,
            selectedPitIndex = 0,
            multiScreen = true
        )

        `when`(sessionB.id).thenReturn("playerB")
        `when`(mancalaGamePlayService.play(mancalaClientMsg, game)).thenReturn(game)

        mancalaGameRegistryService.playGame(mancalaClientMsg, sessionB)

        // Verify that play was called and broadcastMessage was called with the updated game
        verify(mancalaGamePlayService).play(mancalaClientMsg, game)
        verify(mancalaSendMessageService).broadcastMessage(game, gameRoom)
        verifyNoMoreInteractions(mancalaSendMessageService)
    }

    @Test
    fun `test playGame with Player B's unauthorized turn`() {
        val mancalaClientMsg = MancalaClientMsg(
            gameId = game.gameId,
            player = Turn.B,
            type = ClientStatus.InProgress,
            selectedPitIndex = 6,
            multiScreen = true
        )

        `when`(sessionA.id).thenReturn("playerA")

        mancalaGameRegistryService.playGame(mancalaClientMsg, sessionA)

        // Verify that emitMessage was called with INVALID_REQUEST and no other interactions occurred
        verify(mancalaSendMessageService).emitMessage(sessionA, game, MancalaConstants.INVALID_REQUEST)
        verifyNoMoreInteractions(mancalaSendMessageService)
        verifyNoInteractions(mancalaGamePlayService)
    }


}
