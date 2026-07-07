# Booms Epic Sounds

Booms Epic Sounds is a RuneLite plugin that plays custom sound effects and local chat notifications for selected Old School RuneScape events.

The plugin supports built-in fallback sounds, custom local sound packs, livestream notifications, and configurable sound triggers.

---

# Features

## Loot Notifications
- Loot Tracker integration
- Tracked item notifications
- GP value loot notifications
- Configurable tracked item list
- Configurable minimum GP value

## Player Notifications
- Player death notifications
- Player kill notifications
- Level-up notifications
- Prayer notifications
- Quest completion notifications
- Collection Log notifications
- Report player confirmation notifications

## Livestream Notifications
- Optional local chat notification when BoomEpicKill goes live
- Optional livestream sound notification
- Live stream status is checked from a remote status file
- Remote messages are sanitised before being shown in RuneLite
- Links and formatting tags are blocked from livestream messages

## Audio
- Adjustable master sound volume
- Random sound selection for supported events
- Local custom sound pack support
- Built-in fallback sounds
- One sound played per game tick to prevent overlapping audio
- Queued sound system so events are not skipped during busy gameplay

---

# Custom Sounds

On first launch, the plugin automatically creates this folder:

```text
.runelite/
└── booms-epic-sounds/
```

Supported event folders:

```text
.runelite/
└── booms-epic-sounds/
    ├── Loot/
    ├── Death/
    ├── LevelUp/
    ├── Prayer/
    ├── PlayerKill/
    ├── Report/
    ├── CollectionLog/
    ├── QuestCompleted/
    └── Livestream/
```

Place one or more `.wav` files inside the relevant folder.

If custom sounds exist, one is selected randomly. If no custom sounds exist, the plugin uses its bundled fallback sounds.

---

# Configuration

The plugin allows you to configure:

- Loot notifications
- Loot trigger mode
- Minimum GP value
- Tracked item list
- Announcement volume
- Local streamer message
- Livestream chat notification
- Livestream sound notification
- Prayer messages
- Player kills
- Level ups
- Quest completions
- Death notifications
- Report player notifications
- Collection Log notifications

---

# Streamer and Livestream Messages

Streamer and livestream messages are displayed **only in your own RuneLite chatbox**.

They are **never** sent to:

- Public chat
- Private messages
- Friends Chat
- Clan Chat
- Group Chat
- Other players

---

# Privacy

- Custom sound files remain on your computer.
- Custom sound files are never uploaded, downloaded, or shared by the plugin.
- Livestream status is read from a public remote JSON file.
- Remote livestream messages are sanitised before display.
- Links and formatting tags are blocked from remote messages.

---

# Notes

- Only one sound is played per game tick to prevent overlapping audio.
- Additional sounds are queued and played on following ticks.
- Custom sound folders remain on your computer even if the plugin is disabled or removed.

---

# Planned Features

- Combat Achievement notifications
- Pet notifications
- Raid-specific notifications
- Additional PvM event notifications
- Additional PvP event notifications
- Per-event volume controls
- Custom sound folder opener
- Sound pack import/export support

---

# License

BSD-2-Clause