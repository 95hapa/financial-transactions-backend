package com.example.financialtransactions.adapter

import android.view.LayoutInflater
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financialtransactions.R
import com.example.financialtransactions.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardTransaction: CardView = view.findViewById(R.id.cardTransaction)
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
        holder.tvInstitution.text = transaction.institution
        
        val context = holder.itemView.context
        
        // Reset card background to default surface color
        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        holder.cardTransaction.setCardBackgroundColor(typedValue.data)
        
        // Plaid Logic: Positive = Spending (Debit), Negative = Income (Credit)
        if (transaction.amount > 0) {
            // Debit (Spending) - Amount in Red
            holder.tvAmount.text = String.format(Locale.US, "-$%.2f", transaction.amount)
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.text_debit))
        } else {
            // Credit (Income/Refund) - Amount in Green
            // Convert negative to positive display
            holder.tvAmount.text = String.format(Locale.US, "+$%.2f", Math.abs(transaction.amount))
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.text_credit))
        }
        
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(transaction.date)
    }

    override fun getItemCount() = transactions.size
}
