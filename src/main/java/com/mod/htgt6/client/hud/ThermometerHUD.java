package com.mod.htgt6.client.hud;


public class ThermometerHUD {

    private static String title = "";
    private static String text = "";

    private static long startTime = 0;
     private static long duration = 3000;

    public static void show(String t, String msg) {
        title = t;
        text = msg;
        startTime = System.currentTimeMillis();

    }
    public static boolean isVisible() {
        return System.currentTimeMillis() - startTime < duration;
    }

    public static float getAlpha() {

        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed < 250)
            return elapsed / 250.0F;

        if (elapsed > duration - 500)
            return (duration - elapsed) / 500.0F;

        return 1.0F;
    }

    public static String getTitle() {
        return title;
    }

    public static String getText() {
        return text;
    }
}
