package com.project.irequest

data class FilterOptions(
    val branch: String? = null,
    val shift: String? = null,
    val employeeTypes: List<String> = emptyList(),
    val joinOrder: String? = null, // "newest" or "oldest"
    val contractStatus: List<String> = emptyList(),
    val kpiSort: String? = null // "low_to_high" or "high_to_low"
)
