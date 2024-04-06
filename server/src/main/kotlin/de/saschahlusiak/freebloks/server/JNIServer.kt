package de.saschahlusiak.freebloks.server

import android.util.Log
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape

object JNIServer {
    private val tag = JNIServer::class.java.simpleName

    private const val DEFAULT_PORT = GameClient.DEFAULT_PORT

    // abstract name of the unix domain socket for local games
    const val LOCAL_SOCKET_NAME = ""

    @Suppress("FunctionName")
    private external fun get_number_of_processors(): Int

    @Suppress("FunctionName")
    private external fun native_run_server(
        interface_: String?,
        port: Int,
        game_mode: Int,
        field_size_x: Int,
        field_size_y: Int,
        stones: IntArray?,
        ki_mode: Int,
        ki_threads: Int,
        force_delay: Boolean
    ): Int

    @Suppress("FunctionName")
    private external fun native_resume_server(
        interface_: String?,
        port: Int,
        field_size_x: Int,
        field_size_y: Int,
        current_player: Int,
        spieler: IntArray,
        field_data: IntArray,
        player_stone_data: IntArray,
        game_mode: Int,
        ki_mode: Int,
        ki_threads: Int,
        force_delay: Boolean
    ): Int

    /**
     * @param isLocal whether local unix domain sockets should be used
     * @param gameMode GameMode
     * @param size board size
     * @param stones the stones to use, or null for default
     * @param kiMode KI Strength (higher values means more random; easier)
     * @param forceDelay whether the spawned server should insert delays between CPU players
     */
    fun runServerForNewGame(
        isLocal: Boolean,
        gameMode: GameMode,
        size: Int,
        stones: IntArray?,
        kiMode: Int,
        forceDelay: Boolean
    ): Int {
        val threads = get_number_of_processors()
        Log.d(tag, "spawning server with $threads threads")

        return native_run_server(
            interface_ = if (isLocal) LOCAL_SOCKET_NAME else null,
            port = if (isLocal) 0 else DEFAULT_PORT,
            game_mode = gameMode.ordinal,
            field_size_x = size,
            field_size_y = size,
            stones = stones,
            ki_mode = kiMode,
            ki_threads = threads,
            force_delay = forceDelay
        )
    }

    fun runServerForExistingGame(game: Game, kiMode: Int): Int {
        val threads = get_number_of_processors()
        Log.d(tag, "spawning server with $threads threads")

        val board = game.board
        val playerStonesAvailable = IntArray(Shape.COUNT * 4)

        for (player in 0..3) {
            for (shape in 0 until Shape.COUNT) {
                playerStonesAvailable[player * Shape.COUNT + shape] = board.getPlayer(player).getStone(shape).available
            }
        }

        // always uses unix domain socket
        return native_resume_server(
            LOCAL_SOCKET_NAME,
            port = 0,
            field_size_x = board.width,
            field_size_y = board.height,
            current_player = game.currentPlayer,
            spieler = game.playerTypes,
            field_data = board.fields,
            player_stone_data = playerStonesAvailable,
            game_mode = game.gameMode.ordinal,
            ki_mode = kiMode,
            ki_threads = threads,
            force_delay = true
        )
    }

    init {
        Log.d(tag, "loading server.so")
        System.loadLibrary("server")
    }
}