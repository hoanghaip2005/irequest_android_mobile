package com.project.irequest

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Process(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var status: String,
    var date: String,
    // Dòng dưới đây là dòng quan trọng để sửa lỗi "Unresolved reference 'creationDate'"
    var creationDate: String = "" 
) : Parcelable