package com.rut.glebporoshin.sop.bpm_main_service.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.io.IOException
import java.util.*

class LoggingAndTracingFilter : Filter {

    companion object {
        private val log = LoggerFactory.getLogger(LoggingAndTracingFilter::class.java)
        private const val CORRELATION_ID_HEADER = "X-Request-ID"
        private const val CORRELATION_ID_MDC_KEY = "correlationId"
    }

    @Throws(IOException::class)
    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain
    ) {
        val request = servletRequest as HttpServletRequest
        val response = servletResponse as HttpServletResponse

        var correlationId = request.getHeader(CORRELATION_ID_HEADER)
        if (correlationId.isNullOrBlank()) {
            correlationId = UUID.randomUUID().toString()
        }

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)

        response.setHeader(CORRELATION_ID_HEADER, correlationId)

        val startTime = System.currentTimeMillis()

        try {
            if (request.requestURI.startsWith("/api/")) {
                log.info("Request started: {} {}", request.method, request.requestURI)
            }

            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - startTime

            if (request.requestURI.startsWith("/api/")) {
                log.info(
                    "Request finished: {} {} with status {} in {}ms",
                    request.method,
                    request.requestURI,
                    response.status,
                    duration
                )
            }

            MDC.remove(CORRELATION_ID_MDC_KEY)
        }
    }
}
