package com.rut.glebporoshin.sop.bpm_statistics_service.events

import java.io.Serializable
import java.time.Instant
import java.time.LocalDate


data class StatisticsAlertEvent(
    val alertId: String,
    val metric: String,
    val date: LocalDate,
    val currentValue: Int,
    val baseline: Double,
    val detectedAt: Instant,
    val message: String,
    val correlationId: String?,
    val traceId: String?,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
