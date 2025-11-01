# Garden Defense (LibGDX)

A LibGDX-based 2D tower defense prototype focused on protecting a garden from waves of pests.

## Prerequisites

- Java 17 or newer
- Gradle 8.0+ (or add the Gradle wrapper via `gradle wrapper`)

## Getting Started

### Build

```
gradle build
```

### Run (Desktop)

```
gradle run
```

The desktop launcher (`com.gamedev.towerdefense.DesktopLauncher`) boots the core game class `TowerDefenseGame`, which currently prepares the rendering pipeline and clears the screen.

## Next Steps

- Implement gameplay entities (towers, pests, projectiles)
- Lay out pathing and wave logic
- Add UI for budget management and tower placement

