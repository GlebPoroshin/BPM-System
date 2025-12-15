package com.rut.glebporoshin.sop.bpm_main_service.assembler

import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeResponse
import com.rut.glebporoshin.sop.bpm_main_service.controller.EmployeeController
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component

@Component
class CreateNewEmployeeModelAssembler : 
    RepresentationModelAssembler<CreateNewEmployeeResponse, EntityModel<CreateNewEmployeeResponse>> {

    override fun toModel(entity: CreateNewEmployeeResponse): EntityModel<CreateNewEmployeeResponse> {
        return EntityModel.of(
            entity,
            linkTo(methodOn(EmployeeController::class.java).getEmployeeById(entity.employeeId))
                .withRel("employee")
                .withTitle("View Employee"),
            linkTo(methodOn(EmployeeController::class.java).getAllEmployees(0, 10))
                .withRel("collection")
                .withTitle("All Employees"),
        )
    }
}
