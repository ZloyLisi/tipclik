package com.example.tipclik4

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Role::class, PrintType::class, PaperType::class, Order::class, OrderType::class], version = 1)
abstract class MainDB : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: MainDB? = null

        fun getInstance(context: Context): MainDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDB::class.java,
                    "main_database"
                ).addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}