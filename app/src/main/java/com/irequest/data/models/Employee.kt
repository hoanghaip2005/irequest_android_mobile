package com.example.irequest.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Employee(
    // Existing fields
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val department: String = "",
    val email: String = "",
    val phone: String = "",
    val avatar: String = "",
    val joinDate: String = "",
    val status: String = "Đang làm việc",
    val address: String = "",
    val dateOfBirth: String = "",

    // Personal Information
    val gender: String = "",
    val cccd: String = "",
    val bankAccount: String = "",

    // Work Information
    val contractStatus: String = "",
    val employeeType: String = "",
    val manager: String = "",
    val branch: String = "",
    val paymentMethod: String = "",
    val notes: String = "",

    // Recent Activity
    val checkIn: String = "",
    val checkOut: String = "",
    val shiftRegister: String = "",

    // Schedule Information
    val workSchedule: String = "",
    val totalWorkHours: String = "",
    
    // Assets - Placeholder
    val assets: String = ""
) : Parcelable
