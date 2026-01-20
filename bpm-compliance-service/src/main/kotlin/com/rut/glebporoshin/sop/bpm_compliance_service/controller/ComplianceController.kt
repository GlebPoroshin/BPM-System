package com.rut.glebporoshin.sop.bpm_compliance_service.controller

import com.rut.glebporoshin.sop.bpm_compliance_service.model.AssignTaskRequest
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceOverdueTask
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceRecord
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceSummary
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTemplate
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTemplateRequest
import com.rut.glebporoshin.sop.bpm_compliance_service.model.CompleteTaskRequest
import com.rut.glebporoshin.sop.bpm_compliance_service.service.ComplianceService
import com.rut.glebporoshin.sop.bpm_compliance_service.tracing.MdcUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus


@RestController
@RequestMapping("/compliance")
class ComplianceController(
    private val complianceService: ComplianceService
) {

    @GetMapping("/summary")
    fun getSummary(): ComplianceSummary = complianceService.getSummary()

    @GetMapping("/records")
    fun getRecords(): Collection<ComplianceRecord> = complianceService.getRecords()

    @GetMapping("/records/{employeeId}")
    fun getRecord(@PathVariable employeeId: String): ComplianceRecord {
        return complianceService.getRecord(employeeId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена: $employeeId")
    }

    @PostMapping("/{employeeId}/tasks/complete")
    fun completeTask(
        @PathVariable employeeId: String,
        @RequestParam(required = false) taskId: String?,
        @RequestParam(required = false) task: String?,
        @RequestParam(required = false) actor: String?,
        @RequestParam(required = false) comment: String?,
    ): ResponseEntity<Void> {
        if (taskId.isNullOrBlank() && task.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Не указан taskId или task")
        }

        val updated = complianceService.completeTask(
            employeeId = employeeId,
            taskId = taskId,
            taskName = task,
            actor = actor,
            comment = comment,
            correlationId = MdcUtils.currentCorrelationId(),
            traceId = MdcUtils.currentTraceId(),
        )

        if (!updated) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена")
        }

        return ResponseEntity.accepted().build()
    }

    @PostMapping("/{employeeId}/tasks/{taskId}/complete")
    fun completeTaskById(
        @PathVariable employeeId: String,
        @PathVariable taskId: String,
        @RequestBody(required = false) request: CompleteTaskRequest?
    ): ResponseEntity<Void> {
        val updated = complianceService.completeTask(
            employeeId = employeeId,
            taskId = taskId,
            taskName = null,
            actor = request?.actor,
            comment = request?.comment,
            correlationId = MdcUtils.currentCorrelationId(),
            traceId = MdcUtils.currentTraceId(),
        )

        if (!updated) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена")
        }

        return ResponseEntity.accepted().build()
    }

    @PostMapping("/{employeeId}/tasks/{taskId}/assign")
    fun assignTask(
        @PathVariable employeeId: String,
        @PathVariable taskId: String,
        @RequestBody request: AssignTaskRequest
    ): ResponseEntity<Void> {
        val updated = complianceService.assignTask(
            employeeId = employeeId,
            taskId = taskId,
            assignee = request.assignee,
            actor = request.actor,
            comment = request.comment,
            correlationId = MdcUtils.currentCorrelationId(),
            traceId = MdcUtils.currentTraceId(),
        )

        if (!updated) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена")
        }

        return ResponseEntity.accepted().build()
    }

    @GetMapping("/overdue")
    fun getOverdue(): List<ComplianceOverdueTask> = complianceService.getOverdueTasks()

    @PostMapping("/overdue/check")
    fun triggerOverdueCheck(): List<ComplianceOverdueTask> = complianceService.detectOverdueTasks()

    @GetMapping("/templates")
    fun getTemplates(): List<ComplianceTemplate> = complianceService.getTemplates()

    @PostMapping("/templates")
    fun createTemplate(@RequestBody request: ComplianceTemplateRequest): ComplianceTemplate =
        complianceService.createTemplate(request)

    @PutMapping("/templates/{templateId}")
    fun updateTemplate(
        @PathVariable templateId: String,
        @RequestBody request: ComplianceTemplateRequest
    ): ComplianceTemplate {
        return complianceService.updateTemplate(templateId, request)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Шаблон не найден")
    }

    @DeleteMapping("/templates/{templateId}")
    fun deleteTemplate(@PathVariable templateId: String): ResponseEntity<Void> {
        if (!complianceService.deleteTemplate(templateId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Шаблон не найден")
        }
        return ResponseEntity.noContent().build()
    }
}
