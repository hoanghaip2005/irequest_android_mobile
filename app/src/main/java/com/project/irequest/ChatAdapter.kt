package com.project.irequest

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val chatList: List<ChatItem>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = chatList[position]

        holder.tvName.text = item.name
        holder.tvLastMessage.text = item.message

        val relativeTime = DateUtils.getRelativeTimeSpanString(
            item.timestampMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
        holder.tvTime.text = relativeTime

        holder.imgAvatar.setImageResource(item.avatarResId)

        if (item.unreadCount > 0) {
            holder.tvUnreadCount.visibility = View.VISIBLE
            holder.tvUnreadCount.text = item.unreadCount.toString()
        } else {
            holder.tvUnreadCount.visibility = View.GONE
        }

        // Xử lý sự kiện nhấp chuột
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatDetailActivity::class.java)
            intent.putExtra("CHAT_ID", item.chatId) // Truyền chat ID (để update unread)
            intent.putExtra("SHARED_CHAT_ID", item.sharedChatId) // Truyền shared chat ID (để lắng nghe tin nhắn)
            intent.putExtra("CHAT_NAME", item.name) // Truyền tên cuộc trò chuyện
            intent.putExtra("AVATAR_RES_ID", item.avatarResId) // Truyền ID ảnh đại diện
            
            // Truyền receiver info nếu có (cho direct chat)
            item.receiverId?.let { intent.putExtra("RECEIVER_ID", it) }
            item.receiverName?.let { intent.putExtra("RECEIVER_NAME", it) }
            
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvUnreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
    }
}