package com.mod.htgt6.common.ui;

/** Compact terminal labels; diagnostic tooltips retain exact values. */
public final class SingleblockUiText {
    private static final String[] UNITS = {"", "k", "M", "G", "T", "P", "E"};
    private SingleblockUiText() { }

    public static String number(long value) {
        if (value < 1000) return Long.toString(value);
        long divisor = 1;
        int unit = 0;
        while (unit < UNITS.length - 1 && value / divisor >= 1000) {
            divisor *= 1000;
            unit++;
        }
        long whole = value / divisor;
        long fraction = (value % divisor) / (divisor / 100);
        String decimals = fraction == 0 ? "" : fraction % 10 == 0 ? "." + fraction / 10
                : "." + (fraction < 10 ? "0" : "") + fraction;
        return whole + decimals + UNITS[unit];
    }

    public static String label(String text) {
        if (text == null) return "";
        return text.length() <= 32 ? text : text.substring(0, 29) + "...";
    }
}
