package com.rut.glebporoshin.sop.bpmapi.api.endpoints

import com.rut.glebporoshin.sop.bpmapi.api.dto.common.StatusResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.onboarding.OnboardingStatusResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.onboarding.UpdateOnboardingTaskRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(
    name = "onboarding",
    description = "API for employee onboarding management via gRPC"
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = StatusResponse::class)
                )
            ]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = StatusResponse::class)
                )
            ]
        )
    ]
)
interface OnboardingApi {

    @Operation(
        summary = "Get onboarding status",
        description = "Retrieves onboarding status and tasks via gRPC"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Onboarding status retrieved successfully"
    )
    @GetMapping(
        value = ["/api/onboarding/{onboardingId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getOnboardingStatus(
        @PathVariable("onboardingId") onboardingId: String
    ): OnboardingStatusResponse

    @Operation(
        summary = "Update onboarding task",
        description = "Updates task completion status via gRPC"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Task updated successfully"
    )
    @PutMapping(
        value = ["/api/onboarding/{onboardingId}/tasks"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateOnboardingTask(
        @PathVariable("onboardingId") onboardingId: String,
        @Valid @RequestBody request: UpdateOnboardingTaskRequest
    ): OnboardingStatusResponse
}

