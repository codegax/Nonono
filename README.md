# Nonono

A [Nonogram](https://en.wikipedia.org/wiki/Nonogram) game I built to learn Kotlin and Android.

## What it does

You pick a difficulty or play the daily puzzle, and you solve a nonogram.

- Tap a cell to fill it (or, with the toggle at the bottom, to mark it as empty).
- A wrong tap costs you a life. Three wrongs and you lose.
- A wrong tap also reveals what the cell should have been, so you can keep going with fewer lives.
- Solve the puzzle and the screen says you won.

Multiple difficulty tiers from small to large. A daily puzzle that's the same for everyone on the same day, with a streak counter. Endless mode keeps generating new puzzles at the difficulty you picked. Stats per tier so you can see your best times.

Optionally, if you and your friends want to share progress, one of you runs the small self-hosted server and the rest point the app at it. You add each other by friend code, see each other's times on the daily puzzle, and join the time-bounded events anyone sets up. No global leaderboard, no email-and-password account, friends-only by design. You never have to opt in; the app is fully usable forever without it.

## How it's built

| Area  | What                                |
| ----- | ----------------------------------- |
| App   | Kotlin, Jetpack Compose, Material 3 |
| State | `ViewModel` + `StateFlow`           |
| Build | Gradle Kotlin DSL                   |

One module: `:app`. Domain types (`Puzzle`, `Board`, `CellState`, `TapMode`, `GameStatus`) live in `domain/`, the screen and view model in `ui/game/`. Plain Kotlin where I can manage it; Compose only where there's actually a UI.

The package is still `com.example.nonono` because I haven't picked a real one.

## Build

```bash
./gradlew :app:assembleDebug         # build the APK
./gradlew :app:installDebug          # install on a running emulator/device
./gradlew :app:testDebugUnitTest     # run unit tests
```

## Why it exists

I wanted to learn Kotlin. I picked a small game I'd actually play. It's open source so other people can pick it up too.

## License

GPL-3.0-or-later. See [`LICENSE`](./LICENSE).
