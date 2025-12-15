package com.rut.glebporoshin.sop.bpm_main_service.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val EMPLOYEE_CREATED_EXCHANGE = "bpm-employee-created"
        const val EMPLOYEE_DISMISSED_EXCHANGE = "bpm-employee-dismissed"
        const val ROUTING_KEY_EMPLOYEE_CREATED = ""
        const val ROUTING_KEY_EMPLOYEE_DISMISSED = ""
    }

    @Bean
    fun employeeCreatedExchange() = FanoutExchange(EMPLOYEE_CREATED_EXCHANGE, true, false)

    @Bean
    fun employeeDismissedExchange() = FanoutExchange(EMPLOYEE_DISMISSED_EXCHANGE, true, false)

    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter(ObjectMapper().findAndRegisterModules())
}
