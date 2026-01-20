package com.rut.glebporoshin.sop.bpm_audit_service.tracing

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered


class TracingConfig {

    @Bean
    fun correlationContextFilter(): FilterRegistrationBean<CorrelationContextFilter> {
        val registration = FilterRegistrationBean(CorrelationContextFilter())
        registration.order = Ordered.HIGHEST_PRECEDENCE
        return registration
    }
}
