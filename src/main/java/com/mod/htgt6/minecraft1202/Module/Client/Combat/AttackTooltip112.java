package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.AttackCooldown112;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/** Adds the Minecraft 1.12 main-hand combat attributes to vanilla tool tooltips. */
public final class AttackTooltip112 {
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!AttackCooldown112.isVanillaTool(event.itemStack)) return;
        List<String> tooltip = event.toolTip;
        String legacyDamageName = StatCollector.translateToLocal("attribute.name.generic.attackDamage");
        int insert = -1;
        for (int i = 0; i < tooltip.size(); ++i) {
            String line = EnumChatFormatting.getTextWithoutFormattingCodes(tooltip.get(i));
            if (line != null && line.contains(legacyDamageName)) {
                insert = i;
                tooltip.remove(i);
                break;
            }
        }
        if (insert < 0) {
            tooltip.add("");
            tooltip.add(EnumChatFormatting.GRAY
                    + StatCollector.translateToLocal("combat.tooltip.whenInMainHand"));
            insert = tooltip.size();
        }
        tooltip.add(insert++, attribute(AttackCooldown112.getAttackSpeed(event.itemStack),
                "combat.tooltip.attackSpeed"));
        tooltip.add(insert, attribute(AttackCooldown112.getVanillaAttackDamage(event.itemStack),
                "combat.tooltip.attackDamage"));
    }

    private static String attribute(float value, String key) {
        return EnumChatFormatting.BLUE + " " + number(value) + " " + StatCollector.translateToLocal(key);
    }

    private static String number(float value) {
        int integer = (int) value;
        return value == integer ? Integer.toString(integer) : Float.toString(value);
    }
}
