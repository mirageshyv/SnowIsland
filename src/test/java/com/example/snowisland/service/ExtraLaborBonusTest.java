package com.example.snowisland.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtraLaborBonusTest {

    @Test
    void bonusIsHalfRounded() {
        assertEquals(5, ActionService.extraLaborBonusQuantity(10, 1));
        assertEquals(8, ActionService.extraLaborBonusQuantity(15, 1));
        assertEquals(10, ActionService.extraLaborBonusQuantity(10, 2));
        assertEquals(1, ActionService.extraLaborBonusQuantity(1, 1));
        assertEquals(0, ActionService.extraLaborBonusQuantity(0, 1));
        assertEquals(0, ActionService.extraLaborBonusQuantity(10, 0));
    }
}
