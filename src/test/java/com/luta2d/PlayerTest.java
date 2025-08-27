package com.luta2d;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    @Test
    void testConstructorAndGetters() {
        Character c = new Character("Anna", 1);
        Player p = new Player(c, 100, 200, 500);
        assertEquals(c, p.getCharacter());
        assertEquals(100, p.getX());
        assertEquals(200, p.getY());
        assertEquals(400, p.getWidth());
        assertEquals(400, p.getHeight());
    }

    @Test
    void testSetters() {
        Character c = new Character("Anna", 1);
        Player p = new Player(c, 100, 200, 500);
        p.setX(150);
        p.setY(250);
        assertEquals(150, p.getX());
        assertEquals(250, p.getY());
    }

    @Test
    void testMoveLeftAndRight() {
        Character c = new Character("Anna", 1);
        Player p = new Player(c, 100, 200, 500);
        int initialX = p.getX();
        p.moveRight();
        assertTrue(p.getX() > initialX);
        p.moveLeft();
        assertEquals(initialX, p.getX());
    }

    @Test
    void testCrouchingAndBlocking() {
        Character c = new Character("Anna", 1);
        Player p = new Player(c, 100, 200, 500);
        p.setCrouching(true);
        assertTrue(p.isCrouching());
        p.setCrouching(false);
        assertFalse(p.isCrouching());
        p.setBlocking(true);
        assertTrue(p.isBlocking());
        p.setBlocking(false);
        assertFalse(p.isBlocking());
    }
} 