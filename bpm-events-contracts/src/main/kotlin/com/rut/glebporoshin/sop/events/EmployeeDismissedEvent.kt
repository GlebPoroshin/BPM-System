package com.rut.glebporoshin.sop.events

import java.io.Serializable

data class EmployeeDismissedEvent(
    val employeeId: String,
    val dismissalProcessId: String,
    val lastWorkingDay: String,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

