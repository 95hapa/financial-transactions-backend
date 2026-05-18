package com.example.financialtransactions.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.financialtransactions.model.PlaidItem

@Database(entities = [PlaidItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plaidItemDao(): PlaidItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "financial_app_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
