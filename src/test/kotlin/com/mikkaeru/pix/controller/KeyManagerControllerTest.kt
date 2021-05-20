package com.mikkaeru.pix.controller

import com.mikkaeru.KeyPixResponse
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.shared.GrpcClientFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpHeaders.LOCATION
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpStatus.CREATED
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerControllerTest {

    @field:Inject
    lateinit var keymanagerGrpc: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub

    companion object {
        const val CLIENT_ID = "0d1bb194-3c52-4e67-8c35-a93c0af9284f"
    }

    @field:Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `deve registrar um chave pix`() {
        val pixId = UUID.randomUUID().toString()

        given(keymanagerGrpc.registerPixKey(any())).willReturn(
            KeyPixResponse.newBuilder()
                .setClientId(CLIENT_ID)
                .setPixId(pixId)
                .build()
        )

        val key = KeyRequest(
            type = KeyType.CPF,
            key = "14938637675",
            accountType = AccountType.CACC
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
    fun `nao deve registrar um chave pix quando os o tipo de chave for invalido`() {
        val key = KeyRequest(
            type = null,
            key = "14938637675",
            accountType = AccountType.CACC
        )

        val request = POST("/v1/users/$CLIENT_ID/keys", key)
        val response = client.toBlocking().exchange(request, KeyRequest::class.java)

        with(response) {
            assertTrue(body.isPresent)
            assertThat(status, equalTo(CREATED))
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