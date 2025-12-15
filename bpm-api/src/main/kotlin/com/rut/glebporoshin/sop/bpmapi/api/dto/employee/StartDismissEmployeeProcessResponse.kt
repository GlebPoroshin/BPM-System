package com.rut.glebporoshin.sop.bpmapi.api.dto.employee

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "dismissals", itemRelation = "dismissal")
class StartDismissEmployeeProcessResponse(
    val employeeId: String,
    val dismissalProcessId: String,
) : RepresentationModel<StartDismissEmployeeProcessResponse>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StartDismissEmployeeProcessResponse) return false
        if (!super.equals(other)) return false

        if (employeeId != other.employeeId) return false
        if (dismissalProcessId != other.dismissalProcessId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + employeeId.hashCode()
        result = 31 * result + dismissalProcessId.hashCode()
        return result
    }

    override fun toString(): String {
        return "StartDismissEmployeeProcessResponse(employeeId='$employeeId', dismissalProcessId='$dismissalProcessId')"
    }
}
