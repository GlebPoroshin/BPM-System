package com.rut.glebporoshin.sop.bpm_statistics_service.service

import com.rut.glebporoshin.sop.bpm_statistics_service.config.RabbitMQConfig
import com.rut.glebporoshin.sop.bpm_statistics_service.events.StatisticsAlertEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service


@Service
class StatisticsAlertPublisher(
    private val rabbitTemplate: RabbitTemplate,
) {

    private val log = LoggerFactory.getLogger(StatisticsAlertPublisher::class.java)

    fun publish(event: StatisticsAlertEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.STATISTICS_ALERTS_EXCHANGE,
            RabbitMQConfig.ROUTING_KEY_STATISTICS_ALERTS,
            event
        )

        log.info("Отправлено статистическое предупреждение {}", event.alertId)
    }
}
