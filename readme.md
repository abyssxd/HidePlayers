
# HidePlayers

A lightweight Spigot plugin that lets each player toggle whether they see other players. Ideal for crowded lobbies, parkour areas, and large events. Includes an item-based toggle, permissions, and configurable messages.

---

## Features

- **Toggle Command & Item**
  - Players with `hideplayers.use` can toggle visibility via `/hideplayers toggle` or by right-clicking the toggle item.
  - Admins with `hideplayers.admin` can set the toggle item in-game using `/hideplayers setitem`.

- **Configurable Item**
  - Choose any material, name, lore, and optional glow effect.
  - Decide if it can be moved or dropped.
  - Auto-give the item on join in a specific slot.

- **Messages File**
  - Customize all plugin messages (permission errors, toggle alerts, etc.) in `messages.yml`.

- **BossBar Notification**
  - When players hide others, a configurable BossBar can appear (color, style, message).

- **World Whitelist**
  - Restrict the plugin to certain worlds.
  - If enabled, players outside these worlds can’t toggle visibility or receive the item.

---

## Installation

1. **Download** the latest `.jar` and place it into your server’s `plugins` folder.
2. **Start** or **restart** your server to generate `config.yml` and `messages.yml`.
3. **Configure** the plugin (see below).
4. **Adjust Permissions** as needed in your permission manager.

---

## Configuration

### `config.yml` (Sample)

```yaml
toggle-item:
  material: BLAZE_ROD
  name: "&aToggle Players"
  lore:
    - "&7Click to hide/show players"
  glow: true

auto-give:
  enabled: true
  slot: 8

options:
  movable: false
  droppable: false

bossbar:
  enabled: true
  color: "RED"
  style: "SOLID"

worlds:
  use-whitelist: false
  whitelist:
    - "world"
    - "world_nether"
```

- **`toggle-item`**: Defines the material, display name, lore lines, and glow effect for the Hide/Show item.
- **`auto-give`**: Automatically gives the item in the specified slot when players join, if enabled.
- **`options.movable`**: Whether players can move the item.
- **`options.droppable`**: Whether the item can be dropped.
- **`bossbar.enabled`**: Whether to show a BossBar when a player hides others.
- **`bossbar.color`** and **`bossbar.style`**: The color and style of the BossBar.
- **`worlds.use-whitelist`**: If true, the plugin only functions in the worlds listed in `whitelist`.
- **`worlds.whitelist`**: List of world names where the plugin works.

### `messages.yml` (Sample)

```yaml
no_permission: "&cYou do not have permission!"
player_only: "&cOnly players can do this!"
toggle_on: "&aAll players hidden!"
toggle_off: "&aYou can now see all players!"
setitem_no_item: "&cYou must be holding a valid item to set."
setitem_success: "&aHide/Show toggle item has been updated in the config!"
bossbar_message: "&cPlayers are hidden!"
disallowed_world: "&cYou cannot toggle player visibility in this world."
```

---

## Permissions

- **`hideplayers.use`**: Allows toggling visibility via command or by right-clicking the toggle item.
- **`hideplayers.admin`**: Allows setting the Hide/Show item using `/hideplayers setitem`.

---

## Commands

- **`/hideplayers toggle`**  
  Hides or shows other players for the command sender.
- **`/hideplayers setitem`**  
  Sets the current item in hand as the Hide/Show item (admin only).

---

## License

This plugin is licensed under the **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)**.

### You are free to:
- **Share**: Copy and redistribute the material in any medium or format.
- **Adapt**: Remix, transform, and build upon the material.

### Under the following terms:
- **Attribution**: You must give appropriate credit, provide a link to the license, and indicate if changes were made.
- **NonCommercial**: You may not use the material for commercial purposes.
- **ShareAlike**: If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.

For more details, see [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International](https://creativecommons.org/licenses/by-nc-sa/4.0/).

