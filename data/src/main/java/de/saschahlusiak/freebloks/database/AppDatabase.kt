package de.saschahlusiak.freebloks.database

import android.app.Application
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.saschahlusiak.freebloks.database.dao.HighScoreDao
import de.saschahlusiak.freebloks.database.entity.HighScoreEntry
import javax.inject.Singleton

@Database(
    entities = [
        HighScoreEntry::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
}

@Module
@InstallIn(SingletonComponent::class)
class AppDatabaseModule {
    @Provides
    @Singleton
    fun getDatabase(app: Application): AppDatabase = Room.databaseBuilder(
        app,
        AppDatabase::class.java, "freebloks.db"
    ).build()

    @Provides
    fun getHighScoreDao(db: AppDatabase) = db.highScoreDao()
}