package com.project.irequest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.irequest.data.models.User
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firebase Repository for User management
 */
class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    private val usersCollection = firestore.collection("users")
    
    /**
     * Get current user profile
     */
    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.success(null)
            
            val snapshot = usersCollection
                .document(userId)
                .get()
                .await()
            
            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create or update user profile
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            usersCollection
                .document(userId)
                .set(user)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile fields
     */
    suspend fun updateUserFields(fields: Map<String, Any>): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            usersCollection
                .document(userId)
                .update(fields)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val snapshot = usersCollection
                .document(userId)
                .get()
                .await()
            
            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
