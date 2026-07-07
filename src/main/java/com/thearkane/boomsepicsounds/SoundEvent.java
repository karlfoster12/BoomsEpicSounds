package com.thearkane.boomsepicsounds;

import lombok.Getter;

/**
 * A single place that pairs a sound category with its custom-sound folder
 * name and its bundled fallback sound(s). Adding a new sound event is now
 * a one-line addition here instead of touching 3-4 places in the plugin.
 */

@Getter
public enum SoundEvent
{
    LOOT("Loot", "/thehit.wav" ),
    PRAYER("Prayer", "/prayer.wav"),
    DEATH("Death", "/death.wav"),
    PLAYER_KILL("PlayerKill", "/death.wav"),
    REPORT("Report", "/wellhesgunnagetbanned.wav", "/wellyourgunnagetbanned.wav"),
    LEVEL_UP("LevelUp", "/goodjobbuddyhopeyourhavingfun.wav"),
    COLLECTION_LOG("thehit.wav"),
    QUEST("QuestCompleted", "/finallyquestcomplete.wav", "/ohmyyoufinishedanotherquest.wav");


    private final String folderName;
    private final String[] fallbackSounds;

    SoundEvent(String folderName, String... fallbackSounds)
    {
        this.folderName = folderName;
        this.fallbackSounds = fallbackSounds;
    }

}