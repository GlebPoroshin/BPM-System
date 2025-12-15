package com.rut.glebporoshin.sop.bpmapi.api.endpoints

import com.rut.glebporoshin.sop.bpmapi.api.dto.common.StatusResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeRequest
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.CreateNewEmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.EmployeeResponse
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessRequest
import com.rut.glebporoshin.sop.bpmapi.api.dto.employee.StartDismissEmployeeProcessResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(
    name = "employees",
    description = "API для управления жизненным циклом сотрудников",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = StatusResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Ресурс не найден",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = StatusResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервиса",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = StatusResponse::class),
                ),
            ],
        ),
    ],
)
interface EmployeeApi {

    @Operation(
        summary = "Запустить процесс найма сотрудника",
        description = "Процесс под капотом создаёт запись о сотруднике, заявки на доступы, технику и кадровые операции.",
    )
    @ApiResponse(
        responseCode = "202",
        description = "Процесс найма инициирован",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = CreateNewEmployeeResponse::class),
            ),
        ],
    )
    @PostMapping(
        value = ["/api/employees"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createEmployee(
        @Valid @RequestBody request: CreateNewEmployeeRequest,
    ): ResponseEntity<EntityModel<CreateNewEmployeeResponse>>

    @Operation(
        summary = "Инициировать процесс увольнения сотрудника",
        description = "Процесс закрывает доступы, оформляет кадровые документы и подготавливает возврат техники.",
    )
    @ApiResponse(
        responseCode = "202",
        description = "Процесс увольнения инициирован",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = StartDismissEmployeeProcessResponse::class),
            ),
        ],
    )
    @PostMapping(
        value = ["/api/employees/dismissals"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun startDismissalProcess(
        @Valid @RequestBody request: StartDismissEmployeeProcessRequest,
    ): ResponseEntity<EntityModel<StartDismissEmployeeProcessResponse>>

    @Operation(
        summary = "Получить сотрудника по ID",
        description = "Возвращает информацию о сотруднике по его идентификатору",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Сотрудник найден",
    )
    @GetMapping(
        value = ["/api/employees/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getEmployeeById(
        @PathVariable("id") id: String,
    ): EntityModel<EmployeeResponse>

    @Operation(
        summary = "Получить список всех сотрудников",
        description = "Возвращает постраничный список всех активных сотрудников",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Список сотрудников",
    )
    @GetMapping(
        value = ["/api/employees"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getAllEmployees(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PagedModel<EntityModel<EmployeeResponse>>
}
