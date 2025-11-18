package za.co.ee.learning.infrastructure.security

import arrow.core.raise.either
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.DomainResult
import za.co.ee.learning.domain.security.JWTProvider
import za.co.ee.learning.domain.security.TokenInfo
import za.co.ee.learning.domain.users.User
import java.time.Instant
import java.util.Date
import java.util.UUID

class DefaultJWTProvider(
    private val secret: String,
    private val issuer: String,
    private val expirationSeconds: Long,
) : JWTProvider {
    private val algorithm = Algorithm.HMAC256(secret)

    private val verifier =
        JWT
            .require(algorithm)
            .withIssuer(issuer)
            .build()

    override fun generate(user: User): TokenInfo {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(expirationSeconds)

        val token =
            JWT
                .create()
                .withIssuer(issuer)
                .withSubject(user.id.toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm)

        return TokenInfo(
            token = token,
            expires = expiresAt.epochSecond,
        )
    }

    override fun verify(jwt: String): DomainResult<UUID> =
        either {
            try {
                val decodedJWT = verifier.verify(jwt)
                UUID.fromString(decodedJWT.subject)
            } catch (e: JWTVerificationException) {
                raise(DomainError.JWTError("Invalid or expired token: ${e.message}"))
            } catch (e: Exception) {
                raise(DomainError.JWTError("Token verification failed: ${e.message}"))
            }
        }
}
