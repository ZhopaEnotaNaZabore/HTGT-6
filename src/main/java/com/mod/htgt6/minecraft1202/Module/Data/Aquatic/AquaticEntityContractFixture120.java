package com.mod.htgt6.minecraft1202.Module.Data.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockCoralBlock120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockTurtleEgg120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntitySeaTurtle;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockMarinePlant;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockTurtleEgg120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemTrident;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemTurtleHelmet120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemAquaticFishBucket120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.RiptideAttackHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenCoralFeature120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.ZombieDrowningHandler;

import com.google.common.collect.Multimap;
import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnHusk;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.IShearable;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Headless regression contract for the 1.20.2 Aquatic entity/item adapters. */
public final class AquaticEntityContractFixture120 {
    public static void assertContract() {
        require(OceanContent.trident.getMaxDamage() == 250, "trident durability");
        require(ItemTrident.THROW_THRESHOLD_TICKS == 10, "trident throw threshold");
        require(EntityThrownTrident.BASE_DAMAGE == 8.0F, "thrown trident base damage");
        require(EntityThrownTrident.IMPALING_DAMAGE_PER_LEVEL == 2.5F, "Impaling damage");
        require(EntityThrownTrident.GRAVITY == 0.05F, "trident gravity");
        require(EntityThrownTrident.WATER_INERTIA == 0.99D, "trident water inertia");
        require(EntityDrowned.TRIDENT_EQUIPMENT_CHANCE == 0.0625F, "drowned trident equipment chance");
        require(EntityDrowned.TRIDENT_DROP_CHANCE == 0.085F, "drowned trident drop chance");
        require(EntityDrowned.OCEAN_SPAWN_ROLL == 40, "ocean drowned spawn roll");
        require(EntityDrowned.RIVER_SPAWN_ROLL == 15, "river drowned spawn roll");
        require(EntityDrowned.MINIMUM_DEPTH_BELOW_SEA == 5, "drowned ocean spawn depth");
        require(EntityDrowned.TRIDENT_CHARGE_TICKS == 10, "drowned trident charge duration");
        require(ZombieDrowningHandler.CONVERSION_TICKS == 900, "zombie conversion duration");
        require(EntityFrostburnHusk.class.getSuperclass() == net.minecraft.entity.monster.EntityZombie.class,
                "Husk -> Zombie -> Drowned conversion chain");
        require(RiptideAttackHandler.AUTO_SPIN_TICKS == 20
                        && RiptideAttackHandler.AUTO_SPIN_DAMAGE == 8.0F,
                "Riptide auto-spin attack");
        try {
            require(RiptideAttackHandler.class.getMethod("onLivingUpdate",
                    net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent.class)
                    .isAnnotationPresent(SubscribeEvent.class), "Riptide event registration contract");
        } catch (NoSuchMethodException error) {
            throw new IllegalStateException("Riptide event registration contract", error);
        }
        require(EntitySeaTurtle.MOVEMENT_SPEED == 0.25D, "turtle movement speed");
        require(OceanContent.turtleEgg instanceof BlockTurtleEgg120,
                "turtle egg registration");
        require(BlockTurtleEgg120.eggCount(BlockTurtleEgg120.metadata(4, 2)) == 4
                        && BlockTurtleEgg120.hatchStage(BlockTurtleEgg120.metadata(4, 2)) == 2,
                "turtle egg count/hatch metadata");
        require(new ItemBlockTurtleEgg120(OceanContent.turtleEgg).getItemStackLimit() == 64,
                "stackable turtle egg item adapter");
        require(OceanContent.scute.getUnlocalizedName().endsWith("scute"), "scute registration");
        require(OceanContent.turtleHelmet.getUnlocalizedName().endsWith("turtle_helmet"),
                "turtle helmet registration");
        require(OceanContent.turtleHelmet.getMaxDamage()==ItemTurtleHelmet120.DURABILITY,
                "turtle helmet durability");
        require(OceanContent.turtleHelmet.damageReduceAmount==ItemTurtleHelmet120.DEFENCE,
                "turtle helmet defence");
        require(OceanContent.turtleHelmet.getItemEnchantability()==ItemTurtleHelmet120.ENCHANTABILITY,
                "turtle helmet enchantability");
        require(OceanContent.turtleHelmet.getIsRepairable(new ItemStack(OceanContent.turtleHelmet),
                new ItemStack(OceanContent.scute)),"turtle helmet scute repair ingredient");
        require(OceanContent.TURTLE_HELMET_SCUTE_COUNT==5,"turtle helmet recipe scute count");
        require(ItemTurtleHelmet120.shouldRefreshWaterBreathing(true,false)
                        &&!ItemTurtleHelmet120.shouldRefreshWaterBreathing(true,true)
                        &&ItemTurtleHelmet120.WATER_BREATHING_TICKS==200,
                "turtle helmet ten-second Water Breathing window");

        require(OceanContent.loyalty.getMaxLevel() == 3, "Loyalty levels");
        require(OceanContent.impaling.getMaxLevel() == 5, "Impaling levels");
        require(OceanContent.riptide.getMaxLevel() == 3, "Riptide levels");
        require(OceanContent.channeling.getMaxLevel() == 1, "Channeling levels");
        require(OceanContent.riptide.getMinEnchantability(1) == 17
                        && OceanContent.riptide.getMinEnchantability(2) == 24
                        && OceanContent.riptide.getMinEnchantability(3) == 31,
                "Riptide minimum enchantability");
        Set<Integer> enchantmentIds = new HashSet<Integer>();
        enchantmentIds.add(HTGT6Config.LOYALTY_ENCHANTMENT_ID);
        enchantmentIds.add(HTGT6Config.IMPALING_ENCHANTMENT_ID);
        enchantmentIds.add(HTGT6Config.RIPTIDE_ENCHANTMENT_ID);
        enchantmentIds.add(HTGT6Config.CHANNELING_ENCHANTMENT_ID);
        require(enchantmentIds.size() == 4, "stable aquatic enchantment IDs");
        require(!OceanContent.riptide.canApplyTogether(OceanContent.loyalty),
                "Riptide/Loyalty incompatibility");
        require(!OceanContent.riptide.canApplyTogether(OceanContent.channeling),
                "Riptide/Channeling incompatibility");
        require(OceanContent.impaling.canApplyTogether(OceanContent.loyalty),
                "Impaling/Loyalty compatibility");

        Multimap modifiers = OceanContent.trident.getItemAttributeModifiers();
        Collection values = modifiers.get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
        require(values.size() == 1, "trident attack modifier count");
        AttributeModifier modifier = (AttributeModifier) values.iterator().next();
        require(modifier.getAmount() == ItemTrident.ATTACK_DAMAGE_MODIFIER,
                "trident melee damage modifier");
        require(OceanContent.marinePlant instanceof IShearable, "seagrass shearing integration");
        require(ItemAquaticFishBucket120.clampSpecies(-1)==0&&ItemAquaticFishBucket120.clampSpecies(9)==3,
                "fish bucket metadata bounds");
        require(ItemAquaticFishBucket120.entityClassForSpecies(0)==com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.Cod.class
                        &&ItemAquaticFishBucket120.entityClassForSpecies(1)==com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.Salmon.class
                        &&ItemAquaticFishBucket120.entityClassForSpecies(2)==com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.Pufferfish.class
                        &&ItemAquaticFishBucket120.entityClassForSpecies(3)==com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.Tropical.class,
                "fish bucket entity mapping");
        require(OceanContent.coralBlock instanceof BlockCoralBlock120,
                "1.20.2 coral block registration");
        require(OceanContent.wallCoralFans.length == 5,
                "five directional 1.20.2 wall coral fan registrations");
        for (int type = 0; type < OceanContent.wallCoralFans.length; type++) {
            require(OceanContent.wallCoralFans[type].getCoralType() == type,
                    "wall coral fan type mapping");
        }
        require(BlockCoralBlock120.isLive(OceanContent.coralBlock, 0)
                        && BlockCoralBlock120.isLive(OceanContent.coralBlock, 4)
                        && !BlockCoralBlock120.isLive(OceanContent.coralBlock, 8),
                "live/dead coral metadata contract");
        require(WorldGenCoralFeature120.TREE == 0
                        && WorldGenCoralFeature120.CLAW == 1
                        && WorldGenCoralFeature120.MUSHROOM == 2,
                "coral feature selector contract");

        ItemBlockMarinePlant item = new ItemBlockMarinePlant(OceanContent.marinePlant);
        require(item.getMetadata(-1) == 0 && item.getMetadata(7) == 7 && item.getMetadata(99) == 8,
                "marine plant item normalization (one pickle per placement)");
    }

    private static void require(boolean condition, String name) {
        if (!condition) throw new IllegalStateException(name + " differs from the 1.20.2 contract");
    }

    public static void main(String[] ignored) {
        assertContract();
        System.out.println("Aquatic entities/items 1.20.2 contract OK");
    }

    private AquaticEntityContractFixture120() { }
}
