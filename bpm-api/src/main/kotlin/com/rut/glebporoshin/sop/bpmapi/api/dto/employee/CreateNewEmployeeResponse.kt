package com.rut.glebporoshin.sop.bpmapi.api.dto.employee

import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Department
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Team
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "employees", itemRelation = "employee")
class CreateNewEmployeeResponse(
    val employeeId: String,
    val onboardingProcessId: String,
    val position: String,
    val department: Department,
    val team: Team,
) : RepresentationModel<CreateNewEmployeeResponse>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CreateNewEmployeeResponse) return false
        if (!super.equals(other)) return false

        if (employeeId != other.employeeId) return false
        if (onboardingProcessId != other.onboardingProcessId) return false
        if (position != other.position) return false
        if (department != other.department) return false
        if (team != other.team) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + employeeId.hashCode()
        result = 31 * result + onboardingProcessId.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + department.hashCode()
        result = 31 * result + team.hashCode()
        return result
    }

    override fun toString(): String {
        return "CreateNewEmployeeResponse(employeeId='$employeeId', onboardingProcessId='$onboardingProcessId', position='$position', department=$department, team=$team)"
    }
}
