package com.rut.glebporoshin.sop.bpm_main_service.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Tag(name = "Root", description = "API root entry point")
class RootController {

    @GetMapping
    @Operation(
        summary = "Get API root entry point",
        description = "Returns links to all available API resources"
    )
    fun getRoot(): RepresentationModel<RepresentationModel<*>> {
        val rootModel = object : RepresentationModel<RepresentationModel<*>>() {}
        
        rootModel.add(
            linkTo(methodOn(EmployeeController::class.java).getAllEmployees(0, 10))
                .withRel("employees")
                .withTitle("Employee Management"),
            
            linkTo(RootController::class.java)
                .slash("/swagger-ui.html")
                .withRel("documentation")
                .withTitle("API Documentation"),
            
            linkTo(RootController::class.java)
                .slash("/graphql")
                .withRel("graphql")
                .withTitle("GraphQL API"),
            
            linkTo(RootController::class.java)
                .slash("/graphiql")
                .withRel("graphiql")
                .withTitle("GraphiQL IDE")
        )
        
        return rootModel
    }
}

