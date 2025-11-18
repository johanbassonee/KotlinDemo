package za.co.ee.learning.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidationTest :
    FunSpec({

        context("isEmailAddress") {
            test("should return true for valid email addresses") {
                Validation.isEmailAddress("user@example.com") shouldBe true
                Validation.isEmailAddress("test.user@example.com") shouldBe true
                Validation.isEmailAddress("user_name@example.co.uk") shouldBe true
                Validation.isEmailAddress("first.last@example-domain.com") shouldBe true
            }

            test("should return false for invalid email addresses") {
                Validation.isEmailAddress("invalid") shouldBe false
                Validation.isEmailAddress("@example.com") shouldBe false
                Validation.isEmailAddress("user@") shouldBe false
                Validation.isEmailAddress("user @example.com") shouldBe false
                Validation.isEmailAddress("user@.com") shouldBe false
                Validation.isEmailAddress("") shouldBe false
            }

            test("should return false for email without domain") {
                Validation.isEmailAddress("user@") shouldBe false
            }

            test("should return false for email without @") {
                Validation.isEmailAddress("userexample.com") shouldBe false
            }
        }

        context("isValidPasswordLength") {
            test("should return true for passwords longer than 8 characters") {
                Validation.isValidPasswordLength("password123") shouldBe true
                Validation.isValidPasswordLength("verylongpassword") shouldBe true
                Validation.isValidPasswordLength("123456789") shouldBe true
            }

            test("should return false for passwords with 7 or fewer characters") {
                Validation.isValidPasswordLength("short") shouldBe false
                Validation.isValidPasswordLength("1234567") shouldBe false
                Validation.isValidPasswordLength("") shouldBe false
            }

            test("should trim whitespace when checking password length") {
                Validation.isValidPasswordLength("  1234567  ") shouldBe false
                Validation.isValidPasswordLength("  12345678  ") shouldBe true
            }
        }
    })
