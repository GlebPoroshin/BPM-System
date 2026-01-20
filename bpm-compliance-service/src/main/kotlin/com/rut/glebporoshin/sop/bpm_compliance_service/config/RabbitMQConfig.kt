package com.rut.glebporoshin.sop.bpm_compliance_service.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val COMPLIANCE_OVERDUE_EXCHANGE = "bpm-compliance-overdue"
        const val ROUTING_KEY_COMPLIANCE_OVERDUE = ""
    }

    @Bean
    fun complianceOverdueExchange() = FanoutExchange(COMPLIANCE_OVERDUE_EXCHANGE, true, false)

    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter(ObjectMapper().findAndRegisterModules())
}
