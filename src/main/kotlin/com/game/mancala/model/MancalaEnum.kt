package com.game.mancala.model



enum class Turn {
    A, B
}

enum class GameStatus {
    Started,
    Waiting,
    InProgress,
    End
}

enum class ClientStatus {
    Start,
    InProgress,
}

enum class Winner {
    A, B, Draw
}
