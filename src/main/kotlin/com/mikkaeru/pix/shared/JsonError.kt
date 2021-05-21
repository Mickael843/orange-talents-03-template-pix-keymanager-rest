package com.mikkaeru.pix.shared

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class JsonError(
    val message: String?,
    val code: Int,
    val fields: List<Field> = listOf(),
    val dateTime: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
) {

    data class Field(val name: String, val description: String)
}