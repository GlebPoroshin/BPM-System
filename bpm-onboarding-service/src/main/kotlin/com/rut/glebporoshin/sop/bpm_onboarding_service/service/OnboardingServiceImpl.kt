package com.rut.glebporoshin.sop.bpm_onboarding_service.service

import com.rut.glebporoshin.sop.grpc.onboarding.*
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@GrpcService
class OnboardingServiceImpl : OnboardingServiceGrpc.OnboardingServiceImplBase() {

    private val onboardingRecords = ConcurrentHashMap<String, OnboardingRecord>()

    override fun createOnboarding(
        request: CreateOnboardingRequest,
        responseObserver: StreamObserver<CreateOnboardingResponse>
    ) {
        val onboardingId = "ONB-${UUID.randomUUID()}"
        val tasks = generateDefaultTasks(request.position)
        
        val record = OnboardingRecord.newBuilder()
            .setOnboardingId(onboardingId)
            .setEmployeeId(request.employeeId)
            .setStatus("IN_PROGRESS")
            .addAllTasks(tasks)
            .build()
        
        onboardingRecords[onboardingId] = record
        
        log.info("Created onboarding record - ID: {}, Employee: {}, Tasks: {}", 
            onboardingId, request.employeeId, tasks.size)
        
        responseObserver.onNext(
            CreateOnboardingResponse.newBuilder()
                .setOnboarding(record)
                .build()
        )
        responseObserver.onCompleted()
    }

    override fun getOnboardingStatus(
        request: GetOnboardingStatusRequest,
        responseObserver: StreamObserver<GetOnboardingStatusResponse>
    ) {
        val record = onboardingRecords[request.onboardingId]
        
        if (record != null) {
            log.info("Retrieved onboarding status - ID: {}, Status: {}", 
                request.onboardingId, record.status)
            
            responseObserver.onNext(
                GetOnboardingStatusResponse.newBuilder()
                    .setOnboarding(record)
                    .build()
            )
        } else {
            log.warn("Onboarding record not found - ID: {}", request.onboardingId)
            responseObserver.onError(
                io.grpc.Status.NOT_FOUND
                    .withDescription("Onboarding record not found: ${request.onboardingId}")
                    .asRuntimeException()
            )
            return
        }
        
        responseObserver.onCompleted()
    }

    override fun updateOnboardingTask(
        request: UpdateOnboardingTaskRequest,
        responseObserver: StreamObserver<UpdateOnboardingTaskResponse>
    ) {
        val record = onboardingRecords[request.onboardingId]
        
        if (record == null) {
            log.warn("Onboarding record not found for task update - ID: {}", request.onboardingId)
            responseObserver.onError(
                io.grpc.Status.NOT_FOUND
                    .withDescription("Onboarding record not found: ${request.onboardingId}")
                    .asRuntimeException()
            )
            return
        }
        
        val updatedTasks = record.tasksList.map { task ->
            if (task.taskId == request.taskId) {
                task.toBuilder().setCompleted(request.completed).build()
            } else {
                task
            }
        }
        
        val updatedRecord = record.toBuilder()
            .clearTasks()
            .addAllTasks(updatedTasks)
            .setStatus(if (updatedTasks.all { it.completed }) "COMPLETED" else "IN_PROGRESS")
            .build()
        
        onboardingRecords[request.onboardingId] = updatedRecord
        
        log.info("Updated task - Onboarding: {}, Task: {}, Completed: {}", 
            request.onboardingId, request.taskId, request.completed)
        
        responseObserver.onNext(
            UpdateOnboardingTaskResponse.newBuilder()
                .setOnboarding(updatedRecord)
                .build()
        )
        responseObserver.onCompleted()
    }

    private fun generateDefaultTasks(position: String): List<OnboardingTask> {
        val commonTasks = listOf(
            OnboardingTask.newBuilder()
                .setTaskId("TASK-001")
                .setName("Complete HR documentation")
                .setCompleted(false)
                .build(),
            OnboardingTask.newBuilder()
                .setTaskId("TASK-002")
                .setName("Setup workstation")
                .setCompleted(false)
                .build(),
            OnboardingTask.newBuilder()
                .setTaskId("TASK-003")
                .setName("Security training")
                .setCompleted(false)
                .build()
        )
        
        return when {
            position.contains("Developer", ignoreCase = true) -> commonTasks + listOf(
                OnboardingTask.newBuilder()
                    .setTaskId("TASK-004")
                    .setName("Setup development environment")
                    .setCompleted(false)
                    .build(),
                OnboardingTask.newBuilder()
                    .setTaskId("TASK-005")
                    .setName("Code review training")
                    .setCompleted(false)
                    .build()
            )
            position.contains("Manager", ignoreCase = true) -> commonTasks + listOf(
                OnboardingTask.newBuilder()
                    .setTaskId("TASK-004")
                    .setName("Team introduction meeting")
                    .setCompleted(false)
                    .build()
            )
            else -> commonTasks
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OnboardingServiceImpl::class.java)
    }
}

