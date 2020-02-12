package de.saschahlusiak.freebloks.network

enum class MessageType(val rawValue: Int) {
    Unknown(0),
    RequestPlayer(1),
    GrantPlayer(2),
    CurrentPlayer(3),
    SetStone(4),
    StartGame(5),
    GameFinish(6),
    ServerStatus(7),
    Chat(8),
    RequestUndo(9),
    UndoStone(10),
    RequestHint(11),
    StoneHint(12),
    RevokePlayer(13),
    RequestGameMode(14);

    companion object {
        fun from(type: Int): MessageType? {
            return values().firstOrNull { it.rawValue == type }
        }
    }
}