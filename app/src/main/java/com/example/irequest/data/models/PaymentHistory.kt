package com.example.irequest.data.models

import com.google.firebase.Timestamp

data class PaymentHistory(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val orderId: String = "",
    val paymentName: String = "",
    val amount: Long = 0,
    val description: String = "",
    val status: String = "pending", // pending, success, failed
    val transId: String = "",
    val payType: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    // Constructor không tham số cho Firestore
    constructor() : this("", "", "", "", "", 0, "", "pending", "", "", null, null)
}
