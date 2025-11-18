package za.co.ee.learning.infrastructure.security

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.bearerToken
import za.co.ee.learning.domain.security.JWTProvider
import za.co.ee.learning.infrastructure.createErrorResponse

class JWTFilter(
    private val jwtProvider: JWTProvider,
    private val excludedPaths: Set<String> = emptySet(),
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler =
        { request ->
            val path = request.uri.path
            when {
                isPathExcluded(path) -> next(request)
                else -> verifyToken(request, next)
            }
        }

    private fun isPathExcluded(path: String): Boolean =
        excludedPaths.any { excludedPath ->
            when {
                excludedPath.endsWith("/*") -> path.startsWith(excludedPath.removeSuffix("/*"))
                else -> path == excludedPath
            }
        }

    private fun verifyToken(
        request: Request,
        next: HttpHandler,
    ): Response {
        val token = request.bearerToken() ?: ""
        return jwtProvider
            .verify(token)
            .fold(
                ifLeft = { error ->
                    createErrorResponse(error)
                },
                ifRight = { id ->
                    val authenticatedRequest = request.withAuthenticatedUser(id)
                    next(authenticatedRequest)
                },
            )
    }
}
