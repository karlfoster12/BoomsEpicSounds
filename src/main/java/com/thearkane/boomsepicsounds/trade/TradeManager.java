package com.thearkane.boomsepicsounds.trade;

import com.thearkane.boomsepicsounds.BoomsEpicSoundsConfig;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;


@Slf4j
public class TradeManager
{
    private static final int TRADE_MAIN_GROUP_ID = 335;
    private static final int TRADE_CONFIRM_GROUP_ID = 334;

    private final BoomsEpicSoundsConfig config;
    private final Client client;

    private boolean wasConfirmScreenOpen;

    @Inject
    public TradeManager(BoomsEpicSoundsConfig config, Client client)
    {
        this.config = config;
        this.client = client;
    }

    /**
     * Call once per game tick. Returns true on the tick a trade is
     * detected as successfully completed.
     */
    public boolean check()
    {
        if (!config.tradeAcceptedSoundNotification())
        {
            wasConfirmScreenOpen = false;
            return false;
        }

        boolean confirmOpen = isGroupOpen(TRADE_CONFIRM_GROUP_ID);
        boolean mainOpen = isGroupOpen(TRADE_MAIN_GROUP_ID);

        boolean tradeAccepted = wasConfirmScreenOpen && !confirmOpen && !mainOpen;

        wasConfirmScreenOpen = confirmOpen;

        return tradeAccepted;
    }

    private boolean isGroupOpen(int groupId)
    {
        Widget widget = client.getWidget(groupId, 0);
        return widget != null && !widget.isHidden();
    }
}