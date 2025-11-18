package za.co.ee.learning.domain.security

import za.co.ee.learning.domain.DomainResult
import za.co.ee.learning.domain.users.User
import java.util.UUID

data class TokenInfo(
    val token: String,
    val expires: Long,
)

interface JWTProvider {
    fun generate(user: User): TokenInfo

    fun verify(jwt: String): DomainResult<UUID>
}
