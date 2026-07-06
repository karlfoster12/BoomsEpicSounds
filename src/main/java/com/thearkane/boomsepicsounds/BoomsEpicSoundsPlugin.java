package com.thearkane.boomsepicsounds;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
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
import net.runelite.client.audio.AudioPlayer;
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
import net.runelite.http.api.item.ItemPrice;

@Slf4j
@PluginDescriptor(
        name = "Booms Epic Sounds",
        description = "Booms combat + PvP + achievement sound system",
        tags = {"sounds", "pvm", "pvp", "fun"}
)
public class BoomsEpicSoundsPlugin extends Plugin
{
    private static final String[] LOOT_SOUNDS =
            {
                    "/thehit.wav",
                    "/letsgo.wav",
            };

    private static final String[] PRAYER_SOUNDS =
            {
                    "/prayer.wav",
            };

    private static final String[] DEATH_SOUNDS =
            {
                    "/ohnostepbro.wav"
            };

    private static final String[] PLAYER_KILL_SOUNDS =
            {
                    "/takethisdlong.wav"
            };

    private static final String[] REPORT_SOUNDS =
            {
                    "/wellhesgunnagetbanned.wav",
                    "/wellyourgunnagetbanned.wav"
            };

    private static final String[] LEVEL_UP_SOUNDS =
            {
                    "/wellthatsgoingtomakeitgrowabit.wav",
                    "/anotherlevelanotherinch.wav"


            };

    private static final String[] QUEST_SOUNDS =
            {
                    "/QuestCompleted/finallyquestcomplete.wav",
                    "/QuestCompleted/goodjobbuddyhopeyourhavingfun.wav",
                    "/QuestCompleted/ohmyyoufinishedanotherquest.wav"
            };

    @Inject
    private Client client;

    @Inject
    private BoomsEpicSoundsConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private AudioPlayer audioPlayer;

    private final Set<Integer> trackedItemIds = new HashSet<>();
    private final int[] lastLevels = new int[Skill.values().length];
    private final Random random = new Random();

    private int lastSoundTick = -1;
    private boolean wasDead;
    private boolean initialized;
    private boolean pendingReload = true;
    private boolean streamerMessageShown;

    @Provides
    BoomsEpicSoundsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BoomsEpicSoundsConfig.class);
    }

    @Override
    protected void startUp()
    {
        Arrays.fill(lastLevels, -1);
        initialized = false;
        pendingReload = true;
        wasDead = false;
        streamerMessageShown = false;
    }

    @Override
    protected void shutDown()
    {
        trackedItemIds.clear();
        initialized = false;
        pendingReload = true;
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

        if ("enableStreamerMessage".equals(event.getKey())
                || "streamerMessage".equals(event.getKey()))
        {
            streamerMessageShown = false;
            sendStreamerMessage();
            return;
        }

        pendingReload = true;
    }

    private void initialiseLevels()
    {
        for (Skill skill : Skill.values())
        {
            lastLevels[skill.ordinal()] = client.getRealSkillLevel(skill);
        }
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
                trigger(LOOT_SOUNDS);
                return;
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            // Only initialise once per login session
            if (lastLevels[0] == -1)
            {
                initialiseLevels();
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
            trigger(LEVEL_UP_SOUNDS);
        }

        lastLevels[idx] = currentLevel;
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

        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return;
        }

        boolean dead = player.getHealthRatio() == 0;

        if (dead && !wasDead && config.death())
        {
            trigger(DEATH_SOUNDS);
        }

        wasDead = dead;
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        String message = event.getMessage();
        if (message == null)
        {
            return;
        }

        String msg = message.toLowerCase();

        if (config.playerKilling()
                && (msg.contains("you have defeated") || msg.contains("you killed")))
        {
            trigger(PLAYER_KILL_SOUNDS);
            return;
        }

        if (config.sendReport() && msg.contains("abuse report"))
        {
            trigger(REPORT_SOUNDS);
            return;
        }

        if (config.prayerMessage()
                && (msg.contains("you have run out of prayer points")
                || msg.contains("you need to recharge your prayer")
                || msg.contains("your prayer has been drained")))
        {
            trigger(PRAYER_SOUNDS);
            return;
        }

        if (config.questCompletions()
                && (msg.contains("quest complete") || msg.contains("congratulations, quest complete")))
        {
            trigger(QUEST_SOUNDS);
        }
    }

    private void trigger(String[] soundFiles)
    {
        if (client.getTickCount() == lastSoundTick)
        {
            return;
        }

        if (soundFiles == null || soundFiles.length == 0)
        {
            return;
        }

        lastSoundTick = client.getTickCount();

        String soundFile = soundFiles[random.nextInt(soundFiles.length)];
        playSound(soundFile);
    }

    private void playSound(String soundFile)
    {
        int volume = Math.max(0, Math.min(100, config.announcementVolume()));

        if (volume == 0)
        {
            return;
        }

        float gain = (float) (20.0 * Math.log10(volume / 100.0));

        try
        {
            audioPlayer.play(BoomsEpicSoundsPlugin.class, soundFile, gain);
        }
        catch (Exception e)
        {
            log.debug("Unable to play sound {}", soundFile, e);
        }
    }

    private void sendStreamerMessage()
    {
        if (!config.enableStreamerMessage())
        {
            return;
        }

        String message = config.streamerMessage();

        if (message == null || message.trim().isEmpty())
        {
            return;
        }

        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .runeLiteFormattedMessage("<col=ff0000>" + message + "</col>")
                        .build()
        );

        streamerMessageShown = true;
    }
}