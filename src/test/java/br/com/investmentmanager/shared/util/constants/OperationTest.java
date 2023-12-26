package br.com.investmentmanager.shared.util.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OperationTest {

    @Test
    void of() {
        assertEquals(Operation.BUY, Operation.of("B"));
        assertThrows(IllegalArgumentException.class, () -> Operation.of(""));
    }
}