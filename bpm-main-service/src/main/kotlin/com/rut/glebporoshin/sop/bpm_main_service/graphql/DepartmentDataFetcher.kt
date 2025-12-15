package com.rut.glebporoshin.sop.bpm_main_service.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Department
import com.rut.glebporoshin.sop.bpmapi.api.exception.DepartmentNotFoundException
import com.rut.glebporoshin.sop.bpm_main_service.storage.InMemoryStorage

@DgsComponent
class DepartmentDataFetcher(
    private val storage: InMemoryStorage,
) {

    @DgsQuery
    fun departments(): List<Department> {
        return storage.departments.values.toList()
    }

    @DgsQuery
    fun departmentById(@InputArgument id: String): Department {
        return storage.departments[id]
            ?: throw DepartmentNotFoundException(id)
    }
}

