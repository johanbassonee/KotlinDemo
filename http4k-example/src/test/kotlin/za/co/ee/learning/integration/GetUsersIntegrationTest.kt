package za.co.ee.learning.integration

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.notNullValue
import java.time.Instant
import java.util.Date
import java.util.UUID

class GetUsersIntegrationTest : BaseIntegrationTest() {
    private fun getValidToken(): String {
        val response =
            given()
                .body("""{"email":"admin@local.com","password":"password123"}""")
                .`when`()
                .post("/api/v1/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .response()

        return response.jsonPath().getString("token")
    }

    init {
        context("GET /api/v1/users - Success Cases") {
            test("should return 200 OK with valid JWT token") {
                val token = getValidToken()

                given()
                    .header("Authorization", "Bearer $token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)
            }

            test("should return list of users") {
                val token = getValidToken()

                val response =
                    given()
                        .header("Authorization", "Bearer $token")
                        .`when`()
                        .get("/api/v1/users")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val users = response.jsonPath().getList<Any>("$")
                users.shouldNotBeEmpty()
                users.size shouldBeGreaterThan 0
            }

            test("should return users with id and email fields") {
                val token = getValidToken()

                given()
                    .header("Authorization", "Bearer $token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)
                    .body("[0].id", notNullValue())
                    .body("[0].email", notNullValue())
            }

            test("should include admin user in response") {
                val token = getValidToken()

                given()
                    .header("Authorization", "Bearer $token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)
                    .body("email", hasItem("admin@local.com"))
            }

            test("should NOT expose password hashes") {
                val token = getValidToken()

                val response =
                    given()
                        .header("Authorization", "Bearer $token")
                        .`when`()
                        .get("/api/v1/users")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val responseBody = response.asString()
                responseBody shouldNotContain "passwordHash"
                responseBody shouldNotContain "password123"
            }

            test("should return JSON array") {
                val token = getValidToken()

                val response =
                    given()
                        .header("Authorization", "Bearer $token")
                        .`when`()
                        .get("/api/v1/users")
                        .then()
                        .statusCode(200)
                        .contentType(containsString("application/json"))
                        .extract()
                        .response()

                val responseBody = response.asString()
                responseBody.trim().shouldStartWith("[")
            }
        }

        context("GET /api/v1/users - Authentication Failures") {
            test("should return 401 when Authorization header is missing") {
                given()
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
            }

            test("should return 401 when Bearer token is missing") {
                given()
                    .header("Authorization", "")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
            }

            test("should return 401 for invalid JWT token") {
                given()
                    .header("Authorization", "Bearer invalid.jwt.token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
                    .body("title", equalTo("JWT token error"))
            }

            test("should return 401 for malformed JWT token") {
                given()
                    .header("Authorization", "Bearer notajwttoken")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
            }

            test("should return 401 for expired JWT token") {
                // Create an expired token
                val now = Instant.now()
                val expiredTime = now.minusSeconds(3600) // 1 hour ago
                val algorithm = Algorithm.HMAC256("test-secret")

                val expiredToken =
                    JWT
                        .create()
                        .withIssuer("http4k-test")
                        .withSubject(UUID.randomUUID().toString())
                        .withIssuedAt(Date.from(now.minusSeconds(7200)))
                        .withExpiresAt(Date.from(expiredTime))
                        .sign(algorithm)

                given()
                    .header("Authorization", "Bearer $expiredToken")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
                    .body("title", equalTo("JWT token error"))
                    .body("description", containsString("Invalid or expired token"))
            }

            test("should return 401 for JWT with wrong signature") {
                val algorithm = Algorithm.HMAC256("wrong-secret")
                val now = Instant.now()
                val expiresAt = now.plusSeconds(3600)

                val wrongSignatureToken =
                    JWT
                        .create()
                        .withIssuer("http4k-test")
                        .withSubject(UUID.randomUUID().toString())
                        .withIssuedAt(Date.from(now))
                        .withExpiresAt(Date.from(expiresAt))
                        .sign(algorithm)

                given()
                    .header("Authorization", "Bearer $wrongSignatureToken")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
            }

            test("should return 401 for JWT with wrong issuer") {
                val algorithm = Algorithm.HMAC256("test-secret")
                val now = Instant.now()
                val expiresAt = now.plusSeconds(3600)

                val wrongIssuerToken =
                    JWT
                        .create()
                        .withIssuer("wrong-issuer")
                        .withSubject(UUID.randomUUID().toString())
                        .withIssuedAt(Date.from(now))
                        .withExpiresAt(Date.from(expiresAt))
                        .sign(algorithm)

                given()
                    .header("Authorization", "Bearer $wrongIssuerToken")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
            }
        }

        context("GET /api/v1/users - Token Reuse") {
            test("should allow multiple requests with same valid token") {
                val token = getValidToken()

                // First request
                given()
                    .header("Authorization", "Bearer $token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)

                // Second request with same token
                given()
                    .header("Authorization", "Bearer $token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)

                // Third request with same token
                given()
                    .header("Authorization", "Bearer $token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)
            }
        }
    }
}
