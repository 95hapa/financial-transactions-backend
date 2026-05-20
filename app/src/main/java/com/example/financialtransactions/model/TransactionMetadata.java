package com.example.financialtransactions.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaction_metadata")
public class TransactionMetadata {
    @PrimaryKey
    @NonNull
    public String transactionId;

    public String customMerchantName;

    public TransactionMetadata(@NonNull String transactionId, String customMerchantName) {
        this.transactionId = transactionId;
        this.customMerchantName = customMerchantName;
    }
}
