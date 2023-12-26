package br.com.investmentmanager.shared.application.rest.util;

import br.com.investmentmanager.MockRequestContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpUtilsTest implements MockRequestContext {

    @Test
    void createEntityLocation() {
        var entityId = UUID.randomUUID();
        var expectedLocation = URI.create("http://localhost:8080/entity/" + entityId);

        assertEquals(expectedLocation, HttpUtils.createEntityLocation(entityId));
    }
}