package com.rut.glebporoshin.sop.bpm_main_service.assembler

import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessResponse
import com.rut.glebporoshin.sop.bpm_main_service.controller.EmployeeController
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component

@Component
class StartDismissEmployeeModelAssembler :
    RepresentationModelAssembler<StartDismissEmployeeProcessResponse, EntityModel<StartDismissEmployeeProcessResponse>> {

    override fun toModel(entity: StartDismissEmployeeProcessResponse): EntityModel<StartDismissEmployeeProcessResponse> {
        return EntityModel.of(
            entity,
            linkTo(methodOn(EmployeeController::class.java).getEmployeeById(entity.employeeId))
                .withRel("employee")
                .withTitle("Dismissed Employee (removed)"),
        )
    }
}
