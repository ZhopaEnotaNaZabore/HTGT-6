package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.AquaticChestLoot120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.Random;

/** 1.20.2 suspicious stew effect payload used by shipwreck supply loot. */
public final class ItemSuspiciousStew120 extends ItemSoup {
    private static final int SATURATION_EFFECT_ID_120 = 23;
    private static final int[] EFFECTS = {
            Potion.nightVision.id, Potion.jump.id, Potion.weakness.id,
            Potion.blindness.id, Potion.poison.id, SATURATION_EFFECT_ID_120
    };
    private static final int[] MIN_SECONDS = {7, 7, 6, 5, 10, 7};
    private static final int[] MAX_SECONDS = {10, 10, 8, 7, 20, 10};

    public ItemSuspiciousStew120() {
        super(6);
        setUnlocalizedName("suspicious_stew");
        setTextureName("htgt6:1_20_backport/suspicious_stew");
    }

    public static ItemStack create(Random random) {
        int effect = random.nextInt(EFFECTS.length);
        ItemStack stack = new ItemStack(OceanContent.suspiciousStew);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("EffectId", EFFECTS[effect]);
        int duration=AquaticChestLoot120.uniform(random,MIN_SECONDS[effect],MAX_SECONDS[effect]);
        tag.setInteger("EffectDuration",duration*(EFFECTS[effect]==SATURATION_EFFECT_ID_120?1:20));
        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote || !stack.hasTagCompound()) return;
        NBTTagCompound tag = stack.getTagCompound();
        int id = tag.getInteger("EffectId");
        int duration = tag.getInteger("EffectDuration");
        // Saturation (PotionHealth #23) also ticks for the complete stew duration.
        if (id > 0 && id < Potion.potionTypes.length && Potion.potionTypes[id] != null && duration > 0) {
            player.addPotionEffect(new PotionEffect(id, duration, 0));
        }
    }
}
