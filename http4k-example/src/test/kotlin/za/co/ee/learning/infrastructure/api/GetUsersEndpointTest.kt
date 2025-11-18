package za.co.ee.learning.infrastructure.api

import arrow.core.left
import arrow.core.right
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.users.User
import za.co.ee.learning.domain.users.UserRepository
import java.util.UUID

class GetUsersEndpointTest :
    FunSpec({
        val userRepository = mockk<UserRepository>()
        val endpoint = GetUsersEndpoint(userRepository)

        val user1 =
            User(
                id = UUID.randomUUID(),
                email = "user1@example.com",
                passwordHash = "hash1",
            )
        val user2 =
            User(
                id = UUID.randomUUID(),
                email = "user2@example.com",
                passwordHash = "hash2",
            )

        beforeTest {
            clearAllMocks()
        }

        context("successful retrieval") {
            test("should return 200 OK with list of users") {
                val users = listOf(user1, user2)
                every { userRepository.findAll() } returns users.right()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
                response.bodyString() shouldContain user1.email
                response.bodyString() shouldContain user2.email
                response.bodyString() shouldContain user1.id.toString()
                response.bodyString() shouldContain user2.id.toString()

                verify { userRepository.findAll() }
            }

            test("should return 200 OK with empty array when no users exist") {
                every { userRepository.findAll() } returns emptyList<User>().right()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
                response.bodyString() shouldBe "[]"

                verify { userRepository.findAll() }
            }

            test("should return valid JSON array") {
                val users = listOf(user1)
                every { userRepository.findAll() } returns users.right()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
                val body = response.bodyString()
                body shouldContain "["
                body shouldContain "]"
                body shouldContain "\"id\":"
                body shouldContain "\"email\":"
            }

            test("should not include password hashes in response") {
                val users = listOf(user1, user2)
                every { userRepository.findAll() } returns users.right()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
                response.bodyString() shouldContain user1.email
                response.bodyString() shouldContain user2.email
                // Password hashes should not be in the response
                response.bodyString() shouldNotContain "hash1"
                response.bodyString() shouldNotContain "hash2"
            }
        }

        context("repository errors") {
            test("should return 400 Bad Request when repository fails") {
                val error = DomainError.ValidationError("Database error")
                every { userRepository.findAll() } returns error.left()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                response.bodyString() shouldContain "Database error"

                verify { userRepository.findAll() }
            }

            test("should return proper error response structure") {
                val error = DomainError.ValidationError("Connection failed")
                every { userRepository.findAll() } returns error.left()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.BAD_REQUEST
                val body = response.bodyString()
                body shouldContain "\"title\":"
                body shouldContain "\"status\":"
                body shouldContain "\"description\":"
            }
        }

        context("multiple users") {
            test("should return all users from repository") {
                val users = listOf(user1, user2)
                every { userRepository.findAll() } returns users.right()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
                // Check that both users are in the response
                response.bodyString() shouldContain user1.id.toString()
                response.bodyString() shouldContain user2.id.toString()

                verify { userRepository.findAll() }
            }

            test("should handle large number of users") {
                val manyUsers =
                    (1..100).map { index ->
                        User(
                            id = UUID.randomUUID(),
                            email = "user$index@example.com",
                            passwordHash = "hash$index",
                        )
                    }
                every { userRepository.findAll() } returns manyUsers.right()

                val request = Request(Method.GET, "/api/users")
                val response = endpoint.handler(request)

                response.status shouldBe Status.OK
                // Should be a valid JSON array
                response.bodyString() shouldContain "["
                response.bodyString() shouldContain "]"

                verify { userRepository.findAll() }
            }
        }
    })
