package com.example.financialtransactions.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plaid_items")
public class PlaidItem {
    @PrimaryKey
    @NonNull
    public String itemId;
    public String accessToken;
    public String institutionName;

    public PlaidItem(@NonNull String itemId, String accessToken, String institutionName) {
        this.itemId = itemId;
        this.accessToken = accessToken;
        this.institutionName = institutionName;
    }
}
