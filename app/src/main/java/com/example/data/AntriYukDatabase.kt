package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SalonEntity::class,
        SalonServiceEntity::class,
        HairstylistEntity::class,
        QueueTicketEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AntriYukDatabase : RoomDatabase() {
    abstract fun antriYukDao(): AntriYukDao

    companion object {
        @Volatile
        private var INSTANCE: AntriYukDatabase? = null

        fun getInstance(context: Context): AntriYukDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AntriYukDatabase::class.java,
                    "antriyuk_salon.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
