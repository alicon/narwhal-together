# Mushroom the Yorkie

**A tiny Yorkie companion with big feelings.**

Mushroom the Yorkie adds one small, loyal dog companion to your Minecraft world. He follows his owner, takes treats, naps indoors at night, asks to go outside, and occasionally gets very invested in nearby peaceful animals.

## Current Features

- One Mushroom per player by default, including creative spawn eggs.
- Spawn eggs create your owned Mushroom directly.
- Yorkie treats tame, feed, and call Mushroom back.
- Mushroom barks when he needs to go outside after being indoors too long.
- At night indoors, Mushroom curls up to sleep with closed eyes.
- Double-click a sleeping Mushroom to wake him briefly before he settles back down.
- Mushroom can bark at and chase peaceful mobs, but holding a treat calls him off for the rest of the Minecraft day.
- Creative flying support lets Mushroom trail along with playful flying tricks.

## Configuration

Mushroom writes a config file at:

```text
config/mushroom_yorkie.json
```

The default behavior allows one loaded Mushroom per player. Set `oneMushroomPerPlayer` to `false` if you want creative worlds or servers to allow multiple Mushrooms per owner.

Wake-up spawning is configurable too:

- `respawn`: after a successful night in bed, a player gets Mushroom if they do not already have a loaded owned Mushroom.
- `extreme`: each player gets Mushroom only once after sleeping; if he dies, that player does not get another one.

## Installation

Mushroom the Yorkie must be installed on every participating client and on the server or LAN host.

### Required

- Fabric Loader
- Fabric API
- Minecraft Java Edition 1.21.11

## Why Mushroom?

Mushroom is built for family Minecraft worlds where a companion should feel small, expressive, and a little bit silly without turning into a full pet-management system.
