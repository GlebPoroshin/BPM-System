package com.rut.glebporoshin.sop.bpm_compliance_service.service

import com.rut.glebporoshin.sop.bpm_compliance_service.config.RabbitMQConfig
import com.rut.glebporoshin.sop.bpm_compliance_service.events.ComplianceOverdueEvent
import com.rut.glebporoshin.sop.bpm_compliance_service.tracing.MdcUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service


@Service
class ComplianceOverduePublisher(
    private val rabbitTemplate: RabbitTemplate,
) {

    private val log = LoggerFactory.getLogger(ComplianceOverduePublisher::class.java)

    fun publish(event: ComplianceOverdueEvent) {
        val correlationId = event.correlationId ?: MdcUtils.currentCorrelationId()
        val traceId = event.traceId ?: MdcUtils.currentTraceId()

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.COMPLIANCE_OVERDUE_EXCHANGE,
            RabbitMQConfig.ROUTING_KEY_COMPLIANCE_OVERDUE,
            event
        ) { message ->
            val properties = message.messageProperties
            if (!correlationId.isNullOrBlank()) {
                properties.correlationId = correlationId
            }
            if (!traceId.isNullOrBlank()) {
                properties.headers["X-B3-TraceId"] = traceId
            }
            message
        }

        log.info("Отправлено событие просрочки комплаенса: сотрудник={}, задача={}", event.employeeId, event.taskId)
    }
}
