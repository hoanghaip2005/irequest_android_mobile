package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectUserAdapter(
    private val users: List<UserItem>,
    private val onUserClick: (UserItem) -> Unit
) : RecyclerView.Adapter<SelectUserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        
        holder.tvUserName.text = user.userName
        holder.tvUserEmail.text = user.email
        
        if (user.departmentName != null) {
            holder.tvDepartment.visibility = View.VISIBLE
            holder.tvDepartment.text = user.departmentName
        } else {
            holder.tvDepartment.visibility = View.GONE
        }
        
        // Set default avatar
        holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background)
        
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
    }
}
