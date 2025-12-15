package com.rut.glebporoshin.sop.events

import java.io.Serializable

data class EmployeeCreatedEvent(
    val employeeId: String,
    val onboardingProcessId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val position: String,
    val departmentId: String,
    val departmentName: String,
    val teamId: String,
    val teamName: String,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

