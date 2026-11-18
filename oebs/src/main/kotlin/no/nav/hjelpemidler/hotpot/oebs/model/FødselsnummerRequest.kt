package no.nav.hjelpemidler.hotpot.oebs.model

import no.nav.hjelpemidler.domain.person.Fødselsnummer

data class FødselsnummerRequest(val fnr: Fødselsnummer) {
    override fun toString(): String = "FødselsnummerRequest(fnr=MASKERT)"
}
