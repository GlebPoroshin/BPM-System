package com.rut.glebporoshin.sop.bpm_gateway_service.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.cloud.gateway.config.HttpClientProperties
import org.springframework.cloud.gateway.filter.NettyRoutingFilter
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter
import org.springframework.cloud.gateway.route.Route
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import reactor.netty.http.HttpProtocol
import reactor.netty.http.client.HttpClient

@Configuration
class GatewayNettyClientConfig {

    @Bean
    fun nettyRoutingFilter(
        httpClient: HttpClient,
        headersFilters: ObjectProvider<List<HttpHeadersFilter>>,
        httpClientProperties: HttpClientProperties
    ): NettyRoutingFilter {
        val grpcClient = HttpClient.create().protocol(HttpProtocol.H2C)

        return object : NettyRoutingFilter(httpClient, headersFilters, httpClientProperties) {
            override fun getHttpClient(route: Route, exchange: ServerWebExchange): HttpClient {
                return if (route.id == "onboarding-grpc") grpcClient else super.getHttpClient(route, exchange)
            }
        }
    }
}
