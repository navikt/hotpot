package no.nav.hjelpemidler.hotpot.oebs

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.hotpot.oebs.model.Adresse
import no.nav.hjelpemidler.hotpot.oebs.model.Adresser
import no.nav.hjelpemidler.hotpot.oebs.model.Bruker
import no.nav.hjelpemidler.hotpot.oebs.model.Brukerpass
import no.nav.hjelpemidler.hotpot.oebs.model.FødselsnummerRequest
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.entraId
import java.io.Closeable
import java.util.UUID

class HjelpemidlerApiClient(
    engine: HttpClientEngine,
    baseUrl: String = HJELPEMIDLER_API_BASE_URL,
) : Closeable {
    private val client: HttpClient = createHttpClient(engine) {
        entraId(HJELPEMIDLER_API_TARGET)
        defaultRequest {
            accept(ContentType.Application.Json)
            url(baseUrl.trimEnd('/') + "/")
        }
    }

    /**
     * Returnerer `null` hvis bruker ikke finnes (404).
     */
    suspend fun hentBruker(
        fnr: Fødselsnummer,
        correlationId: UUID = UUID.randomUUID(),
    ): Bruker? = oppslag("bruker/oppslag", fnr, correlationId).bodyOrNullIfNotFound()

    /**
     * Returnerer `null` hvis adresser ikke finnes (404).
     */
    suspend fun hentAdresser(
        fnr: Fødselsnummer,
        correlationId: UUID = UUID.randomUUID(),
    ): Adresser? = oppslag("bruker/adresser/oppslag", fnr, correlationId).bodyOrNullIfNotFound()

    /**
     * Bostedsadresse som er gyldig for bruk i opprettelse av serviceforespørsel.
     * Returnerer `null` hvis ingen gyldig bostedsadresse finnes (404).
     */
    suspend fun hentBostedsadresse(
        fnr: Fødselsnummer,
        correlationId: UUID = UUID.randomUUID(),
    ): Adresse? = oppslag("bruker/bostedsadresse/oppslag", fnr, correlationId).bodyOrNullIfNotFound()

    /**
     * Returnerer tom liste hvis brukerpass ikke finnes (404).
     */
    suspend fun hentBrukerpass(
        fnr: Fødselsnummer,
        correlationId: UUID = UUID.randomUUID(),
    ): List<Brukerpass> = oppslag("bruker/brukerpass/oppslag", fnr, correlationId).bodyOrNullIfNotFound() ?: emptyList()

    private suspend fun oppslag(path: String, fnr: Fødselsnummer, correlationId: UUID): HttpResponse =
        client.post(path) {
            header(CORRELATION_ID_HEADER, correlationId.toString())
            contentType(ContentType.Application.Json)
            setBody(FødselsnummerRequest(fnr))
        }

    private suspend inline fun <reified T> HttpResponse.bodyOrNullIfNotFound(): T? = when {
        status == HttpStatusCode.NotFound -> null
        status.isSuccess() -> body<T>()
        else -> throw HjelpemidlerApiException(call.request.url.encodedPath, status)
    }

    override fun close() {
        client.close()
    }

    private companion object {
        const val CORRELATION_ID_HEADER = "x-correlation-id"
    }
}

class HjelpemidlerApiException(path: String, val status: HttpStatusCode) :
    RuntimeException("Kall mot hjelpemidler-api feilet, path: $path, status: $status")

val HJELPEMIDLER_API_BASE_URL by EnvironmentVariable
val HJELPEMIDLER_API_TARGET by EnvironmentVariable
