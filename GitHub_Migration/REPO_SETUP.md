# Repository Setup Notes

## Local Review Commands

Run from the project root:

```bash
git status --short --branch
git remote -v
git branch --all
git tag --list
git ls-files | sort
```

## Hygiene Checks

```bash
git ls-files dist Release_Archives build .gradle
git log --stat --oneline -- dist Release_Archives
rg -n "token|secret|password|apikey|api_key|credential|client_secret" .
```

If large generated files are tracked, decide whether to keep history as-is for now or migrate future artifacts to GitHub Releases only.

Current decision: old jars and release archives are kept on the shared drive but removed from Git tracking. The repository keeps only `dist/bobbies-lighthouse-1.0.1-lighthouse-rewrite.jar` as a convenience download; GitHub Releases should hold release artifacts going forward.

## GitHub Metadata Added

- `.github/ISSUE_TEMPLATE/bug_report.md`
- `.github/ISSUE_TEMPLATE/feature_request.md`
- `.github/ISSUE_TEMPLATE/compatibility.md`
- `.github/PULL_REQUEST_TEMPLATE.md`
- `GitHub_Migration/RELEASE_CHECKLIST.md`

## Remote Creation

Owner: `Vicvic`
Visibility: public

After creating the GitHub repository:

```bash
git remote add origin git@github.com:Vicvic/bobbies-lighthouse.git
git branch -M main
git push -u origin main
git push origin --tags
```

Initial release upload:

```bash
gh release create v1.0.1 dist/bobbies-lighthouse-1.0.1-lighthouse-rewrite.jar \
  --repo Vicvic/bobbies-lighthouse \
  --title "Bobbies Lighthouse 1.0.1" \
  --notes-file GitHub_Migration/RELEASE_NOTES_v1.0.1.md
```

## Suggested Repository Settings

- Enable Issues.
- Enable Discussions only if user support becomes noisy.
- Protect the default branch after the first push.
- Require pull requests only once there are multiple contributors.
- Use GitHub Releases for jars and changelog notes.

## Suggested Labels

- `bug`
- `feature`
- `compatibility`
- `release`
- `testing`
- `documentation`
