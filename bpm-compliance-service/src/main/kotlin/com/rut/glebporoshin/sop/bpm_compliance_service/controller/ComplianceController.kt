package com.rut.glebporoshin.sop.bpm_compliance_service.controller

import com.rut.glebporoshin.sop.bpm_compliance_service.service.ComplianceRecord
import com.rut.glebporoshin.sop.bpm_compliance_service.service.ComplianceService
import com.rut.glebporoshin.sop.bpm_compliance_service.service.ComplianceSummary
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/compliance")
class ComplianceController(
    private val complianceService: ComplianceService
) {

    @GetMapping("/summary")
    fun getSummary(): ComplianceSummary = complianceService.getSummary()

    @GetMapping("/records")
    fun getRecords(): Collection<ComplianceRecord> = complianceService.getRecords()

    @PostMapping("/{employeeId}/tasks/complete")
    fun completeTask(
        @PathVariable employeeId: String,
        @RequestParam task: String
    ): ResponseEntity<Void> {
        complianceService.markTaskCompleted(employeeId, task)
        return ResponseEntity.accepted().build()
    }
}
