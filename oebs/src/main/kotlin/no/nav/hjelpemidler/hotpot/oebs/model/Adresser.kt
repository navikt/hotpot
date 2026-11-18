package no.nav.hjelpemidler.hotpot.oebs.model

data class Adresser(
    val bostedsadresse: Adresse? = null,
    val leveringsadresser: List<Adresse> = emptyList(),
)

data class Adresse(
    val gateadresse: String? = null,
    val postnummer: String? = null,
    val by: String? = null,
    val kommune: String? = null,
    val bydel: String? = null,
)
