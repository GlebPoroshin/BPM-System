package com.rut.glebporoshin.sop.bpm_statistics_service.tracing

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered


@Configuration
class TracingConfig {

    @Bean
    fun correlationContextFilter(): FilterRegistrationBean<CorrelationContextFilter> {
        val registration = FilterRegistrationBean(CorrelationContextFilter())
        registration.order = Ordered.HIGHEST_PRECEDENCE
        return registration
    }
}
