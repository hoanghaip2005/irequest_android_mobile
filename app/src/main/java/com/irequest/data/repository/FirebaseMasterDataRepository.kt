package com.example.irequest.data.repository

import com.example.irequest.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Firebase Repository for Master Data (Department, Status, Priority, Workflow)
 */
class FirebaseMasterDataRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    /**
     * Get all departments
     */
    suspend fun getDepartments(): Result<List<Department>> {
        return try {
            val snapshot = firestore.collection("departments")
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
            
            val departments = snapshot.toObjects(Department::class.java)
            Result.success(departments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all statuses
     */
    suspend fun getStatuses(): Result<List<Status>> {
        return try {
            val snapshot = firestore.collection("statuses")
                .orderBy("statusName")
                .get()
                .await()
            
            val statuses = snapshot.toObjects(Status::class.java)
            Result.success(statuses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all priorities
     */
    suspend fun getPriorities(): Result<List<Priority>> {
        return try {
            val snapshot = firestore.collection("priorities")
                .orderBy("sortOrder")
                .get()
                .await()
            
            val priorities = snapshot.toObjects(Priority::class.java)
            Result.success(priorities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all workflows
     */
    suspend fun getWorkflows(): Result<List<Workflow>> {
        return try {
            val snapshot = firestore.collection("workflows")
                .whereEqualTo("isActive", true)
                .orderBy("workflowName")
                .get()
                .await()
            
            val workflows = snapshot.toObjects(Workflow::class.java)
            Result.success(workflows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get workflow steps
     */
    suspend fun getWorkflowSteps(workflowId: Int): Result<List<WorkflowStep>> {
        return try {
            val snapshot = firestore.collection("workflow_steps")
                .whereEqualTo("workflowId", workflowId)
                .orderBy("stepOrder")
                .get()
                .await()
            
            val steps = snapshot.toObjects(WorkflowStep::class.java)
            Result.success(steps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
