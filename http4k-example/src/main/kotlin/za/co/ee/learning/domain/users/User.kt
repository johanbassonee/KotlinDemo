package za.co.ee.learning.domain.users

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
)
