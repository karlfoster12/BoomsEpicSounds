package net.runelite.client.plugins.boomsepicsounds;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.http.api.item.ItemPrice;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;

@PluginDescriptor(
        name = "Booms Epic Sounds",
        description = "Booms combat + PvP + achievement sound system",
        tags = {"sounds", "pvm", "pvp", "fun"}
)
public class BoomsEpicSoundsPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private BoomsEpicSoundsConfig config;

    @Inject
    private ItemManager itemManager;

    private final Set<Integer> trackedItemIds = new HashSet<>();
    private final int[] lastXp = new int[Skill.values().length];

    private int lastSoundTick = -1;
    private boolean wasDead = false;
    private boolean initialized = false;
    private boolean pendingReload = true;

    private static final String SOUND_LOOT = "/thehit.wav";
    private static final String SOUND_PRAYER = "/letsgo.wav";
    private static final String SOUND_PLAYER_KILL = "/letsgo.wav";
    private static final String SOUND_REPORT = "/letsgo.wav";
    private static final String SOUND_DEATH = "/death.wav";
    private static final String SOUND_LEVEL_UP = "/letsgo.wav";
    private static final String SOUND_QUEST = "/letsgo.wav";

    @Provides
    BoomsEpicSoundsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BoomsEpicSoundsConfig.class);
    }

    @Override
    protected void startUp()
    {
        System.out.println("[BES] STARTED");
        Arrays.fill(lastXp, -1);
        initialized = false;
        pendingReload = true;
        wasDead = false;

        
    }

    @Override
    protected void shutDown()
    {
        trackedItemIds.clear();
        initialized = false;
        pendingReload = true;
    
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
            else
            {

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

            boolean trackedMatch = trackedItemIds.contains(item.getId());

            int price = itemManager.getItemPrice(item.getId());
            long stackValue = (long) price * item.getQuantity();
            boolean valueMatch = stackValue >= config.minimumLootValue();

            boolean shouldTrigger =
                    config.lootSoundMode() == BoomsEpicSoundsConfig.LootSoundMode.TRACKED_ITEMS && trackedMatch
                            || config.lootSoundMode() == BoomsEpicSoundsConfig.LootSoundMode.GP_VALUE && valueMatch
                            || config.lootSoundMode() == BoomsEpicSoundsConfig.LootSoundMode.BOTH && (trackedMatch || valueMatch);

            if (shouldTrigger)
            {

                trigger(SOUND_LOOT);
                return;
            }
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
        int xp = event.getXp();

        if (lastXp[idx] == -1)
        {
            lastXp[idx] = xp;
            return;
        }

        int oldLevel = Experience.getLevelForXp(lastXp[idx]);
        int newLevel = Experience.getLevelForXp(xp);

        if (newLevel > oldLevel)
        {
            trigger(SOUND_LEVEL_UP);
        }

        lastXp[idx] = xp;
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        if (!initialized || pendingReload)
        {
            reloadConfig();
            initialized = true;
            pendingReload = false;
        }

        Player p = client.getLocalPlayer();

        if (p == null)
        {
            return;
        }

        boolean dead = p.getHealthRatio() == 0;

        if (dead && !wasDead && config.death())
        {

            trigger(SOUND_DEATH);
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

        if (config.playerKilling() &&
                (msg.contains("you have defeated") || msg.contains("you killed")))
        {

            trigger(SOUND_PLAYER_KILL);
            return;
        }

        if (config.sendReport() &&
                (msg.contains("your abuse report has been received")
                        || msg.contains("thank-you, your abuse report has been received")))
        {

            trigger(SOUND_REPORT);
            return;
        }

        if (config.prayerMessage() &&
                (msg.contains("you have run out of prayer points")
                        || msg.contains("you need to recharge your prayer")
                        || msg.contains("your prayer has been drained")))
        {

            trigger(SOUND_PRAYER);
            return;
        }

        if (config.questCompletions() &&
                (msg.contains("quest complete") || msg.contains("congratulations, quest complete")))
        {

            trigger(SOUND_QUEST);
        }
    }

    private void trigger(String soundFile)
    {
        if (client.getTickCount() == lastSoundTick)
        {
            return;
        }

        lastSoundTick = client.getTickCount();
        playSound(soundFile);
    }

    private void playSound(String soundFile)
    {
        try
        {
            InputStream is = BoomsEpicSoundsPlugin.class.getResourceAsStream(soundFile);

            if (is == null)
            {
                System.out.println("[BES] Sound file missing: " + soundFile);
                return;
            }

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(new BufferedInputStream(is));

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            int volume = Math.max(0, Math.min(100, config.announcementVolume()));

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
            {
                FloatControl gainControl =
                        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                if (volume == 0)
                {
                    gainControl.setValue(gainControl.getMinimum());
                }
                else
                {
                    float gain = (float) (20.0 * Math.log10(volume / 100.0));
                    gain = Math.max(gainControl.getMinimum(), Math.min(gain, gainControl.getMaximum()));
                    gainControl.setValue(gain);
                }
            }

            clip.setFramePosition(0);
            clip.start();


        }
        catch (Exception e)
        {

        }
    }
}