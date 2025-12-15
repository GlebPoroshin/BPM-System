package com.rut.glebporoshin.sop.bpmapi.api.dto.employee

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class StartDismissEmployeeProcessRequest(
    @NotBlank val employeeId: String,
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val lastWorkingDay: LocalDate,
)
