# Release guide

## Create the Modrinth project

Create a Modrinth **Mod** project with these values:

- Name: `NARwhal Together`
- Suggested slug: `narwhal-together`
- Summary: `Little tools for big Minecraft adventures together.`
- License: `MIT`
- Source: `https://github.com/alicon/narwhal-together`
- Client environment: `Required`
- Server environment: `Required`
- Loader: `Fabric`
- Game version: `1.21.11`
- Required dependency: `Fabric API`
- Optional dependency: `Controlify`

Use `src/main/resources/assets/narwhal_together/icon.png` as the project icon, `docs/media/narwhal-together-banner.png` as a gallery image, and `docs/MODRINTH.md` as the long description.

## Configure publishing

After the Modrinth project exists:

1. Create a Modrinth personal access token with version publishing permission.
2. In GitHub, open **Settings → Secrets and variables → Actions**.
3. Add a repository secret named `MODRINTH_TOKEN`.
4. Add a repository variable named `MODRINTH_PROJECT_ID` containing the project ID or slug.

Never commit the token.

## Publish

1. Complete `docs/TESTING.md` using the exact JAR being released.
2. Update `mod_version` in `gradle.properties`.
3. Commit and push the release changes.
4. Open the repository's **Actions** tab.
5. Run **Publish to Modrinth** and select `alpha`, `beta`, or `release`.

The workflow rebuilds from the selected commit and uploads the remapped production JAR. The publishing task declares Fabric API as required and Controlify as optional.
