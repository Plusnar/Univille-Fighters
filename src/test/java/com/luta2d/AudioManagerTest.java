package com.luta2d;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AudioManagerTest {
    @Test
    void testSingletonInstance() {
        AudioManager a1 = AudioManager.getInstance();
        AudioManager a2 = AudioManager.getInstance();
        assertSame(a1, a2);
    }

    @Test
    void testPlayMusicDoesNotThrow() {
        AudioManager audioManager = AudioManager.getInstance();
        assertDoesNotThrow(() -> audioManager.playMusic("menu", false));
    }
} 