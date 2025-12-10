package com.project.irequest

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SelectUserActivity : BaseActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val allUsers = mutableListOf<UserItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        setupViews()
        loadUsers()
        
        setupBottomNavigation()
        setActiveTab(2)
    }
    
    private fun setupViews() {
        rvUsers = findViewById(R.id.rvUsers)
        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        
        rvUsers.layoutManager = LinearLayoutManager(this)
        
        // Make SearchView more responsive
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = "Tìm kiếm theo tên, email..."
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterUsers(query)
                searchView.clearFocus()
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText)
                return true
            }
        })
    }
    
    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        rvUsers.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid
                
                val snapshot = firestore.collection("users")
                    .get()
                    .await()
                
                allUsers.clear()
                
                snapshot.documents.forEach { doc ->
                    val userId = doc.id
                    if (userId != currentUserId) { // Exclude current user
                        val userName = doc.getString("userName") ?: doc.getString("email") ?: "User"
                        val email = doc.getString("email") ?: ""
                        val avatar = doc.getString("avatar")
                        val departmentName = doc.getString("departmentName")
                        
                        allUsers.add(UserItem(userId, userName, email, departmentName, avatar))
                    }
                }
                
                progressBar.visibility = View.GONE
                
                if (allUsers.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Không tìm thấy người dùng"
                } else {
                    rvUsers.visibility = View.VISIBLE
                    displayUsers(allUsers)
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Lỗi: ${e.message}"
                Toast.makeText(
                    this@SelectUserActivity,
                    "Lỗi tải danh sách: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun filterUsers(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allUsers
        } else {
            allUsers.filter { 
                it.userName.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true) ||
                it.departmentName?.contains(query, ignoreCase = true) == true
            }
        }
        
        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Không tìm thấy: $query"
            rvUsers.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvUsers.visibility = View.VISIBLE
            displayUsers(filtered)
        }
    }
    
    private fun displayUsers(users: List<UserItem>) {
        val adapter = SelectUserAdapter(users) { user ->
            onUserSelected(user)
        }
        rvUsers.adapter = adapter
    }
    
    private fun onUserSelected(user: UserItem) {
        // Return selected user to ChatActivity
        val intent = android.content.Intent()
        intent.putExtra("USER_ID", user.userId)
        intent.putExtra("USER_NAME", user.userName)
        intent.putExtra("USER_EMAIL", user.email)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onNavigationChatClicked() {
        finish()
    }
}

data class UserItem(
    val userId: String,
    val userName: String,
    val email: String,
    val departmentName: String?,
    val avatarUrl: String?
) {
    override fun toString() = userName
}
