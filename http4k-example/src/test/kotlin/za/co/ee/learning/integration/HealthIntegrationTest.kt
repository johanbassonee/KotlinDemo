package za.co.ee.learning.integration

import io.kotest.matchers.shouldBe
import org.hamcrest.Matchers.equalTo

class HealthIntegrationTest : BaseIntegrationTest() {
    init {
        context("GET /health") {
            test("should return 200 OK") {
                given()
                    .`when`()
                    .get("/health")
                    .then()
                    .statusCode(200)
            }

            test("should return status UP") {
                val response =
                    given()
                        .`when`()
                        .get("/health")
                        .then()
                        .statusCode(200)
                        .body("status", equalTo("UP"))
                        .extract()
                        .response()

                response.jsonPath().getString("status") shouldBe "UP"
            }

            test("should return service name") {
                given()
                    .`when`()
                    .get("/health")
                    .then()
                    .statusCode(200)
                    .body("service", equalTo("example-api"))
            }

            test("should be accessible without authentication") {
                // No Authorization header
                given()
                    .header("Authorization", "")
                    .`when`()
                    .get("/health")
                    .then()
                    .statusCode(200)
            }

            test("should return JSON content type") {
                given()
                    .`when`()
                    .get("/health")
                    .then()
                    .statusCode(200)
                    .contentType("application/json; charset=utf-8")
            }
        }
    }
}
