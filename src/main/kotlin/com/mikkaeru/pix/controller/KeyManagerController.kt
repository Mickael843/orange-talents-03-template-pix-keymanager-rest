package com.mikkaeru.pix.controller

import com.mikkaeru.KeyPixResponse
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.dto.KeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
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
        log.info("[$clientId] criando uma chave pix com $request")

        val response = keymanagerGrpc.registerPixKey(request.toGrpcModel(clientId.toString()))

        return HttpResponse.created(response.toKeyResponse(), HttpResponse.uri("/v1/users/$clientId/keys/${response.pixId}"))
    }

    private fun KeyPixResponse.toKeyResponse() = KeyResponse(pixId = pixId, clientId = clientId)
}