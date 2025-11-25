package com.rut.glebporoshin.sop.bpm_compliance_service.listener

import com.rabbitmq.client.Channel
import com.rut.glebporoshin.sop.bpm_compliance_service.service.ComplianceService
import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import com.rut.glebporoshin.sop.events.EmployeeDismissedEvent
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
class ComplianceEventListener(
    private val complianceService: ComplianceService
) {

    private val processedEvents = ConcurrentHashMap.newKeySet<String>()

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "compliance-employee-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.compliance.employee")
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
            if (!processedEvents.add("created-${event.employeeId}")) {
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }
            complianceService.registerOnboarding(event)
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Failed to process EmployeeCreatedEvent: {}. Sending to DLQ", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "compliance-employee-dismissed-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.compliance.employee.dismissed")
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
            if (!processedEvents.add("dismissed-${event.employeeId}-${event.dismissalProcessId}")) {
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }
            complianceService.registerDismissal(event)
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Failed to process EmployeeDismissedEvent: {}. Sending to DLQ", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(name = "compliance-queue.dlq", durable = "true"),
            exchange = Exchange(name = "dlx-exchange", type = "direct", durable = "true"),
            key = ["dlq.compliance.employee", "dlq.compliance.employee.dismissed"]
        )]
    )
    fun handleDlqMessages(@Payload failedMessage: Any) {
        log.error("Message received in DLQ: {}", failedMessage)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ComplianceEventListener::class.java)
    }
}
