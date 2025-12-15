package com.rut.glebporoshin.sop.bpmapi.api.exception

class EmployeeNotFoundException(
    employeeId: String,
) : ApiException(
    errorCode = ErrorCode.EMPLOYEE_NOT_FOUND,
    message = "Employee not found: id=${employeeId}",
)

