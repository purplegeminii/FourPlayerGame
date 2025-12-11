## resources/icons README

### Purpose
- Store small icon images used by the in-game GUI (`GameWindow`) item renderer.
- The renderer will try to find PNG images in this folder and fall back to a programmatic shape if none are found.

### Filename conventions
- Filenames should be lowercase and use underscores for spaces: `iron_shield.png`, `swift_boots.png`.
- Recommended formats: PNG (lossless), optionally provide SVGs if you add runtime SVG support.

### Recommended sizes
- Provide at least 32×32 PNGs (the UI scales icons to 14×14 for list cells but higher-resolution assets look better on HiDPI screens).
- Optional: include 64×64 variants for high-DPI or scaled displays (name them with a suffix, e.g. `iron_shield@2x.png`).

### How the renderer selects an icon (lookup order)
1. Exact item name (sanitized): `iron_shield.png` — item name is lowercased and spaces become underscores.
2. Equipment slot name: the renderer reads `config/equipment_slots.properties` and will try the slot value (e.g., `hands.png`, `chest.png`).
3. Item type: the `itemType` string (e.g., `weapon.png`, `armor.png`, `potion.png`).
4. Common fallbacks: `weapon.png`, `armor.png`, `potion.png`, `consumable.png`, `default.png`.

### Notes for authors
- Prefer simple, clear icons that read small; avoid heavy detail at 14×14 pixels.
- Transparent backgrounds are recommended (RGBA) so icons look good against dark/light UI themes.
- If you want SVG support, mention it here and we can add a small SVG rasterizer dependency (e.g., Apache Batik) or pre-rasterize SVGs to PNG before shipping.

### Examples
- `resources/icons/iron_shield.png` — exact match for a shield named "Iron Shield".
- `resources/icons/hands.png` — generic hands/weapon slot icon (slot values are defined in `config/equipment_slots.properties`).
- `resources/icons/potion.png` — fallback for consumable items.
- `resources/icons/default.png` — always present to guarantee a visible icon when nothing else matches.

### Replace placeholders
- If you previously added placeholder text files in this directory, replace them with actual PNG files of the same name.

### Next Steps:
- Generate simple colored PNG placeholders (32×32) for each required name or replace them with different colors/symbols.
- Add `@2x` (64×64) variants for HiDPI.
- Add instructions for embedding the icons into the JAR for a packaged distribution.
