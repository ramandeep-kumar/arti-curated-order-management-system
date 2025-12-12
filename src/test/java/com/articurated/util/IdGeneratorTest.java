package com.articurated.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdGeneratorTest {

    @Test
    void generateOrderNumber_padsAndPrefixes() {
        assertEquals("ARTI-000001", IdGenerator.generateOrderNumber(1));
        assertEquals("ARTI-000123", IdGenerator.generateOrderNumber(123));
        assertEquals("ARTI-999999", IdGenerator.generateOrderNumber(999999));
    }
}
