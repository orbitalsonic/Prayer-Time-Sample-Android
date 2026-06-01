# Prayer Time Sample

A sample Android app that demonstrates **Islamic prayer times**, **per-prayer notifications**, and **Azan playback** using **MVI + Clean Architecture**. Prayer times are calculated offline with [SonicOPT](https://github.com/orbitalsonic/SonicOPT); alarms use a rolling `AlarmManager` strategy so the app never schedules hundreds of pending alarms.

## UI Screen
<img width="360" height="615" alt="Screen" src="https://github.com/user-attachments/assets/b679dd81-5eb3-42aa-9319-f87be9d200bd" />

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
| `ic_notification_azan` | Azan | Notification + `res/raw/azan.mp3` |

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

## References

- [SonicOPT — Offline Prayer Time Library](https://github.com/orbitalsonic/SonicOPT)
- [Location-Finder-MVI-Android](https://github.com/orbitalsonic/Location-Finder-MVI-Android) (location / MVI patterns)

## Contributing
Contributions are welcome! Fork the repository, make changes, and submit a pull request.

---

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

Copyright OrbitalSonic

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
