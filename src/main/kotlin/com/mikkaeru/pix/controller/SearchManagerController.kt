package com.mikkaeru.pix.controller

import com.mikkaeru.SearchManagerServiceGrpc
import com.mikkaeru.SearchRequest
import com.mikkaeru.SearchResponse
import com.mikkaeru.pix.dto.AccountResponse
import com.mikkaeru.pix.dto.OwnerResponse
import com.mikkaeru.pix.dto.PixKeyDetailsResponse
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Controller("/v1/users/{clientId}/keys")
class SearchManagerController(
    private val searchManager: SearchManagerServiceGrpc.SearchManagerServiceBlockingStub
) {

    @Get("/{pixId}")
    fun searchPixKey(@PathVariable clientId: UUID, @PathVariable pixId: UUID): HttpResponse<*> {

        val response = searchManager.searchPixKey(
            SearchRequest.newBuilder().setPixId(
                SearchRequest.FilterById.newBuilder()
                    .setClientId(clientId.toString())
                    .setPixId(pixId.toString())
                    .build()
            ).build()
        )

        return HttpResponse.ok(response.toPixKeyDetailsResponse())
    }

    private fun SearchResponse.toPixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            pixId = pixId,
            clientId = clientId,
            type = KeyType.valueOf(pixKey.type.name),
            key = pixKey.key,
            owner = OwnerResponse(name = pixKey.owner.name, cpf = pixKey.owner.cpf),
            account = AccountResponse(
                institution = pixKey.account.institution,
                agency = pixKey.account.agency,
                number = pixKey.account.number,
                type = AccountType.valueOf(pixKey.account.type.name)
            ),
            createAt = Instant
                .ofEpochSecond(pixKey.createAt.seconds, pixKey.createAt.nanos.toLong())
                .atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME)
        )
    }
}