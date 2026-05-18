package com.example.financialtransactions.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plaid_items")
data class PlaidItem(
    @PrimaryKey val itemId: String,
    val accessToken: String,
    val institutionName: String = "Linked Bank"
)
