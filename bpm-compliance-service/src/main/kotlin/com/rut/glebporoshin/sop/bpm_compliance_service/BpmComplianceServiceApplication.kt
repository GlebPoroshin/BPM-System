package com.rut.glebporoshin.sop.bpm_compliance_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BpmComplianceServiceApplication

fun main(args: Array<String>) {
    runApplication<BpmComplianceServiceApplication>(*args)
}
