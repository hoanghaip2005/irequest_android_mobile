package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MessageAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSent) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = if (viewType == VIEW_TYPE_SENT) {
            layoutInflater.inflate(R.layout.item_message_sent, parent, false)
        } else {
            layoutInflater.inflate(R.layout.item_message_received, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        
        // Handle text message
        if (message.text.isNotEmpty()) {
            holder.messageBody.visibility = View.VISIBLE
            holder.messageBody.text = message.text
        } else {
            holder.messageBody.visibility = View.GONE
        }
        
        // Handle image message
        if (message.imageUrl != null && message.imageUrl.isNotEmpty()) {
            holder.messageImage?.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(message.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.messageImage!!)
        } else {
            holder.messageImage?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageBody: TextView = itemView.findViewById(R.id.tvMessage)
        val messageImage: ImageView? = itemView.findViewById(R.id.ivMessageImage)
    }
}