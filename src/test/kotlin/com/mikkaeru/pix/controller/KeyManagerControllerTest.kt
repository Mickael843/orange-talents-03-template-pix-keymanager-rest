package com.mikkaeru.pix.controller

import com.mikkaeru.*
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.dto.RemovePixKeyResponse
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.shared.GrpcClientFactory
import com.mikkaeru.pix.shared.JsonError
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpHeaders.LOCATION
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpStatus.*
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.StringContains.containsStringIgnoringCase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
internal class KeyManagerControllerTest {

    @field:Inject
    lateinit var keymanagerGrpc: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub

    companion object {
        const val PIX_ID = "1c6db765-016d-46e9-9a0b-dbc09575938a"
        const val CLIENT_ID = "0d1bb194-3c52-4e67-8c35-a93c0af9284f"
    }

    @field:Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `deve registrar uma chave pix`() {
        val pixId = UUID.randomUUID().toString()

        val key = KeyRequest(
            type = KeyType.CPF,
            key = "40998760048",
            accountType = AccountType.CACC
        )

        given(keymanagerGrpc.registerPixKey(
            KeyPixRequest.newBuilder()
                .setKey(key.key)
                .setClientId(CLIENT_ID)
                .setType(com.mikkaeru.KeyType.valueOf(key.type!!.name))
                .setAccountType(com.mikkaeru.AccountType.valueOf(key.accountType!!.name))
                .build()
        )).willReturn(
            KeyPixResponse.newBuilder()
                .setClientId(CLIENT_ID)
                .setPixId(pixId)
                .build()
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)
        val response = client.toBlocking().exchange(request, KeyRequest::class.java)

        with(response) {
            assertTrue(body.isPresent)
            assertThat(status, equalTo(CREATED))
            assertTrue(headers.contains(LOCATION))
            assertTrue(header(LOCATION)!!.contains(pixId))
        }
    }

    @Test
    fun `nao deve registrar uma chave pix com chave duplicada`() {
        val duplicatedKey = "teste@gmail.com"

        val key = KeyRequest(
            type = KeyType.EMAIL,
            key = duplicatedKey,
            accountType = AccountType.CACC
        )

        given(keymanagerGrpc.registerPixKey(
            KeyPixRequest.newBuilder()
                .setKey(key.key)
                .setClientId(CLIENT_ID)
                .setType(com.mikkaeru.KeyType.valueOf(key.type!!.name))
                .setAccountType(com.mikkaeru.AccountType.valueOf(key.accountType!!.name))
                .build()
        )).willThrow(StatusRuntimeException(
            Status.ALREADY_EXISTS.withDescription("Chave pix $duplicatedKey existente"), Metadata())
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(UNPROCESSABLE_ENTITY))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(UNPROCESSABLE_ENTITY.code))
                assertThat(message, containsStringIgnoringCase("pix key already registered"))

                assertThat(fields.last().name, equalTo("key"))
                assertThat(fields.last().description, equalTo("Chave pix $duplicatedKey existente"))
            }
        }
    }

    @Test
    fun `nao deve registrar uma chave pix com cpf invalido`() {
        val key = KeyRequest(
            type = KeyType.CPF,
            key = "13693829230",
            accountType = AccountType.CACC
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(BAD_REQUEST))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(BAD_REQUEST.code))
                assertThat(message, containsStringIgnoringCase("Invalid fields"))

                assertThat(fields.last().description, equalTo("CPF is in an invalid format"))
            }
        }
    }

    @Test
    fun `nao deve registrar uma chave pix com email invalido`() {
        val key = KeyRequest(
            type = KeyType.EMAIL,
            key = "testeInvalidEmailCom",
            accountType = AccountType.CACC
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(BAD_REQUEST))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(BAD_REQUEST.code))
                assertThat(message, containsStringIgnoringCase("Invalid fields"))

                assertThat(fields.last().description, equalTo("Email is in an invalid format"))
            }
        }
    }

    @Test
    fun `nao deve registrar uma chave pix com telefone invalido`() {
        val key = KeyRequest(
            type = KeyType.PHONE,
            key = "13693829230",
            accountType = AccountType.CACC
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(BAD_REQUEST))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(BAD_REQUEST.code))
                assertThat(message, containsStringIgnoringCase("Invalid fields"))

                assertThat(fields.last().description, equalTo("Phone is in an invalid format"))
            }
        }
    }

    @Test
    fun `nao deve receber uma chave pix do tipo random com valor preenchido`() {
        val key = KeyRequest(
            type = KeyType.RANDOM,
            key = "SouUmValorPreenchidoOK",
            accountType = AccountType.CACC
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(BAD_REQUEST))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(BAD_REQUEST.code))
                assertThat(message, containsStringIgnoringCase("Invalid fields"))

                assertThat(fields.last().description, equalTo("Key must not be filled"))
            }
        }
    }

    @Test
    fun `nao deve registrar uma chave pix quando os o tipo de chave for invalido`() {
        val key = KeyRequest(
            type = null,
            key = "14938637675",
            accountType = AccountType.CACC
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(BAD_REQUEST))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(BAD_REQUEST.code))
                assertThat(message, containsStringIgnoringCase("Invalid fields"))

                assertThat(fields.last().name, equalTo("type"))
                assertThat(fields.last().description, equalTo("must not be null"))
            }
        }
    }

    @Test
    fun `nao deve registrar uma chave pix quando os o tipo de conta for invalido`() {
        val key = KeyRequest(
            type = KeyType.CPF,
            key = "40998760048",
            accountType = null
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(BAD_REQUEST))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(BAD_REQUEST.code))
                assertThat(message, containsStringIgnoringCase("Invalid fields"))

                assertThat(fields.last().name, equalTo("accountType"))
                assertThat(fields.last().description, equalTo("must not be null"))
            }
        }
    }

    @Test
    fun `nao deve registrar uma chave pix enviar um clientId em formato invalido`() {
        val invalidClientId = "123321"

        val key = KeyRequest(
            type = KeyType.CPF,
            key = "14938637675",
            accountType = AccountType.CACC
        )

        val request = POST("/v1/users/$invalidClientId/keys", key)

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(request, KeyRequest::class.java)
        }

        with(error.response) {
            assertThat(status, equalTo(BAD_REQUEST))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(BAD_REQUEST.code))
                assertThat(message, containsStringIgnoringCase("Error converting clientId field"))

                assertThat(fields.last().name, equalTo("clientId"))
                assertThat(fields.last().description, equalTo("Failed to convert argument [clientId] for value [$invalidClientId] due to: Invalid UUID string: $invalidClientId"))
            }
        }
    }

    @Test
    fun `deve remover uma chave pix`() {
        given(keymanagerGrpc.removePixKey(
            RemoveKeyPixRequest.newBuilder()
                .setPixId(PIX_ID)
                .setClientId(CLIENT_ID)
                .build()
        )).willReturn(
            RemoveKeyPixResponse.newBuilder()
                .setPixId(PIX_ID)
                .setClientId(CLIENT_ID)
                .build()
        )

        val request = HttpRequest.DELETE<RemovePixKeyResponse>("/v1/users/$CLIENT_ID/keys/$PIX_ID")
        val response = client.toBlocking().exchange(request, RemovePixKeyResponse::class.java)

        with(response) {
            assertThat(status, equalTo(OK))

            with(response.body()!!) {
                assertThat(pixId.toString(), equalTo(PIX_ID))
                assertThat(clientId.toString(), equalTo(CLIENT_ID))
            }
        }
    }

    @Test
    fun `nao deve tentar remover uma chave pix que nao existe`() {
        val invalidPixId = UUID.randomUUID().toString()

        given(keymanagerGrpc.removePixKey(
            RemoveKeyPixRequest.newBuilder()
                .setPixId(invalidPixId)
                .setClientId(CLIENT_ID)
                .build()
        )).willThrow(StatusRuntimeException(Status.NOT_FOUND.withDescription("Chave pix não encontrada")))

        val error = assertThrows<HttpClientResponseException> {
            client.toBlocking().exchange(
                HttpRequest.DELETE<RemovePixKeyResponse>("/v1/users/$CLIENT_ID/keys/$invalidPixId"),
                RemovePixKeyResponse::class.java
            )
        }

        with(error.response) {
            assertThat(status, equalTo(NOT_FOUND))

            with(getBody(JsonError::class.java).get()) {
                assertThat(code, equalTo(NOT_FOUND.code))
                assertThat(message, equalTo("Pix key not found"))
            }
        }
    }

    @Factory
    @Replaces(factory = GrpcClientFactory::class)
    internal class StubFactory {

        @Singleton
        fun stubMock(): KeymanagerServiceGrpc.KeymanagerServiceBlockingStub {
            return mock(KeymanagerServiceGrpc.KeymanagerServiceBlockingStub::class.java)
        }
    }
}