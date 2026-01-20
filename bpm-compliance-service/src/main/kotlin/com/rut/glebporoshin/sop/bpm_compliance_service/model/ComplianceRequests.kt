package com.rut.glebporoshin.sop.bpm_compliance_service.model

data class AssignTaskRequest(
    val assignee: String,
    val actor: String? = null,
    val comment: String? = null,
)

data class CompleteTaskRequest(
    val actor: String? = null,
    val comment: String? = null,
)

data class ComplianceTemplateRequest(
    val templateId: String? = null,
    val name: String,
    val departmentId: String? = null,
    val departmentName: String? = null,
    val positionRegex: String? = null,
    val tasks: List<ComplianceTaskTemplateRequest>,
)

data class ComplianceTaskTemplateRequest(
    val taskId: String? = null,
    val name: String,
    val slaDays: Long,
)
