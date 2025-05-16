# Lunarstuff - Lunar Client Apollo API Integration

A Minecraft plugin that enhances server functionality for Lunar Client users through the Apollo API. This plugin adds staff utility tools, custom nametags, team management, rich presence integration, and limb visibility controls.

## Features

- **Staff Utility Mode**: Gives staff members access to XRAY vision for moderation
- **Custom Nametags**: Display team information and player details in customizable nametag formats
- **Team Integration**: Seamless integration with BetterTeams plugin to show team members on the minimap
- **Rich Presence**: Customizable Discord rich presence for players showing server and player information
- **Limb Management**: Ability to hide or show specific player body parts for cosmetic or fun purposes

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/staffmode` | Toggle XRAY vision mode | `apollo.staff` |
| `/limb <take\|put\|list> <player> [limb]` | Manage player limb visibility | `lunarstuff.limb` |
| `/refreshteam` | Refreshes all teams | `lunarstuff.refreshteam` |

## Configuration

The plugin is highly configurable through the `config.yml` file:

```yaml
staff-mode:
  enabled: true

nametag:
  enabled: true
  lines:
    - "&7Team: %betterteams_name%"
    - "%player_displayname%"
    - "&7Balance: $%betterteams_balance%"

richpresence:
  enabled: true
  update-interval: 100
  gameName: "MyServer"
  gameVariantName: "%player_world%"
  # Additional rich presence options...
```

## Dependencies

- Spigot/Paper 1.21+
- Lunar Client Apollo API
- BetterTeams (optional but recommended)
- PlaceholderAPI (optional for enhanced placeholders)

## Installation

1. Download the latest release JAR
2. Place in your server's `plugins` folder
3. Start/restart your server
4. Edit `config.yml` to customize the plugin behavior
5. Use the commands to manage plugin features

## Building from Source

```bash
git clone https://github.com/oyuh/lunarstuff.git
cd lunarstuff
mvn clean package
```

The built JAR will be in the `target` directory.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
