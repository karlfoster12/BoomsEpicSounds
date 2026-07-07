package com.thearkane.boomsepicsounds.livestream;

 
public class MessageSanitiser
{
    private static final String DEFAULT_LIVE_MESSAGE = "BoomEpicKill is live now!";
    private static final int MAX_MESSAGE_LENGTH = 80;
 
    private static final String ALLOWED_CHARS =
            "A-Za-z0-9 .,!?'\"()\\-:;/£€%&+*=@#\u00C0-\u00FF";
    private static final java.util.regex.Pattern UNSUPPORTED_SYMBOLS =
            java.util.regex.Pattern.compile("[^" + ALLOWED_CHARS + "]");
 
    public static String sanitise(String message)
    {
        if (message == null || message.trim().isEmpty())
        {
            return DEFAULT_LIVE_MESSAGE;
        }
 
        String clean = message.trim();
 
        // Remove RuneLite/HTML-style formatting
        clean = clean.replaceAll("<[^>]*>", "");
 
        // Remove line breaks, tabs and control characters
        clean = clean.replaceAll("[\\r\\n\\t]", " ");
        clean = clean.replaceAll("\\p{Cntrl}", "");
 
        // Strip emoji and any other symbols the chatbox font can't render
        clean = UNSUPPORTED_SYMBOLS.matcher(clean).replaceAll("");
 
        // Normalise spaces
        clean = clean.replaceAll("\\s+", " ").trim();
 
        if (clean.isEmpty())
        {
            return DEFAULT_LIVE_MESSAGE;
        }
 
        String lower = clean.toLowerCase();
 
        if (containsBlockedLink(lower))
        {
            return DEFAULT_LIVE_MESSAGE;
        }
 
        if (clean.length() > MAX_MESSAGE_LENGTH)
        {
            clean = clean.substring(0, MAX_MESSAGE_LENGTH).trim();
        }
 
        return clean;
    }
 
    private static boolean containsBlockedLink(String lower)
    {
        return lower.contains("http://")
                || lower.contains("https://")
                || lower.contains("www.")
                || lower.contains(".com")
                || lower.contains(".net")
                || lower.contains(".org")
                || lower.contains(".gg")
                || lower.contains("discord.gg")
                || lower.contains("kick.com")
                || lower.contains("twitch.tv")
                || lower.contains("youtube.com");
    }
}