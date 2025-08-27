package com.luta2d;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class GameWindowTest {
    @Test
    void testGameWindowInstantiation() {
        assertDoesNotThrow(() -> new GameWindow());
    }
} 