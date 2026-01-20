package com.rut.glebporoshin.sop.bpm_statistics_service.model

import java.time.Instant
import java.time.LocalDate


data class StatisticsOverview(
    val totalEmployees: Int,
    val byDepartment: Map<String, Int>,
    val byPosition: Map<String, Int>,
    val hiresByDate: List<DailyCount>,
)

data class DailyCount(
    val date: LocalDate,
    val count: Int,
)

data class ThroughputStats(
    val averageMinutesBetweenHires: Double?,
    val lastHireAt: Instant?,
    val recentHiresCount: Int,
)

data class StatisticsAnomaly(
    val alertId: String,
    val metric: String,
    val date: LocalDate,
    val currentValue: Int,
    val baseline: Double,
    val detectedAt: Instant,
    val message: String,
    val correlationId: String?,
    val traceId: String?,
)
