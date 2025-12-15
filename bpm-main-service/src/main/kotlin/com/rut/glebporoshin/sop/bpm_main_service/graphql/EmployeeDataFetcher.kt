package com.rut.glebporoshin.sop.bpm_main_service.graphql

import com.netflix.graphql.dgs.*
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Department
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Team
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.EmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessResponse
import com.rut.glebporoshin.sop.bpm_main_service.service.EmployeeService
import graphql.schema.DataFetchingEnvironment

@DgsComponent
class EmployeeDataFetcher(
    private val employeeService: EmployeeService,
) {

    @DgsQuery
    fun employees(): List<EmployeeResponse> {
        return employeeService.getAllEmployees(0, 1000).content
    }

    @DgsQuery
    fun employeeById(@InputArgument id: String): EmployeeResponse {
        return employeeService.getEmployeeById(id)
    }

    @DgsData(parentType = "Employee", field = "department")
    fun employeeDepartment(dfe: DataFetchingEnvironment): Department {
        val employee: EmployeeResponse = dfe.getSource()
        return employee.department
    }

    @DgsData(parentType = "Employee", field = "team")
    fun employeeTeam(dfe: DataFetchingEnvironment): Team {
        val employee: EmployeeResponse = dfe.getSource()
        return employee.team
    }

    @DgsMutation
    fun createEmployee(@InputArgument("input") input: Map<String, String>): CreateNewEmployeeResponse {
        val request = com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeRequest(
            firstName = input["firstName"]!!,
            lastName = input["lastName"]!!,
            passportSeries = input["passportSeries"]!!,
            passportNumber = input["passportNumber"]!!,
            email = input["email"]!!,
            phone = input["phone"]!!,
            contactEmail = input["contactEmail"]!!,
            departmentId = input["departmentId"]!!,
            teamId = input["teamId"]!!,
            position = input["position"]!!,
        )
        return employeeService.createEmployee(request)
    }

    @DgsMutation
    fun startDismissal(@InputArgument("input") input: Map<String, String>): StartDismissEmployeeProcessResponse {
        val request = com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessRequest(
            employeeId = input["employeeId"]!!,
            lastWorkingDay = java.time.LocalDate.parse(input["lastWorkingDay"]!!),
        )
        return employeeService.startDismissalProcess(request)
    }
}

