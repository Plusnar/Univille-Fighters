package com.luta2d;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DummyWindow extends GameWindow {}

public class ArenaScreenTest {
    @Test
    void testArenaScreenInstantiation() {
        Character c1 = new Character("Anna", 1);
        Character c2 = new Character("Jean", 2);
        assertDoesNotThrow(() -> new ArenaScreen(new DummyWindow(), c1, c2, "Arena1"));
    }
} 