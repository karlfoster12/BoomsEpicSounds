package com.thearkane.boomsepicsounds;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.thearkane.boomsepicsounds.livestream.LivestreamManager;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.http.api.item.ItemPrice;

// =============================================================================
// Plugin metadata
// =============================================================================

@Slf4j
@PluginDescriptor(
        name = "Booms Epic Sounds",
        description = "Custom sound notifications for RuneLite events",
        tags = {"sounds", "pvm", "pvp", "fun"}
)
public class BoomsEpicSoundsPlugin extends Plugin
{
    // =========================================================================
    // Constants
    // =========================================================================

    private static final String STREAMER_MESSAGE = "BoomEpicKill is live daily from 11AM ET";

    // =========================================================================
    // Chat triggers
    // =========================================================================

    private final List<ChatTrigger> chatTriggers = List.of(
            new ChatTrigger(SoundEvent.PLAYER_KILL, BoomsEpicSoundsConfig::playerKilling,
                    "you have defeated", "you killed"),
            new ChatTrigger(SoundEvent.REPORT, BoomsEpicSoundsConfig::sendReport,
                    "abuse report"),
            new ChatTrigger(SoundEvent.PRAYER, BoomsEpicSoundsConfig::prayerMessage,
                    "you have run out of prayer points", "you need to recharge your prayer",
                    "your prayer has been drained"),
            new ChatTrigger(SoundEvent.QUEST_COMPLETED, BoomsEpicSoundsConfig::questCompletions,
                    "quest complete", "congratulations, quest complete"),
            new ChatTrigger(SoundEvent.COLLECTION_LOG, BoomsEpicSoundsConfig::collectionLog,
                    "new collection log item",
                    "collection log")
    );

    // =========================================================================
    // Injected RuneLite services
    // =========================================================================

    @Inject
    private Client client;

    @Inject
    private BoomsEpicSoundsConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private SoundManager soundManager;

    @Inject
    private LivestreamManager livestreamManager;

    // =========================================================================
    // Runtime state
    // =========================================================================

    private final Set<Integer> trackedItemIds = new HashSet<>();
    private final int[] lastLevels = new int[Skill.values().length];
    
    private final Queue<SoundEvent> pendingSounds = new PriorityQueue<>(Comparator.comparingInt(SoundEvent::getPriority));

    private boolean wasDead;
    private boolean initialized;
    private boolean pendingReload = true;
    private boolean streamerMessageShown;
    private boolean pendingLevelInit;

    // =========================================================================
    // Config provider
    // =========================================================================

    @Provides
    BoomsEpicSoundsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BoomsEpicSoundsConfig.class);
    }

    // =========================================================================
    // Plugin lifecycle
    // =========================================================================

    @Override
    protected void startUp()
    {
        Arrays.fill(lastLevels, -1);
        initialized = false;
        pendingReload = true;
        pendingLevelInit = false;
        wasDead = false;
        streamerMessageShown = false;
        // Folder creation now lives entirely in SoundManager's constructor.
    }

    @Override
    protected void shutDown()
    {
        trackedItemIds.clear();
        initialized = false;
        pendingReload = true;
        streamerMessageShown = false;
    }

    // =========================================================================
    // Config loading
    // =========================================================================

    private void reloadConfig()
    {
        trackedItemIds.clear();

        String raw = config.trackedItems();
        if (raw == null || raw.trim().isEmpty())
        {
            return;
        }

        for (String name : raw.split(","))
        {
            String trimmed = name.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }

            int id = findItemIdByName(trimmed);
            if (id != -1)
            {
                trackedItemIds.add(id);
            }
        }
    }

    private int findItemIdByName(String itemName)
    {
        return itemManager.search(itemName).stream()
                .filter(item -> item.getName().equalsIgnoreCase(itemName))
                .mapToInt(ItemPrice::getId)
                .findFirst()
                .orElse(-1);
    }

    // =========================================================================
    // RuneLite event handlers
    // =========================================================================
    // Configuration
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!"boomsepicsounds".equals(event.getGroup()))
        {
            return;
        }

        if ("enableStreamerMessage".equals(event.getKey()))
        {
            streamerMessageShown = false;
            sendStreamerMessage();
            return;
        }

        pendingReload = true;
    }

    // Loot
    @Subscribe
    public void onLootReceived(LootReceived event)
    {
        if (!config.enableItemSounds() || event.getItems() == null)
        {
            return;
        }

        for (ItemStack item : event.getItems())
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            boolean trackedMatch = trackedItemIds.contains(item.getId());

            int price = itemManager.getItemPrice(item.getId());
            long stackValue = (long) price * item.getQuantity();
            boolean valueMatch = stackValue >= config.minimumLootValue();

            boolean shouldTrigger =
                    (config.lootSoundMode() == BoomsEpicSoundsConfig.LootSoundMode.TRACKED_ITEMS && trackedMatch)
                            || (config.lootSoundMode() == BoomsEpicSoundsConfig.LootSoundMode.GP_VALUE && valueMatch)
                            || (config.lootSoundMode() == BoomsEpicSoundsConfig.LootSoundMode.BOTH && (trackedMatch || valueMatch));

            if (shouldTrigger)
            {
                trigger(SoundEvent.LOOT);
                return;
            }
        }
    }

    // Login / Logout
    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            if (lastLevels[0] == -1)
            {
                pendingLevelInit = true;
            }

            if (!streamerMessageShown)
            {
                sendStreamerMessage();
            }
        }
        else if (event.getGameState() == GameState.LOGIN_SCREEN)
        {
            streamerMessageShown = false;
            Arrays.fill(lastLevels, -1);
        }
    }

    // Skills
    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (!config.levelUps())
        {
            return;
        }

        Skill skill = event.getSkill();
        int idx = skill.ordinal();
        int currentLevel = event.getLevel();

        if (lastLevels[idx] == -1)
        {
            lastLevels[idx] = currentLevel;
            return;
        }

        if (currentLevel > lastLevels[idx])
        {
            trigger(SoundEvent.LEVEL_UP);
        }

        lastLevels[idx] = currentLevel;
    }

    // Game Tick
    @Subscribe
    public void onGameTick(GameTick event) {
        if (!initialized || pendingReload) {
            reloadConfig();
            initialized = true;
            pendingReload = false;
        }

        if (pendingLevelInit) {
            initialiseLevels();
            pendingLevelInit = false;
        }

        Player player = client.getLocalPlayer();

        if (player != null) {
            boolean dead = player.getHealthRatio() == 0;

            if (dead && !wasDead && config.death()) {
                trigger(SoundEvent.DEATH);
            }

            wasDead = dead;

            if (livestreamManager.check(client.getTickCount()))
            {
                trigger(SoundEvent.LIVESTREAM);
            }
        }

        playNextQueuedSound();
    }

    // Chat
    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        String message = event.getMessage();
        if (message == null)
        {
            return;
        }

        String msg = message.toLowerCase();

        for (ChatTrigger chatTrigger : chatTriggers)
        {
            if (chatTrigger.matches(msg, config))
            {
                trigger(chatTrigger.getEvent());
                return;
            }
        }
    }

    // =========================================================================
    // Sound queue
    // =========================================================================

    private void trigger(SoundEvent event)
    {

        if (!pendingSounds.contains(event))
        {
            pendingSounds.add(event);
        }
    }

    private void playNextQueuedSound()
    {
        if (pendingSounds.isEmpty())
        {
            return;
        }

        SoundEvent next = pendingSounds.poll();
        soundManager.play(next, config.announcementVolume());
        pendingSounds.clear();
    }

    // =========================================================================
    // Chat helpers
    // =========================================================================

    private void sendStreamerMessage()
    {
        if (!config.enableStreamerMessage())
        {
            return;
        }

        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .runeLiteFormattedMessage("<col=ff0000>" + STREAMER_MESSAGE + "</col>")
                        .build()
        );

        streamerMessageShown = true;
    }

    // =========================================================================
    // Level and death helpers
    // =========================================================================

    private void initialiseLevels()
    {
        for (Skill skill : Skill.values())
        {
            lastLevels[skill.ordinal()] = client.getRealSkillLevel(skill);
        }
    }
}