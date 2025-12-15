package com.rut.glebporoshin.sop.bpmapi.api.exception

open class ApiException(
    val errorCode: ErrorCode,
    override val message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
