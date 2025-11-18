package za.co.ee.learning.infrastructure.server

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ContentTypeFilter(
    private val acceptableContentTypes: Set<String>,
    private val excludedPaths: Set<String> = emptySet(),
    private val excludedMethods: Set<Method> = setOf(Method.GET, Method.DELETE, Method.HEAD, Method.OPTIONS),
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler =
        { request ->
            when {
                shouldSkipValidation(request) -> next(request)
                else -> validateContentType(request, next)
            }
        }

    private fun shouldSkipValidation(request: Request): Boolean =
        excludedPaths.contains(request.uri.path) || excludedMethods.contains(request.method)

    private fun validateContentType(
        request: Request,
        next: HttpHandler,
    ): Response {
        val contentType = request.header("Content-Type")
        return if (contentType != null && acceptableContentTypes.none { contentType.startsWith(it) }) {
            Response
                .Companion(
                    Status.BAD_REQUEST,
                ).body("Content-Type must be one of: ${acceptableContentTypes.joinToString(", ")}")
        } else {
            next(request)
        }
    }
}
