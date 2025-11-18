package za.co.ee.learning.domain.users.usecases

import arrow.core.raise.either
import za.co.ee.learning.domain.DomainResult
import za.co.ee.learning.domain.users.UserRepository

data class UserDto(
    val id: String,
    val email: String,
)

class GetUsers(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): DomainResult<List<UserDto>> =
        either {
            val users = userRepository.findAll().bind()
            users.map { UserDto(it.id.toString(), it.email) }
        }
}
