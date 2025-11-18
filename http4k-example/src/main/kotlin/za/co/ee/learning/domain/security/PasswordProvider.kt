package za.co.ee.learning.domain.security

interface PasswordProvider {
    fun encode(password: String): String

    fun matches(
        password: String,
        encodedPassword: String,
    ): Boolean
}
