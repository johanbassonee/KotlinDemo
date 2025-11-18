package za.co.ee.learning.infrastructure.security

import org.mindrot.jbcrypt.BCrypt
import za.co.ee.learning.domain.security.PasswordProvider

class BCryptPasswordProvider : PasswordProvider {
    override fun encode(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

    override fun matches(
        password: String,
        encodedPassword: String,
    ): Boolean = BCrypt.checkpw(password, encodedPassword)
}
