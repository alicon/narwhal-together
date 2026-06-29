# Minecraft Mods

A Fabric mod project targeting Minecraft Java Edition 1.21.11.

## Features

### Teleport to Next Player

Press `G` to teleport to another online player. If more than one other player is online, each press selects the next player alphabetically. A two-second cooldown prevents accidental repeated teleports.

The mod must be installed on every participating client and on the server or LAN host. Players do not need operator permissions. Controller players can bind **Teleport to Next Player** in their controller mod's settings; Controlify can also place the action on its radial menu.

## Requirements

- JDK 21 or newer

## Build

```shell
./gradlew build
```

The distributable mod JAR is written to `build/libs/`.

## Development

Import the repository as a Gradle project in IntelliJ IDEA, or launch a development client from the command line:

```shell
./gradlew runClient
```
