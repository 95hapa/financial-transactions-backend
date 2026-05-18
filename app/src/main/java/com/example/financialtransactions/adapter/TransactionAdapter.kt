package com.example.financialtransactions.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtransactions.R
import com.example.financialtransactions.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMerchant: TextView = view.findViewById(R.id.tvMerchant)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvInstitution: TextView = view.findViewById(R.id.tvInstitution)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvMerchant.text = transaction.merchant
        holder.tvAmount.text = String.format(Locale.US, "$%.2f", transaction.amount)
        holder.tvInstitution.text = transaction.institution
        
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(transaction.date)
    }

    override fun getItemCount() = transactions.size
}
