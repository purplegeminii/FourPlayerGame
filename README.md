
FourPlayerGame
==============

A compact, turn-based Java console RPG for local play or automated simulation.

This repository contains a small Java project that demonstrates a simplified RPG loop: players (1–4) progress through floors, fight randomized monsters, collect equipment and consumables, manage inventory/equipment, and persist game state to JSON.

Highlights
---------
- Turn-based combat with job-specific skills (Warrior, Mage, Thief, Swordsman).
- Equipment and consumables with simple stat-scaling and rank tiers.
- Auto-play mode for non-interactive simulations and automated testing.
- Simple JSON save/load (`save_progress.json`) and an auto-save for simulations (`save_auto_progress.json`).
- Configurable equipment slot mapping via `config/equipment_slots.properties`.

Requirements
------------
- Java 11 or newer (JDK 11+).

Build
-----
Compile all sources to `out/`:

```bash
cd /Users/purplegeminii/Desktop/FourPlayerGame
javac src/*.java -d out
```

Run (interactive)
------------------
Start the game in interactive mode:

```bash
java -cp out Main
```

At startup you will be asked whether to load an existing save or start a new game, how many players will play (1–4), and each player's name and job.

Run (auto-play / simulation)
---------------------------
Auto-play runs the game without interactive prompts and is useful for testing or balance tuning:

```bash
java -cp out Main auto <turns> <checkpoint>
```

- `<turns>`: number of auto turns to simulate (default: 50)
- `<checkpoint>`: how often to print a post-turn summary (default: 5)

Example: run 100 turns and print a summary every 10 turns:

```bash
java -cp out Main auto 100 10
```

Gameplay controls (interactive)
-------------------------------
During a player's turn you are shown a numbered list of actions. Common actions:

- `0` — attack (choose a job skill and target)
- `1` — status (show full status; informational, does not consume the turn)
- `3` — inventory (view items; informational)
- `4` — equip gear from inventory (consumes the turn)
- `5` — drink potion / use consumable (consumes the turn)
- `6` — use skill (consumes the turn and costs MP)
- `7` — pickup items from floor (consumes the turn)
- `8` — inspect item (informational)
- `9` — save game (writes `save_progress.json`; does not consume turn in current UI)
- `10` — load game (use main menu to load; not allowed mid-battle)

Configuration
-------------
- Equipment slot mapping: `config/equipment_slots.properties` contains simple substring -> slot entries (e.g. `sword=hands`). Edit this file to register new equipment name patterns without changing source code.

Save files
----------
- `save_progress.json` — written when you choose to save the game in interactive mode. Contains floor number, players (stats, inventory, equipped items).
- `save_auto_progress.json` — written at the end of auto-play runs.

Project layout
--------------
- `src/` — Java source files
	- `Main.java` — entry point, game loop and auto-play helper
	- `Player.java` — player model, inventory/equipment logic, skills, XP/level logic
	- `Monster.java` — monster model, HP/MP/damage, XP and drops
	- `Floor.java` — spawns monsters and floor items
	- `Item.java`, `Equipment.java`, `Consumable.java` — item hierarchy
	- `Skill.java` — skill metadata
- `config/` — runtime configuration files (e.g. `equipment_slots.properties`)
- `out/` — compiled classes (created by `javac -d out`)

Troubleshooting
---------------
- Scanner/input errors: the program uses a single shared `Scanner` instance (`Main.input`). Avoid creating additional `Scanner(System.in)` instances or piping mixed input that can close `System.in`.
- If the game feels unbalanced: adjust scaling factors in `Monster`, `Equipment`, or `Player` (damage, stat deltas, XP rates).
- If you edit `config/equipment_slots.properties`, restart the program to reload the mapping (it's read on class initialization).

Development notes
-----------------
- The code prioritizes clarity for experimentation and teaching over production-quality architecture.
- Auto-play uses conservative heuristics (auto-pickup, auto-equip best items, basic potion-usage can be added).

Contributing
------------
- Make a branch for your change and open a PR with a description of your goal.
- Keep changes small and focused: add a skill, add a monster type, or tweak a scaling constant.

Future improvements (ideas)
--------------------------
- Deterministic auto-play with seeded RNG and a replay log for debugging.
- More sophisticated AI for auto-play: conditional potion usage, equipment priorities, team tactics.
- Replace ad-hoc JSON handling with a small library (e.g., Gson) to make save/load more robust.

License
-------
This project is currently unlicensed. Add a `LICENSE` file if you want to apply an open-source license.

Questions / next steps
---------------------
If you'd like, I can:
- Add smarter auto-play behaviors (potions, equip priorities).
- Add a CLI flag to print the loaded equipment-slot mapping at startup.
- Replace the simple JSON helpers with a lightweight JSON library for more robust save/load.
