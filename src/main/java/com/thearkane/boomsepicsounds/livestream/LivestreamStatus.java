package com.thearkane.boomsepicsounds.livestream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LivestreamStatus
{
    private boolean enabled;
    private boolean live;
    private String title;
    private String message;
    private String wentLiveAt;
}