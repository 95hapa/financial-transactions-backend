package com.example.financialtransactions

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.financialtransactions.adapter.AccountAdapter
import com.example.financialtransactions.adapter.TransactionAdapter
import com.example.financialtransactions.network.ExchangeRequest
import com.example.financialtransactions.network.NetworkClient
import com.example.financialtransactions.repository.TransactionRepository
import com.plaid.link.OpenPlaidLink
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: TransactionRepository
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private val linkAccountLauncher = registerForActivityResult(OpenPlaidLink()) { result ->
        when (result) {
            is LinkSuccess -> {
                Log.d("MainActivity", "Plaid Success! Exchanging token...")
                exchangePublicToken(result.publicToken)
            }
            is LinkExit -> {
                val error = result.error
                if (error != null) {
                    Log.e("MainActivity", "Plaid Exit Error: ${error.displayMessage}")
                    Toast.makeText(this, "Link Error: ${error.displayMessage}", Toast.LENGTH_LONG).show()
                } else {
                    Log.d("MainActivity", "User cancelled Plaid Link")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        repository = TransactionRepository(this)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        
        // Transactions Setup
        val rvTransactions: RecyclerView = findViewById(R.id.recyclerView)
        rvTransactions.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(emptyList())
        rvTransactions.adapter = transactionAdapter

        // Accounts Setup
        val rvAccounts: RecyclerView = findViewById(R.id.rvAccounts)
        rvAccounts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        accountAdapter = AccountAdapter(emptyList()) { account ->
            unlinkAccount(account.id)
        }
        rvAccounts.adapter = accountAdapter

        findViewById<Button>(R.id.btnLinkAccount).setOnClickListener {
            fetchLinkTokenAndLaunch()
        }

        swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        refreshData()
    }

    private fun fetchLinkTokenAndLaunch() {
        lifecycleScope.launch {
            try {
                val response = NetworkClient.plaidApi.getLinkToken()
                val configuration = LinkTokenConfiguration.Builder()
                    .token(response.link_token)
                    .build()
                linkAccountLauncher.launch(configuration)
            } catch (e: Exception) {
                Log.e("MainActivity", "Link Token Error", e)
                Toast.makeText(this@MainActivity, "Could not start linking", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exchangePublicToken(publicToken: String) {
        lifecycleScope.launch {
            try {
                val response = NetworkClient.plaidApi.exchangePublicToken(ExchangeRequest(publicToken))
                Log.d("MainActivity", "Token exchanged. Saving to DB...")
                
                repository.savePlaidItem(response.item_id, response.access_token)
                
                // Small delay to ensure DB write is visible
                delay(500)
                
                Toast.makeText(this@MainActivity, "Bank connected!", Toast.LENGTH_SHORT).show()
                refreshData()
            } catch (e: Exception) {
                Log.e("MainActivity", "Exchange Error", e)
                Toast.makeText(this@MainActivity, "Failed to save account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unlinkAccount(accountId: String) {
        lifecycleScope.launch {
            try {
                val success = repository.unlinkAccount(accountId)
                if (success) {
                    Toast.makeText(this@MainActivity, "Account removed", Toast.LENGTH_SHORT).show()
                    refreshData()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Unlink Error", e)
            }
        }
    }

    private fun refreshData() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "Refreshing data...")
                
                val realAccounts = repository.getRealAccounts()
                Log.d("MainActivity", "Found ${realAccounts.size} linked accounts")
                accountAdapter.updateAccounts(realAccounts)

                val realTransactions = repository.getRealTransactions()
                Log.d("MainActivity", "Fetched ${realTransactions.size} transactions")
                transactionAdapter = TransactionAdapter(realTransactions)
                findViewById<RecyclerView>(R.id.recyclerView).adapter = transactionAdapter
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Refresh Error", e)
                Toast.makeText(this@MainActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }
}
