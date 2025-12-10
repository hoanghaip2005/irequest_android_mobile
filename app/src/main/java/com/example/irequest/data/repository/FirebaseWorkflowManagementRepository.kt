package com.example.irequest.data.repository

import com.example.irequest.data.models.Workflow
import com.example.irequest.data.models.WorkflowStep
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseWorkflowManagementRepository {
    
    private val db = FirebaseFirestore.getInstance()
    private val workflowsCollection = db.collection("workflows")
    private val workflowStepsCollection = db.collection("workflow_steps")
    
    /**
     * Lấy tất cả workflows đang active
     */
    suspend fun getAllWorkflows(): Result<List<Workflow>> {
        return try {
            val snapshot = workflowsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val workflows = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Workflow::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            
            Result.success(workflows)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Lấy workflow theo ID
     */
    suspend fun getWorkflowById(workflowId: Int): Result<Workflow?> {
        return try {
            val snapshot = workflowsCollection
                .whereEqualTo("workflowId", workflowId)
                .limit(1)
                .get()
                .await()
            
            val workflow = snapshot.documents.firstOrNull()?.toObject(Workflow::class.java)
            Result.success(workflow)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Lấy các bước của workflow theo thứ tự
     * FIXED: Removed orderBy to avoid composite index requirement
     * Now fetches all steps for workflowId and sorts in-memory
     */
    suspend fun getWorkflowSteps(workflowId: Int): Result<List<WorkflowStep>> {
        return try {
            val snapshot = workflowStepsCollection
                .whereEqualTo("workflowId", workflowId)
                .get()
                .await()
            
            val steps = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(WorkflowStep::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }.sortedBy { it.stepOrder } // Sort in-memory by stepOrder
            
            Result.success(steps)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Lấy bước hiện tại của request
     */
    suspend fun getCurrentStep(workflowId: Int, stepOrder: Int): Result<WorkflowStep?> {
        return try {
            val snapshot = workflowStepsCollection
                .whereEqualTo("workflowId", workflowId)
                .whereEqualTo("stepOrder", stepOrder)
                .limit(1)
                .get()
                .await()
            
            val step = snapshot.documents.firstOrNull()?.toObject(WorkflowStep::class.java)
            Result.success(step)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Lấy bước tiếp theo trong workflow
     */
    suspend fun getNextStep(workflowId: Int, currentStepOrder: Int): Result<WorkflowStep?> {
        return try {
            val snapshot = workflowStepsCollection
                .whereEqualTo("workflowId", workflowId)
                .whereGreaterThan("stepOrder", currentStepOrder)
                .orderBy("stepOrder", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .await()
            
            val nextStep = snapshot.documents.firstOrNull()?.toObject(WorkflowStep::class.java)
            Result.success(nextStep)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Kiểm tra user có quyền xử lý bước này không
     */
    suspend fun canUserProcessStep(userId: String, step: WorkflowStep): Boolean {
        // Kiểm tra assigned user
        return step.assignedUserId == userId
    }
    
    /**
     * Cập nhật thông tin bước quy trình
     */
    suspend fun updateWorkflowStep(
        stepId: String,
        title: String,
        description: String,
        assigneeId: String,
        assigneeName: String,
        status: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "stepName" to title,
                "description" to description,
                "assignedUserId" to assigneeId,
                "assignedUserName" to assigneeName,
                "status" to status
            )
            
            workflowStepsCollection
                .document(stepId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
