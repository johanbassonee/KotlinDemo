package za.co.ee.learning.infrastructure.api

import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.security.JWTProvider
import za.co.ee.learning.domain.security.PasswordProvider
import za.co.ee.learning.domain.security.TokenInfo
import za.co.ee.learning.domain.users.User
import za.co.ee.learning.domain.users.UserRepository
import java.util.UUID

class AuthenticateEndpointTest :
    FunSpec({
        val userRepository = mockk<UserRepository>()
        val passwordProvider = mockk<PasswordProvider>()
        val jwtProvider = mockk<JWTProvider>()

        val endpoint = AuthenticateEndpoint(userRepository, passwordProvider, jwtProvider)

        val validEmail = "user@example.com"
        val validPassword = "SecurePass123"
        val passwordHash = "hashed_password"
        val testUser =
            User(
                id = UUID.randomUUID(),
                email = validEmail,
                passwordHash = passwordHash,
            )
        val tokenInfo =
            TokenInfo(
                token = "jwt.token.here",
                expires = 1234567890L,
            )

        beforeTest {
            clearAllMocks()
        }

        context("successful authentication") {
            test("should return 200 OK with token when credentials are valid") {
                val requestBody = """{"email":"$validEmail","password":"$validPassword"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                every { userRepository.findByEmail(validEmail) } returns testUser.some().right()
                every { passwordProvider.matches(validPassword, passwordHash) } returns true
                every { jwtProvider.generate(testUser) } returns tokenInfo

                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
                response.bodyString() shouldContain "\"token\":\"${tokenInfo.token}\""
                response.bodyString() shouldContain "\"expires\":${tokenInfo.expires}"

                verify { userRepository.findByEmail(validEmail) }
                verify { passwordProvider.matches(validPassword, passwordHash) }
                verify { jwtProvider.generate(testUser) }
            }
        }

        context("validation errors") {
            test("should return 400 Bad Request for empty email") {
                val requestBody = """{"email":"","password":"$validPassword"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Email cannot be empty"
            }

            test("should return 400 Bad Request for invalid email format") {
                val requestBody = """{"email":"invalid-email","password":"$validPassword"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Invalid email format"
            }

            test("should return 400 Bad Request for empty password") {
                val requestBody = """{"email":"$validEmail","password":""}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Password cannot be empty"
            }

            test("should return 400 Bad Request for password too short") {
                val requestBody = """{"email":"$validEmail","password":"short"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Password must be at least 8 characters"
            }
        }

        context("invalid credentials") {
            test("should return 400 Bad Request when user does not exist") {
                val requestBody = """{"email":"nonexistent@example.com","password":"$validPassword"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                every { userRepository.findByEmail("nonexistent@example.com") } returns arrow.core.none<User>().right()

                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Invalid email or password"

                verify { userRepository.findByEmail("nonexistent@example.com") }
            }

            test("should return 400 Bad Request when password does not match") {
                val requestBody = """{"email":"$validEmail","password":"WrongPassword123"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                every { userRepository.findByEmail(validEmail) } returns testUser.some().right()
                every { passwordProvider.matches("WrongPassword123", passwordHash) } returns false

                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Invalid email or password"

                verify { userRepository.findByEmail(validEmail) }
                verify { passwordProvider.matches("WrongPassword123", passwordHash) }
            }
        }

        context("repository errors") {
            test("should return 400 Bad Request when repository fails") {
                val requestBody = """{"email":"$validEmail","password":"$validPassword"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                val repositoryError = DomainError.ValidationError("Database connection failed")
                every { userRepository.findByEmail(validEmail) } returns repositoryError.left()

                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Database connection failed"

                verify { userRepository.findByEmail(validEmail) }
            }
        }

        context("request parsing") {
            test("should handle valid JSON request body") {
                val requestBody = """{"email":"$validEmail","password":"$validPassword"}"""
                val request = Request(Method.POST, "/auth/login").body(requestBody)

                every { userRepository.findByEmail(validEmail) } returns testUser.some().right()
                every { passwordProvider.matches(validPassword, passwordHash) } returns true
                every { jwtProvider.generate(testUser) } returns tokenInfo

                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
            }
        }
    })
