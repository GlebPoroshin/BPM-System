package com.rut.glebporoshin.sop.bpmapi.api.dto.onboarding

data class OnboardingStatusResponse(
    val onboardingId: String,
    val employeeId: String,
    val status: String,
    val tasks: List<OnboardingTaskDto>
)

data class OnboardingTaskDto(
    val taskId: String,
    val name: String,
    val completed: Boolean
)

