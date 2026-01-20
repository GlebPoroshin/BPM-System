package com.rut.glebporoshin.sop.bpm_audit_service.controller

import com.rut.glebporoshin.sop.bpm_audit_service.model.AuditRecord
import com.rut.glebporoshin.sop.bpm_audit_service.model.AuditStats
import com.rut.glebporoshin.sop.bpm_audit_service.model.AuditVerificationResult
import com.rut.glebporoshin.sop.bpm_audit_service.service.AuditService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant


@RestController
@RequestMapping("/audit")
class AuditController(
    private val auditService: AuditService
) {

    @GetMapping("/records")
    fun getRecords(
        @RequestParam(required = false) eventType: String?,
        @RequestParam(required = false) employeeId: String?,
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "100") limit: Int,
    ): List<AuditRecord> {
        validatePagination(offset, limit)
        return auditService.findRecords(
            eventType = eventType,
            employeeId = employeeId,
            from = parseInstant(from),
            to = parseInstant(to),
            offset = offset,
            limit = limit,
        )
    }

    @GetMapping("/records/{sequence}")
    fun getRecord(@PathVariable sequence: Long): AuditRecord {
        return auditService.getRecord(sequence)
            ?: throw ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Запись не найдена")
    }

    @GetMapping("/export")
    fun export(
        @RequestParam(required = false) eventType: String?,
        @RequestParam(required = false) employeeId: String?,
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
        @RequestParam(defaultValue = "json") format: String,
    ): ResponseEntity<Any> {
        val records = auditService.findRecords(
            eventType = eventType,
            employeeId = employeeId,
            from = parseInstant(from),
            to = parseInstant(to),
            offset = 0,
            limit = Int.MAX_VALUE,
        )

        return when (format.lowercase()) {
            "csv" -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(auditService.exportCsv(records))
            "json" -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(records)
            else -> throw ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Неизвестный формат экспорта")
        }
    }

    @GetMapping("/verify")
    fun verify(): AuditVerificationResult = auditService.verifyChain()

    @GetMapping("/stats")
    fun stats(): AuditStats = auditService.stats()

    private fun parseInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return try {
            Instant.parse(value)
        } catch (ex: Exception) {
            throw ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Некорректный формат времени")
        }
    }

    private fun validatePagination(offset: Int, limit: Int) {
        if (offset < 0 || limit < 1 || limit > 1000) {
            throw ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Некорректные параметры пагинации")
        }
    }
}
