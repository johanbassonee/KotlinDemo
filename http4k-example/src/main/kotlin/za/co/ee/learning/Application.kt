package za.co.ee.learning

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import mu.KotlinLogging
import za.co.ee.learning.infrastructure.config.ServerConfig
import za.co.ee.learning.infrastructure.server.Server

fun main() {
    val logger = KotlinLogging.logger {}

    val config =
        ConfigLoaderBuilder
            .default()
            .addResourceSource("/application.yml")
            .addPropertySource(
                EnvironmentVariablesPropertySource(
                    useUnderscoresAsSeparator = true,
                    allowUppercaseNames = true,
                ),
            ).build()
            .loadConfigOrThrow<ServerConfig>()

    Server(config).start()

    logger.info { "API started successfully on port 8080" }
}
