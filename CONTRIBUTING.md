# Contributing

This is a hobby project I work on to learn Kotlin, so it moves at a hobby pace and I sometimes pick the more educational option over the most efficient one. PRs and issues are welcome anyway.

## Filing an issue

Issues live on Codeberg. Use one of these prefixes in the title so the tracker stays readable:

- `feat:` something a player would notice
- `fix:` a bug
- `chore:` tooling, refactor, deps, CI
- `docs:` README, inline docs, comments

If it makes sense, add an area label: `area/app`, `area/core`, `area/server`, `area/ci`.

## Sending a PR

Fork on Codeberg, branch off `main`, keep the PR small. One feature or one fix per PR.

Commit messages use the same prefixes as issue titles. Short subject (around 72 chars), body optional.

Before I'll merge:

- `./gradlew :app:lint` is clean
- ktlint is clean
- Tests for any pure-Kotlin code you added pass
- For UI changes, include a screenshot in the PR description. Light and dark mode if the change is visible.

## Code style

Kotlin official style, ktlint enforces it.

- Prefer `val` and pure functions, especially in domain code.
- Anything in `domain/` should be plain Kotlin, no Android or Compose imports. It needs to be JVM-testable and reusable by the server.
- Avoid `!!`. If you really need it, leave a one-line comment saying why.
- Keep Composables small. Hoist state to `ViewModel`s.

## Conduct

Be kind, assume good faith, disagree about technical direction all you want, leave the personal stuff out.
