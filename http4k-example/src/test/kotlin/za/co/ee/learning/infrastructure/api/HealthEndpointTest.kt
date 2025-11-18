package za.co.ee.learning.infrastructure.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status

class HealthEndpointTest :
    FunSpec({

        context("health check") {
            test("should return 200 OK status") {
                val request = Request(Method.GET, "/health")

                val response = HealthEndpoint.handler(request)

                response.status shouldBe Status.OK
            }

            test("should return status UP") {
                val request = Request(Method.GET, "/health")

                val response = HealthEndpoint.handler(request)

                response.bodyString() shouldContain "\"status\":\"UP\""
            }

            test("should return service name") {
                val request = Request(Method.GET, "/health")

                val response = HealthEndpoint.handler(request)

                response.bodyString() shouldContain "\"service\":\"example-api\""
            }

            test("should return valid JSON response") {
                val request = Request(Method.GET, "/health")

                val response = HealthEndpoint.handler(request)

                val body = response.bodyString()
                body shouldContain "\"status\":"
                body shouldContain "\"service\":"
            }

            test("should handle any HTTP method") {
                val methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE)

                methods.forEach { method ->
                    val request = Request(method, "/health")
                    val response = HealthEndpoint.handler(request)
                    response.status shouldBe Status.OK
                }
            }
        }
    })
