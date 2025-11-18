package za.co.ee.learning.domain

import java.util.regex.Pattern

object Validation {
    private const val MIN_PASSWORD_LENGTH = 8

    private val EMAIL_REGEX =
        Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@" +
                "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" +
                "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\." +
                "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" +
                "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|" +
                "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$",
        )

    fun isEmailAddress(value: String): Boolean = EMAIL_REGEX.toRegex().matches(value)

    fun isValidPasswordLength(pwd: String): Boolean = pwd.trim().length >= MIN_PASSWORD_LENGTH
}
