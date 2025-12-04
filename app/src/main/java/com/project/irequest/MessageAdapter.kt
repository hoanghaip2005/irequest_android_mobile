package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Các hằng số để xác định loại view
    private val VIEW_TYPE_TEXT_SENT = 1
    private val VIEW_TYPE_TEXT_RECEIVED = 2
    private val VIEW_TYPE_IMAGE_SENT = 3
    private val VIEW_TYPE_IMAGE_RECEIVED = 4
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isImage && message.isSentByMe -> VIEW_TYPE_IMAGE_SENT
            message.isImage && !message.isSentByMe -> VIEW_TYPE_IMAGE_RECEIVED
            !message.isImage && message.isSentByMe -> VIEW_TYPE_TEXT_SENT
            else -> VIEW_TYPE_TEXT_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT_SENT -> {
                val view = layoutInflater.inflate(R.layout.item_message_sent, parent, false)
                TextMessageViewHolder(view)
            }
            VIEW_TYPE_TEXT_RECEIVED -> {
                val view = layoutInflater.inflate(R.layout.item_message_received, parent, false)
                TextMessageViewHolder(view)
            }
            VIEW_TYPE_IMAGE_SENT -> {
                val view = layoutInflater.inflate(R.layout.item_message_image_sent, parent, false)
                ImageMessageViewHolder(view)
            }
            VIEW_TYPE_IMAGE_RECEIVED -> {
                val view = layoutInflater.inflate(R.layout.item_message_image_received, parent, false)
                ImageMessageViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.item_message_received, parent, false)
                TextMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        
        when (holder) {
            is TextMessageViewHolder -> {
                holder.messageBody.text = message.content
                holder.timeText?.text = timeFormat.format(Date(message.timestamp))
            }
            is ImageMessageViewHolder -> {
                // Load image using Glide
                Glide.with(holder.itemView.context)
                    .load(message.content)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imageView)
                    
                holder.timeText?.text = timeFormat.format(Date(message.timestamp))
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageBody: TextView = itemView.findViewById(R.id.tv_message_body)
        val timeText: TextView? = itemView.findViewById(R.id.tv_message_time)
    }
    
    class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_message_image)
        val timeText: TextView? = itemView.findViewById(R.id.tv_message_time)
    }
}