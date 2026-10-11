package com.mod.htgt6.minecraft1202.Module.Data;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BrushingProgress120;

public final class BrushingProgressFixture120 {
    public static void main(String[] args) {
        BrushingProgress120 shared = new BrushingProgress120();
        int[] dusted = {0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3};
        require(shared.dusted() == 0, "untouched texture");
        for (int i = 0; i < 10; i++) {
            require(shared.brush(i * 10), "accepted swing " + i);
            require(!shared.brush(i * 10 + 5), "second player cannot bypass block cooldown");
            require(shared.progress() == i + 1 && shared.dusted() == dusted[i + 1], "progress and texture " + i);
        }
        require(!shared.brush(100), "no second completion");
        BrushingProgress120 decay = new BrushingProgress120();
        for (int i = 0; i < 7; i++) decay.brush(i * 10);
        require(!decay.tick(99) && decay.progress() == 7, "40 tick grace period");
        require(decay.tick(100) && decay.progress() == 5, "first decay step");
        require(!decay.tick(103), "four tick decay interval");
        require(decay.tick(104) && decay.progress() == 3, "second decay step");
        require(decay.brush(105) && decay.progress() == 4, "resume partial excavation");
        require(!decay.tick(144), "resumed grace period");
        require(decay.tick(145) && decay.tick(149) && decay.progress() == 0 && decay.dusted() == 0,
                "decay ends at zero");
        require(decay.brush(150), "new excavation after reset");
        require(!decay.brush(155) && !decay.tick(190) && decay.tick(195), "cooldown swing postpones reset");
        BrushingProgress120 restored = new BrushingProgress120();
        restored.restore(5, 110, 140);
        require(!restored.brush(109) && restored.brush(110), "restored shared cooldown");
        require(!restored.tick(149) && restored.tick(150) && restored.progress() == 4, "restored progress decay");
        System.out.println("BrushingProgressFixture120: OK");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
