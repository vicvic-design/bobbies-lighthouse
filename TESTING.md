# Quick Test Plan

1. Put Bobby, Fabric API, and the built `bobbies-lighthouse` jar in the same 1.21.10 Fabric profile.
2. Join a server/world and place or walk near a vanilla lodestone while it is inside normal server render distance.
3. Run `/bobbieslighthouse status`; the anchor count should increase after the scanner runs.
4. Check `.minecraft/.bobby/bobbieslighthouse/<server>/<dimension>/anchors.json`; the lodestone position should be stored there.
5. Move far enough that the lodestone area is outside normal/Bobby render distance but still inside `maxAnchorDistanceChunks`.
6. Run `/bobbieslighthouse refresh` and watch logs for Bobby bridge warnings.
7. Remove the lodestone, revisit/refresh the loaded chunk, then run `/bobbieslighthouse list`; the anchor should become disabled.

## Config

The prototype writes `.minecraft/config/bobbieslighthouse.json`.

Useful fields:

- `anchorRadiusChunks`: chunk radius around each lodestone.
- `maxAnchorDistanceChunks`: how far the player can be from an anchor.
- `shape`: `SQUARE` or `CIRCLE`.
- `maxActiveAnchors`: cap on anchors used at once.
- `maxExtraRenderedChunks`: hard cap on extra Bobby chunks.
- `maxChunkLoadsStartedPerTick`: load throttle.

## Expected Prototype Risk

The risky part is the reflection bridge into Bobby. If Bobby internals changed for the installed version, discovery/storage still works, but rendering will pause and log one warning.
