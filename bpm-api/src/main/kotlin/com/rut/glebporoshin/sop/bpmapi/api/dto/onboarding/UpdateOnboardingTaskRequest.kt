package com.rut.glebporoshin.sop.bpmapi.api.dto.onboarding

import jakarta.validation.constraints.NotBlank

data class UpdateOnboardingTaskRequest(
    @NotBlank val taskId: String,
    val completed: Boolean
)

