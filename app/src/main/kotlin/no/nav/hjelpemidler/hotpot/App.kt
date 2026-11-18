package no.nav.hjelpemidler.hotpot

import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import com.expediagroup.graphql.server.operations.Query
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.hjelpemidler.hotpot.graphql.GraphQLTypeRegistry
import no.nav.hjelpemidler.hotpot.hjelpemidler.HjelpemidlerService
import no.nav.hjelpemidler.hotpot.oebs.HjelpemidlerApiClient
import no.nav.hjelpemidler.hotpot.person.Person
import no.nav.hjelpemidler.http.openid.IdentityProvider
import no.nav.hjelpemidler.http.openid.Texas
import no.nav.hjelpemidler.http.openid.TexasClient
import no.nav.hjelpemidler.http.openid.TokenIntrospection
import no.nav.hjelpemidler.http.openid.TokenSet
import no.nav.hjelpemidler.server.ktor.security.authenticateTexas
import no.nav.hjelpemidler.server.ktor.security.texas
import kotlin.time.Duration.Companion.hours

private val log = KotlinLogging.logger {}

fun main() {
    log.info { "Gjeldende miljø: ${Environment.current}" }

    TILLAT_SYNTETISKE_FØDSELSNUMRE = !Environment.current.isProd

    embeddedServer(
        Netty,
        configure = {
            connector { port = Configuration["HTTP_PORT"]?.toInt() ?: 8080 }
            shutdownGracePeriod = 5_000
            shutdownTimeout = 20_000
        },
        module = {
            dependencies {
                provide { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }

                provide<HttpClientEngine> { CIO.create() }

                provide<Texas> {
                    if (Environment.current.isLocal) {
                        LocalTexas
                    } else {
                        TexasClient(engine = resolve())
                    }
                }

                provide { HjelpemidlerApiClient(engine = resolve()) }

                provide(::HjelpemidlerService)
            }

            health()

            val texas: Texas by dependencies
            authentication {
                texas(texas)
            }

            graphQLApi()
        },
    ).start(wait = true)
}

fun Application.health() {
    val meterRegistry: PrometheusMeterRegistry by dependencies
    install(MicrometerMetrics) {
        this.registry = meterRegistry
    }

    routing {
        get("/isalive") {
            call.respond("ALIVE")
        }
        get("/isready") {
            call.respond("READY")
        }
        get("/metrics") {
            call.respond(meterRegistry.scrape())
        }
    }
}

fun Application.graphQLApi(endpoint: String = "graphql") {
    val hjelpemidlerService: HjelpemidlerService by dependencies

    install(GraphQL) {
        schema {
            packages = listOf("no.nav.hjelpemidler.hotpot")
            queries = listOf(RootQuery(hjelpemidlerService))
            hooks = GraphQLTypeRegistry()
        }
        engine {
            dataLoaderRegistryFactory = KotlinDataLoaderRegistryFactory()
        }
    }

    routing {
        authenticateTexas {
            graphQLPostRoute(endpoint)
        }
        if (!Environment.current.isProd) graphiQLRoute(graphQLEndpoint = endpoint)
    }
}

class RootQuery(private val hjelpemidlerService: HjelpemidlerService) : Query {
    @GraphQLDescription("Hent person med data fra OeBS")
    fun person(fnr: Fødselsnummer): Person = Person(fnr, hjelpemidlerService)
}

object LocalTexas : Texas {
    override suspend fun token(
        identityProvider: IdentityProvider,
        target: String,
    ): TokenSet = TokenSet("", 1.hours)

    override suspend fun exchange(
        identityProvider: IdentityProvider,
        target: String,
        userToken: String,
    ): TokenSet = TokenSet(userToken, 1.hours)

    override suspend fun introspection(
        identityProvider: IdentityProvider,
        token: String,
    ): TokenIntrospection = TokenIntrospection(active = true)
}
