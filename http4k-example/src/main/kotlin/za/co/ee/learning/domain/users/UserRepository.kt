package za.co.ee.learning.domain.users

import arrow.core.Option
import za.co.ee.learning.domain.DomainResult

interface UserRepository {
    fun findByEmail(email: String): DomainResult<Option<User>>

    fun findAll(): DomainResult<List<User>>
}
