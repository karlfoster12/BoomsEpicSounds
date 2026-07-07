package com.thearkane.boomsepicsounds;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.audio.AudioPlayer;

/**
 * Owns everything to do with where sound files live and how they get played.
 * The plugin class no longer needs to know about File, folders, or gain math.
 */

@Slf4j
@Singleton

public class SoundManager
{
    private static final String ROOT_FOLDER = "booms-epic-sounds";
    private static final long CACHE_TTL_MS = 5_000;

    private final AudioPlayer audioPlayer;
    private final File soundsDirectory;
    private final Random random = new Random();

    // Cache directory listings instead of hitting disk on every trigger.
    private final Map<SoundEvent, File[]> cachedFiles = new EnumMap<>(SoundEvent.class);
    private final Map<SoundEvent, Long> cacheTimestamps = new EnumMap<>(SoundEvent.class);

    @Inject
    public SoundManager(AudioPlayer audioPlayer)
    {
        this.audioPlayer = audioPlayer;
        this.soundsDirectory = new File(RuneLite.RUNELITE_DIR, ROOT_FOLDER);

        for (SoundEvent event : SoundEvent.values())
        {
            createFolder(event.getFolderName());
        }
    }

    private void createFolder(String name)
    {
        File folder = new File(soundsDirectory, name);
        if (!folder.exists() && !folder.mkdirs())
        {
            log.debug("Unable to create sound folder: {}", folder.getAbsolutePath());
        }
    }

    /**
     * Plays a custom sound for this event if the user has dropped .wav files
     * into its folder, otherwise falls back to the bundled sound(s).
     */
    public void play(SoundEvent event, int volumePercent)
    {
        int volume = Math.max(0, Math.min(100, volumePercent));
        if (volume == 0)
        {
            return;
        }

        float gain = (float) (20.0 * Math.log10(volume / 100.0));

        File custom = getRandomCustomSound(event);
        if (custom != null)
        {
            playLocal(custom, gain);
            return;
        }

        String[] fallback = event.getFallbackSounds();
        if (fallback.length == 0)
        {
            return;
        }

        playResource(fallback[random.nextInt(fallback.length)], gain);
    }

    /** Call this (e.g. from a config-changed listener) if you want to force a re-scan. */
    public void invalidateCache(SoundEvent event)
    {
        cachedFiles.remove(event);
        cacheTimestamps.remove(event);
    }

    private File getRandomCustomSound(SoundEvent event)
    {
        File[] files = getCachedFiles(event);
        if (files == null || files.length == 0)
        {
            return null;
        }
        return files[random.nextInt(files.length)];
    }

    private File[] getCachedFiles(SoundEvent event)
    {
        long now = System.currentTimeMillis();
        Long timestamp = cacheTimestamps.get(event);

        if (timestamp != null && now - timestamp < CACHE_TTL_MS)
        {
            return cachedFiles.get(event);
        }

        File folder = new File(soundsDirectory, event.getFolderName());
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

        cachedFiles.put(event, files);
        cacheTimestamps.put(event, now);

        return files;
    }

    private void playLocal(File file, float gain)
    {
        try
        {
            audioPlayer.play(file, gain);
        }
        catch (Exception e)
        {
            log.debug("Unable to play local sound {}", file.getAbsolutePath(), e);
        }
    }

    private void playResource(String soundFile, float gain)
    {
        try
        {
            audioPlayer.play(BoomsEpicSoundsPlugin.class, soundFile, gain);
        }
        catch (Exception e)
        {
            log.debug("Unable to play bundled sound {}", soundFile, e);
        }
    }
}
