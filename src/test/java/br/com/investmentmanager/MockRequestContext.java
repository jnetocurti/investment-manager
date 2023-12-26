package br.com.investmentmanager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public interface MockRequestContext {

    @BeforeEach
    default void setUp() {
        var request = new MockHttpServletRequest();
        request.setServerPort(8080);
        request.setRequestURI("/entity");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    default void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }
}
