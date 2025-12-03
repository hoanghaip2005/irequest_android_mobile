package com.example.irequest.data.repository

import com.example.irequest.data.models.*
import com.example.irequest.data.models.request.*
import com.example.irequest.data.models.response.ApiResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firebase Authentication Repository
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isAuthenticated: Boolean
        get() = currentUser != null
    
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("User is null"))
            
            // Get user data from Firestore
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            
            val user = userDoc.toObject(User::class.java) ?: User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                userName = firebaseUser.displayName ?: ""
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Register new user
     */
    suspend fun register(request: RegisterRequest): Result<User> {
        return try {
            // Create auth user
            val result = auth.createUserWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("User creation failed"))
            
            // Update profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(request.userName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            // Create user document in Firestore
            val user = User(
                id = firebaseUser.uid,
                userName = request.userName,
                email = request.email,
                phoneNumber = request.phoneNumber,
                departmentId = request.departmentId,
                createdAt = Date()
            )
            
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logout
     */
    fun logout() {
        auth.signOut()
    }
    
    /**
     * Get current user profile
     */
    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val uid = currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            val userDoc = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            
            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("User not found"))
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(request: UpdateProfileRequest): Result<User> {
        return try {
            val uid = currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            val updates = mutableMapOf<String, Any>()
            request.userName?.let { updates["userName"] = it }
            request.phoneNumber?.let { updates["phoneNumber"] = it }
            request.homeAddress?.let { updates["homeAddress"] = it }
            request.avatar?.let { updates["avatar"] = it }
            request.birthDate?.let { updates["birthDate"] = it }
            
            firestore.collection("users")
                .document(uid)
                .update(updates)
                .await()
            
            getCurrentUserProfile()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Change password
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("Not authenticated"))
            val email = user.email ?: return Result.failure(Exception("Email not found"))
            
            // Re-authenticate
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            
            // Update password
            user.updatePassword(newPassword).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
