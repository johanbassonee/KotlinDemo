package za.co.ee.learning.infrastructure

import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.auto
import za.co.ee.learning.domain.DomainError
import za.co.ee.learning.domain.Problem

private val problemLens = Body.auto<Problem>().toLens()

fun createErrorResponse(error: DomainError): Response {
    val problem = error.toProblem()
    val status = Status.fromCode(problem.status) ?: Status.INTERNAL_SERVER_ERROR
    return problemLens(error.toProblem(), Response(status))
}
