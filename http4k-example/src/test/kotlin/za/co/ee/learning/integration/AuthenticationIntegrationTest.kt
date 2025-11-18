package za.co.ee.learning.integration

import com.auth0.jwt.JWT
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue

class AuthenticationIntegrationTest : BaseIntegrationTest() {
    init {
        context("POST /api/v1/authenticate - Success Cases") {
            test("should return 200 OK with valid credentials") {
                given()
                    .body("""{"email":"admin@local.com","password":"password123"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(200)
            }

            test("should return JWT token and expires field") {
                val response =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .`when`()
                        .post("/api/v1/authenticate")
                        .then()
                        .statusCode(200)
                        .body("token", notNullValue())
                        .body("expires", notNullValue())
                        .extract()
                        .response()

                val token = response.jsonPath().getString("token")
                val expires = response.jsonPath().getLong("expires")

                token.shouldNotBeBlank()
                expires shouldNotBe null
            }

            test("should return valid JWT token that can be decoded") {
                val response =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .`when`()
                        .post("/api/v1/authenticate")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val token = response.jsonPath().getString("token")

                // Decode JWT without verification to check structure
                val decodedJWT = JWT.decode(token)
                decodedJWT.subject.shouldNotBeBlank()
                decodedJWT.issuer shouldBe "http4k-test"
            }

            test("should return token with future expiration time") {
                val currentTime = System.currentTimeMillis() / 1000

                val response =
                    given()
                        .body("""{"email":"admin@local.com","password":"password123"}""")
                        .`when`()
                        .post("/api/v1/authenticate")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()

                val expires = response.jsonPath().getLong("expires")
                (expires > currentTime) shouldBe true
            }
        }

        context("POST /api/v1/authenticate - Validation Errors") {
            test("should return 400 for empty email") {
                given()
                    .body("""{"email":"","password":"password123"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .body("title", equalTo("Validation Error"))
                    .body("description", containsString("Email cannot be empty"))
            }

            test("should return 400 for invalid email format") {
                given()
                    .body("""{"email":"invalid-email","password":"password123"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .body("title", equalTo("Validation Error"))
                    .body("description", containsString("Invalid email format"))
            }

            test("should return 400 for empty password") {
                given()
                    .body("""{"email":"admin@local.com","password":""}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .body("title", equalTo("Validation Error"))
                    .body("description", containsString("Password cannot be empty"))
            }

            test("should return 400 for password too short") {
                given()
                    .body("""{"email":"admin@local.com","password":"short"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .body("title", equalTo("Validation Error"))
                    .body("description", containsString("Password must be at least 8 characters"))
            }
        }

        context("POST /api/v1/authenticate - Authentication Failures") {
            test("should return 400 for non-existent user") {
                given()
                    .body("""{"email":"nonexistent@example.com","password":"password123"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .body("title", equalTo("Invalid credentials"))
                    .body("description", containsString("Invalid email or password"))
            }

            test("should return 400 for wrong password") {
                given()
                    .body("""{"email":"admin@local.com","password":"wrongpassword"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .body("title", equalTo("Invalid credentials"))
                    .body("description", containsString("Invalid email or password"))
            }

            test("should return 400 for case-sensitive password mismatch") {
                given()
                    .body("""{"email":"admin@local.com","password":"Password"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
                    .log()
                    .all()
                    .body("title", equalTo("Invalid credentials"))
            }
        }

        context("POST /api/v1/authenticate - Content Type") {
            test("should require JSON content type") {
                given()
                    .contentType("text/plain")
                    .body("""{"email":"admin@local.com","password":"password123"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(400)
            }

            test("should return JSON response") {
                given()
                    .body("""{"email":"admin@local.com","password":"password123"}""")
                    .`when`()
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(200)
                    .contentType(containsString("application/json"))
            }
        }
    }
}
