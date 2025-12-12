package com.articurated.shared.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class TimeRandomNumberGenerator implements NumberGenerator {
    @Override
    public String generate(String prefix) {
        return prefix + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
