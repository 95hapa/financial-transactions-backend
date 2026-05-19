package com.example.financialtransactions

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var repository: TransactionRepository
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    
    private lateinit var tvNetBalance: TextView
    private lateinit var tvTotalAssets: TextView
    private lateinit var tvTotalLiabilities: TextView
    private lateinit var breakdownContainer: View
    private lateinit var rvAccounts: RecyclerView
    private lateinit var ivToggleAccounts: ImageView

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
        tvNetBalance = findViewById(R.id.tvNetBalance)
        tvTotalAssets = findViewById(R.id.tvTotalAssets)
        tvTotalLiabilities = findViewById(R.id.tvTotalLiabilities)
        breakdownContainer = findViewById(R.id.breakdownContainer)
        rvAccounts = findViewById(R.id.rvAccounts)
        ivToggleAccounts = findViewById(R.id.ivToggleAccounts)
        
        // Transactions Setup
        val rvTransactions: RecyclerView = findViewById(R.id.recyclerView)
        rvTransactions.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(emptyList())
        rvTransactions.adapter = transactionAdapter

        // Accounts Setup (Horizontal Dropdown)
        rvAccounts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        accountAdapter = AccountAdapter(emptyList()) { account ->
            unlinkAccount(account.id)
        }
        rvAccounts.adapter = accountAdapter

        findViewById<Button>(R.id.btnLinkAccount).setOnClickListener {
            fetchLinkTokenAndLaunch()
        }

        findViewById<View>(R.id.netBalanceContainer).setOnClickListener {
            toggleBreakdown()
        }

        findViewById<View>(R.id.llAccountsHeader).setOnClickListener {
            toggleAccountDetails()
        }

        swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        refreshData()
    }

    private fun toggleBreakdown() {
        if (breakdownContainer.visibility == View.VISIBLE) {
            breakdownContainer.visibility = View.GONE
        } else {
            breakdownContainer.visibility = View.VISIBLE
        }
    }

    private fun toggleAccountDetails() {
        if (rvAccounts.visibility == View.VISIBLE) {
            rvAccounts.visibility = View.GONE
            ivToggleAccounts.setImageResource(android.R.drawable.arrow_down_float)
        } else {
            rvAccounts.visibility = View.VISIBLE
            ivToggleAccounts.setImageResource(android.R.drawable.arrow_up_float)
        }
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
                repository.savePlaidItem(response.item_id, response.access_token)
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
                val realAccounts = repository.getRealAccounts()
                accountAdapter.updateAccounts(realAccounts)
                
                // Calculate Totals
                var totalAssets = 0.0
                var totalLiabilities = 0.0
                for (account in realAccounts) {
                    if (account.type == com.example.financialtransactions.model.AccountType.CREDIT_CARD) {
                        totalLiabilities += Math.abs(account.balance)
                    } else {
                        if (account.balance >= 0) {
                            totalAssets += account.balance
                        } else {
                            totalLiabilities += Math.abs(account.balance)
                        }
                    }
                }
                
                val netBalance = totalAssets - totalLiabilities
                tvNetBalance.text = String.format(Locale.US, "$%.2f", netBalance)
                tvTotalAssets.text = String.format(Locale.US, "$%.2f", totalAssets)
                tvTotalLiabilities.text = String.format(Locale.US, "$%.2f", totalLiabilities)

                val realTransactions = repository.getRealTransactions()
                transactionAdapter = TransactionAdapter(realTransactions)
                findViewById<RecyclerView>(R.id.recyclerView).adapter = transactionAdapter
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Refresh Error", e)
                val dummyTxns = repository.getDummyTransactions()
                transactionAdapter = TransactionAdapter(dummyTxns)
                findViewById<RecyclerView>(R.id.recyclerView).adapter = transactionAdapter
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }
}
