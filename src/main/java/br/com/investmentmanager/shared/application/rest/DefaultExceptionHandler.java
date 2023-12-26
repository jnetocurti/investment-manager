package br.com.investmentmanager.shared.application.rest;

import br.com.investmentmanager.shared.domain.exceptions.EntityAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static br.com.investmentmanager.shared.application.rest.util.HttpUtils.createEntityLocation;

@RestControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityAlreadyExistsException(EntityAlreadyExistsException ex) {

        var location = createEntityLocation(ex.getEntityId());

        var errorResponse = ApiErrorResponse.builder().message(ex.getMessage())
                .details(Map.of("location", location)).build();

        return ResponseEntity.status(HttpStatus.CONFLICT).location(location).body(errorResponse);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> handleThrowable(Throwable ex) {

        var status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        var apiErrorResponseBuilder = ApiErrorResponse.builder();

        if (ex instanceof ErrorResponse) {
            apiErrorResponseBuilder.message(ex.getMessage());
            status = ((ErrorResponse) ex).getStatusCode().value();
        }

        return ResponseEntity.status(status).body(apiErrorResponseBuilder.build());
    }
}
