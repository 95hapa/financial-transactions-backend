package com.example.financialtransactions.repository

import android.content.Context
import com.example.financialtransactions.db.AppDatabase
import com.example.financialtransactions.model.Account
import com.example.financialtransactions.model.PlaidItem
import com.example.financialtransactions.model.Transaction
import com.example.financialtransactions.network.AccountsRequest
import com.example.financialtransactions.network.NetworkClient
import com.example.financialtransactions.network.TransactionsRequest
import com.example.financialtransactions.network.UnlinkRequest
import java.util.*

class TransactionRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.plaidItemDao()

    suspend fun getRealTransactions(): List<Transaction> {
        val items = dao.getAllItems()
        if (items.isEmpty()) return emptyList()
        
        val tokens = items.map { it.accessToken }
        return NetworkClient.plaidApi.getTransactions(TransactionsRequest(tokens))
    }

    suspend fun getRealAccounts(): List<Account> {
        val items = dao.getAllItems()
        if (items.isEmpty()) return emptyList()

        val tokens = items.map { it.accessToken }
        return NetworkClient.plaidApi.getAccounts(AccountsRequest(tokens))
    }

    suspend fun savePlaidItem(itemId: String, accessToken: String) {
        dao.insertItem(PlaidItem(itemId, accessToken))
    }

    suspend fun unlinkAccount(accountId: String): Boolean {
        // Find the item with this account ID (simplified for demo)
        val items = dao.getAllItems()
        val itemToUnlink = items.firstOrNull() // In real app, match account to item
        
        return if (itemToUnlink != null) {
            try {
                NetworkClient.plaidApi.unlinkAccount(UnlinkRequest(itemToUnlink.accessToken))
                dao.deleteByItemId(itemToUnlink.itemId)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    fun getDummyTransactions(): List<Transaction> {
        return listOf(
            Transaction("1", 0.0, "Starbucks", Date(0), "Quicksilver", "Food & Drink", "Capital One"),
            Transaction("2", 0.0, "Landlord", Date(0), "Total Checking", "Rent", "Chase"),
            Transaction("3", 0.0, "Employer Inc", Date(0), "Savings", "Income", "Fidelity"),
            Transaction("4", 0.0, "Netflix", Date(0), "Sapphire Preferred", "Entertainment", "Chase"),
            Transaction("5", 0.0, "Shell Oil", Date(0), "Voya Credit", "Gas", "Voya")
        )
    }
}
