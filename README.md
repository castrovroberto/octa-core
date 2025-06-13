# OctaCore

OctaCore is a Java library providing core logic for an octagonal cell grid game.

## Features
- Ring-based octagonal grid representation
- Bidirectional linking of cells in eight directions (N, NE, E, SE, S, SW, W, NW)
- Cell states (`EMPTY`, `OCCUPIED`, `BLOCKED`)
- Utility functions for direction randomization and coordinate key generation
- `GameLogic` interface for move validation and game over checks
- Simple game engine scaffold (`GameEngine`)

## Project Structure
```
src/
  main/
    java/
      tech/yump/core/       # Core models and map builder
        OctaCell.java
        GameMap.java
        GameLogic.java
      tech/yump/model/      # Enums and basic types
        Direction.java
        CellState.java
      tech/yump/util/       # Utility classes
        CellUtils.java
        Coordinate.java
      tech/yump/engine/     # Game engine scaffolding
        GameEngine.java
    resources/              # Resource files (if any)
  test/                    # Unit tests (currently empty)
pom.xml                   # Maven build file
```

## Installation
Clone the repository and build with Maven:
```bash
git clone https://github.com/robertocastro/octa-core.git
cd octa-core
mvn clean install
```

## Usage
```java
import tech.yump.core.GameMap;
import tech.yump.engine.GameEngine;

public class Main {
  public static void main(String[] args) {
    GameEngine engine = new GameEngine();
    engine.startGame(); // Initializes the map with default settings
    // Implement turn loop and use GameLogic to process moves
  }
}
```

## Contributing
- Fork the repository and create a feature branch
- Submit pull requests against the `main` branch

## Next Steps
- Implement a concrete `GameLogic` class to define valid moves and win conditions
- Add unit tests for core classes (`OctaCell`, `GameMap`, `CellUtils`)
- Enhance `GameEngine` with turn processing loop and I/O

## License
This project is licensed under the MIT License. (Add LICENSE file as needed)