package com.example.financialtransactions.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.financialtransactions.model.PlaidItem;
import java.util.List;

@Dao
public interface PlaidItemDao {
    @Query("SELECT * FROM plaid_items")
    List<PlaidItem> getAllItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(PlaidItem item);

    @Query("DELETE FROM plaid_items WHERE itemId = :itemId")
    void deleteByItemId(String itemId);
}
