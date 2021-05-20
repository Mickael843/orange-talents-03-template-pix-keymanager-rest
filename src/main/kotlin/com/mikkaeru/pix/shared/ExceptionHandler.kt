package com.mikkaeru.pix.shared

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import javax.validation.ConstraintViolationException

@Controller
class ExceptionHandler {

    // TODO Adicionar Log no tratamento de erro.
    // TODO Adicionar tratamento as possíveis exceções.
    // TODO Refatorar o tratamento de erro utilizando polimorfismo.

    @Error(global = true)
    fun error(request: HttpRequest<*>, e: Throwable): HttpResponse<JsonError> {
        return when(e) {
            is ConstraintViolationException -> {
                val jsonError = JsonError(
                    message = "Invalid fields",
                    code = HttpStatus.BAD_REQUEST.code,
                    fields = e.constraintViolations.map {
                        JsonError.Field(name = it.propertyPath.last().name, description = it.message)
                    }
                )

                HttpResponse.badRequest(jsonError)
            }
            else -> HttpResponse.serverError()
        }
    }
}