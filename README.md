# BobbiesLightHouse Prototype

Client-only Fabric prototype for Minecraft 1.21.10. It discovers lodestones the player actually sees in loaded real chunks, stores them beside Bobby's cache, and asks Bobby to load cached chunks around eligible anchors.

## Requirements

- Minecraft 1.21.10
- Fabric Loader 0.17.x
- Fabric API for 1.21.10
- Bobby installed and working
- Java 21+

## Build

```bash
./gradlew build
```

If this checkout does not include a Gradle wrapper, run with a local Gradle install:

```bash
gradle build
```

The jar will be in `build/libs/`.

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

## Important Prototype Notes

The Bobby integration uses reflection against Bobby internals. That is acceptable for a quick test, but the production version should either contribute a small public API to Bobby or vendor a carefully reviewed compatibility layer per Bobby version.
