package za.co.ee.learning.domain.users.usecases

import arrow.core.Option
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.DomainResult
import za.co.ee.learning.domain.Validation
import za.co.ee.learning.domain.security.JWTProvider
import za.co.ee.learning.domain.security.PasswordProvider
import za.co.ee.learning.domain.security.TokenInfo
import za.co.ee.learning.domain.users.User
import za.co.ee.learning.domain.users.UserRepository

data class AuthenticateRequest(
    val email: String,
    val password: String,
)

data class AuthenticateResponse(
    val token: String,
    val expires: Long,
)

class Authenticate(
    val userRepository: UserRepository,
    val passwordProvider: PasswordProvider,
    val jwtProvider: JWTProvider,
) {
    operator fun invoke(request: AuthenticateRequest): DomainResult<AuthenticateResponse> =
        either {
            val validatedRequest = validate(request).bind()
            val user = findUser(validatedRequest.email).bind()
            val authenticatedUser = checkPassword(user, validatedRequest).bind()
            createToken(authenticatedUser).bind()
        }

    private fun validate(request: AuthenticateRequest): DomainResult<AuthenticateRequest> {
        if (request.email.isEmpty()) {
            return DomainError.ValidationError("Email cannot be empty").left()
        }

        if (request.password.isEmpty()) {
            return DomainError.ValidationError("Password cannot be empty").left()
        }

        if (!Validation.isEmailAddress(request.email)) {
            return DomainError.ValidationError("Invalid email format").left()
        }

        if (!Validation.isValidPasswordLength(request.password)) {
            return DomainError.ValidationError("Password must be at least 8 characters").left()
        }

        return request.right()
    }

    private fun findUser(email: String): DomainResult<User> =
        either {
            val optUser: Option<User> = userRepository.findByEmail(email).bind()
            return optUser.fold(
                ifEmpty = { DomainError.InvalidCredentials.left() },
                ifSome = { user -> user.right() },
            )
        }

    private fun checkPassword(
        user: User,
        validatedRequest: AuthenticateRequest,
    ): DomainResult<User> {
        if (passwordProvider.matches(validatedRequest.password, user.passwordHash)) {
            return user.right()
        }

        return DomainError.InvalidCredentials.left()
    }

    private fun createToken(authenticatedUser: User): DomainResult<AuthenticateResponse> {
        val tokenInfo: TokenInfo = jwtProvider.generate(authenticatedUser)
        return AuthenticateResponse(
            token = tokenInfo.token,
            expires = tokenInfo.expires,
        ).right()
    }
}
