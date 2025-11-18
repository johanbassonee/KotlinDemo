package za.co.ee.learning.infrastructure.routes

import org.http4k.contract.ui.swagger.swaggerUiWebjar
import org.http4k.routing.bind

class SwaggerRoutes {
    val routes =
        "/swagger" bind
            swaggerUiWebjar {
                url = "/openapi.json"
                pageTitle = "Http4k Example API"
                displayOperationId = true
            }
}
