# BobbiesLightHouse Version History

This file tracks human-readable changes. Git tracks the exact file-level history for reverting.

## 1.0.0 - Lighthouse Method Release - 2026-05-21

- Promoted the working Lighthouse Method to the first stable release.
- Returned to manual island chunk loading around lodestone anchors instead of filtering Bobby's whole render distance.
- Kept the improved anchor locator/scanner flow.
- Added Sodium visibility integration so manually loaded lighthouse chunks can render through gaps in Sodium's occlusion pass.
- Retained vanilla visibility injection as a fallback path.
- Removed pathway placeholder chunks and obsolete Bobby filter settings from the graphics options.
- Kept the graphics settings focused on active lighthouse controls: enable, island radius, anchor distance, active anchors, chunk cap, load rate, render horizon, and shape.

## 0.1.0 - Prototype Baseline - 2026-05-19

- Renamed project to BobbiesLightHouse.
- Added Fabric 1.21.10 client-only mod scaffold.
- Added automatic lodestone anchor discovery from real loaded chunks.
- Added per-server/per-dimension anchor storage under `.minecraft/.bobby/bobbieslighthouse/.../anchors.json`.
- Added configurable anchor radius, max anchor distance, square/circle selection, and performance caps.
- Added Bobby reflection bridge prototype for loading cached chunks around active anchors.
- Added development commands under `/bobbieslighthouse`: `status`, `list`, `refresh`, `clear`.
- Added README and test plan.
- Built `bobbies-lighthouse-0.1.0.jar`.

### Known Prototype Risks

- Bobby integration uses reflection into Bobby internals and may break across Bobby versions.
- Rendering behavior needs in-game testing with the exact Bobby/Fabric/Minecraft versions.
- Dev commands are intentionally enabled for testing and should be hidden or removed before release.

## 0.1.1 - Bobby Bridge Diagnostics - 2026-05-19

- Added detailed Bobby bridge diagnostics to `/bobbieslighthouse status`.
- Added one-time warning logs for the main unavailable states:
  - Bobby classes/methods unavailable.
  - Bobby mixin interface missing from the client chunk source.
  - Bobby fake chunk manager is null because Bobby is disabled or not initialized.

## 0.1.2 - Diagnostic Tooling - 2026-05-19

- Expanded `/bobbieslighthouse status` with queue, desired chunk, eligible anchor, and load counters.
- Added `/bobbieslighthouse anchors` for nearby/stored anchor inspection.
- Added `/bobbieslighthouse explain` to explain the nearest anchor render decision.
- Added `/bobbieslighthouse bobby` for a Bobby bridge probe.
- Added `/bobbieslighthouse dump` to write `.minecraft/logs/bobbieslighthouse-diagnostic.json`.

## 0.1.3 - Bobby 5.2.10 Bridge Compatibility - 2026-05-19

- Updated the Bobby bridge to prefer `ClientChunkManagerExt`, with `ClientChunkCacheExt` retained as an older-version fallback.
- Improved Bobby diagnostics to report the exact extension interface selected.
- Stopped starting cached chunk loads while the Bobby bridge is unavailable, preventing inflated failed-load counters.

## 0.1.4 - Bobby Range Handoff - 2026-05-20

- Added `/bobbieslighthouse renderprobe` to classify managed chunks as lighthouse-only, normal Bobby range overlap, or normal Minecraft render range.
- Avoid loading lighthouse-managed chunks when Bobby reports that they are already inside its normal player-centered range.
- Release lighthouse ownership without explicitly unloading chunks that have moved into normal Bobby or normal render range, reducing edge-of-range handoff flicker.

## 0.1.5 - Stale Bobby Chunk Reacquire - 2026-05-20

- Recheck lighthouse-managed chunks against Bobby on each refresh.
- If Bobby has unloaded a chunk that is still desired by a lighthouse anchor, drop the stale managed marker so the chunk can be loaded again.
- Added stale managed chunk counts to `/bobbieslighthouse status`, `/bobbieslighthouse renderprobe`, and diagnostic dumps.

## 0.1.6 - Render Section Rebuild Probe - 2026-05-20

- Mark every vertical client render section dirty after a lighthouse chunk is loaded or adopted from Bobby.
- Notify the level renderer that the lighthouse sections are non-empty so the occlusion graph has a chance to include them.
- Added render section dirty counts to `/bobbieslighthouse status`, `/bobbieslighthouse renderprobe`, and diagnostic dumps.
