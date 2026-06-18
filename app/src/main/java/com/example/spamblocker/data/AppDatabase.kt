package com.example.spamblocker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromMatchType(value: MatchType): String = value.name

    @TypeConverter
    fun toMatchType(value: String): MatchType = MatchType.valueOf(value)

}

@Database(
    entities = [BlockPattern::class, BlockedCallLog::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun blockPatternDao(): BlockPatternDao
    abstract fun blockedCallLogDao(): BlockedCallLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spamblocker_db"
                )
                    .fallbackToDestructiveMigration() // fine during dev; remove for production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}