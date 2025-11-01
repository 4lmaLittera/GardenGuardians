package com.gamedev.towerdefense;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gamedev.towerdefense.model.BudgetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.gamedev.towerdefense.model.Enemy;
import com.gamedev.towerdefense.model.Path;
import com.gamedev.towerdefense.model.Position;

public class TowerDefenseGame extends ApplicationAdapter {

    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private BudgetManager budgetManager;

    private BitmapFont font;

    private Path path;
    private Enemy enemy;
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        // Initialize font
        font = new BitmapFont();
        font.getData().setScale(1f, 1f);

        // Initialize budget manager
        budgetManager = new BudgetManager(110);

        // Initialize path
        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(WORLD_WIDTH, WORLD_HEIGHT/2));
        waypoints.add(new Position(WORLD_WIDTH/2, WORLD_HEIGHT/2));
        waypoints.add(new Position(250, 500));
        waypoints.add(new Position(0, WORLD_HEIGHT/2));
        path = new Path(waypoints);

        // Initialize enemy
        enemy = new Enemy(path, 100, 10.0f, 0);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        enemy.update(Gdx.graphics.getDeltaTime());

        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1); // White color
        List<Position> waypoints = path.getWaypoints();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Position start = waypoints.get(i);
            Position end = waypoints.get(i + 1);
            shapeRenderer.line(start.getX(), start.getY(), end.getX(), end.getY());
        }
        shapeRenderer.end();
        
        // Draw waypoint markers
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw start waypoint (green)
        if (waypoints.size() > 0) {
            Position startPos = waypoints.get(0);
            shapeRenderer.setColor(0, 1, 0, 1); // Green
            shapeRenderer.circle(startPos.getX(), startPos.getY(), 10);
        }
        
        // Draw end waypoint (red)
        if (waypoints.size() > 1) {
            Position endPos = waypoints.get(waypoints.size() - 1);
            shapeRenderer.setColor(1, 0, 0, 1); // Red
            shapeRenderer.circle(endPos.getX(), endPos.getY(), 10);
        }
        
        // Draw enemy (blue)
        Position enemyPos = enemy.getPosition();
        shapeRenderer.setColor(0, 0, 1, 1); // Blue
        shapeRenderer.circle(enemyPos.getX(), enemyPos.getY(), 15);
        
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Render budget
        font.draw(batch, "Budget: " + budgetManager.getBudget(), 10, WORLD_HEIGHT - 10);

        // Render path

        // Render enemy

        // TODO: render tower defense gameplay elements here

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
