package za.co.ee.learning.infrastructure.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith

class BCryptPasswordProviderTest :
    FunSpec({
        val passwordProvider = BCryptPasswordProvider()

        context("encode") {
            test("should generate a BCrypt hash for a password") {
                val password = "mySecurePassword123"

                val hash = passwordProvider.encode(password)

                hash shouldStartWith "\$2a\$"
                hash.length shouldBe 60
            }

            test("should generate different hashes for the same password") {
                val password = "samePassword"

                val hash1 = passwordProvider.encode(password)
                val hash2 = passwordProvider.encode(password)

                hash1 shouldNotBe hash2
            }

            test("should generate different hashes for different passwords") {
                val password1 = "password1"
                val password2 = "password2"

                val hash1 = passwordProvider.encode(password1)
                val hash2 = passwordProvider.encode(password2)

                hash1 shouldNotBe hash2
            }

            test("should handle empty password") {
                val password = ""

                val hash = passwordProvider.encode(password)

                hash shouldStartWith "\$2a\$"
            }
        }

        context("matches") {
            test("should return true when password matches hash") {
                val password = "correctPassword123"
                val hash = passwordProvider.encode(password)

                val result = passwordProvider.matches(password, hash)

                result shouldBe true
            }

            test("should return false when password does not match hash") {
                val correctPassword = "correctPassword123"
                val wrongPassword = "wrongPassword456"
                val hash = passwordProvider.encode(correctPassword)

                val result = passwordProvider.matches(wrongPassword, hash)

                result shouldBe false
            }

            test("should return false for empty password when hash is for non-empty password") {
                val password = "actualPassword"
                val hash = passwordProvider.encode(password)

                val result = passwordProvider.matches("", hash)

                result shouldBe false
            }

            test("should be case sensitive") {
                val password = "Password123"
                val hash = passwordProvider.encode(password)

                val result = passwordProvider.matches("password123", hash)

                result shouldBe false
            }

            test("should handle special characters in password") {
                val password = "P@ssw0rd!#\$%^&*()"
                val hash = passwordProvider.encode(password)

                val result = passwordProvider.matches(password, hash)

                result shouldBe true
            }
        }
    })
