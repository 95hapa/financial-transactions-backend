package com.example.financialtransactions.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "manual_accounts")
public class ManualAccount {
    @PrimaryKey
    @NonNull
    public String id;

    public String name;
    public String institution;
    public double balance;
    public String type; // CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT

    public ManualAccount(@NonNull String id, String name, String institution, double balance, String type) {
        this.id = id;
        this.name = name;
        this.institution = institution;
        this.balance = balance;
        this.type = type;
    }
}
