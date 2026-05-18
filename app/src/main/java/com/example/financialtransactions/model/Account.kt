package com.example.financialtransactions.model

data class Account(
    val id: String,
    val name: String,
    val institution: String,
    val balance: Double,
    val type: AccountType
)

enum class AccountType {
    CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT
}
