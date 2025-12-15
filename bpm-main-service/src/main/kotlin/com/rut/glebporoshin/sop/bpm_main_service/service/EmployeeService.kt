package com.rut.glebporoshin.sop.bpm_main_service.service

import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeRequest
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.EmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessRequest
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessResponse
import com.rut.glebporoshin.sop.bpmapi.api.exception.DepartmentNotFoundException
import com.rut.glebporoshin.sop.bpmapi.api.exception.EmployeeNotFoundException
import com.rut.glebporoshin.sop.bpmapi.api.exception.TeamNotFoundException
import com.rut.glebporoshin.sop.bpm_main_service.config.RabbitMQConfig
import com.rut.glebporoshin.sop.bpm_main_service.dto.PagedResponse
import com.rut.glebporoshin.sop.bpm_main_service.storage.InMemoryStorage
import com.rut.glebporoshin.sop.events.EmployeeCreatedEvent
import com.rut.glebporoshin.sop.events.EmployeeDismissedEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.ceil

@Service
class EmployeeService(
    private val storage: InMemoryStorage,
    private val rabbitTemplate: RabbitTemplate,
    private val onboardingGrpcClient: OnboardingGrpcClient,
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(EmployeeService::class.java)
    }

    fun createEmployee(request: CreateNewEmployeeRequest): CreateNewEmployeeResponse {
        val department = storage.departments[request.departmentId]
            ?: throw DepartmentNotFoundException(request.departmentId)

        val team = storage.teams[request.teamId]
            ?: throw TeamNotFoundException(request.teamId)

        if (team.departmentId != department.id) {
            throw TeamNotFoundException(request.teamId)
        }

        val employeeId = "EMP-${storage.employeeSequence.incrementAndGet()}"
        val onboardingRecord = onboardingGrpcClient.createOnboarding(employeeId, request.position)
        val onboardingProcessId = onboardingRecord?.onboardingId ?: "ONB-${UUID.randomUUID()}"

        val employeeData = InMemoryStorage.EmployeeData(
            employeeId = employeeId,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phone = request.phone,
            position = request.position,
            departmentId = department.id,
            teamId = team.id,
            onboardingProcessId = onboardingProcessId,
        )

        storage.employees[employeeId] = employeeData

        val event = EmployeeCreatedEvent(
            employeeId = employeeId,
            onboardingProcessId = onboardingProcessId,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            position = request.position,
            departmentId = department.id,
            departmentName = department.name,
            teamId = team.id,
            teamName = team.name,
        )
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EMPLOYEE_CREATED_EXCHANGE,
            RabbitMQConfig.ROUTING_KEY_EMPLOYEE_CREATED,
            event
        )
        
        log.info("Published EmployeeCreatedEvent for employee {}", employeeId)

        return CreateNewEmployeeResponse(
            employeeId = employeeId,
            onboardingProcessId = onboardingProcessId,
            position = request.position,
            department = department,
            team = team,
        )
    }

    fun startDismissalProcess(request: StartDismissEmployeeProcessRequest): StartDismissEmployeeProcessResponse {
        storage.employees[request.employeeId]
            ?: throw EmployeeNotFoundException(request.employeeId)

        val dismissalProcessId = "DIS-${UUID.randomUUID()}"

        storage.employees.remove(request.employeeId)

        val event = EmployeeDismissedEvent(
            employeeId = request.employeeId,
            dismissalProcessId = dismissalProcessId,
            lastWorkingDay = request.lastWorkingDay.toString(),
        )
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EMPLOYEE_DISMISSED_EXCHANGE,
            RabbitMQConfig.ROUTING_KEY_EMPLOYEE_DISMISSED,
            event
        )
        
        log.info("Published EmployeeDismissedEvent for employee {}", request.employeeId)

        return StartDismissEmployeeProcessResponse(
            employeeId = request.employeeId,
            dismissalProcessId = dismissalProcessId,
        )
    }

    fun getEmployeeById(id: String): EmployeeResponse {
        val employee = storage.employees[id]
            ?: throw EmployeeNotFoundException(id)

        val department = storage.departments[employee.departmentId]!!
        val team = storage.teams[employee.teamId]!!

        return EmployeeResponse(
            employeeId = employee.employeeId,
            firstName = employee.firstName,
            lastName = employee.lastName,
            email = employee.email,
            phone = employee.phone,
            position = employee.position,
            department = department,
            team = team,
        )
    }

    fun getAllEmployees(page: Int, size: Int): PagedResponse<EmployeeResponse> {
        val allEmployees = storage.employees.values
            .sortedBy { it.employeeId }
            .map { employee ->
                val department = storage.departments[employee.departmentId]!!
                val team = storage.teams[employee.teamId]!!

                EmployeeResponse(
                    employeeId = employee.employeeId,
                    firstName = employee.firstName,
                    lastName = employee.lastName,
                    email = employee.email,
                    phone = employee.phone,
                    position = employee.position,
                    department = department,
                    team = team,
                )
            }

        val totalElements = allEmployees.size.toLong()
        val totalPages = ceil(totalElements.toDouble() / size).toInt()
        val fromIndex = page * size
        val toIndex = minOf(fromIndex + size, allEmployees.size)

        val pageContent = if (fromIndex > allEmployees.size) {
            emptyList()
        } else {
            allEmployees.subList(fromIndex, toIndex)
        }

        return PagedResponse(
            content = pageContent,
            pageNumber = page,
            pageSize = size,
            totalElements = totalElements,
            totalPages = totalPages,
            isLast = page >= totalPages - 1,
        )
    }
}
