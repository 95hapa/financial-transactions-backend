package com.example.financialtransactions.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "account_metadata")
public class AccountMetadata {
    @PrimaryKey
    @NonNull
    public String accountId;

    public String customName;

    public AccountMetadata(@NonNull String accountId, String customName) {
        this.accountId = accountId;
        this.customName = customName;
    }
}
