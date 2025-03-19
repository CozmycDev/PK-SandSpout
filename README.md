[![GitHub Pre-Release](https://img.shields.io/github/release-pre/CozmycDev/PK-SandSpout.svg)](https://github.com/CozmycDev/PK-SandSpout/releases)
[![Github All Releases](https://img.shields.io/github/downloads/CozmycDev/PK-SandSpout/total.svg)](https://github.com/CozmycDev/PK-SandSpout/releases)
![Size](https://img.shields.io/github/repo-size/CozmycDev/PK-SandSpout.svg)


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

- **Minecraft Version**: Tested and working on MC 1.20.4 and 1.21.4.
- **ProjectKorra Version**: Tested and working on PK 1.11.2, 1.11.3, and 1.12 BETA 12/13 and PRE RELEASE 3.

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
```

Language options can be found in `language.yml`:
```yaml
Abilities:
  Earth:
    SandSpout:
      DeathMessage: '{victim} was buried alive under {attacker}''s {ability}'
      Description: An advanced Sandbending skill that takes advantage of the properties
        of the element to form a mobile column of sand. Sandbenders are able to use
        this ability for travelling, evasion, to gain height advantage in combat and
        to build. The erosion generated from the column will blind and damage entities
        standing below.
      Instructions: You must be standing on sand to use this ability. Left Click to
        activate, Space to go up, Shift to go down.
```
