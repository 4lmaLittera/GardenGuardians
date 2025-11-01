package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BudgetManagerTest {

    @Test
    public void testConstructor() {
        BudgetManager budgetManager = new BudgetManager(100);
        assertEquals(100, budgetManager.getBudget());
        assertNotEquals(0, budgetManager.getBudget());
    }

    @Test
    public void setGetBudget() {
        BudgetManager budgetManager = new BudgetManager(0);
        budgetManager.setBudget(69);
        assertEquals(69, budgetManager.getBudget());
        assertNotEquals(0, budgetManager.getBudget());
    }

    @Test
    public void spend() {
        BudgetManager budgetManager = new BudgetManager(100);
        budgetManager.spend(50);
        assertEquals(50, budgetManager.getBudget());
        assertNotEquals(100, budgetManager.getBudget());
    }

    @Test
    public void earn() {
        BudgetManager budgetManager = new BudgetManager(0);
        budgetManager.earn(69);
        assertEquals(69, budgetManager.getBudget());
        assertNotEquals(100, budgetManager.getBudget());

    }

    @Test
    public void canAfford() {
        BudgetManager budgetManager = new BudgetManager(50);
        assertTrue(budgetManager.canAfford(50));
        assertFalse(budgetManager.canAfford(51));

    }
}
