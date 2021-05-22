package com.mikkaeru.pix.controller

import com.mikkaeru.*
import com.mikkaeru.pix.dto.*
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Controller("/v1/users/{clientId}/keys")
class SearchManagerController(
    private val searchManager: SearchManagerServiceGrpc.SearchManagerServiceBlockingStub
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Get
    fun searchAll(@PathVariable clientId: UUID): HttpResponse<*> {
        val response = searchManager.searchAllByOwner(
            SearchAllRequest.newBuilder()
                .setClientId(clientId.toString())
                .build()
        ).also { log.info("[$clientId] Buscando todas as chaves pix.") }

        return HttpResponse.ok(response.toAllPixKeyResponse())
    }

    @Get("/{pixId}")
    fun searchPixKey(@PathVariable clientId: UUID, @PathVariable pixId: UUID): HttpResponse<*> {
        val response = searchManager.searchPixKey(
            SearchRequest.newBuilder().setPixId(
                SearchRequest.FilterById.newBuilder()
                    .setClientId(clientId.toString())
                    .setPixId(pixId.toString())
                    .build()
            ).build()
        ).also { log.info("[$clientId] Buscando chave pix com id '$pixId'.") }

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

    private fun SearchAllResponse.toAllPixKeyResponse(): AllPixKeyResponse {
        val list = mutableListOf<KeyDetailsResponse>()

        if (pixKeysList.isEmpty()) {
            return AllPixKeyResponse(clientId = clientId)
        }

        for (pixKey in pixKeysList) {
            val details = KeyDetailsResponse(
                pixId = pixKey.pixId,
                type = KeyType.valueOf(pixKey.type.name),
                key = pixKey.key,
                accountType = AccountType.valueOf(pixKey.accountType.name),
                createAt = Instant
                    .ofEpochSecond(pixKey.createAt.seconds, pixKey.createAt.nanos.toLong())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME)
            )

            list.add(details)
        }

        return AllPixKeyResponse(clientId = clientId, keys = list)
    }
}