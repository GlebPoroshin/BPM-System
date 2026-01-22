package com.rut.glebporoshin.sop.notification_service.listener

import com.rabbitmq.client.Channel
import com.rut.glebporoshin.sop.events.ComplianceOverdueEvent
import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import com.rut.glebporoshin.sop.events.EmployeeDismissedEvent
import com.rut.glebporoshin.sop.events.StatisticsAlertEvent
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
import java.util.concurrent.ConcurrentHashMap

@Component
class NotificationEventListener(
    private val webSocketHandler: NotificationWebSocketHandler
) {

    private val log = LoggerFactory.getLogger(NotificationEventListener::class.java)
    private val processedOverdueKeys = ConcurrentHashMap.newKeySet<String>()
    private val processedAlertIds = ConcurrentHashMap.newKeySet<String>()

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

            val personalMessage = "Ваш процесс онбординга запущен: ${event.onboardingProcessId}"
            val personalDelivered = webSocketHandler.sendToUser(event.employeeId, personalMessage)
            log.info(
                "Unicast onboarding notification to employee {} delivered={}",
                event.employeeId,
                personalDelivered
            )

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

            val personalMessage = "Ваш процесс увольнения запущен: ${event.dismissalProcessId}"
            val personalDelivered = webSocketHandler.sendToUser(event.employeeId, personalMessage)
            log.info(
                "Unicast dismissal notification to employee {} delivered={}",
                event.employeeId,
                personalDelivered
            )

            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Failed to process EmployeeDismissedEvent: {}", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "notification-compliance-overdue-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.notification.compliance.overdue")
                ]
            ),
            exchange = Exchange(name = "bpm-compliance-overdue", type = "fanout", durable = "true"),
            key = [""]
        )]
    )
    fun handleComplianceOverdue(
        @Payload event: ComplianceOverdueEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        runCatching {
            val key = "${event.employeeId}:${event.taskId}:${event.dueAt}"
            if (!processedOverdueKeys.add(key)) {
                log.warn("Повторное событие просрочки комплаенса: {}", key)
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }

            val message = "Просрочена комплаенс-задача: сотрудник=${event.employeeId}, " +
                "задача=${event.taskName} (${event.taskId}), срок=${event.dueAt}, " +
                "отдел=${event.departmentName}, должность=${event.position}"
            webSocketHandler.broadcast(message)

            val personalMessage = "Просрочена ваша комплаенс-задача: ${event.taskName} (срок ${event.dueAt})"
            val personalDelivered = webSocketHandler.sendToUser(event.employeeId, personalMessage)
            log.info("Уведомление о просрочке комплаенса для сотрудника {} доставлено={}", event.employeeId, personalDelivered)

            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Ошибка обработки ComplianceOverdueEvent: {}", event, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "notification-statistics-alert-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.notification.statistics.alert")
                ]
            ),
            exchange = Exchange(name = "bpm-statistics-alerts", type = "fanout", durable = "true"),
            key = [""]
        )]
    )
    fun handleStatisticsAlert(
        @Payload event: StatisticsAlertEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        runCatching {
            if (!processedAlertIds.add(event.alertId)) {
                log.warn("Повторное статистическое предупреждение: {}", event.alertId)
                channel.basicAck(deliveryTag, false)
                return@runCatching
            }

            val message = "СТАТИСТИКА: ${event.message}"
            webSocketHandler.broadcast(message)
            channel.basicAck(deliveryTag, false)
        }.onFailure { e ->
            log.error("Ошибка обработки StatisticsAlertEvent: {}", event, e)
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
