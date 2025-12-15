package com.rut.glebporoshin.sop.bpm_main_service.storage

import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Department
import com.rut.glebporoshin.sop.bpmapi.api.dto.common.Team
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryStorage {

    val departments: MutableMap<String, Department> = ConcurrentHashMap()
    val teams: MutableMap<String, Team> = ConcurrentHashMap()
    val employees: MutableMap<String, EmployeeData> = ConcurrentHashMap()

    val employeeSequence = AtomicLong(1000)

    @PostConstruct
    fun init() {
        val itDept = Department(
            id = "dept-001",
            name = "IT Department",
            leadId = "lead-001",
        )
        val hrDept = Department(
            id = "dept-002",
            name = "HR Department",
            leadId = "lead-002",
        )
        val financeDept = Department(
            id = "dept-003",
            name = "Finance Department",
            leadId = "lead-003",
        )

        departments[itDept.id] = itDept
        departments[hrDept.id] = hrDept
        departments[financeDept.id] = financeDept

        val backendTeam = Team(
            id = "team-001",
            name = "Backend Team",
            leadId = "lead-101",
            departmentId = itDept.id,
        )
        val frontendTeam = Team(
            id = "team-002",
            name = "Frontend Team",
            leadId = "lead-102",
            departmentId = itDept.id,
        )
        val qaTeam = Team(
            id = "team-003",
            name = "QA Team",
            leadId = "lead-103",
            departmentId = itDept.id,
        )
        val recruitmentTeam = Team(
            id = "team-004",
            name = "Recruitment Team",
            leadId = "lead-104",
            departmentId = hrDept.id,
        )
        val accountingTeam = Team(
            id = "team-005",
            name = "Accounting Team",
            leadId = "lead-105",
            departmentId = financeDept.id,
        )

        teams[backendTeam.id] = backendTeam
        teams[frontendTeam.id] = frontendTeam
        teams[qaTeam.id] = qaTeam
        teams[recruitmentTeam.id] = recruitmentTeam
        teams[accountingTeam.id] = accountingTeam
    }

    data class EmployeeData(
        val employeeId: String,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val position: String,
        val departmentId: String,
        val teamId: String,
        val onboardingProcessId: String,
    )
}
