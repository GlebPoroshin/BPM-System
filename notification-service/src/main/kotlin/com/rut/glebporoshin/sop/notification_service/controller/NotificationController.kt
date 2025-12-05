package com.rut.glebporoshin.sop.notification_service.controller

import com.rut.glebporoshin.sop.notification_service.handler.NotificationWebSocketHandler
import com.rut.glebporoshin.sop.notification_service.model.UnicastRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val handler: NotificationWebSocketHandler
) {

    @PostMapping("/broadcast")
    fun broadcast(@RequestBody message: String): ResponseEntity<Map<String, Any>> {
        val sent = handler.broadcast(message)
        return ResponseEntity.ok(
            mapOf(
                "status" to "ok",
                "sentTo" to sent,
                "message" to message
            )
        )
    }

    @PostMapping("/unicast")
    fun unicast(@RequestBody request: UnicastRequest): ResponseEntity<Map<String, Any>> {
        val sent = handler.sendToUser(request.userId, request.message)
        return ResponseEntity.ok(
            mapOf(
                "status" to if (sent) "ok" else "not_delivered",
                "userId" to request.userId,
                "message" to request.message
            )
        )
    }

    @GetMapping("/stats")
    fun stats(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(
        mapOf("activeConnections" to handler.activeConnections())
    )
}
