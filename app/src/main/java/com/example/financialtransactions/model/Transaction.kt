package com.example.financialtransactions.model

import java.util.Date

data class Transaction(
    val id: String,
    val amount: Double,
    val merchant: String,
    val date: Date,
    val accountName: String,
    val category: String,
    val institution: String
)
