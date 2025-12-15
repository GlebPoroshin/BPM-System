package com.rut.glebporoshin.sop.bpm_main_service.dto

data class PagedResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean,
)

