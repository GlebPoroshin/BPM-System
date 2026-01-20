package com.rut.glebporoshin.sop.bpm_compliance_service.service

import com.rut.glebporoshin.sop.bpm_compliance_service.events.ComplianceOverdueEvent
import com.rut.glebporoshin.sop.bpm_compliance_service.tracing.MdcUtils
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class ComplianceOverdueMonitor(
    private val complianceService: ComplianceService,
    private val overduePublisher: ComplianceOverduePublisher,
) {

    private val log = LoggerFactory.getLogger(ComplianceOverdueMonitor::class.java)

    @Scheduled(fixedDelayString = "\${compliance.overdue-check-ms:60000}")
    fun checkOverdue() {
        val correlationId = MdcUtils.generateId()
        MdcUtils.withMdc(correlationId, null) {
            val detected = complianceService.detectOverdueTasks()
            if (detected.isEmpty()) {
                return@withMdc
            }

            val traceId = MdcUtils.currentTraceId()

            detected.forEach { task ->
                val event = ComplianceOverdueEvent(
                    employeeId = task.employeeId,
                    taskId = task.taskId,
                    taskName = task.taskName,
                    dueAt = task.dueAt,
                    detectedAt = task.detectedAt,
                    departmentName = task.departmentName,
                    position = task.position,
                    correlationId = correlationId,
                    traceId = traceId,
                )
                overduePublisher.publish(event)
            }

            log.info("Обнаружено просроченных задач: {}", detected.size)
        }
    }
}
