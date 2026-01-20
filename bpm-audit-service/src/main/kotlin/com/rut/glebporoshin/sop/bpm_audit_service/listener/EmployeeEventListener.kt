package com.rut.glebporoshin.sop.bpm_audit_service.listener

import com.rabbitmq.client.Channel
import com.rut.glebporoshin.sop.bpm_audit_service.service.AuditService
import com.rut.glebporoshin.sop.bpm_audit_service.tracing.MdcUtils
import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import com.rut.glebporoshin.sop.events.EmployeeDismissedEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap


@Component
class EmployeeEventListener(
    private val auditService: AuditService,
) {

    private val processedEmployeeCreations = ConcurrentHashMap.newKeySet<String>()
    private val processedEmployeeDismissals = ConcurrentHashMap.newKeySet<String>()

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "audit-employee-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.audit.employee")
                ]
            ),
            exchange = Exchange(name = "bpm-employee-created", type = "fanout", durable = "true"),
            key = [""]
        )]
    )
    fun handleEmployeeCreatedEvent(
        @Payload event: EmployeeCreatedEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        runCatching {
            if (!processedEmployeeCreations.add(event.employeeId)) {
                log.warn("Повторное событие найма для сотрудника {}", event.employeeId)
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }
            if (event.firstName.equals("CRASH", ignoreCase = true)) {
                error("Смоделированная ошибка для проверки DLQ")
            }

            auditService.appendEvent(
                eventType = EmployeeCreatedEvent::class.simpleName ?: "EmployeeCreatedEvent",
                employeeId = event.employeeId,
                payload = event,
                correlationId = MdcUtils.currentCorrelationId(),
                traceId = MdcUtils.currentTraceId(),
            )

            log.info(
                "АУДИТ: создан сотрудник {} {} (ID: {}), должность: {}, отдел: {}, команда: {}, онбординг: {}",
                event.firstName,
                event.lastName,
                event.employeeId,
                event.position,
                event.departmentName,
                event.teamName,
                event.onboardingProcessId
            )
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Ошибка обработки EmployeeCreatedEvent: {}. Отправка в DLQ", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "audit-employee-dismissed-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.audit.employee.dismissed")
                ]
            ),
            exchange = Exchange(name = "bpm-employee-dismissed", type = "fanout", durable = "true"),
            key = [""]
        )]
    )
    fun handleEmployeeDismissedEvent(
        @Payload event: EmployeeDismissedEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        runCatching {
            val key = "${event.employeeId}-${event.dismissalProcessId}"
            if (!processedEmployeeDismissals.add(key)) {
                log.warn("Повторное событие увольнения для сотрудника {}", event.employeeId)
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }

            auditService.appendEvent(
                eventType = EmployeeDismissedEvent::class.simpleName ?: "EmployeeDismissedEvent",
                employeeId = event.employeeId,
                payload = event,
                correlationId = MdcUtils.currentCorrelationId(),
                traceId = MdcUtils.currentTraceId(),
            )

            log.info(
                "АУДИТ: увольнение сотрудника {}, процесс: {}, последний день: {}",
                event.employeeId,
                event.dismissalProcessId,
                event.lastWorkingDay
            )
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Ошибка обработки EmployeeDismissedEvent: {}. Отправка в DLQ", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(name = "audit-employee-queue.dlq", durable = "true"),
            exchange = Exchange(name = "dlx-exchange", type = "direct", durable = "true"),
            key = ["dlq.audit.employee", "dlq.audit.employee.dismissed"]
        )]
    )
    fun handleDlqMessages(@Payload failedMessage: Any) {
        log.error("Сообщение в DLQ аудита: {}", failedMessage)
    }

    companion object {
        private val log = LoggerFactory.getLogger(EmployeeEventListener::class.java)
    }
}
