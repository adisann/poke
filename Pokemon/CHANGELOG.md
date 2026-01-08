# Pokémon Game - Changelog

## Version 1.1.0 (2026-01-08)

### 🎮 Battle System Improvements

#### New Emerald-Style Move Selection UI
- Redesigned move selection screen to match Pokémon Emerald
- **Left panel**: 2x2 grid of move names with arrow selector
- **Right panel**: PP and TYPE info that updates on navigation
- Arrow key navigation (UP/DOWN/LEFT/RIGHT) between moves

#### Bug Fixes
- **Fixed FIGHT menu disappearing after using Bag/Pokémon**
  - Root cause: `moveSelectRoot` was hidden when opening Bag/Pokémon but never restored when closing
  - Fix: Added `moveSelectRoot.setVisible(true)` in `hideBagQuiet()` and `hideParty()`

### 🐛 Pokemon Naming
- Removed "Wild" prefix from caught Pokémon names
  - Wild Pokémon are now named without prefix (e.g., "Bulbasaur" instead of "Wild Bulbasaur")
  - Battle message still correctly shows "Wild Bulbasaur appeared!"

### 🧹 Code Cleanup
- Removed debug `System.out.println` statements from battle code
- Removed unused imports from `BattleScreen.java` (Texture, BitmapFont, PokemonGame)

---

## Version 1.0.0 (Initial Release)

- Core game engine with LibGDX
- Overworld exploration with multiple towns
- Wild Pokémon encounters in tall grass
- Turn-based battle system
- Pokémon catching with Pokéballs
- Inventory system with Bag UI
- Party management with Pokémon switching
- Save/Load game functionality
- Pokémon Center healing
- Type effectiveness system
