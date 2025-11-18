package za.co.ee.learning.infrastructure.api

import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto

data class HealthResponse(
    val status: String,
    val service: String,
)

object HealthEndpoint {
    private val healthLens = Body.auto<HealthResponse>().toLens()
    private val healthResponseLens = Body.auto<HealthResponse>().toLens()

    val handler: (Request) -> Response = { _: Request ->
        Response(Status.OK).with(
            healthLens of
                HealthResponse(
                    status = "UP",
                    service = "example-api",
                ),
        )
    }

    val healthRoute: ContractRoute =
        "/health" meta {
            summary = "Health Check"
            description = "Returns the health status of the API"
            returning(
                Status.OK,
                healthResponseLens to HealthResponse(status = "UP", service = "example-api"),
                "Successful health check",
            )
        } bindContract Method.GET to handler

    val livenessRoute: ContractRoute =
        "/health/live" meta {
            summary = "Liveness Probe"
            description = "Kubernetes liveness probe - returns 200 if service is running"
            returning(Status.OK, healthResponseLens to HealthResponse("UP", "http4k-api"))
        } bindContract Method.GET to { _: Request ->
            Response(Status.OK).with(healthLens of HealthResponse("UP", "http4k-api"))
        }

    val readinessRoute: ContractRoute =
        "/health/ready" meta {
            summary = "Readiness Probe"
            description = "Kubernetes readiness probe - returns 200 if service is ready to accept traffic"
            returning(Status.OK, healthResponseLens to HealthResponse("READY", "http4k-api"))
        } bindContract Method.GET to { _: Request ->
            // Could check database connection here in the future
            Response(Status.OK).with(healthLens of HealthResponse("READY", "http4k-api"))
        }
}
