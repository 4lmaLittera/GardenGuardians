package com.gamedev.towerdefense.model;

public class BudgetManager {
    private int budget;

    public BudgetManager(int budget) {
        this.budget = budget;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public void spend(int cost){
        this.budget -= cost;
    }

    public void earn(int amount){
        this.budget += amount;
    }

    public boolean canAfford(int amount){
        if (this.budget - amount >=0) {
            return true;
        }
        return false;
    }
    
    
}