package com.rut.glebporoshin.sop.bpm_audit_service.listener

import com.rabbitmq.client.Channel
import com.rut.glebporoshin.sop.bpm_audit_service.service.AuditService
import com.rut.glebporoshin.sop.bpm_audit_service.tracing.MdcUtils
import com.rut.glebporoshin.sop.events.ComplianceOverdueEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ComplianceOverdueEventListener(
    private val auditService: AuditService,
) {

    private val log = LoggerFactory.getLogger(ComplianceOverdueEventListener::class.java)
    private val processedOverdueKeys = ConcurrentHashMap.newKeySet<String>()

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "audit-compliance-overdue-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.audit.compliance.overdue")
                ]
            ),
            exchange = Exchange(name = "bpm-compliance-overdue", type = "fanout", durable = "true"),
            key = [""]
        )]
    )
    fun handleComplianceOverdue(
        @Payload event: ComplianceOverdueEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        runCatching {
            val key = "${event.employeeId}:${event.taskId}:${event.dueAt}"
            if (!processedOverdueKeys.add(key)) {
                log.warn("Повторное событие просрочки комплаенса: {}", key)
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }

            auditService.appendEvent(
                eventType = ComplianceOverdueEvent::class.simpleName ?: "ComplianceOverdueEvent",
                employeeId = event.employeeId,
                payload = event,
                correlationId = event.correlationId ?: MdcUtils.currentCorrelationId(),
                traceId = event.traceId ?: MdcUtils.currentTraceId(),
            )

            log.info(
                "АУДИТ: просрочка комплаенса сотрудник={}, задача={}, дедлайн={}",
                event.employeeId,
                event.taskName,
                event.dueAt
            )
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Ошибка обработки ComplianceOverdueEvent: {}. Отправка в DLQ", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(name = "audit-compliance-overdue-queue.dlq", durable = "true"),
            exchange = Exchange(name = "dlx-exchange", type = "direct", durable = "true"),
            key = ["dlq.audit.compliance.overdue"]
        )]
    )
    fun handleDlqMessages(@Payload failedMessage: Any) {
        log.error("Сообщение в DLQ аудита (compliance overdue): {}", failedMessage)
    }
}
