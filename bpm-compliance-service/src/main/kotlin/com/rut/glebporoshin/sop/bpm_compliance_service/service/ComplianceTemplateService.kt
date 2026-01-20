package com.rut.glebporoshin.sop.bpm_compliance_service.service

import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTaskTemplate
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTemplate
import com.rut.glebporoshin.sop.bpm_compliance_service.model.ComplianceTemplateRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


@Service
class ComplianceTemplateService {

    private val log = LoggerFactory.getLogger(ComplianceTemplateService::class.java)
    private val templates = ConcurrentHashMap<String, ComplianceTemplate>()

    init {
        seedDefaults()
    }

    fun listTemplates(): List<ComplianceTemplate> = templates.values.sortedBy { it.templateId }

    fun getTemplate(templateId: String): ComplianceTemplate? = templates[templateId]

    fun createTemplate(request: ComplianceTemplateRequest): ComplianceTemplate {
        val templateId = request.templateId?.takeIf { it.isNotBlank() } ?: generateId()
        val template = ComplianceTemplate(
            templateId = templateId,
            name = request.name,
            departmentId = request.departmentId?.takeIf { it.isNotBlank() },
            departmentName = request.departmentName?.takeIf { it.isNotBlank() },
            positionRegex = request.positionRegex?.takeIf { it.isNotBlank() },
            tasks = request.tasks.map {
                ComplianceTaskTemplate(
                    taskId = it.taskId?.takeIf { id -> id.isNotBlank() } ?: generateTaskId(),
                    name = it.name,
                    slaDays = it.slaDays,
                )
            },
        )

        templates[templateId] = template
        log.info("Создан шаблон комплаенса {}", templateId)
        return template
    }

    fun updateTemplate(templateId: String, request: ComplianceTemplateRequest): ComplianceTemplate? {
        val existing = templates[templateId] ?: return null

        val updated = existing.copy(
            name = request.name,
            departmentId = request.departmentId?.takeIf { it.isNotBlank() },
            departmentName = request.departmentName?.takeIf { it.isNotBlank() },
            positionRegex = request.positionRegex?.takeIf { it.isNotBlank() },
            tasks = request.tasks.map {
                ComplianceTaskTemplate(
                    taskId = it.taskId?.takeIf { id -> id.isNotBlank() } ?: generateTaskId(),
                    name = it.name,
                    slaDays = it.slaDays,
                )
            },
        )

        templates[templateId] = updated
        log.info("Обновлен шаблон комплаенса {}", templateId)
        return updated
    }

    fun deleteTemplate(templateId: String): Boolean = templates.remove(templateId) != null

    fun selectTemplate(departmentId: String, departmentName: String, position: String): ComplianceTemplate {
        val candidates = templates.values.filter { template ->
            matchesDepartment(template, departmentId, departmentName) && matchesPosition(template, position)
        }

        return candidates
            .maxWithOrNull(
                compareBy<ComplianceTemplate>(
                    { scoreTemplate(it, departmentId, departmentName, position) },
                    { it.templateId }
                )
            )
            ?: templates[DEFAULT_TEMPLATE_ID]
            ?: error("Шаблон по умолчанию не найден")
    }

    private fun matchesDepartment(template: ComplianceTemplate, departmentId: String, departmentName: String): Boolean {
        val matchesId = template.departmentId?.equals(departmentId, ignoreCase = true) ?: true
        val matchesName = template.departmentName?.equals(departmentName, ignoreCase = true) ?: true
        return matchesId && matchesName
    }

    private fun matchesPosition(template: ComplianceTemplate, position: String): Boolean {
        val regex = template.positionRegex ?: return true
        return Regex(regex, RegexOption.IGNORE_CASE).containsMatchIn(position)
    }

    private fun scoreTemplate(
        template: ComplianceTemplate,
        departmentId: String,
        departmentName: String,
        position: String,
    ): Int {
        var score = 0
        if (template.departmentId != null && template.departmentId.equals(departmentId, ignoreCase = true)) {
            score += 2
        }
        if (template.departmentName != null && template.departmentName.equals(departmentName, ignoreCase = true)) {
            score += 2
        }
        if (template.positionRegex != null && Regex(template.positionRegex, RegexOption.IGNORE_CASE).containsMatchIn(position)) {
            score += 1
        }
        return score
    }

    private fun seedDefaults() {
        val defaultTemplate = ComplianceTemplate(
            templateId = DEFAULT_TEMPLATE_ID,
            name = "Базовый чек-лист",
            departmentId = null,
            departmentName = null,
            positionRegex = null,
            tasks = listOf(
                ComplianceTaskTemplate("CL-001", "Проверить документы личности", 3),
                ComplianceTaskTemplate("CL-002", "Оформить пропуск", 2),
                ComplianceTaskTemplate("CL-003", "Назначить вводный инструктаж по безопасности", 5),
            ),
        )

        val developerTemplate = ComplianceTemplate(
            templateId = "TPL-DEV",
            name = "Чек-лист для разработчиков",
            departmentId = null,
            departmentName = "IT Department",
            positionRegex = "Developer",
            tasks = defaultTemplate.tasks + ComplianceTaskTemplate("CL-010", "Выдать доступы к репозиторию", 2),
        )

        val managerTemplate = ComplianceTemplate(
            templateId = "TPL-MANAGER",
            name = "Чек-лист для менеджеров",
            departmentId = null,
            departmentName = null,
            positionRegex = "Manager",
            tasks = defaultTemplate.tasks + ComplianceTaskTemplate("CL-011", "Назначить встречу с командой", 1),
        )

        templates[defaultTemplate.templateId] = defaultTemplate
        templates[developerTemplate.templateId] = developerTemplate
        templates[managerTemplate.templateId] = managerTemplate
    }

    private fun generateId(): String = "TPL-${UUID.randomUUID()}"

    private fun generateTaskId(): String = "CL-${UUID.randomUUID()}"

    companion object {
        private const val DEFAULT_TEMPLATE_ID = "TPL-DEFAULT"
    }
}
