package com.luta2d;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CharacterTest {
    @Test
    void testConstructorAndGetters() {
        Character c = new Character("Anna", 1);
        assertEquals("Anna", c.getName());
        assertEquals(1, c.getId());
        assertEquals(300, c.getHealth());
        assertEquals(15.0, c.getAttackDamage());
        assertEquals(8, c.getMoveSpeed());
        assertEquals(30, c.getJumpHeight());
        assertEquals(200, c.getAttackRange());
        assertEquals(0.5, c.getBlockReduction());
    }

    @Test
    void testSetters() {
        Character c = new Character("Anna", 1);
        c.setName("Jean");
        c.setId(2);
        c.setAttackDamage(20.0);
        c.setMoveSpeed(10);
        c.setJumpHeight(40);
        c.setAttackRange(250);
        c.setBlockReduction(0.7);
        assertEquals("Jean", c.getName());
        assertEquals(2, c.getId());
        assertEquals(20.0, c.getAttackDamage());
        assertEquals(10, c.getMoveSpeed());
        assertEquals(40, c.getJumpHeight());
        assertEquals(250, c.getAttackRange());
        assertEquals(0.7, c.getBlockReduction());
    }

    @Test
    void testTakeDamageAndRestoreHealth() {
        Character c = new Character("Anna", 1);
        c.takeDamage(50);
        assertEquals(250, c.getHealth());
        c.takeDamage(300);
        assertEquals(0, c.getHealth()); // Não pode ser negativo
        c.restoreHealth();
        assertEquals(300, c.getHealth());
    }

    @Test
    void testAttack() {
        Character c1 = new Character("Anna", 1);
        Character c2 = new Character("Jean", 2);
        c1.attack(c2);
        assertEquals(285, c2.getHealth());
    }

    @Test
    void testDamageTypes() {
        Character c = new Character("Anna", 1);
        assertEquals(15.0, c.getPunchDamage());
        assertEquals(22.5, c.getKickDamage());
        assertEquals(60.0, c.getSpecialAttackDamage());
        assertEquals(90.0, c.getUltimateDamage());
    }
} 