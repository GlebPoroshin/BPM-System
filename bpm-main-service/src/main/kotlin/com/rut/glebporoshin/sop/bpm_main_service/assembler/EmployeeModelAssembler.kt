package com.rut.glebporoshin.sop.bpm_main_service.assembler

import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.EmployeeResponse
import com.rut.glebporoshin.sop.bpm_main_service.controller.EmployeeController
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component

@Component
class EmployeeModelAssembler :
    RepresentationModelAssembler<EmployeeResponse, EntityModel<EmployeeResponse>> {

    override fun toModel(entity: EmployeeResponse): EntityModel<EmployeeResponse> {
        return EntityModel.of(
            entity,
            linkTo(methodOn(EmployeeController::class.java).getEmployeeById(entity.employeeId)).withSelfRel(),
            linkTo(methodOn(EmployeeController::class.java).getAllEmployees(0, 10)).withRel("collection"),
        )
    }

    override fun toCollectionModel(entities: Iterable<EmployeeResponse>): CollectionModel<EntityModel<EmployeeResponse>> {
        return super.toCollectionModel(entities)
            .add(linkTo(methodOn(EmployeeController::class.java).getAllEmployees(0, 10)).withSelfRel())
    }
}

