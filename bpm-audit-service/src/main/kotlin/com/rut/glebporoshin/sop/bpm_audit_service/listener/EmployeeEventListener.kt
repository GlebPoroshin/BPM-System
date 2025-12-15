package com.rut.glebporoshin.sop.bpm_audit_service.listener

import com.rabbitmq.client.Channel
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
class EmployeeEventListener {

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
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        runCatching {
            when {
                !processedEmployeeCreations.add(event.employeeId) -> {
                    log.warn("Duplicate EmployeeCreatedEvent received for employeeId: {}", event.employeeId)
                    channel.basicAck(deliveryTag, false)
                    return@runCatching
                }
                event.firstName.equals("CRASH", ignoreCase = true) ->
                    error("Simulated processing error for DLQ test")
            }

        log.info(
            "AUDIT: New employee created - ID: {}, Name: {} {}, Position: {}, Department: {}, Team: {}, Onboarding Process: {}",
                event.employeeId, event.firstName, event.lastName, event.position,
                event.departmentName, event.teamName, event.onboardingProcessId
        )
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Failed to process EmployeeCreatedEvent: {}. Sending to DLQ", event, e)
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
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        runCatching {
            if (!processedEmployeeDismissals.add("${event.employeeId}-${event.dismissalProcessId}")) {
                log.warn("Duplicate EmployeeDismissedEvent received for employeeId: {}", event.employeeId)
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }

        log.info(
            "AUDIT: Employee dismissed - ID: {}, Dismissal Process: {}, Last Working Day: {}",
                event.employeeId, event.dismissalProcessId, event.lastWorkingDay
        )
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Failed to process EmployeeDismissedEvent: {}. Sending to DLQ", event, e)
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
        log.error("Message received in DLQ: {}", failedMessage)
    }

    companion object {
        private val log = LoggerFactory.getLogger(EmployeeEventListener::class.java)
    }
}
