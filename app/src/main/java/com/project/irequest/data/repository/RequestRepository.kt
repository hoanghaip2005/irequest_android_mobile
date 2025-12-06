package com.project.irequest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.irequest.data.models.Request
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firebase Repository for Request management
 */
class RequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    private val requestsCollection = firestore.collection("requests")
    
    /**
     * Get all requests
     */
    suspend fun getAllRequests(limit: Int = 50): Result<List<Request>> {
        return try {
            val snapshot = requestsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val requests = snapshot.toObjects(Request::class.java)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get requests created by current user
     */
    suspend fun getMyRequests(): Result<List<Request>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            val snapshot = requestsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val requests = snapshot.toObjects(Request::class.java)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get requests assigned to current user
     */
    suspend fun getMyTasks(): Result<List<Request>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            val snapshot = requestsCollection
                .whereEqualTo("assignedUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val requests = snapshot.toObjects(Request::class.java)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get request by ID
     */
    suspend fun getRequestById(requestId: String): Result<Request?> {
        return try {
            val snapshot = requestsCollection
                .document(requestId)
                .get()
                .await()
            
            val request = snapshot.toObject(Request::class.java)
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create new request
     */
    suspend fun createRequest(request: Request): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val userEmail = auth.currentUser?.email
            
            val newRequest = request.copy(
                userId = userId,
                userEmail = userEmail,
                createdAt = Date()
            )
            
            val documentRef = requestsCollection.add(newRequest).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update request
     */
    suspend fun updateRequest(requestId: String, fields: Map<String, Any>): Result<Unit> {
        return try {
            requestsCollection
                .document(requestId)
                .update(fields + mapOf("updatedAt" to Date()))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete request
     */
    suspend fun deleteRequest(requestId: String): Result<Unit> {
        return try {
            requestsCollection
                .document(requestId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get requests by status
     */
    suspend fun getRequestsByStatus(statusId: Int): Result<List<Request>> {
        return try {
            val snapshot = requestsCollection
                .whereEqualTo("statusId", statusId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val requests = snapshot.toObjects(Request::class.java)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
