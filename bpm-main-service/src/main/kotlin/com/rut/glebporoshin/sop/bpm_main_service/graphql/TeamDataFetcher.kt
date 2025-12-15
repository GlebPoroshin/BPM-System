package com.rut.glebporoshin.sop.bpm_main_service.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Department
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Team
import com.rut.glebporoshin.sop.bpmapi.api.exception.TeamNotFoundException
import com.rut.glebporoshin.sop.bpm_main_service.storage.InMemoryStorage
import graphql.schema.DataFetchingEnvironment

@DgsComponent
class TeamDataFetcher(
    private val storage: InMemoryStorage,
) {

    @DgsQuery
    fun teams(): List<Team> {
        return storage.teams.values.toList()
    }

    @DgsQuery
    fun teamById(@InputArgument id: String): Team {
        return storage.teams[id]
            ?: throw TeamNotFoundException(id)
    }

    @DgsQuery
    fun teamsByDepartmentId(@InputArgument departmentId: String): List<Team> {
        return storage.teams.values.filter { it.departmentId == departmentId }
    }

    @DgsData(parentType = "Team", field = "department")
    fun teamDepartment(dfe: DataFetchingEnvironment): Department? {
        val team: Team = dfe.getSource()
        return storage.departments[team.departmentId]
    }
}

