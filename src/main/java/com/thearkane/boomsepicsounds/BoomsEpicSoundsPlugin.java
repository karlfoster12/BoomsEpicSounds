package com.thearkane.boomsepicsounds;

import com.thearkane.boomsepicsounds.livestream.LivestreamManager;
import com.thearkane.boomsepicsounds.trade.TradeManager;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.ActorDeath;
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
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemPrice;

@Slf4j
@PluginDescriptor(
        name = "Booms Epic Sounds",
        description = "Custom sound notifications for RuneLite events",
        tags = {"sounds", "pvm", "pvp", "fun"}
)
public class BoomsEpicSoundsPlugin extends Plugin
{
    private static final String STREAMER_MESSAGE =
            "BoomEpicKill is live daily from 11AM ET";

    /*
     * Maximum time allowed between ActorDeath and the matching
     * "You have defeated..." game message.
     *
     * Five ticks is approximately three seconds.
     */
    private static final int PLAYER_KILL_CONFIRMATION_TICKS = 5;

    /*
     * Player kills and abuse reports are handled separately.
     */
    private final List<ChatTrigger> chatTriggers = List.of(
            new ChatTrigger(
                    SoundEvent.PRAYER,
                    BoomsEpicSoundsConfig::prayerMessage,
                    "you have run out of prayer points",
                    "your prayer has been drained"
            ),
            new ChatTrigger(
                    SoundEvent.QUEST_COMPLETED,
                    BoomsEpicSoundsConfig::questCompletions,
                    "quest complete",
                    "congratulations, quest complete"
            ),
            new ChatTrigger(
                    SoundEvent.COLLECTION_LOG,
                    BoomsEpicSoundsConfig::collectionLog,
                    "new collection log item",
                    "collection log"
            )
    );

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

    @Inject
    private TradeManager tradeManager;

    private final Set<Integer> trackedItemIds = new HashSet<>();
    private final int[] lastLevels = new int[Skill.values().length];

    private final Queue<SoundEvent> pendingSounds =
            new PriorityQueue<>(
                    Comparator.comparingInt(SoundEvent::getPriority)
            );

    private boolean wasDead;
    private boolean initialized;
    private boolean pendingReload = true;
    private boolean streamerMessageShown;
    private boolean pendingLevelInit;

    /*
     * Temporary PvP kill candidate recorded by ActorDeath.
     */
    private String recentlyDefeatedPlayerName;
    private int recentlyDefeatedPlayerTick = -1;

    @Provides
    BoomsEpicSoundsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BoomsEpicSoundsConfig.class);
    }

    @Override
    protected void startUp()
    {
        Arrays.fill(lastLevels, -1);
        pendingSounds.clear();
        clearPlayerKillCandidate();

        initialized = false;
        pendingReload = true;
        pendingLevelInit = false;
        wasDead = false;
        streamerMessageShown = false;
    }

    @Override
    protected void shutDown()
    {
        trackedItemIds.clear();
        pendingSounds.clear();
        clearPlayerKillCandidate();

        initialized = false;
        pendingReload = true;
        pendingLevelInit = false;
        wasDead = false;
        streamerMessageShown = false;
    }

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

            boolean trackedMatch =
                    trackedItemIds.contains(item.getId());

            int price = itemManager.getItemPrice(item.getId());

            long stackValue =
                    (long) price * item.getQuantity();

            boolean valueMatch =
                    stackValue >= config.minimumLootValue();

            boolean shouldTrigger =
                    (config.lootSoundMode()
                            == BoomsEpicSoundsConfig.LootSoundMode.TRACKED_ITEMS
                            && trackedMatch)
                            || (config.lootSoundMode()
                            == BoomsEpicSoundsConfig.LootSoundMode.GP_VALUE
                            && valueMatch)
                            || (config.lootSoundMode()
                            == BoomsEpicSoundsConfig.LootSoundMode.BOTH
                            && (trackedMatch || valueMatch));

            if (shouldTrigger)
            {
                trigger(SoundEvent.LOOT);
                return;
            }
        }
    }

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
            pendingLevelInit = false;

            Arrays.fill(lastLevels, -1);
            clearPlayerKillCandidate();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (!config.levelUps())
        {
            return;
        }

        Skill skill = event.getSkill();
        int index = skill.ordinal();
        int currentLevel = event.getLevel();

        if (lastLevels[index] == -1)
        {
            lastLevels[index] = currentLevel;
            return;
        }

        if (currentLevel > lastLevels[index])
        {
            trigger(SoundEvent.LEVEL_UP);
        }

        lastLevels[index] = currentLevel;
    }

    /*
     * Records a PvP death candidate.
     *
     * This does not play the sound by itself. The game must also send a
     * matching "You have defeated <name>" message shortly afterwards.
     */
    @Subscribe
    public void onActorDeath(ActorDeath event)
    {
        Actor actor = event.getActor();

        if (!(actor instanceof Player))
        {
            return;
        }

        Player deadPlayer = (Player) actor;
        Player localPlayer = client.getLocalPlayer();

        if (localPlayer == null || deadPlayer == localPlayer)
        {
            return;
        }

        /*
         * Confirm that the local player and dead player were interacting.
         *
         * Either direction is accepted because one actor's interacting
         * reference may clear slightly before the death event is received.
         */
        boolean localWasTargetingDeadPlayer =
                localPlayer.getInteracting() == deadPlayer;

        boolean deadPlayerWasTargetingLocal =
                deadPlayer.getInteracting() == localPlayer;

        if (!localWasTargetingDeadPlayer && !deadPlayerWasTargetingLocal)
        {
            return;
        }

        String deadPlayerName = deadPlayer.getName();

        if (deadPlayerName == null || deadPlayerName.trim().isEmpty())
        {
            return;
        }

        recentlyDefeatedPlayerName =
                Text.standardize(deadPlayerName)
                        .toLowerCase(Locale.ROOT);

        recentlyDefeatedPlayerTick = client.getTickCount();

        log.debug(
                "Recorded PvP death candidate: {} at tick {}",
                recentlyDefeatedPlayerName,
                recentlyDefeatedPlayerTick
        );
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!initialized || pendingReload)
        {
            reloadConfig();
            initialized = true;
            pendingReload = false;
        }

        if (pendingLevelInit)
        {
            initialiseLevels();
            pendingLevelInit = false;
        }

        /*
         * Remove an unconfirmed PvP candidate once its time window expires.
         */
        if (recentlyDefeatedPlayerName != null
                && client.getTickCount() - recentlyDefeatedPlayerTick
                > PLAYER_KILL_CONFIRMATION_TICKS)
        {
            clearPlayerKillCandidate();
        }

        if (tradeManager.check())
        {
            trigger(SoundEvent.TRADE_ACCEPTED);
        }

        Player player = client.getLocalPlayer();

        if (player != null)
        {
            boolean dead = player.getHealthRatio() == 0;

            if (dead && !wasDead && config.death())
            {
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

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        String message = event.getMessage();

        if (message == null)
        {
            return;
        }

        String standardizedMessage =
                Text.standardize(message)
                        .toLowerCase(Locale.ROOT);

        /*
         * Abuse report confirmations use SNAPSHOTFEEDBACK.
         */
        if (event.getType() == ChatMessageType.SNAPSHOTFEEDBACK)
        {
            if (config.sendReport()
                    && standardizedMessage.contains("abuse report"))
            {
                trigger(SoundEvent.REPORT);
            }

            return;
        }

        /*
         * Block private messages, public chat, clan chat and friends chat.
         */
        if (event.getType() != ChatMessageType.GAMEMESSAGE
                && event.getType() != ChatMessageType.SPAM)
        {
            return;
        }

        /*
         * Confirm that the recently dead Player is the exact player named
         * by the game's "You have defeated..." message.
         */
        if (config.playerKilling()
                && recentlyDefeatedPlayerName != null
                && recentlyDefeatedPlayerTick >= 0)
        {
            int ticksSinceDeath =
                    client.getTickCount() - recentlyDefeatedPlayerTick;

            boolean withinConfirmationWindow =
                    ticksSinceDeath >= 0
                            && ticksSinceDeath
                            <= PLAYER_KILL_CONFIRMATION_TICKS;

            boolean defeatedMessage =
                    standardizedMessage.contains("you have defeated");

            boolean matchingPlayerName =
                    standardizedMessage.contains(
                            recentlyDefeatedPlayerName
                    );

            if (withinConfirmationWindow
                    && defeatedMessage
                    && matchingPlayerName)
            {
                trigger(SoundEvent.PLAYER_KILL);
                clearPlayerKillCandidate();
                return;
            }
        }

        for (ChatTrigger chatTrigger : chatTriggers)
        {
            if (chatTrigger.matches(standardizedMessage, config))
            {
                trigger(chatTrigger.getEvent());
                return;
            }
        }
    }

    private void clearPlayerKillCandidate()
    {
        recentlyDefeatedPlayerName = null;
        recentlyDefeatedPlayerTick = -1;
    }

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

        soundManager.play(
                next,
                config.announcementVolume()
        );

        /*
         * Only the highest-priority event from this tick is played.
         */
        pendingSounds.clear();
    }

    private void sendStreamerMessage()
    {
        if (!config.enableStreamerMessage())
        {
            return;
        }

        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .runeLiteFormattedMessage(
                                "<col=ff0000>"
                                        + STREAMER_MESSAGE
                                        + "</col>"
                        )
                        .build()
        );

        streamerMessageShown = true;
    }

    private void initialiseLevels()
    {
        for (Skill skill : Skill.values())
        {
            lastLevels[skill.ordinal()] =
                    client.getRealSkillLevel(skill);
        }
    }
}