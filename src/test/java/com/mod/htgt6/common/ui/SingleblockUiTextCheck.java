package com.mod.htgt6.common.ui;

/** Run without Minecraft to guard long-tier overflow and terminal width. */
public final class SingleblockUiTextCheck {
    public static void main(String[] args) {
        check("0", SingleblockUiText.number(0));
        check("999", SingleblockUiText.number(999));
        check("1k", SingleblockUiText.number(1000));
        check("1.01k", SingleblockUiText.number(1010));
        check("32.76k", SingleblockUiText.number(32768));
        check("1.1E", SingleblockUiText.number(1100000000000000000L));
        check("9.22E", SingleblockUiText.number(Long.MAX_VALUE));
        if (SingleblockUiText.label("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").length() != 32)
            throw new AssertionError("Unbounded label");
        System.out.println("SingleblockUiTextCheck: OK");
    }
    private static void check(String expected, String actual) {
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
}
