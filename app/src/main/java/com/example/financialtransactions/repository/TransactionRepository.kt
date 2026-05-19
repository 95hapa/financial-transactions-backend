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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class TransactionRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.plaidItemDao()

    suspend fun getRealTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        val items = dao.getAllItems()
        if (items.isEmpty()) return@withContext emptyList<Transaction>()
        
        val tokens = items.map { it.accessToken }
        return@withContext NetworkClient.plaidApi.getTransactions(TransactionsRequest(tokens))
    }

    suspend fun getRealAccounts(): List<Account> = withContext(Dispatchers.IO) {
        val items = dao.getAllItems()
        if (items.isEmpty()) return@withContext emptyList<Account>()

        val tokens = items.map { it.accessToken }
        return@withContext NetworkClient.plaidApi.getAccounts(AccountsRequest(tokens))
    }

    suspend fun savePlaidItem(itemId: String, accessToken: String) = withContext(Dispatchers.IO) {
        dao.insertItem(PlaidItem(itemId, accessToken, "Linked Bank"))
    }

    suspend fun unlinkAccount(accountId: String): Boolean = withContext(Dispatchers.IO) {
        val items = dao.getAllItems()
        val itemToUnlink = items.firstOrNull() 
        
        return@withContext if (itemToUnlink != null) {
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
            Transaction("1", 45.50, "Starbucks", Date(), "Quicksilver", "Food & Drink", "Capital One"),
            Transaction("2", 1200.00, "Landlord", Date(), "Total Checking", "Rent", "Chase"),
            Transaction("3", -2500.00, "Employer Inc", Date(), "Savings", "Income", "Fidelity"),
            Transaction("4", 15.99, "Netflix", Date(), "Sapphire Preferred", "Entertainment", "Chase"),
            Transaction("5", 60.00, "Shell Oil", Date(), "Voya Credit", "Gas", "Voya")
        )
    }
}
