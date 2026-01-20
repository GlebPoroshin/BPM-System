package com.rut.glebporoshin.sop.bpm_statistics_service.controller

import com.rut.glebporoshin.sop.bpm_statistics_service.model.DailyCount
import com.rut.glebporoshin.sop.bpm_statistics_service.model.StatisticsAnomaly
import com.rut.glebporoshin.sop.bpm_statistics_service.model.StatisticsOverview
import com.rut.glebporoshin.sop.bpm_statistics_service.model.ThroughputStats
import com.rut.glebporoshin.sop.bpm_statistics_service.service.StatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate


@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService,
) {

    @GetMapping("/overview")
    fun overview(): StatisticsOverview = statisticsService.getOverview()

    @GetMapping("/time-series")
    fun timeSeries(
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
    ): List<DailyCount> {
        return statisticsService.getDailyCounts(parseDate(from), parseDate(to))
    }

    @GetMapping("/throughput")
    fun throughput(): ThroughputStats = statisticsService.getThroughput()

    @GetMapping("/anomalies")
    fun anomalies(): List<StatisticsAnomaly> = statisticsService.getAnomalies()

    @GetMapping("/departments")
    fun departments(): Map<String, Int> = statisticsService.getByDepartment()

    @GetMapping("/positions")
    fun positions(): Map<String, Int> = statisticsService.getByPosition()

    @GetMapping("/employees")
    fun employees() = statisticsService.getEmployees()

    private fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value)
        } catch (ex: Exception) {
            throw ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Некорректная дата")
        }
    }
}
