
# FourPlayerGame

A compact, turn-based Java RPG (console + lightweight Swing GUI) for local play or automated simulation.

This repository demonstrates a simplified RPG loop: players (1–4) progress through floors, fight randomized monsters, collect equipment and consumables, manage inventory/equipment, and persist game state to JSON.

## Highlights

- Turn-based combat with job-specific skills (Warrior, Mage, Thief, Swordsman).
- Equipment and consumables with simple stat-scaling and rank tiers.
- Interactive Swing GUI mode with a visual floor view and action buttons.
- Auto-play mode for non-interactive simulations and automated testing.
- Simple JSON save/load (`save_progress.json`) and an auto-save for simulations (`save_auto_progress.json`).
- Configurable equipment-slot mapping via `config/equipment_slots.properties`.

## Requirements

- Java 11 or newer (JDK 11+).

## Build

Compile all sources to `out/`:

```bash
cd /Users/purplegeminii/Desktop/FourPlayerGame
javac src/*.java -d out
```

If you prefer, create `out/` first and run the command from project root; the `-d out` flag places class files under `out/`.

## Run (interactive console)

Start the game in classic console interactive mode:

```bash
java -cp out Main
```

You will be asked whether to load an existing save or start a new game, how many players (1–4), and each player's name and job.

## Run (GUI)

There is a lightweight Swing-based visualizer. It provides a drawing area for players/monsters, a right-side control panel with action buttons (Attack, Status, Inventory, Equip, Use Item, Use Skill, Pickup, Inspect), and basic click-to-select for monsters.

To launch the GUI mode:

```bash
java -cp out Main gui
```

Notes about the GUI:
- The **Status** button shows the selected player's full status in a scrollable dialog.
- The **Attack** flow presents a skill selection dialog and applies damage using the existing game logic.
- After actions that consume a turn, monsters may retaliate; the GUI schedules repaint/refresh so visuals and floor items remain up-to-date.
- Some messages are still printed to the console; adding an in-GUI combat log is a recommended next step.

## Run (auto-play / simulation)

Auto-play runs the game without interactive prompts — useful for testing or balance tuning:

```bash
java -cp out Main auto <turns> <checkpoint>
```

- `<turns>`: number of auto turns to simulate (default: 50)
- `<checkpoint>`: how often to print a post-turn summary (default: 5)

Example: run 100 turns and print a summary every 10 turns:

```bash
java -cp out Main auto 100 10
```

## Gameplay controls (interactive)

During a player's turn the console (or GUI) shows actions. Common actions:

- `0` — attack (choose a job skill and target)
- `1` — status (show full status; informational, does not consume the turn)
- `3` — inventory (view items; informational)
- `4` — equip gear from inventory (consumes the turn)
- `5` — drink potion / use consumable (consumes the turn)
- `6` — use skill (consumes the turn and costs MP)
- `7` — pickup items from floor (consumes the turn)
- `8` — inspect item (informational)
- `9` — save game (writes `save_progress.json`)
- `10` — load game (use main menu to load; not allowed mid-battle)

In the GUI the same action codes are mapped to buttons; informational actions open dialogs or print to console where appropriate.

## GUI Features & Known Limitations

- The GUI renders players and monsters as simple shapes with HP bars and supports clicking on monsters to select them.
- The GUI uses the same game logic as console mode (e.g., `Player.attack(...)`), so side effects (XP, loot, equip) behave identically.
- Known limitations:
	- Some game messages still go to `System.out`. Adding a scrolling in-GUI combat log is recommended (planned).
	- Auto-play and GUI actions run on separate threads in some places; while basic synchronization is applied, a full event-driven game loop would be more robust.

## Configuration

- Equipment slot mapping: `config/equipment_slots.properties` contains substring -> slot entries (e.g. `sword=hands`). Edit this file and restart the program to pick up changes.

## Save files

- `save_progress.json` — written when you choose to save the game in interactive mode. Contains floor number, players (stats, inventory, equipped items).
- `save_auto_progress.json` — written at the end of auto-play runs.

## Project layout

- `src/` — Java source files
	- `Main.java` — entry point, game loop, GUI launch and auto-play helper
	- `Player.java` — player model, inventory/equipment logic, skills, XP/level logic
	- `Monster.java` — monster model, HP/MP/damage, XP and drops
	- `Floor.java` — spawns monsters and floor items
	- `Item.java`, `Equipment.java`, `Consumable.java` — item hierarchy
	- `Skill.java` — skill metadata
- `config/` — runtime configuration files (e.g. `equipment_slots.properties`)
- `out/` — compiled classes (created by `javac -d out`)

## Troubleshooting

- Scanner/input: the program uses a single shared `Scanner` instance (`Main.input`). Avoid creating additional `Scanner(System.in)` instances; closing other scanners can close `System.in` and break interactive input.
- GUI not repainting: the GUI schedules repaints and refreshes after model updates, but if you see stale data try forcing a refresh by interacting with controls or reopening the window.
- Save/Load issues: check `save_progress.json` is writable in the working directory and that your `players` array is set before attempting to load in GUI mode.

## Development notes

- The code prioritizes clarity for experimentation and teaching over production-quality architecture.
- Recommended next steps: add an in-GUI combat log, consolidate the game loop into a single event thread, and improve auto-play AI for potion/equipment choices.

## Contributing

- Make a branch for your change and open a PR with a description of your goal.
- Keep changes small and focused: add a skill, add a monster type, or tweak a scaling constant.

See `CONTRIBUTING.md` for contribution guidelines and `CODE_OF_CONDUCT.md` for community expectations.

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

Next Steps:
- Improve thread-safety by moving the game loop onto a single event thread.
- Replace the ad-hoc JSON helpers with a library like Gson for more robust save/load behavior.

Plans for Sprite / Asset Handling Libraries (libGDX)
----------------------------------------------------
This project can benefit from a lightweight asset pipeline for icons and sprites. Below are suggested approaches using libGDX depending on how much of the app you want to migrate.

- Option A — libGDX for asset loading only (recommended first step):
	- Keep the existing Swing UI. Add libGDX as a dependency (Gradle) and use the headless backend.
	- Use libGDX's AssetManager / Pixmap / Texture to load images and (when needed) TextureAtlas for sprite atlases.
	- Convert libGDX Pixmap data to AWT `BufferedImage` (helper class) so `GameWindow` can continue to use `ImageIcon`/Swing components.
	- Pros: much improved image/atlas handling with minimal GUI changes. Cons: requires adding libGDX to the build (Gradle) and a small adapter layer.

- Option B — Full migration to libGDX rendering (larger effort):
	- Replace Swing with libGDX rendering (Scene2D or raw rendering) for GPU-accelerated drawing, animation, and a single game loop.
	- Pros: high-performance rendering, atlases, built-in asset lifecycle. Cons: significant refactor of UI and input handling; new build and native dependencies.

- Option C — Lightweight image improvements without libGDX:
	- Improve icon handling with higher-quality PNGs, ImageIO, or small helper libraries (no full game framework). Less capability than libGDX but simpler.

Suggested immediate next step
- Start with Option A: add a small Gradle build and a `LibGdxAssetLoader` adapter that initializes the libGDX headless backend and exposes a method to load PNGs as `BufferedImage`. This gives atlas and Pixmap advantages without rewriting the UI.

If you'd like, I can scaffold the Gradle files and the small adapter class (AssetManager/Pixmap -> BufferedImage) and update `GameWindow` to call it when available (falling back to the existing loader when not running under Gradle).
