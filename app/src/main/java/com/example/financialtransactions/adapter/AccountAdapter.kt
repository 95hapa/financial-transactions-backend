package com.example.financialtransactions.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtransactions.R
import com.example.financialtransactions.model.Account
import java.util.*

class AccountAdapter(
    private var accounts: List<Account>,
    private val onUnlinkClick: (Account) -> Unit
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAccountName: TextView = view.findViewById(R.id.tvAccountName)
        val tvInstitution: TextView = view.findViewById(R.id.tvInstitution)
        val tvBalance: TextView = view.findViewById(R.id.tvBalance)
        val btnUnlink: Button = view.findViewById(R.id.btnUnlink)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        holder.tvAccountName.text = account.name
        holder.tvInstitution.text = account.institution
        holder.tvBalance.text = String.format(Locale.US, "$%.2f", account.balance)
        
        holder.btnUnlink.setOnClickListener {
            onUnlinkClick(account)
        }
    }

    override fun getItemCount() = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        this.accounts = newAccounts
        notifyDataSetChanged()
    }
}
