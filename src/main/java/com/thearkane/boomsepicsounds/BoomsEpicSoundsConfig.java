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
            name = "Loot",
            description = "Loot Drop settings",
            position = 1
    )
    String LootSection = "Loot";

    @ConfigSection(
            name = "Combat & Specials",
            description = "Weapon special attacks and combat triggers",
            position = 2
    )
    String combatSection = "combat";

    @ConfigSection(
            name = "PvP",
            description = "Player vs Player triggers",
            position = 3
    )
    String pvpSection = "pvp";

    @ConfigSection(
            name = "Achievements",
            description = "Level ups, quests, pets, diaries and progression triggers",
            position = 4
    )
    String achievementSection = "achievements";

    @ConfigSection(
            name = "Misc",
            description = "Misc triggers",
            position = 5
    )
    String MiscSection = "achievements";

    // =====================================================
    // GENERAL
    // =====================================================

    @ConfigItem(
            keyName = "enableItemSounds",
            name = "Enable Item Sounds",
            description = "Plays sounds when receiving tracked items",
            section = generalSection
    )
    default boolean enableItemSounds() { return true; }

    @ConfigItem(
            keyName = "announcementVolume",
            name = "Announcement Volume",
            description = "Controls overall sound volume (if implemented)",
            section = generalSection
    )
    default int announcementVolume() { return 100; }


    @ConfigItem(
            keyName = "lootSoundMode",
            name = "Loot Trigger Mode",
            description = "Choose whether sounds trigger from tracked items, GP value, or both",
            section = LootSection,
            position = 1
    )
    default LootSoundMode lootSoundMode()
    {
        return LootSoundMode.TRACKED_ITEMS;
    }

    @ConfigItem(
            keyName = "minimumLootValue",
            name = "Minimum GP Value",
            description = "Plays sound when a loot item stack is worth at least this much GP",
            section = LootSection,
            position = 2
    )
    default int minimumLootValue()
    {
        return 100000;
    }

    @ConfigItem(
            keyName = "trackedItems",
            name = "Tracked Items",
            description = "Comma separated item names",
            section = generalSection,
            position = 3

    )
    default String trackedItems()
    {
        return "Bones, Big bones, Clue scroll (hard)";
    }

    // =====================================================
    // COMBAT & SPECIALS
    // =====================================================

    @ConfigItem(
            keyName = "prayerMessage",
            name = "Prayer Messages",
            description = "Plays sound on prayer-related notifications",
            section = combatSection
    )
    default boolean prayerMessage() { return true; }


    // =====================================================
    // PVP
    // =====================================================

    @ConfigItem(
            keyName = "playerKilling",
            name = "Player Kills",
            description = "Plays sound when you kill a player",
            section = pvpSection
    )
    default boolean playerKilling() { return true; }


    // =====================================================
    // ACHIEVEMENTS
    // =====================================================

    @ConfigItem(
            keyName = "levelUps",
            name = "Level Ups",
            description = "Plays sound when leveling up",
            section = achievementSection
    )
    default boolean levelUps() { return true; }

    @ConfigItem(
            keyName = "questCompletions",
            name = "Quest Completion",
            description = "Plays sound when completing quests",
            section = achievementSection
    )
    default boolean questCompletions() { return true; }

    @ConfigItem(
            keyName = "death",
            name = "Death",
            description = "Plays sound when you die",
            section = achievementSection
    )
    default boolean death() { return true; }

    // =====================================================
    // MISC
    // =====================================================

    @ConfigItem(
            keyName = "sendReport",
            name = "Report Player",
            description = "Plays sound when reporting a player",
            section = pvpSection
    )
    default boolean sendReport() { return true; }

}