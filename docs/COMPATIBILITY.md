# Compatibility Policy

This repo optimizes for simple, maintainable Fabric mods for family Minecraft play.

## Supported Platform

- Minecraft Java Edition: target the version in `gradle.properties`
- Loader: Fabric
- Java: target the version in `gradle.properties`
- Required dependency: Fabric API

For now, support one Minecraft version at a time. Upgrade intentionally and keep release notes clear.

## Non-Goals For Now

- Forge, NeoForge, or Quilt support
- multi-loader architecture
- broad compatibility layers
- public plugin APIs
- backport branches

Those can be reconsidered only when there is a real user need.

## Compatibility Rules

- Prefer Fabric APIs over custom hooks.
- Keep client-only code under `src/client/java`.
- Do not import client-only classes from `src/main/java`.
- Keep registry ids, translation keys, packet ids, and save-data keys stable after release.
- Treat world-save data changes as compatibility-sensitive.
- Add migration code before changing released save-data meaning.
- Keep network payloads small and explicit.
- Avoid making one mod depend on another mod's internals.

## Release Expectations

Before publishing:

- run `./gradlew check`
- run `./gradlew build`
- complete the relevant manual checks in `docs/TESTING.md`
- update docs when behavior, requirements, output paths, or release steps change
