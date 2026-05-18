package com.example.financialtransactions.db

import androidx.room.*
import com.example.financialtransactions.model.PlaidItem

@Dao
interface PlaidItemDao {
    @Query("SELECT * FROM plaid_items")
    suspend fun getAllItems(): List<PlaidItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlaidItem)

    @Delete
    suspend fun deleteItem(item: PlaidItem)

    @Query("DELETE FROM plaid_items WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: String)
}
