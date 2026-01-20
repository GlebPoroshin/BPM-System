package com.rut.glebporoshin.sop.bpm_statistics_service.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val STATISTICS_ALERTS_EXCHANGE = "bpm-statistics-alerts"
        const val ROUTING_KEY_STATISTICS_ALERTS = ""
    }

    @Bean
    fun statisticsAlertsExchange() = FanoutExchange(STATISTICS_ALERTS_EXCHANGE, true, false)

    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter(ObjectMapper().findAndRegisterModules())
}
