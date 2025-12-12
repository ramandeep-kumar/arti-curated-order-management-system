package com.articurated.util;

/**
 * Small utility to generate stable order numbers for tests and examples.
 */
public final class IdGenerator {

    private IdGenerator() {}

    /**
     * Generate an order number with prefix ARTI- and zero-padded numeric id (6 digits).
     */
    public static String generateOrderNumber(long id) {
        return String.format("ARTICURATE-%06d", id);
    }
}
