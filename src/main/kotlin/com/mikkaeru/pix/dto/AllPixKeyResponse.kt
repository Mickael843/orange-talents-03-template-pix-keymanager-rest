package com.mikkaeru.pix.dto

import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType

data class AllPixKeyResponse(
    val clientId: String,
    val keys: List<KeyDetailsResponse> = emptyList()
)

data class KeyDetailsResponse(
    val pixId: String,
    val type: KeyType,
    val key: String,
    val accountType: AccountType,
    val createAt: String
)