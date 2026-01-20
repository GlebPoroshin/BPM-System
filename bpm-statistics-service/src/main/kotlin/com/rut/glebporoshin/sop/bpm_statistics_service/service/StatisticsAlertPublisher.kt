package com.rut.glebporoshin.sop.bpm_statistics_service.service

import com.rut.glebporoshin.sop.bpm_statistics_service.config.RabbitMQConfig
import com.rut.glebporoshin.sop.bpm_statistics_service.events.StatisticsAlertEvent
import com.rut.glebporoshin.sop.bpm_statistics_service.tracing.MdcUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service


@Service
class StatisticsAlertPublisher(
    private val rabbitTemplate: RabbitTemplate,
) {

    private val log = LoggerFactory.getLogger(StatisticsAlertPublisher::class.java)

    fun publish(event: StatisticsAlertEvent) {
        val correlationId = event.correlationId ?: MdcUtils.currentCorrelationId()
        val traceId = event.traceId ?: MdcUtils.currentTraceId()

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.STATISTICS_ALERTS_EXCHANGE,
            RabbitMQConfig.ROUTING_KEY_STATISTICS_ALERTS,
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

        log.info("Отправлено статистическое предупреждение {}", event.alertId)
    }
}
