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
import za.co.ee.learning.domain.security.JWTProvider
import za.co.ee.learning.domain.security.PasswordProvider
import za.co.ee.learning.domain.users.UserRepository
import za.co.ee.learning.domain.users.usecases.Authenticate
import za.co.ee.learning.domain.users.usecases.AuthenticateRequest
import za.co.ee.learning.domain.users.usecases.AuthenticateResponse
import za.co.ee.learning.infrastructure.createErrorResponse

class AuthenticateEndpoint(
    userRepository: UserRepository,
    passwordProvider: PasswordProvider,
    jwtProvider: JWTProvider,
) {
    private val authenticate = Authenticate(userRepository, passwordProvider, jwtProvider)
    private val requestLens = Body.auto<AuthenticateRequest>().toLens()
    private val responseLens = Body.auto<AuthenticateResponse>().toLens()

    private val authRequestLens = Body.auto<AuthenticateRequest>().toLens()
    private val authResponseLens = Body.auto<AuthenticateResponse>().toLens()

    val handler: (Request) -> Response = { request: Request ->
        val authRequest = requestLens(request)
        when (val result = authenticate(authRequest)) {
            is Either.Left -> createErrorResponse(result.value)
            is Either.Right -> Response(Status.OK).with(responseLens of result.value)
        }
    }

    val route: ContractRoute =
        "/api/v1/authenticate" meta {
            summary = "Authenticate User"
            description = "Authenticates a user with email and password, returns JWT token"
            receiving(
                authRequestLens to
                    AuthenticateRequest(
                        email = "admin@local.com",
                        password = "password123",
                    ),
            )
            returning(
                Status.OK,
                authResponseLens to
                    AuthenticateResponse(
                        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        expires = 1234567890L,
                    ),
                "Successfully authenticated",
            )
            returning(Status.BAD_REQUEST to "Invalid credentials or validation error")
        } bindContract Method.POST to handler
}
