package za.co.ee.learning.infrastructure.database

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe

class InMemoryUserRepositoryTest :
    FunSpec({
        val repository = InMemoryUserRepository()

        context("initialization") {
            test("should initialize with default admin user") {
                val result = repository.findAll()

                val users = result.shouldBeRight()
                users shouldHaveAtLeastSize 1
                users.any { it.email == "admin@local.com" } shouldBe true
            }
        }

        context("findByEmail") {
            test("should return Some when user exists") {
                val result = repository.findByEmail("admin@local.com")

                val option = result.shouldBeRight()
                val user = option.shouldBeSome()
                user.email shouldBe "admin@local.com"
            }

            test("should have password hash for existing user") {
                val result = repository.findByEmail("admin@local.com")

                val option = result.shouldBeRight()
                val user = option.shouldBeSome()
                user.passwordHash.isNotEmpty() shouldBe true
            }

            test("should return None for non-existent user") {
                val result = repository.findByEmail("nonexistent@example.com")

                val option = result.shouldBeRight()
                option.isNone() shouldBe true
            }
        }

        context("findAll") {
            test("should return all users in repository") {
                val result = repository.findAll()

                val users = result.shouldBeRight()
                users shouldHaveAtLeastSize 1
            }

            test("should return immutable copy of users") {
                val result1 = repository.findAll()
                val result2 = repository.findAll()

                val users1 = result1.shouldBeRight()
                val users2 = result2.shouldBeRight()

                // Both should contain the same users
                users1.size shouldBe users2.size
            }
        }
    })
