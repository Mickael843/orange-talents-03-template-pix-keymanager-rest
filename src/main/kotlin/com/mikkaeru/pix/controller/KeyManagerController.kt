package com.mikkaeru.pix.controller

import com.mikkaeru.KeyPixResponse
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.RemoveKeyPixRequest
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.dto.KeyResponse
import com.mikkaeru.pix.dto.RemovePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.Valid

@Validated
@Controller("/v1/users/{clientId}/keys")
class KeyManagerController(
    private val keymanagerGrpc: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Post
    fun register(@PathVariable clientId: UUID, @Body @Valid request: KeyRequest): HttpResponse<*> {
        val response = keymanagerGrpc.registerPixKey(request.toGrpcModel(clientId.toString())).also {
            log.info("[$clientId] criando uma chave pix com $request")
        }

        return HttpResponse.created(response.toKeyResponse(), HttpResponse.uri("/v1/users/$clientId/keys/${response.pixId}"))
    }

    @Delete("/{pixId}")
    fun remove(@PathVariable clientId: UUID, @PathVariable pixId: UUID): HttpResponse<*> {
        keymanagerGrpc.removePixKey(
            RemoveKeyPixRequest.newBuilder()
                .setClientId(clientId.toString())
                .setPixId(pixId.toString())
                .build()
        ).also {
            log.info("[${it.clientId}] removeu a chave pix com o id: ${it.pixId}")
        }

        return HttpResponse.ok(RemovePixKeyResponse(pixId = pixId, clientId = clientId))
    }

    private fun KeyPixResponse.toKeyResponse() = KeyResponse(pixId = pixId, clientId = clientId)
}