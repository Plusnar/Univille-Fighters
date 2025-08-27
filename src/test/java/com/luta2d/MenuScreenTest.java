package com.luta2d;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DummyGameWindow extends GameWindow {}

public class MenuScreenTest {
    @Test
    void testMenuScreenInstantiation() {
        assertDoesNotThrow(() -> new MenuScreen(new DummyGameWindow()));
    }
} 