package za.co.ee.learning.infrastructure.server

import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.filter.ServerFilters.CatchLensFailure
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import za.co.ee.learning.infrastructure.config.ServerConfig
import za.co.ee.learning.infrastructure.database.InMemoryUserRepository
import za.co.ee.learning.infrastructure.routes.ContractRoutes
import za.co.ee.learning.infrastructure.routes.SwaggerRoutes
import za.co.ee.learning.infrastructure.security.BCryptPasswordProvider
import za.co.ee.learning.infrastructure.security.DefaultJWTProvider
import za.co.ee.learning.infrastructure.security.JWTFilter

class Server(
    config: ServerConfig = ServerConfig(),
) {
    private val logger = mu.KotlinLogging.logger {}
    private val passwordProvider = BCryptPasswordProvider()
    private val userRepository = InMemoryUserRepository()
    private val jwtProvider = DefaultJWTProvider(config.jwtSecret, config.jwtIssuer, config.jwtExpirationSeconds)

    private val contractRoutes = ContractRoutes(userRepository, passwordProvider, jwtProvider)
    private val swaggerRoutes = SwaggerRoutes()

    private val corsFilter = ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive)
    private val contentTypeFilter =
        ContentTypeFilter(
            acceptableContentTypes = setOf("application/json"),
            excludedPaths = setOf("/health", "/openapi.json", "/swagger"),
        )
    private val jwtFilter =
        JWTFilter(
            jwtProvider = jwtProvider,
            excludedPaths = setOf("/health", "/api/v1/authenticate", "/openapi.json", "/swagger/*"),
        )

    private val allRoutes: HttpHandler =
        routes(
            swaggerRoutes.routes,
            contractRoutes.routes,
        )

    private val app: HttpHandler = contentTypeFilter.then(jwtFilter).then(allRoutes)

    private val appWithFilters =
        DebuggingFilters
            .PrintRequestAndResponse()
            .then(ServerFilters.CatchAll())
            .then(CatchLensFailure())
            .then(corsFilter)
            .then(app)

    private val netty = appWithFilters.asServer(Netty(config.port))

    fun start(): Server {
        netty.start()

        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info { "Shutdown signal received, stopping server gracefully..." }
                stop()
                logger.info { "Server stopped successfully" }
            },
        )

        return this
    }

    fun stop() {
        netty.stop()
    }

    fun port(): Int = netty.port()
}
