package com.rut.glebporoshin.sop.bpm_compliance_service.model

import java.time.Instant


data class ComplianceRecord(
    val employeeId: String,
    val fullName: String,
    val position: String,
    val departmentId: String,
    val departmentName: String,
    val onboardingProcessId: String,
    val dismissalProcessId: String?,
    val status: ComplianceStatus,
    val tasks: List<ComplianceTask>,
    val createdAt: Instant,
    val lastUpdatedAt: Instant,
)

enum class ComplianceStatus {
    OPEN,
    OVERDUE,
    CLOSED,
}

data class ComplianceTask(
    val taskId: String,
    val name: String,
    val status: TaskStatus,
    val dueAt: Instant?,
    val assignee: String?,
    val history: List<TaskHistoryEntry>,
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    OVERDUE,
    COMPLETED,
    WAIVED,
}

enum class TaskAction {
    CREATED,
    ASSIGNED,
    COMPLETED,
    WAIVED,
    OVERDUE,
}

data class TaskHistoryEntry(
    val at: Instant,
    val action: TaskAction,
    val actor: String?,
    val comment: String?,
    val correlationId: String?,
    val traceId: String?,
)

data class ComplianceSummary(
    val totalTracked: Int,
    val open: Int,
    val overdue: Int,
    val closed: Int,
    val openByDepartment: Map<String, Int>,
    val overdueByDepartment: Map<String, Int>,
)

data class ComplianceOverdueTask(
    val employeeId: String,
    val taskId: String,
    val taskName: String,
    val dueAt: Instant,
    val detectedAt: Instant,
    val departmentName: String,
    val position: String,
)

data class ComplianceTemplate(
    val templateId: String,
    val name: String,
    val departmentId: String?,
    val departmentName: String?,
    val positionRegex: String?,
    val tasks: List<ComplianceTaskTemplate>,
)

data class ComplianceTaskTemplate(
    val taskId: String,
    val name: String,
    val slaDays: Long,
)
