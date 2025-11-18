package za.co.ee.learning.infrastructure.config

data class ServerConfig(
    val port: Int = 8080,
    val jwtSecret: String = "mysupersecret",
    val jwtIssuer: String = "http4k",
    val jwtExpirationSeconds: Long = 120,
)
