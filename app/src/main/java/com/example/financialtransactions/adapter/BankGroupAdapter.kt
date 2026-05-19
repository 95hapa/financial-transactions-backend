package com.example.financialtransactions.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtransactions.R
import com.example.financialtransactions.model.Account

class BankGroupAdapter(
    private var groups: Map<String, List<Account>>,
    private val onUnlinkClick: (Account) -> Unit
) : RecyclerView.Adapter<BankGroupAdapter.BankViewHolder>() {

    class BankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBankName: TextView = view.findViewById(R.id.tvBankName)
        val rvBankAccounts: RecyclerView = view.findViewById(R.id.rvBankAccounts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bank_group, parent, false)
        return BankViewHolder(view)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bankName = groups.keys.elementAt(position)
        val accounts = groups[bankName] ?: emptyList()
        
        holder.tvBankName.text = bankName
        
        holder.rvBankAccounts.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.rvBankAccounts.adapter = AccountAdapter(accounts, onUnlinkClick)
    }

    override fun getItemCount() = groups.size

    fun updateGroups(newGroups: Map<String, List<Account>>) {
        this.groups = newGroups
        notifyDataSetChanged()
    }
}
