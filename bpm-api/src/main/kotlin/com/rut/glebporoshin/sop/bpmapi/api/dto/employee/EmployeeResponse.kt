package com.rut.glebporoshin.sop.bpmapi.api.dto.employee

import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Department
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Team
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "employees", itemRelation = "employee")
class EmployeeResponse(
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val position: String,
    val department: Department,
    val team: Team,
) : RepresentationModel<EmployeeResponse>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmployeeResponse) return false
        if (!super.equals(other)) return false

        if (employeeId != other.employeeId) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (email != other.email) return false
        if (phone != other.phone) return false
        if (position != other.position) return false
        if (department != other.department) return false
        if (team != other.team) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + employeeId.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + department.hashCode()
        result = 31 * result + team.hashCode()
        return result
    }

    override fun toString(): String {
        return "EmployeeResponse(employeeId='$employeeId', firstName='$firstName', lastName='$lastName', position='$position')"
    }
}

