package com.rut.glebporoshin.sop.bpm_statistics_service.service

import com.rut.glebporoshin.sop.bpm_statistics_service.events.StatisticsAlertEvent
import com.rut.glebporoshin.sop.bpm_statistics_service.model.DailyCount
import com.rut.glebporoshin.sop.bpm_statistics_service.model.StatisticsAnomaly
import com.rut.glebporoshin.sop.bpm_statistics_service.model.StatisticsOverview
import com.rut.glebporoshin.sop.bpm_statistics_service.model.ThroughputStats
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.ArrayDeque
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


@Service
class StatisticsService(
    private val alertPublisher: StatisticsAlertPublisher,
) {

    private val log = LoggerFactory.getLogger(StatisticsService::class.java)
    private val employees = ConcurrentHashMap<String, EmployeeStatistic>()
    private val hiresByDate = ConcurrentHashMap<LocalDate, Int>()
    private val hiresByDepartment = ConcurrentHashMap<String, Int>()
    private val hiresByPosition = ConcurrentHashMap<String, Int>()
    private val recentHires = ArrayDeque<Instant>()
    private val anomalies = CopyOnWriteArrayList<StatisticsAnomaly>()
    private val anomalyKeys = ConcurrentHashMap.newKeySet<String>()

    data class EmployeeStatistic(
        val employeeId: String,
        val fullName: String,
        val position: String,
        val departmentName: String,
        val createdAt: Instant,
    )

    fun recordEmployeeCreation(
        employeeId: String,
        firstName: String,
        lastName: String,
        position: String,
        departmentName: String,
        correlationId: String?,
        traceId: String?,
    ) {
        val now = Instant.now()
        val fullName = "$firstName $lastName"
        employees[employeeId] = EmployeeStatistic(employeeId, fullName, position, departmentName, now)

        val date = LocalDate.ofInstant(now, ZoneOffset.UTC)
        hiresByDate.merge(date, 1) { existing, one -> existing + one }
        hiresByDepartment.merge(departmentName, 1) { existing, one -> existing + one }
        hiresByPosition.merge(position, 1) { existing, one -> existing + one }

        updateRecentHires(now)

        log.info("СТАТИСТИКА: учтён сотрудник {} ({}). Всего в системе: {}", employeeId, fullName, employees.size)
        log.info("СТАТИСТИКА: распределение по отделам: {}", hiresByDepartment)

        detectAnomaly(date, correlationId, traceId)
    }

    fun getOverview(): StatisticsOverview {
        val dateCounts = hiresByDate.entries
            .sortedBy { it.key }
            .map { DailyCount(it.key, it.value) }

        return StatisticsOverview(
            totalEmployees = employees.size,
            byDepartment = hiresByDepartment.toMap(),
            byPosition = hiresByPosition.toMap(),
            hiresByDate = dateCounts,
        )
    }

    fun getDailyCounts(from: LocalDate?, to: LocalDate?): List<DailyCount> {
        return hiresByDate.entries
            .asSequence()
            .filter { from == null || !it.key.isBefore(from) }
            .filter { to == null || !it.key.isAfter(to) }
            .sortedBy { it.key }
            .map { DailyCount(it.key, it.value) }
            .toList()
    }

    fun getThroughput(): ThroughputStats {
        val timestamps = synchronized(recentHires) { recentHires.toList() }
        val averageMinutes = calculateAverageMinutesBetween(timestamps)
        val lastHire = timestamps.maxOrNull()

        return ThroughputStats(
            averageMinutesBetweenHires = averageMinutes,
            lastHireAt = lastHire,
            recentHiresCount = timestamps.size,
        )
    }

    fun getAnomalies(): List<StatisticsAnomaly> = anomalies.toList()

    fun getEmployees(): Collection<EmployeeStatistic> = employees.values

    fun getByDepartment(): Map<String, Int> = hiresByDepartment.toMap()

    fun getByPosition(): Map<String, Int> = hiresByPosition.toMap()

    private fun updateRecentHires(timestamp: Instant) {
        synchronized(recentHires) {
            recentHires.addLast(timestamp)
            while (recentHires.size > MAX_RECENT_HIRES) {
                recentHires.removeFirst()
            }
        }
    }

    private fun calculateAverageMinutesBetween(timestamps: List<Instant>): Double? {
        if (timestamps.size < 2) return null
        val sorted = timestamps.sorted()
        val intervals = sorted.zipWithNext { a, b ->
            java.time.Duration.between(a, b).toMinutes().toDouble()
        }
        return if (intervals.isEmpty()) null else intervals.average()
    }

    private fun detectAnomaly(date: LocalDate, correlationId: String?, traceId: String?) {
        val todayCount = hiresByDate[date] ?: return
        val previousDays = hiresByDate.entries
            .filter { it.key.isBefore(date) }
            .sortedByDescending { it.key }
            .take(ANOMALY_LOOKBACK_DAYS)
            .map { it.value }

        if (previousDays.size < MIN_LOOKBACK_DAYS) {
            return
        }

        val baseline = previousDays.average()
        if (baseline <= 0.0) {
            return
        }

        if (todayCount >= MIN_ANOMALY_COUNT && todayCount > baseline * ANOMALY_THRESHOLD_MULTIPLIER) {
            val key = "HIRES_SPIKE:$date"
            if (!anomalyKeys.add(key)) {
                return
            }

            val alertId = "ALERT-${UUID.randomUUID()}"
            val message = "Резкий рост найма: $todayCount за день при среднем $baseline"
            val anomaly = StatisticsAnomaly(
                alertId = alertId,
                metric = "HIRING_RATE",
                date = date,
                currentValue = todayCount,
                baseline = baseline,
                detectedAt = Instant.now(),
                message = message,
                correlationId = correlationId,
                traceId = traceId,
            )
            anomalies.add(anomaly)

            alertPublisher.publish(
                StatisticsAlertEvent(
                    alertId = alertId,
                    metric = anomaly.metric,
                    date = anomaly.date,
                    currentValue = anomaly.currentValue,
                    baseline = anomaly.baseline,
                    detectedAt = anomaly.detectedAt,
                    message = anomaly.message,
                    correlationId = correlationId,
                    traceId = traceId,
                )
            )

            log.warn("СТАТИСТИКА: обнаружена аномалия {}", anomaly.message)
        }
    }

    companion object {
        private const val MAX_RECENT_HIRES = 100
        private const val ANOMALY_LOOKBACK_DAYS = 7
        private const val MIN_LOOKBACK_DAYS = 3
        private const val MIN_ANOMALY_COUNT = 3
        private const val ANOMALY_THRESHOLD_MULTIPLIER = 2.0
    }
}
