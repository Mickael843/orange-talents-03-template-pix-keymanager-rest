package com.mikkaeru.pix.shared

import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.model.KeyType
import io.grpc.Status.Code.ALREADY_EXISTS
import io.grpc.Status.Code.NOT_FOUND
import io.grpc.StatusRuntimeException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import javax.validation.ConstraintViolationException

@Controller
class ExceptionHandler {

    @Error(global = true)
    fun error(e: ConstraintViolationException): HttpResponse<JsonError> {
        val jsonError = JsonError(
            message = "Invalid fields",
            code = HttpStatus.BAD_REQUEST.code,
            fields = e.constraintViolations.map {
                val field = if (it.invalidValue is KeyRequest) {
                    val request: KeyRequest = it.invalidValue as KeyRequest

                    when(request.type) {
                        KeyType.CPF -> JsonError.Field(name = "key", description = "CPF is in an invalid format")
                        KeyType.EMAIL -> JsonError.Field(name = "key", description = "Email is in an invalid format")
                        KeyType.PHONE -> JsonError.Field(name = "key", description = "Phone is in an invalid format")
                        else -> JsonError.Field(name = "key", description = "Key must not be filled")
                    }
                } else {
                    JsonError.Field(name = it.propertyPath.last().name, description = it.message)
                }

                field
            }
        )

        return HttpResponse.badRequest(jsonError)
    }

    @Error(global = true)
    fun error(e: ConversionErrorException): HttpResponse<JsonError> {
        val jsonError = JsonError(
            message = "Error converting ${e.argument.name} field",
            code = HttpStatus.BAD_REQUEST.code,
            fields = listOf(JsonError.Field(name = e.argument.name, description = e.message ?: ""))
        )

        return HttpResponse.badRequest(jsonError)
    }

    @Error(global = true)
    fun error(e: StatusRuntimeException): HttpResponse<JsonError> {
        return when(e.status.code) {
            ALREADY_EXISTS -> HttpResponse.unprocessableEntity<JsonError>().body(JsonError(
                message = "pix key already registered",
                code = HttpStatus.UNPROCESSABLE_ENTITY.code,
                fields = listOf(JsonError.Field(name = "key", description = e.status.description ?: ""))
            ))
            NOT_FOUND -> HttpResponse.notFound<JsonError?>().body(JsonError(
                message = "Pix key not found",
                code = HttpStatus.NOT_FOUND.code
            ))
            else -> HttpResponse.serverError<JsonError?>().body(JsonError(message = "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR.code))
        }
    }
}