package com.rut.glebporoshin.sop.notification_service.listener

import com.rabbitmq.client.Channel
import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import com.rut.glebporoshin.sop.events.EmployeeDismissedEvent
import com.rut.glebporoshin.sop.notification_service.handler.NotificationWebSocketHandler
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

@Component
class NotificationEventListener(
    private val webSocketHandler: NotificationWebSocketHandler
) {

    private val log = LoggerFactory.getLogger(NotificationEventListener::class.java)

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "notification-employee-created-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.notification.employee.created")
                ]
            ),
            exchange = Exchange(name = "bpm-employee-created", type = "fanout", durable = "true"),
            key = [""]
        )]
    )
    fun handleEmployeeCreated(
        @Payload event: EmployeeCreatedEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        runCatching {
            val message = "Новый сотрудник: ${event.firstName} ${event.lastName} (${event.position})"
            webSocketHandler.broadcast(message)
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Failed to process EmployeeCreatedEvent: {}", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "notification-employee-dismissed-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.notification.employee.dismissed")
                ]
            ),
            exchange = Exchange(name = "bpm-employee-dismissed", type = "fanout", durable = "true"),
            key = [""]
        )]
    )
    fun handleEmployeeDismissed(
        @Payload event: EmployeeDismissedEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        runCatching {
            val message = "Сотрудник ${event.employeeId} начал увольнение: процесс ${event.dismissalProcessId}"
            webSocketHandler.broadcast(message)
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Failed to process EmployeeDismissedEvent: {}", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(name = "notification-queue.dlq", durable = "true"),
            exchange = Exchange(name = "dlx-exchange", type = "direct", durable = "true"),
            key = ["dlq.notification.#"]
        )]
    )
    fun handleDlq(@Payload failedMessage: Any) {
        log.error("Notification DLQ message: {}", failedMessage)
    }
}
