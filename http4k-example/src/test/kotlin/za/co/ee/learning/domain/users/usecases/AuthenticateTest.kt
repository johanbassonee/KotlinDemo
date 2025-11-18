package za.co.ee.learning.domain.users.usecases

import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.security.JWTProvider
import za.co.ee.learning.domain.security.PasswordProvider
import za.co.ee.learning.domain.security.TokenInfo
import za.co.ee.learning.domain.users.User
import za.co.ee.learning.domain.users.UserRepository
import java.util.UUID

class AuthenticateTest :
    FunSpec({
        val userRepository = mockk<UserRepository>()
        val passwordProvider = mockk<PasswordProvider>()
        val jwtProvider = mockk<JWTProvider>()
        val authenticate = Authenticate(userRepository, passwordProvider, jwtProvider)

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
            io.mockk.clearAllMocks()
        }

        context("successful authentication") {
            test("should return token when credentials are valid") {
                val request = AuthenticateRequest(email = validEmail, password = validPassword)

                every { userRepository.findByEmail(validEmail) } returns testUser.some().right()
                every { passwordProvider.matches(validPassword, passwordHash) } returns true
                every { jwtProvider.generate(testUser) } returns tokenInfo

                val result = authenticate(request)

                val value = result.shouldBeRight()
                value shouldBe
                    AuthenticateResponse(
                        token = tokenInfo.token,
                        expires = tokenInfo.expires,
                    )

                verify { userRepository.findByEmail(validEmail) }
                verify { passwordProvider.matches(validPassword, passwordHash) }
                verify { jwtProvider.generate(testUser) }
            }
        }

        context("validation errors") {
            test("should return ValidationError when email is empty") {
                val request = AuthenticateRequest(email = "", password = validPassword)

                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.ValidationError>()
                error.message shouldBe "Email cannot be empty"
            }

            test("should return ValidationError when email format is invalid") {
                val request = AuthenticateRequest(email = "invalid-email", password = validPassword)
                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.ValidationError>()
                error.message shouldBe "Invalid email format"
            }

            test("should return ValidationError when password is empty") {
                val request = AuthenticateRequest(email = validEmail, password = "")

                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.ValidationError>()
                error.message shouldBe "Password cannot be empty"
            }

            test("should return ValidationError when password is too short") {
                val request = AuthenticateRequest(email = validEmail, password = "short")

                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<DomainError.ValidationError>()
                error.message shouldBe "Password must be at least 8 characters"
            }
        }

        context("user not found") {
            test("should return InvalidCredentials when user does not exist") {
                val request = AuthenticateRequest(email = "nonexistent@example.com", password = validPassword)

                every { userRepository.findByEmail("nonexistent@example.com") } returns arrow.core.none<User>().right()

                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error shouldBe DomainError.InvalidCredentials

                verify { userRepository.findByEmail("nonexistent@example.com") }
            }

            test("should return InvalidCredentials when repository returns None") {
                val request = AuthenticateRequest(email = validEmail, password = validPassword)

                every { userRepository.findByEmail(validEmail) } returns arrow.core.none<User>().right()

                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error shouldBe DomainError.InvalidCredentials
            }
        }

        context("invalid credentials") {
            test("should return InvalidCredentials when password does not match") {
                val request = AuthenticateRequest(email = validEmail, password = "WrongPassword123")

                every { userRepository.findByEmail(validEmail) } returns testUser.some().right()
                every { passwordProvider.matches("WrongPassword123", passwordHash) } returns false

                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error shouldBe DomainError.InvalidCredentials

                verify { userRepository.findByEmail(validEmail) }
                verify { passwordProvider.matches("WrongPassword123", passwordHash) }
            }
        }

        context("repository errors") {
            test("should propagate error when repository fails") {
                val request = AuthenticateRequest(email = validEmail, password = validPassword)
                val repositoryError = DomainError.ValidationError("Database connection failed")

                every { userRepository.findByEmail(validEmail) } returns repositoryError.left()

                val result = authenticate(request)

                val error = result.shouldBeLeft()
                error shouldBe repositoryError

                verify { userRepository.findByEmail(validEmail) }
            }
        }
    })
