package com.project.irequest

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Process(
    val name: String,
    val status: String,
    val creationDate: String
) : Parcelable