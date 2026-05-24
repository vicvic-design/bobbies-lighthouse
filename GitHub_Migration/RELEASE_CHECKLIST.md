# Release Checklist

Use this before publishing a jar through GitHub Releases.

## Preflight

- [ ] Confirm target version in `gradle.properties`.
- [ ] Confirm supported Minecraft, Fabric Loader, Fabric API, Bobby, and Sodium versions.
- [ ] Review `VERSION_HISTORY.md` and draft release notes.
- [ ] Confirm no local-only files are included.
- [ ] Confirm license status is intentional.

## Build

- [ ] Run `./gradlew clean build` or `gradle clean build`.
- [ ] Confirm the jar appears under `build/libs/`.
- [ ] Confirm sources jar handling is intentional.

## Manual Test

- [ ] Launch Minecraft 1.21.10 with Fabric.
- [ ] Load Bobby, Fabric API, and Bobbies Lighthouse.
- [ ] Join a test world or server.
- [ ] Place or approach a lodestone in normal render distance.
- [ ] Run `/bobbieslighthouse status`.
- [ ] Confirm anchor storage under `.minecraft/.bobby/bobbieslighthouse/`.
- [ ] Move outside normal render distance and run `/bobbieslighthouse refresh`.
- [ ] Confirm no repeated Bobby bridge warnings.
- [ ] Remove a lodestone and confirm the anchor is disabled after revisiting.

## Publish

- [ ] Create a GitHub Release.
- [ ] Attach the release jar or archive.
- [ ] Include compatibility versions.
- [ ] Include known risks or limitations.
- [ ] Archive the same artifact under the shared drive.
