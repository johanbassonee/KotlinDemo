package za.co.ee.learning.infrastructure.database

import arrow.core.Option
import arrow.core.right
import za.co.ee.learning.domain.DomainResult
import za.co.ee.learning.domain.users.User
import za.co.ee.learning.domain.users.UserRepository
import za.co.ee.learning.infrastructure.security.BCryptPasswordProvider

class InMemoryUserRepository : UserRepository {
    private val users = mutableListOf<User>()

    init {
        val passwordProvider = BCryptPasswordProvider()
        users.add(User(java.util.UUID.randomUUID(), "admin@local.com", passwordProvider.encode("password123")))
    }

    // Find the first user in the list that has the matching email, wrap it in an option and return a Either.right()
    override fun findByEmail(email: String): DomainResult<Option<User>> =
        Option
            .fromNullable(users.firstOrNull { it.email == email })
            .right()

    override fun findAll(): DomainResult<List<User>> = users.toList().right()
}
