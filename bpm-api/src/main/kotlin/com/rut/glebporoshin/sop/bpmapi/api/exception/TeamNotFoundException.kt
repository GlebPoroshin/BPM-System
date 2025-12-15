package com.rut.glebporoshin.sop.bpmapi.api.exception

class TeamNotFoundException(
    teamId: String,
) : ApiException(
    errorCode = ErrorCode.TEAM_NOT_FOUND,
    message = "Team not found: id=${teamId}",
)


