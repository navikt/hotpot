package no.nav.hjelpemidler.hotpot.oebs.model

import java.time.LocalDate

data class Brukerpass(
    val kontraktnr: String? = null,
    val startdato: LocalDate? = null,
    val sluttdato: LocalDate? = null,
)
