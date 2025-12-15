package com.rut.glebporoshin.sop.bpmapi.api.exception

enum class ErrorCode(val httpStatus: Int) {
    VALIDATION_FAILED(400),
    DEPARTMENT_NOT_FOUND(404),
    TEAM_NOT_FOUND(404),
    EMPLOYEE_NOT_FOUND(404),
    INTERNAL_ERROR(500),
}

