package de.saschahlusiak.freebloks.server

import android.util.Log
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape

object JNIServer {
    private val tag = JNIServer::class.java.simpleName

    private const val DEFAULT_PORT = GameClient.DEFAULT_PORT

    @Suppress("FunctionName")
    private external fun get_number_of_processors(): Int

    @Suppress("FunctionName")
    private external fun native_run_server(
        port: Int,
        game_mode: Int,
        field_size_x: Int,
        field_size_y: Int,
        stones: IntArray?,
        ki_mode: Int,
        ki_threads: Int
    ): Int

    @Suppress("FunctionName")
    private external fun native_resume_server(
        port: Int,
        field_size_x: Int,
        field_size_y: Int,
        current_player: Int,
        spieler: IntArray,
        field_data: IntArray,
        player_stone_data: IntArray,
        game_mode: Int,
        ki_mode: Int,
        ki_threads: Int
    ): Int

    fun runServerForNewGame(gameMode: GameMode, size: Int, stones: IntArray?, kiMode: Int): Int {
        val threads = get_number_of_processors()
        Log.d(tag, "spawning server with $threads threads")
        return native_run_server(
            port = DEFAULT_PORT,
            game_mode = gameMode.ordinal,
            field_size_x = size,
            field_size_y = size,
            stones = stones,
            ki_mode = kiMode,
            ki_threads = threads
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

        return native_resume_server(
            port = DEFAULT_PORT,
            field_size_x = board.width,
            field_size_y = board.height,
            current_player = game.currentPlayer,
            spieler = game.playerTypes,
            field_data = board.fields,
            player_stone_data = playerStonesAvailable,
            game_mode = game.gameMode.ordinal,
            ki_mode = kiMode,
            ki_threads = threads
        )
    }

    init {
        Log.d(tag, "loading server.so")
        System.loadLibrary("server")
    }
}