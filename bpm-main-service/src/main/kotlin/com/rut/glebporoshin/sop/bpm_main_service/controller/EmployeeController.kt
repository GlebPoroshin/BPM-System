package com.rut.glebporoshin.sop.bpm_main_service.controller

import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeRequest
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.EmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessRequest
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessResponse
import com.rut.glebporoshin.sop.bpmapi.api.endpoints.EmployeeApi
import com.rut.glebporoshin.sop.bpm_main_service.assembler.CreateNewEmployeeModelAssembler
import com.rut.glebporoshin.sop.bpm_main_service.assembler.EmployeeModelAssembler
import com.rut.glebporoshin.sop.bpm_main_service.assembler.StartDismissEmployeeModelAssembler
import com.rut.glebporoshin.sop.bpm_main_service.service.EmployeeService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.lang.Thread.sleep

@RestController
class EmployeeController(
    private val employeeService: EmployeeService,
    private val employeeAssembler: EmployeeModelAssembler,
    private val createEmployeeAssembler: CreateNewEmployeeModelAssembler,
    private val dismissEmployeeAssembler: StartDismissEmployeeModelAssembler,
    private val pagedResourcesAssembler: PagedResourcesAssembler<EmployeeResponse>,
) : EmployeeApi {

    override fun createEmployee(request: CreateNewEmployeeRequest): ResponseEntity<EntityModel<CreateNewEmployeeResponse>> {
        val response = employeeService.createEmployee(request)
        val entityModel = createEmployeeAssembler.toModel(response)
        
        return ResponseEntity
            .accepted()
            .body(entityModel)
    }

    override fun startDismissalProcess(request: StartDismissEmployeeProcessRequest): ResponseEntity<EntityModel<StartDismissEmployeeProcessResponse>> {
        val response = employeeService.startDismissalProcess(request)
        val entityModel = dismissEmployeeAssembler.toModel(response)
        
        return ResponseEntity
            .accepted()
            .body(entityModel)
    }

    override fun getEmployeeById(id: String): EntityModel<EmployeeResponse> {
        val employee = employeeService.getEmployeeById(id)
        return employeeAssembler.toModel(employee)
    }

    override fun getAllEmployees(page: Int, size: Int): PagedModel<EntityModel<EmployeeResponse>> {
        val pagedResponse = employeeService.getAllEmployees(page, size)
        
        val employeePage = PageImpl(
            pagedResponse.content,
            PageRequest.of(pagedResponse.pageNumber, pagedResponse.pageSize),
            pagedResponse.totalElements
        )
        
        sleep(500)
        
        return pagedResourcesAssembler.toModel(employeePage, employeeAssembler)
    }
}
