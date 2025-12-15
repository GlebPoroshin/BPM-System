package com.rut.glebporoshin.sop.bpmapi.api.dto.employee

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateNewEmployeeRequest(
    @NotBlank val firstName: String,
    @NotBlank val lastName: String,
    @NotBlank val passportSeries: String,
    @NotBlank val passportNumber: String,
    @Email val email: String,
    @NotBlank val phone: String,
    @NotBlank val contactEmail: String,
    @NotBlank val departmentId: String,
    @NotBlank val teamId: String,
    @NotBlank val position: String,
)
