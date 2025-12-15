package com.rut.glebporoshin.sop.bpm_main_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.hateoas.config.EnableHypermediaSupport

@SpringBootApplication(
    scanBasePackages = [
        "com.rut.glebporoshin.sop.bpm_main_service",
        "com.rut.glebporoshin.sop.bpmapi.api",
    ],
    exclude = [DataSourceAutoConfiguration::class],
)
@EnableHypermediaSupport(type = [EnableHypermediaSupport.HypermediaType.HAL])
class BpmMainServiceApplication

fun main(args: Array<String>) {
    runApplication<BpmMainServiceApplication>(*args)
}
