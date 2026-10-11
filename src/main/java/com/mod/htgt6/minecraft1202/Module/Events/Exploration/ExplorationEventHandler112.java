package com.mod.htgt6.minecraft1202.Module.Events.Exploration;

import com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketTotemActivation112;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionGenerator112;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

/** Server-side 1.12 item behavior shared by the Exploration module. */
public final class ExplorationEventHandler112 {
    @SubscribeEvent
    public void onChunkLoad(net.minecraftforge.event.world.ChunkEvent.Load event) {
        if (event.world instanceof net.minecraft.world.World)
            WoodlandMansionGenerator112.catchUpLoadedChunk((net.minecraft.world.World) event.world, event.getChunk());
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent event) {
        if (event.entityPlayer.worldObj.isRemote || event.drops == null) return;
        java.util.Iterator<net.minecraft.entity.item.EntityItem> iterator = event.drops.iterator();
        while (iterator.hasNext()) {
            net.minecraft.entity.item.EntityItem drop = iterator.next();
            if (drop != null && ExplorationHooks112.hasVanishing(drop.getEntityItem())) {
                drop.setDead();
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.entityLiving.worldObj.isRemote || bypassesTotem(event.source)) return;
        net.minecraft.entity.EntityLivingBase living = event.entityLiving;
        EntityPlayer player = living instanceof EntityPlayer ? (EntityPlayer)living : null;
        ItemStack main = living.getHeldItem();
        ItemStack off = player == null ? null : OffhandHooks.getOffhand(player);
        boolean mainHand = main != null && main.getItem() == ExplorationContent112.totemOfUndying;
        boolean offHand = off != null && off.getItem() == ExplorationContent112.totemOfUndying;
        if (!mainHand && !offHand) return;

        event.setCanceled(true);
        ItemStack consumed = mainHand ? main : off;
        if (--consumed.stackSize <= 0) {
            if (mainHand) {
                if(player!=null)player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                else living.setCurrentItemOrArmor(0,null);
            }
            else OffhandHooks.setOffhand(player, null);
        }
        living.setHealth(1.0F);
        if(player!=null)com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(player,"totem_of_undying");
        living.clearActivePotions();
        living.addPotionEffect(new PotionEffect(Potion.regeneration.id, 900, 1));
        living.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 100, 1));
        living.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 800, 0));
        living.worldObj.playSoundAtEntity(living, "random.levelup", 1.0F, 1.0F);
        if(player!=null)player.inventory.markDirty();
        if (player instanceof net.minecraft.entity.player.EntityPlayerMP)
            com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationUpdate112.NETWORK.sendTo(new PacketTotemActivation112(),
                    (net.minecraft.entity.player.EntityPlayerMP)player);
    }

    public static boolean bypassesTotem(net.minecraft.util.DamageSource source) {
        // 1.20 bypasses_invulnerability tag, translated to the available legacy sources.
        return source == net.minecraft.util.DamageSource.outOfWorld
                || source != null && ("outOfWorld".equals(source.getDamageType())
                || "out_of_world".equals(source.getDamageType()) || "genericKill".equals(source.getDamageType())
                || "generic_kill".equals(source.getDamageType()));
    }
}
