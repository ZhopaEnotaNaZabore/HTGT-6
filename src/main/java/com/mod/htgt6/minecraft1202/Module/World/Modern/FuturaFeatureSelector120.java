package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Random;

/** Selection is separate from placement: a selected child failing does not retry the default. */
public final class FuturaFeatureSelector120 {
    private FuturaFeatureSelector120() {}

    public static JsonElement select(String type, JsonObject config, Random random) {
        if (type.equals("minecraft:random_selector")) {
            for (JsonElement entry : config.getAsJsonArray("features")) {
                JsonObject weighted = entry.getAsJsonObject();
                if (random.nextFloat() < weighted.get("chance").getAsFloat()) return weighted.get("feature");
            }
            return config.get("default");
        }
        if (type.equals("minecraft:simple_random_selector")) {
            JsonArray choices = config.getAsJsonArray("features");
            return choices.get(random.nextInt(choices.size()));
        }
        if (type.equals("minecraft:random_boolean_selector"))
            return config.get(random.nextBoolean() ? "feature_true" : "feature_false");
        throw new IllegalArgumentException("Not a feature selector: " + type);
    }
}
