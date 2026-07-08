package com.thearkane.boomsepicsounds;


import lombok.Getter;

/**
 * A single place that pairs a sound category with its custom-sound folder
 * name, its bundled fallback sound(s), and its priority.
 *
 * Priority determines which sound wins when multiple events fire on the
 * same game tick (e.g. a quest completes on the same tick you level up).
 * Lower number = higher priority = plays first / wins ties.
 */

@Getter
public enum SoundEvent
{
    DEATH(1, "Death", "/death.wav", "/ohno.wav"),
    PLAYER_KILL(2, "PlayerKill", "/sitdown.wav", "/sitdown1.wav"),
    QUEST_COMPLETED(3, "QuestCompleted", "/finallyquestcomplete.wav", "/ohmyyoufinishedanotherquest.wav"),
    COLLECTION_LOG(4, "CollectionLog", "/thehit.wav"),
    LEVEL_UP(5, "LevelUp", "/goodjobbuddyhopeyourhavingfun.wav", "/levelup.wav", "/levelup1.wav"),
    LOOT(6, "Loot", "/thehit.wav"),
    PRAYER(7, "Prayer", "/prayer.wav"),
    REPORT(8, "Report", "/wellhesgunnagetbanned.wav", "/wellyourgunnagetbanned.wav"),
    LIVESTREAM(9, "Livestream", "/livestream.wav"),
    TRADE_ACCEPTED(10, "TradeAccepted", "/tradeaccepted.wav");

    private final int priority;
    private final String folderName;
    private final String[] fallbackSounds;

    SoundEvent(int priority, String folderName, String... fallbackSounds)
    {
        this.priority = priority;
        this.folderName = folderName;
        this.fallbackSounds = fallbackSounds;
    }

}