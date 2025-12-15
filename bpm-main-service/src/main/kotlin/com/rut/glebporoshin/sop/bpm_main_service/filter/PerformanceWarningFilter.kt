package com.rut.glebporoshin.sop.bpm_main_service.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@Order(2)
class PerformanceWarningFilter : Filter {

    companion object {
        private val log = LoggerFactory.getLogger(PerformanceWarningFilter::class.java)
        private const val SLOW_REQUEST_THRESHOLD_MS = 20L
    }

    @Throws(IOException::class)
    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain
    ) {
        val request = servletRequest as HttpServletRequest
        val startTime = System.currentTimeMillis()

        try {
            filterChain.doFilter(request, servletResponse)
        } finally {
            val duration = System.currentTimeMillis() - startTime

            if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn(
                    "Slow request detected: {} {} took {}ms",
                    request.method,
                    request.requestURI,
                    duration
                )
            }
        }
    }
}

