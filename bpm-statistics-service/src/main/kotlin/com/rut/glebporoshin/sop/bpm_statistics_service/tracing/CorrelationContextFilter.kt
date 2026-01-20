package com.rut.glebporoshin.sop.bpm_statistics_service.tracing

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter


class CorrelationContextFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val correlationId = resolveCorrelationId(request)
        val traceId = resolveTraceId(request)

        MdcUtils.withMdc(correlationId, traceId) {
            response.setHeader("X-Correlation-Id", MdcUtils.currentCorrelationId())
            MdcUtils.currentTraceId()?.let { response.setHeader("X-Trace-Id", it) }
            filterChain.doFilter(request, response)
        }
    }

    private fun resolveCorrelationId(request: HttpServletRequest): String? {
        val headers = listOf("X-Correlation-Id", "X-Request-Id")
        return headers.firstNotNullOfOrNull { header ->
            request.getHeader(header)?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

    private fun resolveTraceId(request: HttpServletRequest): String? {
        val explicit = request.getHeader("X-B3-TraceId")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        if (explicit != null) return explicit

        val legacy = request.getHeader("X-Trace-Id")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        if (legacy != null) return legacy

        return parseTraceParent(request.getHeader("traceparent"))
    }

    private fun parseTraceParent(traceParent: String?): String? {
        if (traceParent.isNullOrBlank()) return null
        val parts = traceParent.trim().split("-")
        if (parts.size < 4) return null
        val traceId = parts[1]
        return traceId.takeIf { it.length == 32 || it.length == 16 }
    }
}
