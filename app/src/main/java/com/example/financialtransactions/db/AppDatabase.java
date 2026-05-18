package com.example.financialtransactions.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.financialtransactions.model.PlaidItem;

@Database(entities = {PlaidItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlaidItemDao plaidItemDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "financial_app_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
