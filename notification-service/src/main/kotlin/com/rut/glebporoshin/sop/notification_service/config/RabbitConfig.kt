package com.rut.glebporoshin.sop.notification_service.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {

    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter(ObjectMapper().findAndRegisterModules())
}
