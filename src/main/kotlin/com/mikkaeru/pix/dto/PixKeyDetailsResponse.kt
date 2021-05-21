package com.mikkaeru.pix.dto

import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType

data class PixKeyDetailsResponse(
    val pixId: String,
    val clientId: String,
    val type: KeyType,
    val key: String,
    val owner: OwnerResponse,
    val account: AccountResponse,
    val createAt: String
)

data class OwnerResponse(
    val name: String,
    val cpf: String
)

data class AccountResponse(
    val institution: String,
    val agency: String,
    val number: String,
    val type: AccountType
)