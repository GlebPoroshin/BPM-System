package com.rut.glebporoshin.sop.bpm_statistics_service.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class StatisticsService {

    private val employees = ConcurrentHashMap<String, EmployeeStatistic>()

    data class EmployeeStatistic(
        val employeeId: String,
        val fullName: String,
        val position: String,
        val departmentName: String
    )

    fun recordEmployeeCreation(
        employeeId: String,
        firstName: String,
        lastName: String,
        position: String,
        departmentName: String
    ) {
        val fullName = "$firstName $lastName"
        employees[employeeId] = EmployeeStatistic(employeeId, fullName, position, departmentName)
        
        log.info("STATISTICS: New employee recorded - ID: {}, Name: {}", employeeId, fullName)
        log.info("STATISTICS: Total employees in system: {}", employees.size)
        log.info("STATISTICS: Employees by department: {}", 
            employees.values.groupingBy { it.departmentName }.eachCount())
    }

    fun getTotalEmployeeCount() = employees.size

    fun getEmployeesByDepartment() = employees.values.groupingBy { it.departmentName }.eachCount()

    fun getAllEmployees(): Collection<EmployeeStatistic> = employees.values

    companion object {
        private val log = LoggerFactory.getLogger(StatisticsService::class.java)
    }
}

