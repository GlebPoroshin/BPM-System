package com.rut.glebporoshin.sop.notification_service.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class NotificationWebSocketHandler : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(NotificationWebSocketHandler::class.java)
    private val sessionsByUser = ConcurrentHashMap<String, WebSocketSession>()
    private val anonymousSessions = ConcurrentHashMap.newKeySet<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = extractUserId(session)
        if (userId != null) {
            sessionsByUser[userId] = session
            log.info("WebSocket connected: userId={}, sessionId={}, totalUsers={}", userId, session.id, sessionsByUser.size)
        } else {
            anonymousSessions.add(session)
            log.info("WebSocket connected: anonymous sessionId={}, anonymousActive={}", session.id, anonymousSessions.size)
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        if (payload.equals("PING", ignoreCase = true)) {
            sendMessage(session, TextMessage("PONG"))
            return
        }
        log.debug("Message from {}: {}", session.id, payload)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        removeSession(session)
        log.info("WebSocket closed: sessionId={}, reason={}", session.id, status.reason)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("WebSocket transport error for {}: {}", session.id, exception.message)
        removeSession(session)
    }

    fun broadcast(message: String): Int {
        val textMessage = TextMessage(message)
        val recipients = sessionsByUser.values + anonymousSessions
        var sent = 0
        recipients.forEach {
            if (sendMessage(it, textMessage)) {
                sent++
            }
        }
        log.info("Broadcast sent to {}/{} clients", sent, recipients.size)
        return sent
    }

    fun sendToUser(userId: String, message: String): Boolean {
        val session = sessionsByUser[userId]
        val sent = session?.let { sendMessage(it, TextMessage(message)) } == true
        log.info("Unicast to {} success={}", userId, sent)
        return sent
    }

    fun activeConnections(): Int = sessionsByUser.size + anonymousSessions.size

    private fun sendMessage(session: WebSocketSession, message: TextMessage): Boolean {
        if (!session.isOpen) {
            removeSession(session)
            return false
        }
        return runCatching {
            session.sendMessage(message)
        }.onFailure {
            log.warn("Failed to send to {}: {}", session.id, it.message)
            removeSession(session)
        }.isSuccess
    }

    private fun removeSession(session: WebSocketSession) {
        anonymousSessions.remove(session)
        sessionsByUser.entries.removeIf { it.value.id == session.id }
    }

    private fun extractUserId(session: WebSocketSession): String? {
        val query = session.uri?.query ?: return null
        return query.split("&")
            .mapNotNull { it.split("=").takeIf { parts -> parts.size == 2 } }
            .firstOrNull { it[0] == "userId" }
            ?.get(1)
            ?.takeIf { it.isNotBlank() }
    }
}
