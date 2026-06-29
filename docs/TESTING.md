# Multiplayer test checklist

Run this checklist with disposable test worlds before each public release.

## Setup

- [ ] Minecraft 1.21.11 launches with Fabric API and NARwhal Together.
- [ ] The server or LAN host has the same NARwhal Together JAR as every client.
- [ ] Controlify detects each controller and displays the teleport action.
- [ ] The teleport action can be assigned to a controller button or radial-menu slot.

## Teleport behavior

- [ ] With one player online, pressing the action shows `No other players are online`.
- [ ] With two players online, each player can teleport to the other.
- [ ] Teleporting works in both directions without operator permissions.
- [ ] Teleporting between the Overworld, Nether, and End works.
- [ ] The arriving player appears at the target player's position and does not retain fall damage.
- [ ] The target player sees an arrival message.
- [ ] Repeated presses during the two-second cooldown do not teleport again.

## Three or more players

- [ ] Repeated uses cycle through the other players alphabetically.
- [ ] Spectators are skipped.
- [ ] A player leaving during play does not break the next teleport.

## Compatibility and failure behavior

- [ ] A client connecting to a server without the mod gets a clear message when using the action.
- [ ] Keyboard `G` and the configured controller action both work.
- [ ] Existing `G` key conflicts are visible and can be rebound in Controls.
- [ ] Split-screen instances can use the action independently.
- [ ] No errors attributed to `narwhal_together` appear in the latest log.
