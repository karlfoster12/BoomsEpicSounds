package com.thearkane.boomsepicsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("boomsepicsounds")
public interface BoomsEpicSoundsConfig extends Config
{
    enum LootSoundMode
    {
        TRACKED_ITEMS,
        GP_VALUE,
        BOTH
    }

    // =====================================================
    // SECTIONS
    // =====================================================

    @ConfigSection(
            name = "General",
            description = "General plugin settings",
            position = 0
    )
    String generalSection = "general";

    @ConfigSection(
            name = "Chat",
            description = "Chat message notifications",
            position = 1
    )
    String chatSection = "chat";

    @ConfigSection(
            name = "Loot",
            description = "Loot drop settings",
            position = 2
    )
    String lootSection = "loot";

    @ConfigSection(
            name = "Combat & Specials",
            description = "Weapon special attacks and combat triggers",
            position = 3
    )
    String combatSection = "combat";

    @ConfigSection(
            name = "PvP",
            description = "Player vs Player triggers",
            position = 4
    )
    String pvpSection = "pvp";

    @ConfigSection(
            name = "Achievements",
            description = "Level ups, quests, pets, diaries and progression triggers",
            position = 5
    )
    String achievementSection = "achievements";

    @ConfigSection(
            name = "Misc",
            description = "Misc triggers",
            position = 6
    )
    String miscSection = "misc";

    // =====================================================
    // GENERAL
    // =====================================================

    @ConfigItem(
            keyName = "announcementVolume",
            name = "Announcement Volume",
            description = "Controls overall sound volume",
            section = generalSection,
            position = 0
    )
    default int announcementVolume() { return 100; }

    // =====================================================
    // CHAT
    // =====================================================

    @ConfigItem(
            keyName = "enableStreamerMessage",
            name = "Enable Streamer Message",
            description = "Shows a local chatbox message for BoomEpicKill",
            section = chatSection,
            position = 0
    )
    default boolean enableStreamerMessage() { return true; }

    @ConfigItem(
            keyName = "livestreamChatNotification",
            name = "Livestream Chat Notification",
            description = "Display a local chat message when BoomEpicKill goes live",
            section = chatSection
    )
    default boolean livestreamChatNotification()
    {
        return true;
    }

    @ConfigItem(
            keyName = "livestreamSoundNotification",
            name = "Livestream Sound Notification",
            description = "Play a sound when BoomEpicKill goes live",
            section = chatSection
    )
    default boolean livestreamSoundNotification()
    {
        return true;
    }

    // =====================================================
    // LOOT
    // =====================================================

    @ConfigItem(
            keyName = "enableItemSounds",
            name = "Enable Item Sounds",
            description = "Master toggle for loot sound notifications",
            section = lootSection,
            position = 0
    )
    default boolean enableItemSounds() { return true; }

    @ConfigItem(
            keyName = "lootSoundMode",
            name = "Loot Trigger Mode",
            description = "Choose whether sounds trigger from tracked items, GP value, or both",
            section = lootSection,
            position = 1
    )
    default LootSoundMode lootSoundMode() { return LootSoundMode.TRACKED_ITEMS; }

    @ConfigItem(
            keyName = "minimumLootValue",
            name = "Minimum GP Value",
            description = "Plays sound when a loot item stack is worth at least this much GP",
            section = lootSection,
            position = 2
    )
    default int minimumLootValue() { return 100000; }

    @ConfigItem(
            keyName = "trackedItems",
            name = "Tracked Items",
            description = "Comma separated item names",
            section = lootSection,
            position = 3
    )
    default String trackedItems() { return "Bones, Big bones, Clue scroll (hard)"; }

    // =====================================================
    // COMBAT & SPECIALS
    // =====================================================

    @ConfigItem(
            keyName = "prayerMessage",
            name = "Prayer Messages",
            description = "Plays sound on prayer-related notifications",
            section = combatSection,
            position = 0
    )
    default boolean prayerMessage() { return true; }

    // =====================================================
    // PVP
    // =====================================================

    @ConfigItem(
            keyName = "playerKilling",
            name = "Player Kills",
            description = "Plays sound when you kill a player",
            section = pvpSection,
            position = 0
    )
    default boolean playerKilling() { return true; }

    // =====================================================
    // ACHIEVEMENTS
    // =====================================================

    @ConfigItem(
            keyName = "levelUps",
            name = "Level Ups",
            description = "Plays sound when leveling up",
            section = achievementSection,
            position = 0
    )
    default boolean levelUps() { return true; }

    @ConfigItem(
            keyName = "questCompletions",
            name = "Quest Completion",
            description = "Plays sound when completing quests",
            section = achievementSection,
            position = 1
    )
    default boolean questCompletions() { return true; }

    @ConfigItem(
            keyName = "death",
            name = "Death",
            description = "Plays sound when you die",
            section = achievementSection,
            position = 2
    )
    default boolean death() { return true; }

    // =====================================================
    // MISC
    // =====================================================

    @ConfigItem(
            keyName = "sendReport",
            name = "Report Player",
            description = "Plays sound when reporting a player",
            section = miscSection,
            position = 0
    )
    default boolean sendReport() { return true; }

    @ConfigItem(
            keyName = "collectionLog",
            name = "Collection Log",
            description = "Play a sound when you unlock a new collection log item",
            section = miscSection
    )
    default boolean collectionLog()
    {
        return true;
    }
}