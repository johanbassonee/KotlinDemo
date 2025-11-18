package za.co.ee.learning.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.users.User
import java.time.Instant
import java.util.Date
import java.util.UUID

class DefaultJWTProviderTest :
    FunSpec({
        val secret = "test-secret-key"
        val issuer = "test-issuer"
        val expirationSeconds = 3600L

        val jwtProvider = DefaultJWTProvider(secret, issuer, expirationSeconds)

        val testUser =
            User(
                id = UUID.randomUUID(),
                email = "test@example.com",
                passwordHash = "hash",
            )

        context("generate") {
            test("should generate a valid JWT token") {
                val tokenInfo = jwtProvider.generate(testUser)

                tokenInfo.token shouldContain "."
                tokenInfo.expires shouldBeGreaterThan Instant.now().epochSecond
            }

            test("should include correct issuer in token") {
                val tokenInfo = jwtProvider.generate(testUser)

                val decodedJWT = JWT.decode(tokenInfo.token)
                decodedJWT.issuer shouldBe issuer
            }

            test("should include user ID as subject in token") {
                val tokenInfo = jwtProvider.generate(testUser)

                val decodedJWT = JWT.decode(tokenInfo.token)
                decodedJWT.subject shouldBe testUser.id.toString()
            }

            test("should set expiration time correctly") {
                val beforeGeneration = Instant.now().epochSecond
                val tokenInfo = jwtProvider.generate(testUser)

                // Expiration should be approximately expirationSeconds from now
                // Allow for 2 seconds of tolerance
                tokenInfo.expires shouldBeGreaterThan beforeGeneration + expirationSeconds - 2
            }

            test("should include issued at timestamp") {
                val beforeGeneration = Instant.now().epochSecond
                val tokenInfo = jwtProvider.generate(testUser)

                val decodedJWT = JWT.decode(tokenInfo.token)
                val issuedAt = decodedJWT.issuedAt.toInstant().epochSecond

                issuedAt shouldBeGreaterThan beforeGeneration - 1
            }
        }

        context("verify") {
            test("should successfully verify a valid token") {
                val tokenInfo = jwtProvider.generate(testUser)

                val result = jwtProvider.verify(tokenInfo.token)

                val userId = result.shouldBeRight()
                userId shouldBe testUser.id
            }

            test("should return error for expired token") {
                // Create a JWT provider with very short expiration
                val shortExpirationProvider = DefaultJWTProvider(secret, issuer, -1)
                val tokenInfo = shortExpirationProvider.generate(testUser)

                // Wait a bit to ensure token is expired
                Thread.sleep(1000)

                val result = jwtProvider.verify(tokenInfo.token)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.JWTError>()
                error.message shouldContain "Invalid or expired token"
            }

            test("should return error for token with invalid signature") {
                val differentSecretProvider = DefaultJWTProvider("different-secret", issuer, expirationSeconds)
                val tokenInfo = differentSecretProvider.generate(testUser)

                val result = jwtProvider.verify(tokenInfo.token)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.JWTError>()
                error.message shouldContain "Invalid or expired token"
            }

            test("should return error for malformed token") {
                val malformedToken = "not.a.valid.jwt.token"

                val result = jwtProvider.verify(malformedToken)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.JWTError>()
            }

            test("should return error for empty token") {
                val result = jwtProvider.verify("")

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.JWTError>()
            }

            test("should return error for token with wrong issuer") {
                val wrongIssuerProvider = DefaultJWTProvider(secret, "wrong-issuer", expirationSeconds)
                val tokenInfo = wrongIssuerProvider.generate(testUser)

                val result = jwtProvider.verify(tokenInfo.token)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.JWTError>()
                error.message shouldContain "Invalid or expired token"
            }

            test("should return error for token with invalid subject format") {
                val now = Instant.now()
                val expiresAt = now.plusSeconds(expirationSeconds)
                val algorithm = Algorithm.HMAC256(secret)

                val invalidToken =
                    JWT
                        .create()
                        .withIssuer(issuer)
                        .withSubject("not-a-valid-uuid")
                        .withIssuedAt(Date.from(now))
                        .withExpiresAt(Date.from(expiresAt))
                        .sign(algorithm)

                val result = jwtProvider.verify(invalidToken)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.JWTError>()
                error.message shouldContain "Token verification failed"
            }
        }
    })
