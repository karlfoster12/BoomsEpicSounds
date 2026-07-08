
package com.thearkane.boomsepicsounds;

import lombok.Getter;

import java.util.function.Predicate;

/**
 * Pairs a set of chat phrases + a config toggle with the SoundEvent to fire.
 * Instead of a growing if/else chain in onChatMessage, the plugin just loops
 * over a List<ChatTrigger> - adding a new chat-based sound is one new entry.
 */

public class ChatTrigger
{
    @Getter
    private final SoundEvent event;
    private final Predicate<BoomsEpicSoundsConfig> enabledCheck;
    private final String[] phrases;

    public ChatTrigger(SoundEvent event, Predicate<BoomsEpicSoundsConfig> enabledCheck, String... phrases)
    {
        this.event = event;
        this.enabledCheck = enabledCheck;
        this.phrases = phrases;
    }

    public boolean matches(String lowerCaseMessage, BoomsEpicSoundsConfig config)
    {
        if (!enabledCheck.test(config))
        {
            return false;
        }

        for (String phrase : phrases)
        {
            if (lowerCaseMessage.contains(phrase))
            {
                return true;
            }
        }

        return false;
    }
}