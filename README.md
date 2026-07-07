# Booms Epic Sounds

Booms Epic Sounds is a RuneLite plugin that plays custom sound effects and local chat notifications for a wide range of Old School RuneScape events.

The plugin supports both built-in fallback sounds and fully custom local sound packs, allowing players and streamers to personalise their RuneLite experience.

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
- Report player confirmation notifications

## Audio
- Adjustable master sound volume
- Random sound selection for supported events
- Local custom sound pack support
- Built-in fallback sounds
- One sound played per game tick to prevent overlapping audio
- Queued sound system so events are not skipped during busy gameplay

## Quality of Life
- Optional local streamer message
- Simple configuration through the RuneLite Config panel

---

# Custom Sounds

Booms Epic Sounds supports fully custom local sound packs.

On first launch, the plugin automatically creates the following folder inside your RuneLite directory:

```text
.runelite/
└── booms-epic-sounds/
```

Each supported event receives its own folder automatically.

Example:

```text
.runelite/
└── booms-epic-sounds/
    ├── Loot/
    ├── Death/
    ├── LevelUp/
    ├── Prayer/
    ├── PlayerKill/
    ├── Report/
    └── QuestCompleted/
```

Simply place one or more `.wav` files inside the relevant folder.

Example:

```text
.runelite/
└── booms-epic-sounds/
    └── Loot/
        ├── loot1.wav
        ├── loot2.wav
        └── loot3.wav
```

Whenever that event occurs, the plugin randomly selects one of the available sound files.

If no custom sounds exist, the plugin automatically falls back to its bundled default sounds.

---

# Supported Sound Folders

- Loot
- Death
- LevelUp
- Prayer
- PlayerKill
- Report
- QuestCompleted

---

# Sound Requirements

- `.wav` format only
- Place sounds inside the correct event folder
- Multiple sound files per folder are supported
- Sounds are randomly selected each time the event occurs

---

# Configuration

The plugin allows you to configure:

- Enable or disable loot notifications
- Tracked Item mode
- GP Value mode
- Combined loot mode
- Minimum GP value
- Announcement volume
- Tracked item list
- Local streamer message

---

# Streamer Message

The optional streamer message is displayed **only in your own RuneLite chatbox**.

It is **never** sent to:

- Public chat
- Private messages
- Friends Chat
- Clan Chat
- Group Chat
- Other players

---

# Privacy

Booms Epic Sounds is entirely local.

- Your sound files never leave your computer.
- The plugin does not upload, download, or share custom sound files.
- No custom audio is distributed through the plugin.

---

# Notes

- Only one sound is played per game tick to prevent overlapping audio.
- Additional sounds are automatically queued and played on following ticks.
- Custom sound folders remain on your computer even if the plugin is disabled or removed.

---

# Planned Features

- Collection Log notifications
- Combat Achievement notifications
- Pet notifications
- Raid-specific notifications
- Additional PvM event notifications
- Additional PvP event notifications
- Per-event volume controls
- Per-event enable/disable toggles
- Custom sound folder opener
- Sound pack import/export support
- Plugin Hub sound pack compatibility

---

# License

BSD-2-Clause