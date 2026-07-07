# Booms Epic Sounds

Booms Epic Sounds is a RuneLite plugin that plays custom sound effects and local chat notifications for selected in-game events.

## Features

- Loot Tracker integration
- Tracked item notifications
- GP value loot notifications
- Player death notifications
- Level-up notifications
- Prayer notifications
- Quest completion notifications
- Player kill notifications
- Report player confirmation notifications
- Adjustable sound volume
- Random sound selection for supported events
- Local custom sound pack support
- Built-in fallback sounds
- Optional local streamer message

## Custom Sounds

Booms Epic Sounds supports user-provided custom sound packs.

On first launch, the plugin automatically creates the following folder inside your RuneLite directory:

```text
.runelite/
└── booms-epic-sounds/
```

Within this folder, event folders are automatically created:

```text
Loot/
Death/
LevelUp/
Prayer/
PlayerKill/
Report/
QuestCompleted/
```

To use your own sounds, simply copy one or more **.wav** files into the relevant folder.

For example:

```text
.runelite/
└── booms-epic-sounds/
    └── Loot/
        ├── loot1.wav
        ├── loot2.wav
        └── loot3.wav
```

Whenever that event occurs, the plugin will randomly select one of your custom sounds.

If no custom sounds exist for an event, the plugin will automatically play one of its bundled fallback sounds.

### Supported Event Folders

- Loot
- Death
- LevelUp
- Prayer
- PlayerKill
- Report
- QuestCompleted

### Sound Requirements

- Must be in **.wav** format.
- Place files inside the correct event folder.
- Multiple sounds per folder are supported and selected randomly.

## Configuration

- Enable or disable loot sounds
- Trigger on tracked items
- Trigger on GP value
- Configure minimum GP value
- Configure announcement volume
- Configure tracked item list
- Enable or disable the local streamer message

## Streamer Message

The optional streamer message is displayed **only in your own RuneLite chatbox**.

It is never sent to public chat, private messages, clan chat, friends chat, or any other players.

## Notes

- Only one sound is played per game tick to prevent overlapping audio.
- Multiple events are queued to ensure sounds are not skipped during busy gameplay.
- Custom sounds remain on your computer and are never uploaded or shared.
- The plugin does not download or distribute user-provided sound files.

## License

BSD-2-Clause

## Audio

Booms Epic Sounds includes bundled fallback sounds for supported events.

Users may replace or extend these by adding their own local `.wav` files inside the `.runelite/booms-epic-sounds` folder.

The plugin does not upload, download, or distribute user-provided sound files.

## Planned Features

- Per-event custom volume controls
- Additional PvM and PvP event support
- Collection Log notifications
- Combat Achievement notifications
- Pet notifications
- Raid-specific notifications
- Sound pack import/export support
- Optional sound folder opener from the plugin configuration