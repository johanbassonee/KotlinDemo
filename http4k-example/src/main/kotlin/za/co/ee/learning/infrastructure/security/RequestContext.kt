package za.co.ee.learning.infrastructure.security

import org.http4k.core.Request
import org.http4k.lens.RequestKey
import java.util.UUID

private val authenticatedUserKey = RequestKey.required<UUID>("authenticated-user")

fun Request.authenticatedUser(): UUID =
    try {
        authenticatedUserKey(this)
    } catch (e: Exception) {
        UUID.randomUUID()
    }

fun Request.withAuthenticatedUser(userId: UUID): Request = authenticatedUserKey(userId, this)

fun Request.requireAuthenticatedUser(): UUID = authenticatedUserKey(this)
