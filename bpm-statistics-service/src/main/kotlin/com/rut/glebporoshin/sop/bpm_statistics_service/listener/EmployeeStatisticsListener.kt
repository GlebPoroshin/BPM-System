package com.rut.glebporoshin.sop.bpm_statistics_service.listener

import com.rabbitmq.client.Channel
import com.rut.glebporoshin.sop.bpm_statistics_service.service.StatisticsService
import com.rut.glebporoshin.sop.bpm_statistics_service.tracing.MdcUtils
import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap


@Component
class EmployeeStatisticsListener(
    private val statisticsService: StatisticsService
) {

    private val processedEmployeeIds = ConcurrentHashMap.newKeySet<String>()

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                name = "statistics-employee-created-queue",
                durable = "true",
                arguments = [
                    Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                    Argument(name = "x-dead-letter-routing-key", value = "dlq.statistics.employee.created")
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
        message: Message,
    ) {
        val correlationId = resolveHeaderId(message.messageProperties.correlationId)
        val traceId = resolveHeaderId(message.messageProperties.headers["X-B3-TraceId"])

        MdcUtils.withMdc(correlationId, traceId) {
            runCatching {
                if (!processedEmployeeIds.add(event.employeeId)) {
                    log.warn("Повторное событие найма для сотрудника {}", event.employeeId)
                    channel.basicAck(deliveryTag, false)
                    return@runCatching
                }

                log.info("СТАТИСТИКА: получено событие EmployeeCreatedEvent для {}", event.employeeId)

                statisticsService.recordEmployeeCreation(
                    employeeId = event.employeeId,
                    firstName = event.firstName,
                    lastName = event.lastName,
                    position = event.position,
                    departmentName = event.departmentName,
                    correlationId = MdcUtils.currentCorrelationId(),
                    traceId = MdcUtils.currentTraceId(),
                )
                channel.basicAck(deliveryTag, false)
            }.onFailure { e ->
                log.error("Ошибка обработки EmployeeCreatedEvent: {}. Отправка в DLQ", event, e)
                channel.basicNack(deliveryTag, false, false)
            }
        }
    }

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(name = "statistics-employee-created-queue.dlq", durable = "true"),
            exchange = Exchange(name = "dlx-exchange", type = "direct", durable = "true"),
            key = ["dlq.statistics.employee.created"]
        )]
    )
    fun handleDlqMessages(@Payload failedMessage: Any) {
        log.error("Сообщение в DLQ статистики: {}", failedMessage)
    }

    private fun resolveHeaderId(header: Any?): String? {
        return when (header) {
            null -> null
            is String -> header.trim().takeIf { it.isNotEmpty() }
            is ByteArray -> String(header, StandardCharsets.UTF_8).trim().takeIf { it.isNotEmpty() }
            else -> header.toString().trim().takeIf { it.isNotEmpty() }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(EmployeeStatisticsListener::class.java)
    }
}
