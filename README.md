# Prayer Time Sample

A sample Android app that demonstrates **Islamic prayer times**, **per-prayer notifications**, and **Azan playback** using **MVI + Clean Architecture**. Prayer times are calculated offline with [SonicOPT](https://github.com/orbitalsonic/SonicOPT); alarms use a rolling `AlarmManager` strategy so the app never schedules hundreds of pending alarms.

## Features

### Prayer times (6 entries)

| Prayer   | Notifications | Azan |
|----------|---------------|------|
| Fajr     | Yes           | Yes  |
| Sunrise  | Yes           | No (notification only) |
| Dhuhr    | Yes           | Yes  |
| Asr      | Yes           | Yes  |
| Maghrib  | Yes           | Yes  |
| Isha     | Yes           | Yes  |

- Offline calculation via **SonicOPT** (latitude/longitude + convention)
- Home screen: **address**, date, prayer list (`hh:mm a`), next-prayer countdown
- **Refresh** to update location and times

### Notification modes (on each prayer row)

Tap the icon on the right of a row to cycle modes:

| Icon | Mode | Behavior |
|------|------|----------|
| `ic_notification_off` | Disabled | No alarm, no notification |
| `ic_notification` | Notification only | Alert at prayer time |
| `ic_volume_up` | Azan | Notification + `res/raw/azan.mp3` |

**Sunrise** cycles only: Disabled ↔ Notification only (never Azan).

Settings are stored in **DataStore** and restored after restart.

### Permissions (Settings screen)

- Location (fine / coarse)
- Notifications (Android 13+)
- Exact alarms (Android 12+)
- Battery optimization guidance

### Reliability

- **Rolling alarms**: at most 5 pending exact alarms; on each trigger, the next prayer is scheduled
- **Boot** / **time / date / timezone** changes reschedule via receivers + WorkManager
- **Azan** stops on: Stop action, notification dismiss, or new prayer alarm
- **Doze**: `setExactAndAllowWhileIdle` when permitted

## Architecture

```
Presentation (MVI) → Domain (Use cases) → Data (Repositories, DataStore, SonicOPT)
```

| Layer | Responsibility |
|-------|----------------|
| **Presentation** | `PrayerFragment`, `SettingsFragment`, ViewModels, `PrayerUiModel` |
| **Domain** | Models, repository contracts, `PrayerAlarmPlanner`, use cases |
| **Data** | DataStore, SonicOPT wrapper, Fused Location, `AlarmSchedulerImpl`, `AzanPlayerManager` |

Manual DI via `AppContainer` (no Hilt/Koin in this sample).

See **[ARCHITECTURE.md](ARCHITECTURE.md)** for diagrams and package layout.

## Tech stack

- Kotlin, Coroutines, Flow / StateFlow
- AndroidX: Navigation, Lifecycle, ViewBinding, DataStore, WorkManager
- Google Play Services Location
- [SonicOPT](https://github.com/orbitalsonic/SonicOPT) `1.0.3` (JitPack)
- Min SDK 24 · Target SDK 36

## Getting started

### Prerequisites

- Android Studio (Ladybug or newer recommended)
- JDK 11+
- Android SDK with API 36

### Clone and open

```bash
git clone <your-repo-url>
cd PrayerTimeSample
```

Open the folder in Android Studio and **Sync Project with Gradle Files**.

### Build from command line

On Windows, if `gradlew.bat` fails with a classpath error:

```powershell
$env:CLASSPATH = "."
java -jar gradle\wrapper\gradle-wrapper.jar assembleDebug
```

Otherwise:

```bash
./gradlew assembleDebug
```

### Run

1. Install on a device or emulator with Google Play services (for location).
2. Grant **location** when prompted.
3. Open **Settings** (gear on the prayer screen) and grant **notifications**, **exact alarms**, and optionally disable battery optimization.
4. Tap **Refresh** on the home screen to fetch location and prayer times.

### Tests

```bash
./gradlew testDebugUnitTest
```

Unit tests cover alarm planning, notification settings policy, and ViewModel behavior.

## Project structure (high level)

```
app/src/main/java/com/orbitalsonic/prayertimesample/
├── di/                 AppContainer, AppModule
├── domain/             Models, repositories (interfaces), use cases
├── data/               Repositories, DataStore, SonicOPT, alarms, location, Azan
├── presentation/       Prayer & Settings UI (MVI)
├── receiver/           Alarm, boot, time change, dismiss, stop
├── worker/             PrayerRescheduleWorker
└── lifecycle/          Foreground time/date observer
```

## Key files

| File | Purpose |
|------|---------|
| `PrayerFragment` | Home UI + per-row notification toggles |
| `PrayerViewModel` | MVI state: times, address, countdown |
| `SonicPrayerCalculator` | SonicOPT → domain `PrayerDayTimes` |
| `AlarmSchedulerImpl` | Exact alarms |
| `AzanPlayerManager` | Plays `R.raw.azan` |
| `PrayerPreferencesDataStore` | Per-prayer notification modes + location cache |

## References

- [SonicOPT — Offline Prayer Time Library](https://github.com/orbitalsonic/SonicOPT)
- [Location-Finder-MVI-Android](https://github.com/orbitalsonic/Location-Finder-MVI-Android) (location / MVI patterns)

## License

Sample project for learning and integration reference. SonicOPT and other third-party libraries are subject to their own licenses.
