package com.mikkaeru.pix.shared

data class JsonError(
    val message: String?,
    val code: Int,
    val fields: List<Field> = listOf()
) {

    data class Field(val name: String, val description: String)
}