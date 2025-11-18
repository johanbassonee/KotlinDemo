package za.co.ee.learning.domain.users.usecases

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.users.User
import za.co.ee.learning.domain.users.UserRepository
import java.util.UUID

class GetUsersTest :
    FunSpec({
        val userRepository = mockk<UserRepository>()
        val getUsers = GetUsers(userRepository)

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
            test("should return list of user DTOs when users exist") {
                val users = listOf(user1, user2)
                every { userRepository.findAll() } returns users.right()

                val result = getUsers()

                val userDtos = result.shouldBeRight()
                userDtos shouldHaveSize 2
                userDtos[0].id shouldBe user1.id.toString()
                userDtos[0].email shouldBe user1.email
                userDtos[1].id shouldBe user2.id.toString()
                userDtos[1].email shouldBe user2.email

                verify { userRepository.findAll() }
            }

            test("should return empty list when no users exist") {
                every { userRepository.findAll() } returns emptyList<User>().right()

                val result = getUsers()

                val userDtos = result.shouldBeRight()
                userDtos.shouldBeEmpty()

                verify { userRepository.findAll() }
            }
        }

        context("repository errors") {
            test("should propagate error when repository fails") {
                val error = DomainError.ValidationError("Database error")
                every { userRepository.findAll() } returns error.left()

                val result = getUsers()

                val domainError = result.shouldBeLeft()
                domainError shouldBe error

                verify { userRepository.findAll() }
            }
        }

        context("DTO mapping") {
            test("should correctly map User to UserDto") {
                val users = listOf(user1)
                every { userRepository.findAll() } returns users.right()

                val result = getUsers()

                val userDtos = result.shouldBeRight()
                userDtos shouldHaveSize 1
                userDtos[0].id shouldBe user1.id.toString()
                userDtos[0].email shouldBe user1.email
            }

            test("should not include password hash in DTO") {
                val users = listOf(user1)
                every { userRepository.findAll() } returns users.right()

                val result = getUsers()

                val userDtos = result.shouldBeRight()
                // UserDto only has id and email fields, no passwordHash
                userDtos[0].toString() shouldBe "UserDto(id=${user1.id}, email=user1@example.com)"
            }
        }
    })
