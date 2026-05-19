package com.example.financialtransactions.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "approved_transactions")
public class ApprovedTransaction {
    @PrimaryKey
    @NonNull
    public String transactionId;

    public ApprovedTransaction(@NonNull String transactionId) {
        this.transactionId = transactionId;
    }
}
