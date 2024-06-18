package de.saschahlusiak.freebloks.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "highscores"
)
data class HighScoreEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Int?,

    @ColumnInfo("created_at", defaultValue = "0")
    val timestamp: Long = System.currentTimeMillis() / 1000L,

    @ColumnInfo(name = "gamemode")
    val gameMode: Int,

    @ColumnInfo(name = "points")
    val points: Int,

    @ColumnInfo(name = "stonesleft")
    val stonesLeft: Int,

    @ColumnInfo(name = "playercolor")
    val playerColor: Int,

    @ColumnInfo(name = "place")
    val place: Int,

    @ColumnInfo(name = "flags")
    val flags: Int
) {
    val createdAt: Date get() = Date(timestamp * 1000L)

    val isPerfect: Boolean get() = (flags and FLAG_PERFECT) == FLAG_PERFECT

    companion object {
        const val FLAG_PERFECT = 0x01
    }
}
