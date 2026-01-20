package com.rut.glebporoshin.sop.bpm_audit_service.model

import java.time.Instant


data class AuditRecord(
    val sequence: Long,
    val eventType: String,
    val employeeId: String?,
    val payload: Map<String, Any?>,
    val occurredAt: Instant,
    val receivedAt: Instant,
    val correlationId: String?,
    val traceId: String?,
    val previousHash: String?,
    val hash: String,
)

data class AuditVerificationResult(
    val valid: Boolean,
    val checked: Int,
    val invalidSequences: List<Long>,
    val lastHash: String?,
)

data class AuditStats(
    val total: Int,
    val byEventType: Map<String, Int>,
    val byEmployee: Map<String, Int>,
)
