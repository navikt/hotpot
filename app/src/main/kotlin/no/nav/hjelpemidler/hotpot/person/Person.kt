package no.nav.hjelpemidler.hotpot.person

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.hotpot.hjelpemidler.HjelpemidlerService
import no.nav.hjelpemidler.hotpot.oebs.model.Adresse
import no.nav.hjelpemidler.hotpot.oebs.model.Adresser
import no.nav.hjelpemidler.hotpot.oebs.model.Bruker
import no.nav.hjelpemidler.hotpot.oebs.model.Brukerpass

class Person(
    val fnr: Fødselsnummer,
    private val hjelpemidlerService: HjelpemidlerService,
) {
    @GraphQLDescription("Brukernummer og status i OeBS, null hvis personen ikke finnes som bruker")
    suspend fun bruker(): Bruker? = hjelpemidlerService.hentBruker(fnr)

    @GraphQLDescription("Adresser registrert i OeBS, null hvis ingen adresser finnes")
    suspend fun adresser(): Adresser? = hjelpemidlerService.hentAdresser(fnr)

    @GraphQLDescription("Bostedsadresse i OeBS som er gyldig for opprettelse av serviceforespørsel, null hvis ingen finnes")
    suspend fun bostedsadresse(): Adresse? = hjelpemidlerService.hentBostedsadresse(fnr)

    @GraphQLDescription("Brukerpass registrert i OeBS")
    suspend fun brukerpass(): List<Brukerpass> = hjelpemidlerService.hentBrukerpass(fnr)
}
