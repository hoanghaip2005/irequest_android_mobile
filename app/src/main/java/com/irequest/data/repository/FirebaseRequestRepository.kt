package com.example.irequest.data.repository

import com.example.irequest.data.models.*
import com.example.irequest.data.models.request.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firebase Firestore Repository for Requests
 */
class FirebaseRequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    private val requestsCollection = firestore.collection("requests")
    private val commentsCollection = firestore.collection("comments")
    private val notificationsCollection = firestore.collection("notifications")
    private val historyCollection = firestore.collection("request_history")
    
    /**
     * Get all requests (for admin/kanban board view)
     */
    suspend fun getAllRequests(pageSize: Int = 100): Result<List<Request>> {
        return try {
            val snapshot = requestsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
                .get()
                .await()
            
            val requests = snapshot.toObjects(Request::class.java)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all requests for current user
     */
    suspend fun getMyRequests(page: Int = 1, pageSize: Int = 20): Result<List<Request>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            val snapshot = requestsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
                .get()
                .await()
            
            val requests = snapshot.toObjects(Request::class.java)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get requests assigned to me
     */
    suspend fun getMyTasks(page: Int = 1, pageSize: Int = 20): Result<List<Request>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            val snapshot = requestsCollection
                .whereEqualTo("assignedUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
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
    suspend fun getRequestById(requestId: Int): Result<Request> {
        return try {
            val snapshot = requestsCollection
                .whereEqualTo("requestId", requestId)
                .limit(1)
                .get()
                .await()
            
            val request = snapshot.documents.firstOrNull()?.toObject(Request::class.java)
                ?: return Result.failure(Exception("Request not found"))
            
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create new request
     */
    suspend fun createRequest(request: CreateRequestModel): Result<Request> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val user = auth.currentUser
            
            // Generate new request ID (in real app, use auto-increment or Firebase auto ID)
            val requestId = System.currentTimeMillis().toInt()
            
            val newRequest = Request(
                requestId = requestId,
                title = request.title,
                description = request.description,
                priorityId = request.priorityId,
                workflowId = request.workflowId,
                departmentId = request.departmentId,
                attachmentUrl = request.attachmentUrl,
                attachmentFileName = request.attachmentFileName,
                attachmentFileType = request.attachmentFileType,
                attachmentFileSize = request.attachmentFileSize,
                userId = userId,
                userName = user?.displayName,
                userEmail = user?.email,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            requestsCollection.document(requestId.toString()).set(newRequest).await()
            
            // Create history entry
            createHistoryEntry(requestId, "created", "Request created")
            
            Result.success(newRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update request
     */
    suspend fun updateRequest(request: UpdateRequestModel): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            updates["updatedAt"] = Date()
            
            request.title?.let { updates["title"] = it }
            request.description?.let { updates["description"] = it }
            request.priorityId?.let { updates["priorityId"] = it }
            request.statusId?.let { updates["statusId"] = it }
            request.assignedUserId?.let { updates["assignedUserId"] = it }
            
            requestsCollection
                .document(request.requestId.toString())
                .update(updates)
                .await()
            
            // Create history entry
            createHistoryEntry(request.requestId, "updated", "Request updated")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update starred status
     */
    suspend fun updateStarred(requestId: Int, isStarred: Boolean): Result<Unit> {
        return try {
            requestsCollection
                .document(requestId.toString())
                .update("isStarred", isStarred)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get comments for request
     */
    suspend fun getComments(requestId: Int): Result<List<Comment>> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("requestId", requestId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val comments = snapshot.toObjects(Comment::class.java)
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add comment
     */
    suspend fun addComment(request: AddCommentRequest): Result<Comment> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val user = auth.currentUser
            
            val commentId = System.currentTimeMillis().toInt()
            
            val comment = Comment(
                commentId = commentId,
                content = request.content,
                requestId = request.requestId,
                userId = userId,
                userName = user?.displayName,
                createdAt = Date()
            )
            
            commentsCollection.document(commentId.toString()).set(comment).await()
            
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Approve request
     */
    suspend fun approveRequest(request: ApproveRequestModel): Result<Unit> {
        return try {
            val updates = mapOf(
                "isApproved" to true,
                "approvedAt" to Date(),
                "updatedAt" to Date()
            )
            
            requestsCollection
                .document(request.requestId.toString())
                .update(updates)
                .await()
            
            // Create history entry
            createHistoryEntry(request.requestId, "approved", "Request approved: ${request.notes ?: ""}")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reject request
     */
    suspend fun rejectRequest(request: RejectRequestModel): Result<Unit> {
        return try {
            val updates = mapOf(
                "isApproved" to false,
                "rejectionReason" to request.reason,
                "updatedAt" to Date()
            )
            
            requestsCollection
                .document(request.requestId.toString())
                .update(updates)
                .await()
            
            // Create history entry
            createHistoryEntry(request.requestId, "rejected", "Request rejected: ${request.reason}")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get request history
     */
    suspend fun getRequestHistory(requestId: Int): Result<List<RequestHistory>> {
        return try {
            val snapshot = historyCollection
                .whereEqualTo("requestId", requestId)
                .orderBy("changedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val history = snapshot.toObjects(RequestHistory::class.java)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create history entry (private helper)
     */
    private suspend fun createHistoryEntry(requestId: Int, actionType: String, notes: String) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val user = auth.currentUser
            
            val historyId = System.currentTimeMillis().toInt()
            
            val history = RequestHistory(
                historyId = historyId,
                requestId = requestId,
                stepId = 0,
                stepName = actionType,
                userId = userId,
                userName = user?.displayName,
                startTime = Date(),
                endTime = null,
                status = "Completed",
                note = notes,
                processingTimeHours = null,
                createdAt = Date()
            )
            
            historyCollection.document(historyId.toString()).set(history).await()
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            e.printStackTrace()
        }
    }
    
    /**
     * Search requests
     */
    suspend fun searchRequests(query: String): Result<List<Request>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            // Note: Firestore doesn't support full-text search natively
            // This is a simple implementation - consider using Algolia or similar for production
            val snapshot = requestsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val requests = snapshot.toObjects(Request::class.java)
                .filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.description?.contains(query, ignoreCase = true) == true 
                }
            
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
