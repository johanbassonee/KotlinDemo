package za.co.ee.learning.infrastructure.routes

import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.format.Jackson
import za.co.ee.learning.domain.security.JWTProvider
import za.co.ee.learning.domain.security.PasswordProvider
import za.co.ee.learning.domain.users.UserRepository
import za.co.ee.learning.infrastructure.api.AuthenticateEndpoint
import za.co.ee.learning.infrastructure.api.GetUsersEndpoint
import za.co.ee.learning.infrastructure.api.HealthEndpoint

class ContractRoutes(
    userRepository: UserRepository,
    passwordProvider: PasswordProvider,
    jwtProvider: JWTProvider,
) {
    private val authenticateEndpoint = AuthenticateEndpoint(userRepository, passwordProvider, jwtProvider)
    private val getUsersEndpoint = GetUsersEndpoint(userRepository)

    val routes =
        contract {
            renderer =
                OpenApi3(
                    apiInfo =
                        ApiInfo(
                            title = "http4k Example API",
                            version = "1.0.0",
                            description = "Example API demonstrating http4k with JWT authentication",
                        ),
                    json = Jackson,
                )
            descriptionPath = "/openapi.json"

            routes += HealthEndpoint.healthRoute
            routes += HealthEndpoint.readinessRoute
            routes += HealthEndpoint.livenessRoute
            routes += authenticateEndpoint.route
            routes += getUsersEndpoint.route
        }
}
