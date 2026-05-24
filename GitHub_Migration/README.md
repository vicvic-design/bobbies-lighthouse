# Bobbies Lighthouse GitHub Migration

Created: 2026-05-24

This folder tracks the work to bring the Bobbies Lighthouse mod repository onto GitHub cleanly.

## Goals

- Publish the existing Git history without leaking local-only files.
- Make the repository understandable to a new visitor.
- Preserve release artifacts separately from source history where practical.
- Set up a lightweight issue/release workflow for future mod work.

## Current Assumptions

- Local source repo: `/home/kern/AI_Share/Hobbies/Minecraft/Bobbies_Lighthouse`
- Current branch: `lighthouse-0.1.10-sodium-anchor`
- Project name: `Bobbies_Lighthouse`
- GitHub repo name candidate: `bobbies-lighthouse`
- Default visibility should be confirmed before creating the remote.

## Migration Checklist

- [x] Create local GitHub migration planning folder.
- [x] Add GitHub issue and pull request templates.
- [x] Add release checklist.
- [x] Confirm target GitHub account or organization: `Vicvic`.
- [x] Confirm repository visibility: public.
- [x] Review `.gitignore` for local build output, Gradle caches, IDE files, and release archives.
- [x] Check working tree for secrets before pushing.
- [x] Decide artifact handling: keep only latest convenience jar in `dist/`; publish release jars through GitHub Releases.
- [x] Update `README.md` with install instructions, supported Minecraft/Fabric versions, and command usage.
- [ ] Update `TESTING.md` with a concise manual test pass for release builds.
- [x] Add a license: MIT.
- [x] Add issue templates for bugs and feature requests.
- [ ] Create the GitHub remote.
- [ ] Push branches and tags.
- [ ] Create an initial GitHub Release for the latest known good jar.

## Recommended First Pass

1. Keep the initial GitHub repository private.
2. Clean `.gitignore` and documentation first.
3. Move bulky jar archives out of normal source history going forward.
4. Push the existing branch after a secret/binary review.
5. Publish public only after the README, license decision, and latest release are settled.

## Open Questions

- Which GitHub account or organization owns the repo?
- Should the repo be public for mod users, or private until the mod reaches a polish threshold?
- Should old jars remain in repo history, or should only future releases be published through GitHub Releases?
- Which jar is the latest stable release to attach first?
- What license should the project use?

## Local Completion Status

As of 2026-05-24, the local planning side is ready for a GitHub handoff. The remaining blockers are external decisions or actions:

- Remote creation and push.
