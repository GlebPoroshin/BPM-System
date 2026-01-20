package com.rut.glebporoshin.sop.bpm_statistics_service.tracing

import org.slf4j.MDC
import java.util.UUID


object MdcUtils {
    const val CORRELATION_ID = "correlationId"
    const val TRACE_ID = "traceId"

    fun withMdc(correlationId: String?, traceId: String?, block: () -> Unit) {
        val previousCorrelationId = MDC.get(CORRELATION_ID)
        val previousTraceId = MDC.get(TRACE_ID)

        val resolvedCorrelationId = normalizeId(correlationId) ?: previousCorrelationId ?: generateId()
        MDC.put(CORRELATION_ID, resolvedCorrelationId)

        val resolvedTraceId = normalizeId(traceId) ?: previousTraceId
        if (resolvedTraceId != null) {
            MDC.put(TRACE_ID, resolvedTraceId)
        }

        try {
            block()
        } finally {
            restoreOrClear(CORRELATION_ID, previousCorrelationId)
            restoreOrClear(TRACE_ID, previousTraceId)
        }
    }

    fun currentCorrelationId(): String? = MDC.get(CORRELATION_ID)

    fun currentTraceId(): String? = MDC.get(TRACE_ID)

    fun generateId(): String = UUID.randomUUID().toString()

    private fun normalizeId(value: String?): String? = value?.trim()?.takeIf { it.isNotEmpty() }

    private fun restoreOrClear(key: String, value: String?) {
        if (value == null) {
            MDC.remove(key)
        } else {
            MDC.put(key, value)
        }
    }
}
