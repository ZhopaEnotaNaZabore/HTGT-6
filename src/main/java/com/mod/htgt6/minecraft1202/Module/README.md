# Minecraft_1.20.2 modules

Backport implementation sources are organized here by responsibility:

- `Blocks` — blocks, item-blocks and tile entities;
- `Items` — items, equipment and item-specific behavior;
- `Entity` — server-side entity implementations;
- `World` — Futura, biomes, terrain, structures and world generation;
- `Client` — renderers, models, sounds and client handlers;
- `Network` — packets and network channels;
- `Recipes` — recipe implementations and recipe-book support;
- `Registry` — update entry points and content registries;
- `Events` — Forge/FML event integration;
- `Data` — fixtures, persisted data contracts and validation;
- `Gameplay` — mechanics which do not belong to a narrower category.

Each category is split by Minecraft update (`Bountiful`, `Combat`, `Aquatic`,
`Modern`, and so on).

Some source files intentionally retain their historical Java `package`
declaration. HTGT6 ASM patches link to those binary names directly, and moving
the binary classes would require changing ASM. File location does not control
Forge ownership: registration is performed only while the
`Minecraft_1.20.2` mod container is active.
