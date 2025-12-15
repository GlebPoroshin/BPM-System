package com.rut.glebporoshin.sop.bpm_main_service.controller

import com.rut.glebporoshin.sop.bpmapi.api.dto.onboarding.OnboardingStatusResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.onboarding.OnboardingTaskDto
import com.rut.glebporoshin.sop.bpmapi.api.dto.onboarding.UpdateOnboardingTaskRequest
import com.rut.glebporoshin.sop.bpmapi.api.endpoints.OnboardingApi
import com.rut.glebporoshin.sop.bpm_main_service.service.OnboardingGrpcClient
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class OnboardingController(
    private val onboardingGrpcClient: OnboardingGrpcClient
) : OnboardingApi {

    override fun getOnboardingStatus(onboardingId: String): OnboardingStatusResponse {
        val record = onboardingGrpcClient.getOnboardingStatus(onboardingId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Onboarding record not found: $onboardingId")
        
        return OnboardingStatusResponse(
            onboardingId = record.onboardingId,
            employeeId = record.employeeId,
            status = record.status,
            tasks = record.tasksList.map { task ->
                OnboardingTaskDto(
                    taskId = task.taskId,
                    name = task.name,
                    completed = task.completed
                )
            }
        )
    }

    override fun updateOnboardingTask(
        onboardingId: String,
        request: UpdateOnboardingTaskRequest
    ): OnboardingStatusResponse {
        val record = onboardingGrpcClient.updateTask(
            onboardingId = onboardingId,
            taskId = request.taskId,
            completed = request.completed
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Onboarding record not found: $onboardingId")
        
        return OnboardingStatusResponse(
            onboardingId = record.onboardingId,
            employeeId = record.employeeId,
            status = record.status,
            tasks = record.tasksList.map { task ->
                OnboardingTaskDto(
                    taskId = task.taskId,
                    name = task.name,
                    completed = task.completed
                )
            }
        )
    }
}

