package com.rut.glebporoshin.sop.bpm_compliance_service.service

import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceOverdueTask
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceRecord
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceStatus
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceSummary
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTask
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTaskTemplate
import com.rut.glebporoshin.sop.bpm_compliance_service.model.TaskAction
import com.rut.glebporoshin.sop.bpm_compliance_service.model.TaskHistoryEntry
import com.rut.glebporoshin.sop.bpm_compliance_service.model.TaskStatus
import com.rut.glebporoshin.sop.bpm_compliance_service.tracing.MdcUtils
import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import com.rut.glebporoshin.sop.events.EmployeeDismissedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap


@Service
class ComplianceService(
    private val templateService: ComplianceTemplateService,
) {

    private val log = LoggerFactory.getLogger(ComplianceService::class.java)
    private val records = ConcurrentHashMap<String, ComplianceRecord>()

    fun registerOnboarding(event: EmployeeCreatedEvent, correlationId: String?, traceId: String?) {
        val template = templateService.selectTemplate(event.departmentId, event.departmentName, event.position)
        val now = Instant.now()
        val tasks = template.tasks.map { templateTask ->
            buildTaskFromTemplate(templateTask, now, correlationId, traceId, template.templateId)
        }

        val record = ComplianceRecord(
            employeeId = event.employeeId,
            fullName = "${event.firstName} ${event.lastName}",
            position = event.position,
            departmentId = event.departmentId,
            departmentName = event.departmentName,
            onboardingProcessId = event.onboardingProcessId,
            dismissalProcessId = null,
            status = calculateStatus(tasks),
            tasks = tasks,
            createdAt = now,
            lastUpdatedAt = now,
        )

        records[event.employeeId] = record
        log.info("Создан комплаенс-реестр для сотрудника {} (шаблон {})", event.employeeId, template.templateId)
    }

    fun registerDismissal(event: EmployeeDismissedEvent, correlationId: String?, traceId: String?) {
        val now = Instant.now()

        records.compute(event.employeeId) { _, existing ->
            val base = existing ?: ComplianceRecord(
                employeeId = event.employeeId,
                fullName = "",
                position = "",
                departmentId = "",
                departmentName = "",
                onboardingProcessId = "",
                dismissalProcessId = null,
                status = ComplianceStatus.OPEN,
                tasks = emptyList(),
                createdAt = now,
                lastUpdatedAt = now,
            )

            val updatedTasks = base.tasks.map { task ->
                if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.WAIVED) {
                    task
                } else {
                    val history = task.history + TaskHistoryEntry(
                        at = now,
                        action = TaskAction.WAIVED,
                        actor = "system",
                        comment = "Закрыто при увольнении",
                        correlationId = correlationId,
                        traceId = traceId,
                    )
                    task.copy(status = TaskStatus.WAIVED, history = history)
                }
            }

            val status = calculateStatus(updatedTasks)
            base.copy(
                dismissalProcessId = event.dismissalProcessId,
                status = status,
                tasks = updatedTasks,
                lastUpdatedAt = now,
            )
        }

        log.info("Закрыт комплаенс-процесс по увольнению сотрудника {}", event.employeeId)
    }

    fun assignTask(
        employeeId: String,
        taskId: String,
        assignee: String,
        actor: String?,
        comment: String?,
        correlationId: String?,
        traceId: String?,
    ): Boolean {
        var updated = false
        val now = Instant.now()

        records.computeIfPresent(employeeId) { _, record ->
            val tasks = record.tasks.map { task ->
                if (task.taskId != taskId) {
                    task
                } else if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.WAIVED) {
                    task
                } else {
                    updated = true
                    val history = task.history + TaskHistoryEntry(
                        at = now,
                        action = TaskAction.ASSIGNED,
                        actor = actor ?: assignee,
                        comment = comment ?: "Назначено исполнителю: $assignee",
                        correlationId = correlationId,
                        traceId = traceId,
                    )
                    task.copy(
                        assignee = assignee,
                        status = if (task.status == TaskStatus.PENDING) TaskStatus.IN_PROGRESS else task.status,
                        history = history,
                    )
                }
            }

            if (updated) {
                record.copy(
                    tasks = tasks,
                    status = calculateStatus(tasks),
                    lastUpdatedAt = now,
                )
            } else {
                record
            }
        }

        if (updated) {
            log.info("Назначен исполнитель {} для задачи {} сотрудника {}", assignee, taskId, employeeId)
        }
        return updated
    }

    fun completeTask(
        employeeId: String,
        taskId: String?,
        taskName: String?,
        actor: String?,
        comment: String?,
        correlationId: String?,
        traceId: String?,
    ): Boolean {
        var updated = false
        val now = Instant.now()

        records.computeIfPresent(employeeId) { _, record ->
            val tasks = record.tasks.map { task ->
                if (updated) {
                    task
                } else if (taskId != null && task.taskId == taskId) {
                    updated = true
                    markCompleted(task, now, actor, comment, correlationId, traceId)
                } else if (taskName != null && task.name.equals(taskName, ignoreCase = true)) {
                    updated = true
                    markCompleted(task, now, actor, comment, correlationId, traceId)
                } else {
                    task
                }
            }

            if (updated) {
                record.copy(
                    tasks = tasks,
                    status = calculateStatus(tasks),
                    lastUpdatedAt = now,
                )
            } else {
                record
            }
        }

        if (updated) {
            log.info("Задача комплаенса закрыта: сотрудник={}, задача={}", employeeId, taskId ?: taskName)
        }
        return updated
    }

    fun getSummary(): ComplianceSummary {
        refreshOverdueForAll()
        val allRecords = records.values
        val openRecords = allRecords.filter { it.status == ComplianceStatus.OPEN }
        val overdueRecords = allRecords.filter { it.status == ComplianceStatus.OVERDUE }
        val closedRecords = allRecords.filter { it.status == ComplianceStatus.CLOSED }

        return ComplianceSummary(
            totalTracked = allRecords.size,
            open = openRecords.size,
            overdue = overdueRecords.size,
            closed = closedRecords.size,
            openByDepartment = openRecords.groupingBy { it.departmentName.ifBlank { "unknown" } }.eachCount(),
            overdueByDepartment = overdueRecords.groupingBy { it.departmentName.ifBlank { "unknown" } }.eachCount(),
        )
    }

    fun getRecords(): Collection<ComplianceRecord> {
        refreshOverdueForAll()
        return records.values.sortedBy { it.employeeId }
    }

    fun getRecord(employeeId: String): ComplianceRecord? {
        refreshOverdue(employeeId)
        return records[employeeId]
    }

    fun getOverdueTasks(): List<ComplianceOverdueTask> {
        refreshOverdueForAll()
        val now = Instant.now()
        return records.values.flatMap { record ->
            record.tasks.filter { task -> task.status == TaskStatus.OVERDUE && task.dueAt != null }
                .map { task ->
                    ComplianceOverdueTask(
                        employeeId = record.employeeId,
                        taskId = task.taskId,
                        taskName = task.name,
                        dueAt = task.dueAt!!,
                        detectedAt = now,
                        departmentName = record.departmentName,
                        position = record.position,
                    )
                }
        }
    }

    fun detectOverdueTasks(): List<ComplianceOverdueTask> {
        val now = Instant.now()
        val correlationId = MdcUtils.currentCorrelationId()
        val traceId = MdcUtils.currentTraceId()
        val overdueTasks = mutableListOf<ComplianceOverdueTask>()

        records.forEach { (employeeId, _) ->
            records.computeIfPresent(employeeId) { _, record ->
                val update = markOverdue(record, now, correlationId, traceId)
                if (update.newlyOverdue.isNotEmpty()) {
                    overdueTasks.addAll(update.newlyOverdue)
                }
                update.record
            }
        }

        return overdueTasks
    }

    fun getTemplates() = templateService.listTemplates()

    fun createTemplate(request: com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTemplateRequest) =
        templateService.createTemplate(request)

    fun updateTemplate(templateId: String, request: com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTemplateRequest) =
        templateService.updateTemplate(templateId, request)

    fun deleteTemplate(templateId: String): Boolean = templateService.deleteTemplate(templateId)

    private fun refreshOverdue(employeeId: String) {
        val now = Instant.now()
        val correlationId = MdcUtils.currentCorrelationId()
        val traceId = MdcUtils.currentTraceId()

        records.computeIfPresent(employeeId) { _, record ->
            markOverdue(record, now, correlationId, traceId).record
        }
    }

    private fun refreshOverdueForAll() {
        val now = Instant.now()
        val correlationId = MdcUtils.currentCorrelationId()
        val traceId = MdcUtils.currentTraceId()

        records.forEach { (employeeId, _) ->
            records.computeIfPresent(employeeId) { _, record ->
                markOverdue(record, now, correlationId, traceId).record
            }
        }
    }

    private fun markCompleted(
        task: ComplianceTask,
        now: Instant,
        actor: String?,
        comment: String?,
        correlationId: String?,
        traceId: String?,
    ): ComplianceTask {
        if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.WAIVED) {
            return task
        }

        val history = task.history + TaskHistoryEntry(
            at = now,
            action = TaskAction.COMPLETED,
            actor = actor,
            comment = comment ?: "Задача выполнена",
            correlationId = correlationId,
            traceId = traceId,
        )
        return task.copy(status = TaskStatus.COMPLETED, history = history)
    }

    private data class OverdueUpdate(
        val record: ComplianceRecord,
        val newlyOverdue: List<ComplianceOverdueTask>,
    )

    private fun markOverdue(
        record: ComplianceRecord,
        now: Instant,
        correlationId: String?,
        traceId: String?,
    ): OverdueUpdate {
        val newlyOverdue = mutableListOf<ComplianceOverdueTask>()
        var changed = false

        val tasks = record.tasks.map { task ->
            if (task.dueAt == null) {
                task
            } else if (task.status == TaskStatus.PENDING || task.status == TaskStatus.IN_PROGRESS) {
                if (task.dueAt.isBefore(now)) {
                    changed = true
                    newlyOverdue.add(
                        ComplianceOverdueTask(
                            employeeId = record.employeeId,
                            taskId = task.taskId,
                            taskName = task.name,
                            dueAt = task.dueAt,
                            detectedAt = now,
                            departmentName = record.departmentName,
                            position = record.position,
                        )
                    )
                    val history = task.history + TaskHistoryEntry(
                        at = now,
                        action = TaskAction.OVERDUE,
                        actor = "system",
                        comment = "Просрочено",
                        correlationId = correlationId,
                        traceId = traceId,
                    )
                    task.copy(status = TaskStatus.OVERDUE, history = history)
                } else {
                    task
                }
            } else {
                task
            }
        }

        if (!changed) {
            return OverdueUpdate(record, emptyList())
        }

        val updatedRecord = record.copy(
            tasks = tasks,
            status = calculateStatus(tasks),
            lastUpdatedAt = now,
        )
        return OverdueUpdate(updatedRecord, newlyOverdue)
    }

    private fun calculateStatus(tasks: List<ComplianceTask>): ComplianceStatus {
        if (tasks.isEmpty()) {
            return ComplianceStatus.CLOSED
        }
        return when {
            tasks.all { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.WAIVED } ->
                ComplianceStatus.CLOSED
            tasks.any { it.status == TaskStatus.OVERDUE } ->
                ComplianceStatus.OVERDUE
            else -> ComplianceStatus.OPEN
        }
    }

    private fun buildTaskFromTemplate(
        template: ComplianceTaskTemplate,
        now: Instant,
        correlationId: String?,
        traceId: String?,
        templateId: String,
    ): ComplianceTask {
        val history = listOf(
            TaskHistoryEntry(
                at = now,
                action = TaskAction.CREATED,
                actor = "system",
                comment = "Создано по шаблону $templateId",
                correlationId = correlationId,
                traceId = traceId,
            )
        )

        return ComplianceTask(
            taskId = template.taskId,
            name = template.name,
            status = TaskStatus.PENDING,
            dueAt = now.plus(Duration.ofDays(template.slaDays)),
            assignee = null,
            history = history,
        )
    }
}
