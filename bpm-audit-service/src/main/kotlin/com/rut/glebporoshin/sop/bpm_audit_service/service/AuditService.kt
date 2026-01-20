package com.rut.glebporoshin.sop.bpm_audit_service.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.rut.glebporoshin.sop.bpm_audit_service.model.AuditRecord
import com.rut.glebporoshin.sop.bpm_audit_service.model.AuditStats
import com.rut.glebporoshin.sop.bpm_audit_service.model.AuditVerificationResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


@Service
class AuditService(
    objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(AuditService::class.java)
    private val records = mutableListOf<AuditRecord>()
    private val lock = ReentrantLock()
    private val sequence = AtomicLong(0)
    private val mapper = objectMapper.copy()
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

    fun appendEvent(
        eventType: String,
        employeeId: String?,
        payload: Map<String, Any?>,
        correlationId: String?,
        traceId: String?,
    ): AuditRecord {
        val receivedAt = Instant.now()
        val occurredAt = receivedAt

        return lock.withLock {
            val previousHash = records.lastOrNull()?.hash
            val sequenceNumber = sequence.incrementAndGet()
            val payloadJson = mapper.writeValueAsString(payload)

            val hashMaterial = listOf(
                sequenceNumber,
                eventType,
                employeeId ?: "",
                occurredAt.toString(),
                receivedAt.toString(),
                correlationId ?: "",
                traceId ?: "",
                previousHash ?: "",
                payloadJson,
            ).joinToString("|")

            val hash = sha256(hashMaterial)
            val record = AuditRecord(
                sequence = sequenceNumber,
                eventType = eventType,
                employeeId = employeeId,
                payload = payload,
                occurredAt = occurredAt,
                receivedAt = receivedAt,
                correlationId = correlationId,
                traceId = traceId,
                previousHash = previousHash,
                hash = hash,
            )

            records.add(record)
            log.info("Аудит: записано событие {} (#{}). Хэш: {}", eventType, sequenceNumber, hash)
            record
        }
    }

    fun appendEvent(
        eventType: String,
        employeeId: String?,
        payload: Any,
        correlationId: String?,
        traceId: String?,
    ): AuditRecord {
        val mapPayload = mapper.convertValue(payload, object : TypeReference<Map<String, Any?>>() {})
        return appendEvent(eventType, employeeId, mapPayload, correlationId, traceId)
    }

    fun getRecord(sequence: Long): AuditRecord? = lock.withLock {
        records.firstOrNull { it.sequence == sequence }
    }

    fun findRecords(
        eventType: String?,
        employeeId: String?,
        from: Instant?,
        to: Instant?,
        offset: Int,
        limit: Int,
    ): List<AuditRecord> = lock.withLock {
        records.asSequence()
            .filter { eventType == null || it.eventType.equals(eventType, ignoreCase = true) }
            .filter { employeeId == null || it.employeeId?.equals(employeeId, ignoreCase = true) == true }
            .filter { from == null || !it.receivedAt.isBefore(from) }
            .filter { to == null || !it.receivedAt.isAfter(to) }
            .drop(offset)
            .take(limit)
            .toList()
    }

    fun verifyChain(): AuditVerificationResult = lock.withLock {
        val invalid = mutableListOf<Long>()
        var previousHash: String? = null

        records.forEach { record ->
            val payloadJson = mapper.writeValueAsString(record.payload)
            val hashMaterial = listOf(
                record.sequence,
                record.eventType,
                record.employeeId ?: "",
                record.occurredAt.toString(),
                record.receivedAt.toString(),
                record.correlationId ?: "",
                record.traceId ?: "",
                previousHash ?: "",
                payloadJson,
            ).joinToString("|")

            val expectedHash = sha256(hashMaterial)
            if (expectedHash != record.hash || record.previousHash != previousHash) {
                invalid.add(record.sequence)
            }
            previousHash = record.hash
        }

        AuditVerificationResult(
            valid = invalid.isEmpty(),
            checked = records.size,
            invalidSequences = invalid,
            lastHash = records.lastOrNull()?.hash,
        )
    }

    fun stats(): AuditStats = lock.withLock {
        val byEventType = records.groupingBy { it.eventType }.eachCount()
        val byEmployee = records.mapNotNull { it.employeeId }
            .groupingBy { it }
            .eachCount()
        AuditStats(
            total = records.size,
            byEventType = byEventType,
            byEmployee = byEmployee,
        )
    }

    fun exportCsv(records: List<AuditRecord>): String {
        val header = listOf(
            "sequence",
            "eventType",
            "employeeId",
            "occurredAt",
            "receivedAt",
            "correlationId",
            "traceId",
            "previousHash",
            "hash",
            "payload",
        )
        val builder = StringBuilder()
        builder.append(header.joinToString(",")).append("\n")

        records.forEach { record ->
            val payloadJson = mapper.writeValueAsString(record.payload)
            val row = listOf(
                record.sequence.toString(),
                record.eventType,
                record.employeeId ?: "",
                record.occurredAt.toString(),
                record.receivedAt.toString(),
                record.correlationId ?: "",
                record.traceId ?: "",
                record.previousHash ?: "",
                record.hash,
                payloadJson,
            ).joinToString(",") { value -> escapeCsv(value) }
            builder.append(row).append("\n")
        }

        return builder.toString()
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\n") || value.contains('"')
        if (!needsQuotes) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}
