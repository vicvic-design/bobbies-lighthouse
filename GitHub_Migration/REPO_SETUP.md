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

## Remote Creation

After Viktor confirms the owner and visibility:

```bash
git remote add origin git@github.com:<owner>/bobbies-lighthouse.git
git push -u origin lighthouse-0.1.10-sodium-anchor
```

If the branch should become `main`:

```bash
git branch -M main
git push -u origin main
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
