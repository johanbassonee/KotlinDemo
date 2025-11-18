package za.co.ee.learning.domain

import arrow.core.Either
import org.http4k.core.Status

typealias DomainResult<T> = Either<DomainError, T>

data class Problem(
    val title: String,
    val status: Int,
    val description: String,
    val invalidParams: Map<String, String> = emptyMap(),
)

sealed interface DomainError {
    fun toProblem(): Problem

    data class ValidationError(
        val message: String,
    ) : DomainError {
        override fun toProblem(): Problem =
            Problem(
                title = "Validation Error",
                status = Status.BAD_REQUEST.code,
                description = message,
            )
    }

    data object InvalidCredentials : DomainError {
        override fun toProblem(): Problem =
            Problem(
                title = "Invalid credentials",
                status = Status.BAD_REQUEST.code,
                description = "Invalid email or password specified",
            )
    }

    data class JWTError(
        val message: String,
    ) : DomainError {
        override fun toProblem(): Problem =
            Problem(
                title = "JWT token error",
                status = Status.UNAUTHORIZED.code,
                description = message,
            )
    }
}
