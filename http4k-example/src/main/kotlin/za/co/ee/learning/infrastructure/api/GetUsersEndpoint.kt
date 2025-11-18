package za.co.ee.learning.infrastructure.api

import arrow.core.Either
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import za.co.ee.learning.domain.users.UserRepository
import za.co.ee.learning.domain.users.usecases.GetUsers
import za.co.ee.learning.domain.users.usecases.UserDto
import za.co.ee.learning.infrastructure.createErrorResponse

class GetUsersEndpoint(
    userRepository: UserRepository,
) {
    private val usersLens = Body.auto<List<UserDto>>().toLens()
    private val getUsers = GetUsers(userRepository)

    private val usersResponseLens = Body.auto<List<UserDto>>().toLens()

    val handler: (Request) -> Response = { _: Request ->
        when (val result = getUsers()) {
            is Either.Left -> createErrorResponse(result.value)
            is Either.Right -> Response(Status.OK).with(usersLens of result.value)
        }
    }

    val route: ContractRoute =
        "/api/v1/users" meta {
            summary = "Get Users"
            description = "Retrieves list of all users (requires JWT authentication)"
            returning(
                Status.OK,
                usersResponseLens to
                    listOf(
                        UserDto(
                            id = "550e8400-e29b-41d4-a716-446655440000",
                            email = "user@example.com",
                        ),
                    ),
                "Successfully retrieved users",
            )
            returning(Status.UNAUTHORIZED to "Missing or invalid JWT token")
        } bindContract Method.GET to handler
}
