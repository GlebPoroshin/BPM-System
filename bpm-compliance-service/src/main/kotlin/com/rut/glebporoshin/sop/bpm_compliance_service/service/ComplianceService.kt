package com.rut.glebporoshin.sop.bpm_compliance_service.service

import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import com.rut.glebporoshin.sop.events.EmployeeDismissedEvent
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class ComplianceService {

    private val records = ConcurrentHashMap<String, ComplianceRecord>()

    fun registerOnboarding(event: EmployeeCreatedEvent) {
        val defaultTasks = defaultTasks(event.position)
        val record = ComplianceRecord(
            employeeId = event.employeeId,
            fullName = "${event.firstName} ${event.lastName}",
            position = event.position,
            departmentName = event.departmentName,
            onboardingProcessId = event.onboardingProcessId,
            dismissalProcessId = null,
            status = ComplianceStatus.OPEN,
            pendingTasks = defaultTasks,
            completedTasks = emptyList(),
            lastUpdatedAt = Instant.now()
        )
        records[event.employeeId] = record
    }

    fun registerDismissal(event: EmployeeDismissedEvent) {
        records.compute(event.employeeId) { _, existing ->
            val base = existing ?: ComplianceRecord(
                employeeId = event.employeeId,
                fullName = "",
                position = "",
                departmentName = "",
                onboardingProcessId = "",
                dismissalProcessId = null,
                status = ComplianceStatus.OPEN,
                pendingTasks = emptyList(),
                completedTasks = emptyList(),
                lastUpdatedAt = Instant.now()
            )
            base.copy(
                dismissalProcessId = event.dismissalProcessId,
                status = ComplianceStatus.CLOSED,
                completedTasks = base.completedTasks + base.pendingTasks,
                pendingTasks = emptyList(),
                lastUpdatedAt = Instant.now()
            )
        }
    }

    fun markTaskCompleted(employeeId: String, task: String) {
        records.computeIfPresent(employeeId) { _, record ->
            val remaining = record.pendingTasks.filterNot { it.equals(task, ignoreCase = true) }
            record.copy(
                pendingTasks = remaining,
                completedTasks = record.completedTasks + task,
                lastUpdatedAt = Instant.now(),
                status = if (remaining.isEmpty()) ComplianceStatus.CLOSED else ComplianceStatus.OPEN
            )
        }
    }

    fun getSummary(): ComplianceSummary {
        val allRecords = records.values
        val openCount = allRecords.count { it.status == ComplianceStatus.OPEN }
        val closedCount = allRecords.size - openCount
        val openByDepartment = allRecords
            .filter { it.status == ComplianceStatus.OPEN }
            .groupingBy { it.departmentName.ifBlank { "unknown" } }
            .eachCount()
        return ComplianceSummary(
            totalTracked = allRecords.size,
            open = openCount,
            closed = closedCount,
            openByDepartment = openByDepartment
        )
    }

    fun getRecords(): Collection<ComplianceRecord> = records.values.sortedBy { it.employeeId }

    private fun defaultTasks(position: String): List<String> {
        val shared = listOf(
            "Verify identity documents",
            "Issue access badge",
            "Confirm security training enrollment"
        )
        return when {
            position.contains("Manager", ignoreCase = true) -> shared + "Confirm leadership training enrollment"
            position.contains("Developer", ignoreCase = true) -> shared + "Provision repository access"
            else -> shared
        }
    }
}

data class ComplianceRecord(
    val employeeId: String,
    val fullName: String,
    val position: String,
    val departmentName: String,
    val onboardingProcessId: String,
    val dismissalProcessId: String?,
    val status: ComplianceStatus,
    val pendingTasks: List<String>,
    val completedTasks: List<String>,
    val lastUpdatedAt: Instant
)

enum class ComplianceStatus {
    OPEN,
    CLOSED
}

data class ComplianceSummary(
    val totalTracked: Int,
    val open: Int,
    val closed: Int,
    val openByDepartment: Map<String, Int>
)
