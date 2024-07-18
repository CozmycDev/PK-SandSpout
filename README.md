# SandSpout Ability for ProjectKorra

This is an addon ability for the [ProjectKorra](https://projectkorra.com/) plugin for Spigot Minecraft servers. Inspired by the original ability that was removed from core years ago.

https://github.com/user-attachments/assets/9f76dea1-1db4-410b-a470-ee14e537c0e9

## Description

**SandSpout** is a Sand ability that allows earthbenders to use sand itself for mobility. This move requires that you are standing on, or are over sand. Sand blocks are configured from the global PK config.

### Features

- **Sand Column**: Raise yourself into the air on a column of sand.
- **Blindning Effect**: Applies temporary blindness to other entities within the column.
- **Damage**: Applies damage to entities within the column.
- **Sand Configuration**: Uses the global sand configuration `Properties.Earth.SandBlocks`, so you can add SOUL_SAND, '#concrete_powder', etc.

## Instructions

- **Activation**: While standing on a sand block, Left Click to activate or deactivate. Hold Space to go up, Shift to go down.

## Installation

1. Download the latest `sandspout.jar` file from [releases](https://github.com/CozmycDev/PK-SandSpout/releases).
2. Place the latest `sandspout.jar` file in the `./plugins/ProjectKorra/Abilities` directory.
3. Restart your server or reload the ProjectKorra plugin with `/b reload` to enable the ability.

## Compatibility

- **Minecraft Version**: Tested and working on MC 1.20.4.
- **ProjectKorra Version**: Tested and working on PK 1.11.2 and 1.11.3. Might support earlier versions too.

## Configuration

The ability can be configured in the ProjectKorra `config.yml` file under `ExtraAbilities.Cozmyc.SandSpout`:

```yaml
ExtraAbilities:
  Cozmyc:
    SandSpout:
      Cooldown: 0  # milliseconds
      Height: 10
      BlindnessTime: 10  # seconds
      SpoutDamage: 1
      Sound:
        Name: ENTITY_HORSE_BREATHE
        Volume: 0.6  # acceptable values are 0.0 - 2.0
        Pitch: 0.35  # acceptable values are 0.0 - 2.0
      FlySpeed: 0.075
