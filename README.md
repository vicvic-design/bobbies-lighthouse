# Bobbies Lighthouse

Client-only Fabric mod for Minecraft 1.21.10. It discovers lodestones the player actually sees in loaded real chunks, stores them beside Bobby's cache, and asks Bobby to load cached chunks around eligible anchors.

## Requirements

- Minecraft 1.21.10
- Fabric Loader 0.17.x
- Fabric API for 1.21.10
- Bobby installed and working
- Java 21+

## Install

1. Download the latest jar from GitHub Releases.
2. Put the jar in your Minecraft `mods` folder.
3. Launch with Fabric, Fabric API, and Bobby installed.

The latest known jar is also kept in this repository at `dist/bobbies-lighthouse-1.0.1-lighthouse-rewrite.jar` for convenience. Future release jars should be attached to GitHub Releases first.

## Build

```bash
./gradlew build
```

If this checkout does not include a Gradle wrapper, run with a local Gradle install:

```bash
gradle build
```

The jar will be in `build/libs/`.

## Release Notes

Release planning and GitHub migration notes live in `GitHub_Migration/`.

Before publishing a jar, use `GitHub_Migration/RELEASE_CHECKLIST.md` and update `VERSION_HISTORY.md`.

## Prototype Behavior

- Discovers vanilla lodestone blocks in real client chunks.
- Stores anchors automatically under `.minecraft/.bobby/bobbieslighthouse/<server>/<dimension>/anchors.json`.
- Renders a square or circular chunk area around discovered anchors.
- Skips chunks already inside the current client render distance.
- Disables anchors when a loaded real chunk confirms the lodestone is gone.
- Uses throttled scanning/loading and caps to avoid runaway resource use.

## Dev Commands

These are intentionally development-only and can be removed before release.

- `/bobbieslighthouse status`
- `/bobbieslighthouse list`
- `/bobbieslighthouse refresh`
- `/bobbieslighthouse clear`

## Important Notes

The Bobby integration uses reflection against Bobby internals. That is acceptable for a quick test, but the production version should either contribute a small public API to Bobby or vendor a carefully reviewed compatibility layer per Bobby version.

## License

MIT. See `LICENSE`.
