# BobbiesLightHouse Version History

This file tracks human-readable changes. Git tracks the exact file-level history for reverting.

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
