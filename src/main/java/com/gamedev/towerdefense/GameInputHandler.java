package com.gamedev.towerdefense;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.BudgetManager;
import com.gamedev.towerdefense.model.GameState;
import com.gamedev.towerdefense.model.NearestEnemyStrategy;
import com.gamedev.towerdefense.model.Position;
import com.gamedev.towerdefense.model.StrongestEnemyStrategy;
import com.gamedev.towerdefense.model.TargetingStrategy;
import com.gamedev.towerdefense.model.Tower;
import com.gamedev.towerdefense.model.WeakestEnemyStrategy;


public class GameInputHandler {

    private final TowerDefenseGame game;
    private final GameWorld gameWorld;

    public GameInputHandler(TowerDefenseGame game) {
        this.game = game;
        this.gameWorld = game.getGameWorld();
    }

    public void update() {
        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameWorld.getGameState() == GameState.PAUSED) {
                gameWorld.setGameState(GameState.PLAYING);
                return;
            }
            gameWorld.setGameState(GameState.PAUSED);
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T) && gameWorld.getSelectedTower() != null) {
            cycleTargetingStrategy(gameWorld.getSelectedTower());
        }

        int towerKey = getTowerKeyPressed();

        switch (towerKey) {
            case 1 -> toggleTowerSelection(0);
            case 2 -> toggleTowerSelection(1);
            case 3 -> toggleTowerSelection(2);
        }

        handleTowerPlacement();
        Tower clickedTower = handleTowerClick();
        if (clickedTower != null) {
            gameWorld.setSelectedTower(clickedTower);
        } else if (handleTowerDescriptionClick()) {
            // Click was handled by tower description
        } else if (Gdx.input.justTouched()) {
            gameWorld.setSelectedTower(null);
        }
    }

    private void cycleTargetingStrategy(Tower tower) {
        TargetingStrategy current = tower.getTargetingStrategy();
        TargetingStrategy next;

        if (current instanceof NearestEnemyStrategy) {
            next = new StrongestEnemyStrategy();
        } else if (current instanceof StrongestEnemyStrategy) {
            next = new WeakestEnemyStrategy();
        } else {
            next = new NearestEnemyStrategy();
        }

        tower.setTargetingStrategy(next);
    }

    private int getTowerKeyPressed() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            return 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            return 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            return 3;
        }
        return 0;
    }

    public void toggleTowerSelection(int towerIndex) {
        try {
            GameConfig gameConfig = game.getGameConfig();
            if (gameConfig == null || gameConfig.getTowerTypes() == null) {
                return;
            }

            if (towerIndex < 0 || towerIndex >= gameConfig.getTowerTypes().size()) {
                return;
            }

            GameConfig.TowerTypeConfig towerType = gameConfig.getTowerTypes().get(towerIndex);

            if (gameWorld.getSelectedTowerType() == towerType) {
                gameWorld.setSelectedTowerType(null);
            } else {
                gameWorld.setSelectedTowerType(towerType);
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Invalid tower index: " + towerIndex + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error toggling tower selection: " + e.getMessage());
        }
    }

    public Tower handleTowerClick() {
        if (!Gdx.input.justTouched()) {
            return null;
        }
        if (gameWorld.getSelectedTowerType() != null) {
            return null;
        }

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        Vector2 worldCoords = new Vector2(screenX, screenY);
        game.getViewport().unproject(worldCoords);

        for (Tower tower : gameWorld.getTowers()) {
            if (worldCoords.x + 20 > tower.getPosition().getX()
                    && worldCoords.x - 20 < tower.getPosition().getX()) {
                if (worldCoords.y + 20 > tower.getPosition().getY()
                        && worldCoords.y - 20 < tower.getPosition().getY()) {
                    return tower;
                }
            }
        }
        return null;
    }

    public void handleTowerPlacement() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        Vector2 worldCoords = new Vector2(screenX, screenY);
        game.getViewport().unproject(worldCoords);

        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        GameConfig.TowerTypeConfig selectedTowerType = gameWorld.getSelectedTowerType();
        BudgetManager budgetManager = gameWorld.getBudgetManager();

        if (selectedTowerType == null || !budgetManager.canAfford(selectedTowerType.getCost())) {
            return;
        }

        if (!gameWorld.isValidTowerPlacement(worldX, worldY, selectedTowerType.getRange())) {
            return;
        }

        Position towerPos = new Position(worldX, worldY);
        GameConfig gameConfig = game.getGameConfig();
        float projectileSpeed = selectedTowerType.getProjectileSpeed() > 0
                ? selectedTowerType.getProjectileSpeed()
                : (gameConfig.getProjectileSpeed() > 0 ? gameConfig.getProjectileSpeed() : TowerDefenseGame.DEFAULT_PROJECTILE_SPEED);

        Tower newTower = new Tower(
                selectedTowerType.getCost(),
                selectedTowerType.getRange(),
                selectedTowerType.getDamage(),
                selectedTowerType.getAttackCooldown(),
                projectileSpeed,
                towerPos,
                selectedTowerType.getId());

        gameWorld.getTowers().add(newTower);
        budgetManager.spend(selectedTowerType.getCost());
        gameWorld.setSelectedTowerType(null);
    }

    private boolean handleTowerDescriptionClick() {
        if (!Gdx.input.justTouched()) {
            return false;
        }
        Tower selectedTower = gameWorld.getSelectedTower();
        if (selectedTower == null) {
            return false;
        }

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        Vector2 worldCoords = new Vector2(screenX, screenY);
        game.getViewport().unproject(worldCoords);

        GameConfig gameConfig = game.getGameConfig();
        GameConfig.UpgradeConfig upgrades = gameConfig != null ? gameConfig.getUpgrades() : null;
        if (upgrades == null) {
            return false;
        }

        BudgetManager budgetManager = gameWorld.getBudgetManager();

        if (game.getDamageTextBounds().contains(worldCoords.x, worldCoords.y)) {
            int damageCost = upgrades.getDamageCost();
            int damageAmount = upgrades.getDamageAmount();
            if (budgetManager.canAfford(damageCost)) {
                budgetManager.spend(damageCost);
                selectedTower.increaseDamage(damageAmount);
            }
            return true;
        }
        if (game.getRangeTextBounds().contains(worldCoords.x, worldCoords.y)) {
            int rangeCost = upgrades.getRangeCost();
            int rangeAmount = upgrades.getRangeAmount();
            if (budgetManager.canAfford(rangeCost)) {
                budgetManager.spend(rangeCost);
                selectedTower.increaseRange(rangeAmount);
            }
            return true;
        }
        if (game.getCooldownTextBounds().contains(worldCoords.x, worldCoords.y)) {
            int cooldownCost = upgrades.getCooldownCost();
            float cooldownAmount = upgrades.getCooldownAmount();
            if (budgetManager.canAfford(cooldownCost)) {
                budgetManager.spend(cooldownCost);
                selectedTower.decreaseAttackCooldown(cooldownAmount);
            }
            return true;
        }
        // Strategy toggle - click to cycle through targeting strategies
        if (game.getStrategyTextBounds().contains(worldCoords.x, worldCoords.y)) {
            cycleTargetingStrategy(selectedTower);
            return true;
        }
        return false;
    }
}
