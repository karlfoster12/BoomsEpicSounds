package com.thearkane.boomsepicsounds.livestream;

import com.thearkane.boomsepicsounds.BoomsEpicSoundsConfig;
import com.thearkane.boomsepicsounds.SoundManager;


import com.google.gson.Gson;
import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class LivestreamManager
{
    private static final String LIVE_STATUS_URL =
            "https://raw.githubusercontent.com/karlfoster12/booms-epic-sounds-live/main/livestream.json";

    private static final int CHECK_INTERVAL_TICKS = 100;

    private final BoomsEpicSoundsConfig config;
    private final ChatMessageManager chatMessageManager;
    private final OkHttpClient okHttpClient;
    private final SoundManager soundManager;
    private final Gson gson;

    private int lastCheckTick = -CHECK_INTERVAL_TICKS;

    // Written from the OkHttp callback thread, read from the game thread in
    // handleCachedStatus() - must be volatile so the game thread is
    // guaranteed to see the update instead of a stale cached value.
    private volatile boolean requestInProgress;
    private volatile LivestreamStatus cachedStatus;

    private boolean liveMessageShown;

    @Inject
    public LivestreamManager(
            BoomsEpicSoundsConfig config,
            ChatMessageManager chatMessageManager,
            SoundManager soundManager,
            OkHttpClient okHttpClient,
            Gson gson)
    {
        this.config = config;
        this.chatMessageManager = chatMessageManager;
        this.soundManager = soundManager;
        this.okHttpClient = okHttpClient;
        this.gson = gson;
    }



    /**
     * Polls (at most every CHECK_INTERVAL_TICKS) for livestream status and
     * sends the chat notification itself if configured to. Sound is NOT
     * played here - this returns true when a "went live" sound should be
     * triggered, so the caller can route it through the plugin's priority
     * queue instead of it playing instantly and stepping on whatever else
     * fired the same tick.
     */
    public boolean check(int tickCount)
    {
        if (!config.livestreamChatNotification()
                && !config.livestreamSoundNotification())
        {
            liveMessageShown = false;
            return false;
        }

        if (tickCount - lastCheckTick >= CHECK_INTERVAL_TICKS)
        {
            lastCheckTick = tickCount;
            fetchStatusAsync();
        }

        return handleCachedStatus();
    }

    private void fetchStatusAsync()
    {
        if (requestInProgress)
        {
            return;
        }

        requestInProgress = true;

        Request request = new Request.Builder()
                .url(LIVE_STATUS_URL)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                requestInProgress = false;
                log.debug("Failed to fetch livestream status", e);
            }

            @Override
            public void onResponse(Call call, Response response)
            {
                requestInProgress = false;

                try (Response res = response)
                {
                    if (!res.isSuccessful() || res.body() == null)
                    {
                        return;
                    }

                    cachedStatus = gson.fromJson(res.body().charStream(), LivestreamStatus.class);
                }
                catch (Exception e)
                {
                    log.debug("Failed to parse livestream status", e);
                }
            }
        });
    }

    private boolean handleCachedStatus()
    {
        LivestreamStatus status = cachedStatus;

        if (status == null || !status.isEnabled() || !status.isLive())
        {
            liveMessageShown = false;
            return false;
        }

        if (liveMessageShown)
        {
            return false;
        }

        if (config.livestreamChatNotification())
        {
            showLiveMessage(status);
        }

        liveMessageShown = true;

        return config.livestreamSoundNotification();
    }

    private void showLiveMessage(LivestreamStatus status)
    {
        String message = MessageSanitiser.sanitise(status.getMessage());

        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .runeLiteFormattedMessage("<col=ff0000>" + message + "</col>")
                        .build()
        );
    }
}