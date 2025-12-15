package com.rut.glebporoshin.sop.bpmapi.api.exception

class DepartmentNotFoundException(
    departmentId: String,
) : ApiException(
    errorCode = ErrorCode.DEPARTMENT_NOT_FOUND,
    message = "Department not found: id=${departmentId}",
)
