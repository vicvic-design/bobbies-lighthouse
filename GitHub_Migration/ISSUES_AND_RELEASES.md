# Issues and Releases Plan

## Initial Issues

1. GitHub migration prep
   - Review tracked files.
   - Confirm visibility and owner.
   - Push source branch.

2. Documentation cleanup
   - README install steps.
   - Commands and config notes.
   - Supported Minecraft/Fabric versions.

3. Artifact strategy
   - Decide future location for jars.
   - Move new jars to GitHub Releases.
   - Keep shared-drive archive as local backup.

4. Latest stable release
   - Identify latest stable jar.
   - Confirm manual test pass.
   - Draft release notes.
   - Attach jar archive to GitHub Release.

5. Compatibility sweep
   - Document Sodium compatibility status.
   - Document Fabric Loader and API versions.
   - Record known conflicts.

## Release Template

```markdown
## Summary

- 

## Compatibility

- Minecraft:
- Fabric Loader:
- Fabric API:
- Optional mods tested:

## Changes

- 

## Testing

- 

## Files

- 
```

## Versioning Recommendation

Use simple semantic-ish versions for public releases:

- Patch version for fixes and compatibility tweaks.
- Minor version for new behavior or commands.
- Major version only for config/world-impacting changes.
