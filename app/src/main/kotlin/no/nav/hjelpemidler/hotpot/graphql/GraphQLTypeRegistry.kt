package no.nav.hjelpemidler.hotpot.graphql

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import java.time.LocalDate
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.KType

class GraphQLTypeRegistry : SchemaGeneratorHooks {
    private val registry = mutableMapOf<KClass<*>, GraphQLType?>()

    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        val key = type.classifier as? KClass<*> ?: return null
        return registry.computeIfAbsent(key) {
            when (it) {
                LocalDate::class -> graphQLStringTypeOf(
                    name = "Date",
                    description = """
                        Dato uten tidspunkt i ISO-8601-format.

                        Eksempel: "2024-03-04"
                    """.trimIndent(),
                    transform = LocalDate::parse,
                )

                Fødselsnummer::class -> graphQLStringTypeOf(
                    name = "FNR",
                    description = """
                        Fødselsnummer.

                        NB! I testmiljøer kan også syntetiske verdier benyttes.
                    """.trimIndent(),
                    transform = ::Fødselsnummer,
                )

                else -> null
            }
        }
    }

    private fun <T> graphQLStringTypeOf(
        name: String,
        description: String,
        transform: (String) -> T,
    ): GraphQLScalarType = GraphQLScalarType.newScalar()
        .name(name)
        .description(description)
        .coercing(
            object : Coercing<T, String> {
                override fun serialize(dataFetcherResult: Any, graphQLContext: GraphQLContext, locale: Locale): String =
                    dataFetcherResult.toString()

                override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): T {
                    val value = input as? String ?: throw CoercingParseValueException("Forventet streng for $name")
                    return parse(value, ::CoercingParseValueException)
                }

                override fun parseLiteral(
                    input: Value<*>,
                    variables: CoercedVariables,
                    graphQLContext: GraphQLContext,
                    locale: Locale,
                ): T {
                    val value = (input as? StringValue)?.value
                        ?: throw CoercingParseLiteralException("Forventet streng for $name")
                    return parse(value, ::CoercingParseLiteralException)
                }

                override fun valueToLiteral(input: Any, graphQLContext: GraphQLContext, locale: Locale): Value<*> =
                    StringValue(input.toString())

                // Unngår at ugyldig input (f.eks. fnr) havner i feilmeldingen.
                private fun parse(value: String, error: (String) -> RuntimeException): T = try {
                    transform(value)
                } catch (_: Exception) {
                    throw error("Ugyldig verdi for $name")
                }
            },
        )
        .build()
}
