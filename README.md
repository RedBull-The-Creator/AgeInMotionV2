# Age in Motion — Android Widget

A compact monochrome Android home-screen widget inspired by the Nothing / Age in Motion concept.

## Modes

### Original Counter
Shows your current age in **years to 9 decimal places**.

Example:
`26.262801372 YEARS`

### Day Progress
Keeps the same live age counter, but adds a progress bar for the **current 24-hour day** rather than your expected lifespan.

The percentage runs from 0.00% at local midnight to 100.00% just before the next midnight.

## Size

The widget is designed for **one home-screen row** and horizontal resizing. Start at roughly **2 cells wide**, then resize it to 3 or 4 cells if you want more breathing room.

The layout intentionally stays compact so it should no longer occupy two rows like the first version.

## Build without Android Studio — easiest method

You do NOT need Android Studio on your computer.

1. Create a GitHub account if you don't already have one.
2. Create a new empty GitHub repository.
3. Extract this ZIP.
4. Upload the **contents of the `AgeInMotion` folder** into the GitHub repository. Make sure `.github/workflows/build.yml` is uploaded too.
5. Open the repository's **Actions** tab.
6. Select **Build APK**.
7. Click **Run workflow** (or push to `main`).
8. Wait for the green check mark.
9. Open the completed workflow run and scroll to **Artifacts**.
10. Download **AgeInMotion-debug-apk**.
11. Extract it and install `app-debug.apk` on your Android phone.

If Android blocks the installation, allow your browser/file manager to install unknown apps, then try again.

## GitHub build environment

The included workflow installs Java 17, Android SDK API 36, Build Tools 36.0.0 and Gradle 9.5 automatically. You don't need to install any of those locally.

## Refresh behaviour

Android does not guarantee second-by-second home-screen widget refreshes. This version schedules an inexact **minute-level refresh** while the widget exists, plus Android's normal widget update mechanism. The age and day percentage are recalculated from the actual current time on every refresh, so they remain accurate even if Android delays a refresh.

## Configuration

- Date of birth
- Expected lifespan: 70–100 years (kept for the original/life settings and future extensions)
- Original Counter or Day Progress mode
