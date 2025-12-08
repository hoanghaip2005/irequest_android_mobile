package com.example.irequest.data.repository

import com.example.irequest.data.models.PaymentHistory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebasePaymentRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val paymentsCollection = firestore.collection("payments")
    
    // Lưu lịch sử thanh toán
    suspend fun savePayment(payment: PaymentHistory): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
            val userName = auth.currentUser?.displayName ?: "User"
            
            val paymentData = payment.copy(
                userId = userId,
                userName = userName,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val docRef = paymentsCollection.add(paymentData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Cập nhật trạng thái thanh toán
    suspend fun updatePaymentStatus(
        orderId: String,
        status: String,
        transId: String = "",
        payType: String = ""
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            val query = paymentsCollection
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            
            if (query.documents.isNotEmpty()) {
                val docId = query.documents[0].id
                val updates = hashMapOf<String, Any>(
                    "status" to status,
                    "updatedAt" to Timestamp.now()
                )
                
                if (transId.isNotEmpty()) {
                    updates["transId"] = transId
                }
                if (payType.isNotEmpty()) {
                    updates["payType"] = payType
                }
                
                paymentsCollection.document(docId).update(updates).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Không tìm thấy thanh toán"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Lấy lịch sử thanh toán của user
    suspend fun getMyPayments(): Result<List<PaymentHistory>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            val querySnapshot = paymentsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            // Sort locally sau khi lấy về
            val payments = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(PaymentHistory::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt?.toDate()?.time ?: 0 }
            
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Lấy thanh toán theo orderId
    suspend fun getPaymentByOrderId(orderId: String): Result<PaymentHistory?> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            val querySnapshot = paymentsCollection
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            
            val payment = querySnapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(PaymentHistory::class.java)?.copy(id = doc.id)
            }
            
            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
