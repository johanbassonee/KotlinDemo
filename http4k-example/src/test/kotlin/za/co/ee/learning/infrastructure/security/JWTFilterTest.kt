package za.co.ee.learning.infrastructure.security

import arrow.core.left
import arrow.core.right
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.security.JWTProvider
import java.util.UUID

class JWTFilterTest :
    FunSpec({
        val jwtProvider = mockk<JWTProvider>()
        val jwtFilter = JWTFilter(jwtProvider)

        val validToken = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val nextHandler: (Request) -> Response = { Response(Status.OK).body("Success") }

        context("valid bearer token") {
            test("should pass through to next handler when token is valid") {
                val request =
                    Request(Method.GET, "/api/protected")
                        .header("Authorization", "Bearer $validToken")

                every { jwtProvider.verify(validToken) } returns userId.right()

                val handler = jwtFilter(nextHandler)
                val response = handler(request)

                response.status shouldBe Status.OK
                response.bodyString() shouldBe "Success"
                verify { jwtProvider.verify(validToken) }
            }

            test("should set authenticated user ID in request context") {
                val request =
                    Request(Method.GET, "/api/protected")
                        .header("Authorization", "Bearer $validToken")

                every { jwtProvider.verify(validToken) } returns userId.right()

                var capturedRequest: Request? = null
                val capturingHandler: (Request) -> Response = { req ->
                    capturedRequest = req
                    Response(Status.OK)
                }

                val handler = jwtFilter(capturingHandler)
                handler(request)

                capturedRequest?.requireAuthenticatedUser() shouldBe userId
                verify { jwtProvider.verify(validToken) }
            }
        }

        context("invalid bearer token") {
            test("should return error response when token verification fails") {
                val request =
                    Request(Method.GET, "/api/protected")
                        .header("Authorization", "Bearer invalid.token")

                val jwtError = DomainError.JWTError("Invalid token")
                every { jwtProvider.verify("invalid.token") } returns jwtError.left()

                val handler = jwtFilter(nextHandler)
                val response = handler(request)

                response.status shouldBe Status.UNAUTHORIZED
                verify { jwtProvider.verify("invalid.token") }
            }

            test("should not call next handler when token is invalid") {
                val request =
                    Request(Method.GET, "/api/protected")
                        .header("Authorization", "Bearer invalid.token")

                val jwtError = DomainError.JWTError("Invalid token")
                every { jwtProvider.verify("invalid.token") } returns jwtError.left()

                var nextHandlerCalled = false
                val trackingHandler: (Request) -> Response = {
                    nextHandlerCalled = true
                    Response(Status.OK)
                }

                val handler = jwtFilter(trackingHandler)
                handler(request)

                nextHandlerCalled shouldBe false
                verify { jwtProvider.verify("invalid.token") }
            }
        }

        context("missing bearer token") {
            test("should return error response when Authorization header is missing") {
                val request = Request(Method.GET, "/api/protected")

                val jwtError =
                    DomainError.JWTError(
                        "Invalid or expired token: The Token's Signature resulted invalid when verified using the Algorithm: HMacSHA256",
                    )
                every { jwtProvider.verify("") } returns jwtError.left()

                val handler = jwtFilter(nextHandler)
                val response = handler(request)

                response.status shouldBe Status.UNAUTHORIZED
                verify { jwtProvider.verify("") }
            }

            test("should use empty string when bearer token is missing") {
                val request = Request(Method.GET, "/api/protected")

                val jwtError = DomainError.JWTError("Token required")
                every { jwtProvider.verify("") } returns jwtError.left()

                val handler = jwtFilter(nextHandler)
                handler(request)

                verify { jwtProvider.verify("") }
            }

            test("should not call next handler when token is missing") {
                val request = Request(Method.GET, "/api/protected")

                val jwtError = DomainError.JWTError("Token required")
                every { jwtProvider.verify("") } returns jwtError.left()

                var nextHandlerCalled = false
                val trackingHandler: (Request) -> Response = {
                    nextHandlerCalled = true
                    Response(Status.OK)
                }

                val handler = jwtFilter(trackingHandler)
                handler(request)

                nextHandlerCalled shouldBe false
            }
        }

        context("expired token") {
            test("should return error response for expired token") {
                val expiredToken = "expired.jwt.token"
                val request =
                    Request(Method.GET, "/api/protected")
                        .header("Authorization", "Bearer $expiredToken")

                val jwtError = DomainError.JWTError("Invalid or expired token: Token expired")
                every { jwtProvider.verify(expiredToken) } returns jwtError.left()

                val handler = jwtFilter(nextHandler)
                val response = handler(request)

                response.status shouldBe Status.UNAUTHORIZED
                verify { jwtProvider.verify(expiredToken) }
            }
        }
    })
