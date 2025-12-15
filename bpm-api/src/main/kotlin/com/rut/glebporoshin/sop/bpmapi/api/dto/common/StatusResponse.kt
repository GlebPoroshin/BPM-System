package com.rut.glebporoshin.sop.bpmapi.api.dto.common

data class StatusResponse(
    val status: String,
    val error: String? = null,
)
