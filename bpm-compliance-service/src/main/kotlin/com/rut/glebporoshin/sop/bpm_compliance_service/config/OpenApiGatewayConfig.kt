package com.rut.glebporoshin.sop.bpm_compliance_service.config

import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiGatewayConfig {

    @Bean
    fun openApiServers(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        openApi.servers = listOf(Server().url("/"))
    }
}
