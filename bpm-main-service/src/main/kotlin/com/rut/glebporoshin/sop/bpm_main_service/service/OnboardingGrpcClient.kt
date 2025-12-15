package com.rut.glebporoshin.sop.bpm_main_service.service

import com.rut.glebporoshin.sop.grpc.onboarding.*
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OnboardingGrpcClient {

    @GrpcClient("onboarding-service")
    private lateinit var onboardingServiceStub: OnboardingServiceGrpc.OnboardingServiceBlockingStub

    fun createOnboarding(employeeId: String, position: String): OnboardingRecord? {
        return try {
            val request = CreateOnboardingRequest.newBuilder()
                .setEmployeeId(employeeId)
                .setPosition(position)
                .build()
            
            val response = onboardingServiceStub.createOnboarding(request)
            log.info("gRPC: Created onboarding for employee {} - Onboarding ID: {}", 
                employeeId, response.onboarding.onboardingId)
            response.onboarding
        } catch (e: StatusRuntimeException) {
            log.error("gRPC: Failed to create onboarding for employee {}: {}", employeeId, e.status)
            null
        }
    }

    fun getOnboardingStatus(onboardingId: String): OnboardingRecord? {
        return try {
            val request = GetOnboardingStatusRequest.newBuilder()
                .setOnboardingId(onboardingId)
                .build()
            
            val response = onboardingServiceStub.getOnboardingStatus(request)
            log.info("gRPC: Retrieved onboarding status - ID: {}, Status: {}", 
                onboardingId, response.onboarding.status)
            response.onboarding
        } catch (e: StatusRuntimeException) {
            log.error("gRPC: Failed to get onboarding status {}: {}", onboardingId, e.status)
            null
        }
    }

    fun updateTask(onboardingId: String, taskId: String, completed: Boolean): OnboardingRecord? {
        return try {
            val request = UpdateOnboardingTaskRequest.newBuilder()
                .setOnboardingId(onboardingId)
                .setTaskId(taskId)
                .setCompleted(completed)
                .build()
            
            val response = onboardingServiceStub.updateOnboardingTask(request)
            log.info("gRPC: Updated task {} in onboarding {} - Completed: {}", 
                taskId, onboardingId, completed)
            response.onboarding
        } catch (e: StatusRuntimeException) {
            log.error("gRPC: Failed to update task {} in onboarding {}: {}", 
                taskId, onboardingId, e.status)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OnboardingGrpcClient::class.java)
    }
}

