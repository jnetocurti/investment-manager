package br.com.investmentmanager.shared.application.rest;

import br.com.investmentmanager.MockRequestContext;
import br.com.investmentmanager.shared.domain.exceptions.EntityAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultExceptionHandlerTest implements MockRequestContext {

    private final DefaultExceptionHandler exceptionHandler = new DefaultExceptionHandler();

    @Test
    void handleEntityAlreadyExistsException() {
        var entityId = UUID.randomUUID();
        var expectedLocation = URI.create("http://localhost:8080/entity/" + entityId);

        var response = exceptionHandler.handleEntityAlreadyExistsException(
                new EntityAlreadyExistsException("The entity already exists", entityId));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Unable to process request", response.getBody().getTitle());
        assertEquals("The entity already exists", response.getBody().getMessage());
        assertEquals(expectedLocation, ((Map) response.getBody().getDetails()).get("location"));
    }

    @Test
    void handleThrowable() {
        var response = exceptionHandler.handleThrowable(new RuntimeException("some error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to process request", response.getBody().getTitle());
        assertEquals("Sorry something went wrong. Please try again later", response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void handleThrowableInstanceofErrorResponse() {
        var response = exceptionHandler.handleThrowable(
                new HttpRequestMethodNotSupportedException(HttpMethod.GET.name()));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Unable to process request", response.getBody().getTitle());
        assertEquals("Request method 'GET' is not supported", response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }
}