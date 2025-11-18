package za.co.ee.learning.integration

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue

class EndToEndFlowIntegrationTest : BaseIntegrationTest() {
    init {
        context("End-to-End Authentication Flow") {
            test("should complete full flow: login → access protected resource") {
                // Step 1: Authenticate and get token
                val authResponse =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .`when`()
                        .post("/api/v1/authenticate")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val token = authResponse.jsonPath().getString("token")
                token.shouldNotBeBlank()

                // Step 2: Use token to access protected endpoint
                val usersResponse =
                    given()
                        .header("Authorization", "Bearer $token")
                        .`when`()
                        .get("/api/v1/users")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val users = usersResponse.jsonPath().getList<Any>("$")
                users.size shouldBeGreaterThan 0
            }

            test("should fail to access protected resource without authentication first") {
                // Try to access protected endpoint without authenticating
                given()
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
            }

            test("should allow multiple protected requests after single login") {
                // Login once
                val authResponse =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .`when`()
                        .post("/api/v1/authenticate")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val token = authResponse.jsonPath().getString("token")

                // Make multiple requests with the same token
                repeat(5) { _ ->
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
                    users.size shouldBeGreaterThan 0
                }
            }

            test("should reject access after login with wrong credentials") {
                // Try to login with wrong password
                given()
                    .body("""{"email":"admin@local.com","password":"wrongpassword"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)

                // Should not be able to access protected endpoint
                given()
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
            }
        }

        context("CORS Headers") {
            test("should include CORS headers in response") {
                val response =
                    given()
                        .header("Origin", "http://example.com")
                        .`when`()
                        .get("/health")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                // Check for CORS headers
                val accessControlAllowOrigin = response.header("access-control-allow-origin")
                accessControlAllowOrigin shouldNotBe null
            }

            test("should include CORS headers on authenticated endpoints") {
                val authResponse =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .`when`()
                        .post("/api/v1/authenticate")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val token = authResponse.jsonPath().getString("token")

                val response =
                    given()
                        .header("Authorization", "Bearer $token")
                        .header("Origin", "http://example.com")
                        .`when`()
                        .get("/api/v1/users")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val accessControlAllowOrigin = response.header("access-control-allow-origin")
                accessControlAllowOrigin shouldNotBe null
            }
        }

        context("API Response Format Consistency") {
            test("successful responses should return JSON") {
                given()
                    .`when`()
                    .get("/health")
                    .then()
                    .statusCode(200)
                    .contentType(containsString("application/json"))
            }

            test("error responses should return JSON with problem format") {
                given()
                    .body("""{"email":"","password":"password123"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .contentType(containsString("application/json"))
                    .body("title", notNullValue())
                    .body("status", notNullValue())
                    .body("description", notNullValue())
            }

            test("unauthorized responses should return JSON with problem format") {
                given()
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(401)
                    .contentType(containsString("application/json"))
                    .body("title", equalTo("JWT token error"))
                    .body("status", equalTo(401))
            }
        }

        context("Different Scenarios") {
            test("should handle user checking health before and after authentication") {
                // Check health (no auth needed)
                given()
                    .`when`()
                    .get("/health")
                    .then()
                    .statusCode(200)

                // Authenticate
                val authResponse =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .`when`()
                        .post("/api/v1/authenticate")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val token = authResponse.jsonPath().getString("token")

                // Check health again (still no auth needed)
                given()
                    .`when`()
                    .get("/health")
                    .then()
                    .statusCode(200)

                // Access protected resource
                given()
                    .header("Authorization", "Bearer $token")
                    .`when`()
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)
            }

            test("should maintain consistent behavior across multiple user sessions") {
                // Session 1
                val token1 =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .post("/api/v1/authenticate")
                        .jsonPath()
                        .getString("token")

                given()
                    .header("Authorization", "Bearer $token1")
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)

                // Session 2 (independent)
                val token2 =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .post("/api/v1/authenticate")
                        .jsonPath()
                        .getString("token")

                given()
                    .header("Authorization", "Bearer $token2")
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)

                // Both tokens should still work
                given()
                    .header("Authorization", "Bearer $token1")
                    .get("/api/v1/users")
                    .then()
                    .statusCode(200)
            }
        }
    }
}
