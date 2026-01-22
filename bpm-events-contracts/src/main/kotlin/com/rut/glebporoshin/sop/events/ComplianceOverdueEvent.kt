package com.rut.glebporoshin.sop.events

import java.io.Serializable
import java.time.Instant

data class ComplianceOverdueEvent(
    val employeeId: String,
    val taskId: String,
    val taskName: String,
    val dueAt: Instant,
    val detectedAt: Instant,
    val departmentName: String,
    val position: String,
    val correlationId: String?,
    val traceId: String?,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
