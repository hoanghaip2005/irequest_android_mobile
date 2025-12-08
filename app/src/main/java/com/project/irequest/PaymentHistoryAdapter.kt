package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.PaymentHistory
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentHistoryAdapter(
    private var payments: List<PaymentHistory> = emptyList(),
    private val onItemClick: (PaymentHistory) -> Unit = {}
) : RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        val tvPaymentName: TextView = view.findViewById(R.id.tvPaymentName)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        
        holder.tvPaymentName.text = payment.paymentName
        holder.tvOrderId.text = "Mã đơn: ${payment.orderId}"
        
        // Format số tiền
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        holder.tvAmount.text = formatter.format(payment.amount)
        
        // Format ngày
        payment.createdAt?.let { timestamp ->
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.tvDate.text = sdf.format(timestamp.toDate())
        }
        
        // Trạng thái
        when (payment.status) {
            "success" -> {
                holder.tvStatus.text = "Thành công"
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
                )
            }
            "failed" -> {
                holder.tvStatus.text = "Thất bại"
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
                )
            }
            else -> {
                holder.tvStatus.text = "Đang xử lý"
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
                )
            }
        }
        
        holder.cardView.setOnClickListener {
            onItemClick(payment)
        }
    }

    override fun getItemCount() = payments.size

    fun updateData(newPayments: List<PaymentHistory>) {
        payments = newPayments
        notifyDataSetChanged()
    }
}
