package com.mikkaeru.pix.controller

import com.google.protobuf.Timestamp
import com.mikkaeru.*
import com.mikkaeru.pix.dto.AccountResponse
import com.mikkaeru.pix.dto.AllPixKeyResponse
import com.mikkaeru.pix.dto.OwnerResponse
import com.mikkaeru.pix.dto.PixKeyDetailsResponse
import com.mikkaeru.pix.helper.IntegrationHelper
import com.mikkaeru.pix.shared.JsonError
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

internal class SearchManagerControllerTest: IntegrationHelper() {

    @field:Inject
    lateinit var searchManager: SearchManagerServiceGrpc.SearchManagerServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var client: HttpClient

    private lateinit var pixKeyDetails: PixKeyDetailsResponse

    companion object {
        val today: LocalDateTime = LocalDateTime.now()
    }

    @BeforeEach
    internal fun setUp() {
        pixKeyDetails = PixKeyDetailsResponse(
            pixId = "1c6db765-016d-46e9-9a0b-dbc09575938a",
            clientId = "0d1bb194-3c52-4e67-8c35-a93c0af9284f",
            type = com.mikkaeru.pix.model.KeyType.CPF,
            key = "47991407012",
            owner = OwnerResponse(name = "Rafael M C Ponte", cpf = "47991407012"),
            account = AccountResponse(
                institution = "ITAÚ UNIBANCO S.A.",
                agency = "0001",
                number = "291900",
                type = com.mikkaeru.pix.model.AccountType.CACC
            ),
            createAt = today.toString()
        )
    }

    @Test
    fun `deve buscar uma chave pix pelo seu id e pelo clientId`() {
        val pixIdTmp = UUID.randomUUID().toString()

        given(searchManager.searchPixKey(
            SearchRequest.newBuilder()
                .setPixId(
                    SearchRequest.FilterById.newBuilder()
                        .setPixId(pixIdTmp)
                        .setClientId(pixKeyDetails.clientId)
                        .build()
                ).build()
        )).willReturn(getSearchResponse(pixIdTmp))

        val request = HttpRequest.GET<PixKeyDetailsResponse>("/v1/users/${pixKeyDetails.clientId}/keys/${pixIdTmp}")
        val response = client.toBlocking().exchange(request, PixKeyDetailsResponse::class.java)

        with(response.body()!!) {
            assertThat(pixId, equalTo(pixIdTmp))
            assertThat(key, equalTo(pixKeyDetails.key))
            assertThat(type, equalTo(pixKeyDetails.type))
            assertThat(clientId, equalTo(pixKeyDetails.clientId))

            with(owner) {
                assertThat(name, equalTo(pixKeyDetails.owner.name))
                assertThat(cpf, equalTo(pixKeyDetails.owner.cpf))
            }

            with(account) {
                assertThat(type, equalTo(pixKeyDetails.account.type))
                assertThat(agency, equalTo(pixKeyDetails.account.agency))
                assertThat(number, equalTo(pixKeyDetails.account.number))
                assertThat(institution, equalTo(pixKeyDetails.account.institution))
            }
        }
    }

    @Test
    fun `deve retornar status code 404 para uma chave pix que nao existe`() {
        given(searchManager.searchPixKey(
            SearchRequest.newBuilder()
                .setPixId(
                    SearchRequest.FilterById.newBuilder()
                        .setPixId(pixKeyDetails.pixId)
                        .setClientId(pixKeyDetails.clientId)
                        .build()
                ).build()
        )).willThrow(StatusRuntimeException(Status.NOT_FOUND.withDescription("Pix key not found")))

        val exception = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(
                HttpRequest.GET<PixKeyDetailsResponse>(
                    "/v1/users/${pixKeyDetails.clientId}/keys/${pixKeyDetails.pixId}"
                ),PixKeyDetailsResponse::class.java
            )
        }

        with(exception.response) {
            assertThat(status, equalTo(io.micronaut.http.HttpStatus.NOT_FOUND))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(io.micronaut.http.HttpStatus.NOT_FOUND.code))
                assertThat(message, equalTo("Pix key not found"))
            }
        }
    }

    @Test
    fun `deve buscar uma lista de chave pix pelo clientId`() {
        given(searchManager.searchAllByOwner(
            SearchAllRequest.newBuilder().setClientId(pixKeyDetails.clientId).build()
        )).willReturn(
            SearchAllResponse.newBuilder()
                .setClientId(pixKeyDetails.clientId)
                .addAllPixKeys(pixKeyDetails())
                .build()
        )

        val request = HttpRequest.GET<AllPixKeyResponse>("/v1/users/${pixKeyDetails.clientId}/keys")
        val response = client.toBlocking().exchange(request, AllPixKeyResponse::class.java)

        with(response.body()!!) {
            assertThat(clientId, equalTo(pixKeyDetails.clientId))
            assertThat(keys, IsCollectionWithSize.hasSize(pixKeyDetails().size))
        }
    }

    @Test
    fun `deve retornar uma lista vazia para um client sem chave pix cadastrada`() {
        given(searchManager.searchAllByOwner(
            SearchAllRequest.newBuilder().setClientId(pixKeyDetails.clientId).build()
        )).willReturn(
            SearchAllResponse.newBuilder()
                .setClientId(pixKeyDetails.clientId)
                .addAllPixKeys(listOf())
                .build()
        )

        val request = HttpRequest.GET<AllPixKeyResponse>("/v1/users/${pixKeyDetails.clientId}/keys")
        val response = client.toBlocking().exchange(request, AllPixKeyResponse::class.java)

        with(response.body()!!) {
            assertTrue(keys.isEmpty())
            assertThat(clientId, equalTo(pixKeyDetails.clientId))
        }
    }

    private fun getSearchResponse(pixId: String): SearchResponse {
        return SearchResponse.newBuilder()
            .setPixId(pixId)
            .setClientId(pixKeyDetails.clientId)
            .setPixKey(
                SearchResponse.PixKey.newBuilder()
                    .setKey(pixKeyDetails.key)
                    .setType(KeyType.valueOf(pixKeyDetails.type.name))
                    .setOwner(
                        SearchResponse.PixKey.Owner.newBuilder()
                            .setName(pixKeyDetails.owner.name)
                            .setCpf(pixKeyDetails.owner.cpf)
                            .build())
                    .setAccount(
                        SearchResponse.PixKey.Account.newBuilder()
                            .setType(AccountType.valueOf(pixKeyDetails.account.type.name))
                            .setAgency(pixKeyDetails.account.agency)
                            .setNumber(pixKeyDetails.account.number)
                            .setInstitution(pixKeyDetails.account.institution)
                            .build())
                    .setCreateAt(
                        Timestamp.newBuilder()
                            .setNanos(today.nano)
                            .setSeconds(today.second.toLong())
                            .build())
            ).build()
    }

    private fun pixKeyDetails(): List<SearchAllResponse.PixKeyDetails> {
        val pixId = UUID.randomUUID().toString()
        val list = mutableListOf<SearchAllResponse.PixKeyDetails>()

        for (i in 1..10) {
            val build = SearchAllResponse.PixKeyDetails.newBuilder()
                .setPixId(pixId)
                .setType(KeyType.valueOf(KeyType.EMAIL.name))
                .setKey("teste$i@gmail.com")
                .setAccountType(AccountType.valueOf("CACC"))
                .setCreateAt(
                    Timestamp.newBuilder().setNanos(today.nano).setSeconds(today.second.toLong()).build()
                ).build()

            list.add(build)
        }

        return list
    }
}