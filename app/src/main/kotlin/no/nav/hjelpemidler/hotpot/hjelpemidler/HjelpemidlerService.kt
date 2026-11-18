package no.nav.hjelpemidler.hotpot.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.hotpot.oebs.HjelpemidlerApiClient
import no.nav.hjelpemidler.hotpot.oebs.model.Adresser
import no.nav.hjelpemidler.hotpot.oebs.model.Bruker
import no.nav.hjelpemidler.hotpot.oebs.model.Brukerpass

private val log = KotlinLogging.logger {}

class HjelpemidlerService(
    private val hjelpemidlerApiClient: HjelpemidlerApiClient,
) {
    suspend fun hentBruker(fnr: Fødselsnummer): Bruker? =
        hjelpemidlerApiClient.hentBruker(fnr)

    suspend fun hentAdresser(fnr: Fødselsnummer): Adresser? =
        hjelpemidlerApiClient.hentAdresser(fnr)

    suspend fun hentBrukerpass(fnr: Fødselsnummer): List<Brukerpass> =
        hjelpemidlerApiClient.hentBrukerpass(fnr)
}
