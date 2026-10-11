package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared, side-independent URL validation. Never rewrites signed query parameters. */
public final class ChatImageLinks {
    public static final int MAX_URL = 2048;
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\\\"\\u00a7]+", Pattern.CASE_INSENSITIVE);
    private ChatImageLinks() {}

    public static boolean valid(String value) {
        if (value == null || value.length() > MAX_URL) return false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isISOControl(c) || c == '\u00a7') return false;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            String path = uri.getPath();
            return ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                    && uri.getHost() != null && uri.getUserInfo() == null && uri.getFragment() == null
                    && (uri.getPort() == -1 || uri.getPort() == 80 || uri.getPort() == 443)
                    && path != null && path.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpe?g)");
        } catch (Exception invalid) { return false; }
    }

    public static List<String> find(String text) {
        List<String> result = new ArrayList<String>();
        if (text == null) return result;
        Matcher matcher = URL.matcher(text);
        while (matcher.find() && result.size() < 3) {
            String url = matcher.group();
            while (url.endsWith(")") || url.endsWith("]") || url.endsWith(",") || url.endsWith("."))
                url = url.substring(0, url.length() - 1);
            if (valid(url) && !result.contains(url)) result.add(url);
        }
        return result;
    }
}
