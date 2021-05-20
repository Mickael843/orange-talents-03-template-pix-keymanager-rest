package com.mikkaeru.pix.dto

import com.mikkaeru.KeyPixRequest
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import com.mikkaeru.AccountType as AccountTypeGrpc
import com.mikkaeru.KeyType as KeyTypeGrpc

@Introspected
data class KeyRequest(
    @field:NotNull val type: KeyType?,
    @field:NotNull val accountType: AccountType?,
    @field:NotBlank @Size(max = 77) val key: String?
) {

    fun toGrpcModel(clientId: String): KeyPixRequest {
        // TODO Colocar o valor equivalente dentro do próprio enum
        return KeyPixRequest.newBuilder()
            .setClientId(clientId)
            .setKey(key)
            .setType(KeyTypeGrpc.valueOf(type!!.name))
            .setAccountType(AccountTypeGrpc.valueOf(accountType!!.name))
            .build()
    }
}