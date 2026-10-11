package com.mod.htgt6.minecraft1202.Module.Events.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.AttackCooldown112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ElytraFlight;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldMechanics120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatRules120;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketSweepAttackEffect;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatUpdate;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MathHelper;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.world.BlockEvent;

/** Adaptations of the 1.9 combat, Shield, Elytra, Frost Walker and Mending mechanics. */
public final class CombatEventHandler {
    @SubscribeEvent
    public void onArrowLoose(ArrowLooseEvent event) {
        int slot = findSpecialArrow(event.entityPlayer);
        if (slot < 0) return;
        event.setCanceled(true);
        float power = event.charge / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        if (power < 0.1F) return;
        power = Math.min(1.0F, power);
        EntityPlayer player = event.entityPlayer;
        if (!player.worldObj.isRemote) {
            ItemStack ammunition = player.inventory.getStackInSlot(slot);
            int type = ammunition.getItem() == CombatContent.spectralArrow
                    ? EntityCombatArrow.TYPE_SPECTRAL : EntityCombatArrow.TYPE_TIPPED;
            EntityCombatArrow arrow = new EntityCombatArrow(player.worldObj, player, power * 2.0F,
                    type, ammunition.getItemDamage());
            arrow.setPotionTag(ammunition.getTagCompound());
            if (power >= 1.0F) arrow.setIsCritical(true);
            int powerLevel = EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantment.power.effectId, event.bow);
            if (powerLevel > 0) arrow.setDamage(arrow.getDamage() + powerLevel * 0.5D + 0.5D);
            int punch = EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantment.punch.effectId, event.bow);
            if (punch > 0) arrow.setKnockbackStrength(punch);
            if (EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantment.flame.effectId, event.bow) > 0) arrow.setFire(100);
            arrow.canBePickedUp = player.capabilities.isCreativeMode ? 2 : 1;
            player.worldObj.spawnEntityInWorld(arrow);
            if (!player.capabilities.isCreativeMode && --ammunition.stackSize <= 0) player.inventory.setInventorySlotContents(slot, null);
            event.bow.damageItem(1, player);
        }
        player.worldObj.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (player.getRNG().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }

    private int findSpecialArrow(EntityPlayer player) {
        for (int slot = 0; slot < player.inventory.mainInventory.length; ++slot) {
            ItemStack stack = player.inventory.mainInventory[slot];
            if (stack != null && (stack.getItem() == CombatContent.tippedArrow || stack.getItem() == CombatContent.spectralArrow)) return slot;
        }
        return -1;
    }

    @SubscribeEvent(priority = cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST)
    public void onRightClickAir(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;
        EntityPlayer player = event.entityPlayer;
        ItemStack held = player.getHeldItem();

        // ItemEnderPearl in 1.7.10 returns immediately in Creative. Later versions permit the
        // throw without consuming the stack, so handle that legacy special case explicitly.
        if (held != null && held.getItem() == Items.ender_pearl && player.capabilities.isCreativeMode) {
            event.setCanceled(true);
            if (!event.world.isRemote) {
                event.world.playSoundAtEntity(player, "random.bow", 0.5F,
                        0.4F / (player.getRNG().nextFloat() * 0.4F + 0.8F));
                event.world.spawnEntityInWorld(new EntityEnderPearl(event.world, player));
            }
            return;
        }

        ItemStack rocket = held != null && held.getItem() == Items.fireworks
                ? held : OffhandHooks.getOffhand(player);
        if (rocket == null || rocket.getItem() != Items.fireworks || !ElytraFlight.boost(player, rocket)) return;
        event.setCanceled(true);
        if (!event.world.isRemote) {
            event.world.playSoundAtEntity(player, "fireworks.launch", 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode && --rocket.stackSize <= 0) {
                if (rocket == held) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                else OffhandHooks.setOffhand(player, null);
            }
            if (player instanceof net.minecraft.entity.player.EntityPlayerMP)
                OffhandSystem.syncInventory((net.minecraft.entity.player.EntityPlayerMP) player);
        }
    }

    @SubscribeEvent(priority = cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        blockWithShield(event);
    }

    private boolean blockWithShield(LivingAttackEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) event.entityLiving;
        if (player.worldObj.isRemote || event.source.isUnblockable()) return false;
        ItemStack active = activeShield(player);
        if (active == null) return false;
        net.minecraft.entity.Entity impact = event.source.getSourceOfDamage();
        if (impact == null) impact = event.source.getEntity();
        if (impact instanceof EntityCombatArrow && ((EntityCombatArrow)impact).getPierceLevel() > 0) return false;
        if (impact != null) {
            double dx = impact.posX - player.posX;
            double dz = impact.posZ - player.posZ;
            if (!ShieldMechanics120.isInFront(dx, dz, player.rotationYaw)) return false;
        }
        float blocked = event.ammount;
        // Cancel before LivingHurt: attackEntityFrom returns false. Vanilla
        // arrows use that result to reverse their velocity instead of entering
        // the player, matching post-1.9 shield ricochet behaviour.
        event.setCanceled(true);
        if(event.source.isProjectile())com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(player,"deflect_arrow");
        int durabilityDamage = ShieldMechanics120.durabilityDamage(blocked);
        if (durabilityDamage > 0) active.damageItem(durabilityDamage, player);
        if (active.stackSize <= 0) {
            if (active == OffhandHooks.getOffhand(player)) OffhandHooks.setOffhand(player, null);
            else player.destroyCurrentEquippedItem();
            player.clearItemInUse();
            OffhandSystem.stopBlocking(player);
        }
        if (player instanceof net.minecraft.entity.player.EntityPlayerMP)
            OffhandSystem.syncInventory((net.minecraft.entity.player.EntityPlayerMP) player);
        if (impact instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) impact;
            if (attacker instanceof com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager)
                ((com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager)attacker).onBlockedByShield(player);
            else if(attacker instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoglinCombat116.Animated){
                if(!attacker.isChild())com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoglinCombat116.throwTarget(attacker,player);
            }else knockBackShieldAttacker(player, attacker);
            ItemStack weapon = attacker.getHeldItem();
            if(attacker instanceof com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120)OffhandSystem.disableShield(player);
            else if (weapon != null && weapon.getItem() instanceof ItemAxe) {
                int efficiency = EnchantmentHelper.getEnchantmentLevel(
                        net.minecraft.enchantment.Enchantment.efficiency.effectId, weapon);
                // Player.blockUsingShield calls disableShield(true) for AxeItem attackers.
                // The sprinting term makes this at least 1.0, as in 1.20.2.
                if (player.getRNG().nextFloat() < ShieldMechanics120.disableChance(efficiency, true))
                    OffhandSystem.disableShield(player);
            }
        }
        player.worldObj.playSoundAtEntity(player, "random.anvil_land", 0.25F, 1.8F);
        return true;
    }

    private static ItemStack activeShield(EntityPlayer player) {
        if (OffhandSystem.isShieldDisabled(player)) return null;
        ItemStack used = com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PlayerItemUse120.item(player);
        if (used != null && used.getItem() == CombatContent.shield
                && com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PlayerItemUse120.duration(player)
                    >= ShieldMechanics120.EFFECTIVE_BLOCK_DELAY) return used;
        return OffhandSystem.isShieldBlocking(player)
                && OffhandSystem.getShieldUseTicks(player) >= ShieldMechanics120.EFFECTIVE_BLOCK_DELAY
                ? OffhandHooks.getOffhand(player) : null;
    }

    private static void knockBackShieldAttacker(EntityPlayer player, EntityLivingBase attacker) {
        double dx = attacker.posX - player.posX;
        double dz = attacker.posZ - player.posZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-4D) {
            double yaw = Math.toRadians(player.rotationYaw);
            dx = -Math.sin(yaw);
            dz = Math.cos(yaw);
            length = 1.0D;
        }
        com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatAttack120.knockback(attacker,.5,(float)(Math.atan2(-dx,dz)*180/Math.PI));
        attacker.velocityChanged = true;
    }

    @SubscribeEvent public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatAttack120.tick(event.player);
        ElytraFlight.tick(event.player);
        if(!event.player.worldObj.isRemote)applyFrostWalker(event.player);
    }
    @SubscribeEvent public void onPlayerLogout(PlayerLoggedOutEvent event){ElytraFlight.clear(event.player);}

    private void applyFrostWalker(EntityPlayer player) {
        ItemStack boots = player.inventory.armorInventory[0];
        int level = boots == null ? 0 : EnchantmentHelper.getEnchantmentLevel(CombatContent.frostWalker.effectId, boots);
        if (level <= 0 || !player.onGround) return;
        int radius = Math.min(16, 2 + level);
        int cy = MathHelper.floor_double(player.boundingBox.minY) - 1;
        for (int x = MathHelper.floor_double(player.posX) - radius; x <= player.posX + radius; ++x) {
            for (int z = MathHelper.floor_double(player.posZ) - radius; z <= player.posZ + radius; ++z) {
                double dx=x+.5-player.posX,dy=cy+.5-player.boundingBox.minY,dz=z+.5-player.posZ;
                if (dx*dx+dy*dy+dz*dz>=radius*radius||!player.worldObj.blockExists(x,cy,z))continue;
                net.minecraft.block.Block water=player.worldObj.getBlock(x,cy,z);
                if ((water==Blocks.water||water==Blocks.flowing_water||water==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.aquiferWater)
                        && player.worldObj.getBlockMetadata(x, cy, z) == 0 && player.worldObj.isAirBlock(x, cy + 1, z)
                        && player.worldObj.checkNoEntityCollision(net.minecraft.util.AxisAlignedBB.getBoundingBox(x,cy,z,x+1,cy+1,z+1))
                        && player.worldObj.setBlock(x, cy, z, CombatContent.frostedIce, 0, 3)) {
                    player.worldObj.scheduleBlockUpdate(x, cy, z, CombatContent.frostedIce,
                            60 + player.getRNG().nextInt(61));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPickupXp(PlayerPickupXpEvent event) {
        EntityPlayer player = event.entityPlayer;
        if(player.worldObj.isRemote||event.orb.xpValue<=0)return;
        event.orb.xpValue=com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.MendingRepair120.repair(
                findMendingStacks(player),event.orb.xpValue,player.getRNG());
    }

    private List<ItemStack> findMendingStacks(EntityPlayer player) {
        List<ItemStack> candidates = new ArrayList<ItemStack>();
        ItemStack held = player.getHeldItem();
        if (isDamagedMending(held)) candidates.add(held);
        ItemStack offhand = OffhandHooks.getOffhand(player);
        if (isDamagedMending(offhand)) candidates.add(offhand);
        for (ItemStack armor : player.inventory.armorInventory) {
            if (isDamagedMending(armor)) candidates.add(armor);
        }
        return candidates;
    }

    private static boolean isDamagedMending(ItemStack stack) {
        return stack != null && stack.isItemDamaged()
                && EnchantmentHelper.getEnchantmentLevel(CombatContent.mending.effectId, stack) > 0;
    }
}
