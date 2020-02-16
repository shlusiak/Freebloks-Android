package de.saschahlusiak.freebloks.client

import android.util.Log
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape

object JNIServer {
    private val tag = JNIServer::class.java.simpleName

    private external fun get_number_of_processors(): Int

    private external fun native_run_server(
        game_mode: Int,
        field_size_x: Int,
        field_size_y: Int,
        stones: IntArray?,
        ki_mode: Int,
        ki_threads: Int
    ): Int

    private external fun native_resume_server(
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

    @JvmStatic
    fun runServerForNewGame(gameMode: GameMode, size: Int, stones: IntArray?, kiMode: Int): Int {
        val threads = get_number_of_processors()
        Log.d(tag, "spawning server with $threads threads")
        return native_run_server(gameMode.ordinal, size, size, stones, kiMode, threads)
    }

    @JvmStatic
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
            board.width,
            board.height,
            game.currentPlayer,
            game.playerTypes,
            board.fields,
            playerStonesAvailable,
            game.gameMode.ordinal,
            kiMode,
            threads
        )
    }

    init {
        Log.d(tag, "loading server.so")
        System.loadLibrary("server")
    }
}