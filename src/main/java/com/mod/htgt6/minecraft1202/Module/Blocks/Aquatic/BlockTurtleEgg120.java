package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntitySeaTurtle;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

/** Turtle egg stages/counts and hatching lifecycle adapted from 1.20.2. */
public final class BlockTurtleEgg120 extends Block {
    static final int MAX_HATCH_STAGE = 2;
    private final IIcon[] icons = new IIcon[3];

    public BlockTurtleEgg120() {
        super(Material.dragonEgg);
        setBlockName("turtle_egg");
        setHardness(0.5F);
        setResistance(Backport120Content.legacyResistance(0.5F));
        setStepSound(soundTypePiston);
        setTickRandomly(true);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
        setBlockBounds(3.0F / 16.0F, 0.0F, 3.0F / 16.0F,
                12.0F / 16.0F, 7.0F / 16.0F, 12.0F / 16.0F);
    }

    public static int metadata(int eggs, int hatch) {
        return Math.max(0, Math.min(2, hatch)) * 4 + Math.max(0, Math.min(3, eggs - 1));
    }

    public static int eggCount(int metadata) { return (metadata & 3) + 1; }
    public static int hatchStage(int metadata) { return Math.min(MAX_HATCH_STAGE, metadata >> 2); }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        String root = HTGT6.MOD_ID.toLowerCase() + ":";
        icons[0] = register.registerIcon(root + "turtle_egg");
        icons[1] = register.registerIcon(root + "turtle_egg_slightly_cracked");
        icons[2] = register.registerIcon(root + "turtle_egg_very_cracked");
        blockIcon = icons[0];
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) { return icons[hatchStage(metadata)]; }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return OceanContent.getTurtleEggRenderId(); }
    @Override public int quantityDropped(Random random) { return 0; }

    @Override
    public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        if (eggCount(world.getBlockMetadata(x, y, z)) > 1) {
            setBlockBounds(1.0F / 16.0F, 0.0F, 1.0F / 16.0F,
                    15.0F / 16.0F, 7.0F / 16.0F, 15.0F / 16.0F);
        } else {
            setBlockBounds(3.0F / 16.0F, 0.0F, 3.0F / 16.0F,
                    12.0F / 16.0F, 7.0F / 16.0F, 12.0F / 16.0F);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && world.getBlock(x, y - 1, z) == Blocks.sand;
    }

    @Override
    public int tickRate(World world) { return 20; }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote || world.getBlock(x, y - 1, z) != Blocks.sand || !isHatchingTime(world)) return;
        int meta = world.getBlockMetadata(x, y, z);
        int hatch = hatchStage(meta);
        if (hatch < MAX_HATCH_STAGE) {
            world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.7F, 0.9F + random.nextFloat() * 0.2F);
            world.setBlockMetadataWithNotify(x, y, z, metadata(eggCount(meta), hatch + 1), 2);
            return;
        }
        int eggs = eggCount(meta);
        world.setBlockToAir(x, y, z);
        for (int index = 0; index < eggs; index++) {
            EntitySeaTurtle turtle = new EntitySeaTurtle(world);
            turtle.setGrowingAge(-24000);
            turtle.setHome(x, y, z);
            turtle.setLocationAndAngles(x + 0.3D + index * 0.2D, y, z + 0.3D, 0.0F, 0.0F);
            world.spawnEntityInWorld(turtle);
        }
    }

    @Override
    public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
        if (canDestroyEgg(world, entity) && world.rand.nextInt(100) == 0) {
            breakOneEgg(world, x, y, z);
        }
        super.onEntityWalking(world, x, y, z, entity);
    }

    @Override
    public void onFallenUpon(World world, int x, int y, int z, Entity entity, float distance) {
        if (canDestroyEgg(world, entity) && world.rand.nextInt(3) == 0) {
            breakOneEgg(world, x, y, z);
        }
        super.onFallenUpon(world, x, y, z, entity, distance);
    }

    private void breakOneEgg(World world, int x, int y, int z) {
        if (world.isRemote || world.getBlock(x, y, z) != this) return;
        int meta = world.getBlockMetadata(x, y, z), eggs = eggCount(meta);
        world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(this));
        if (eggs <= 1) world.setBlockToAir(x, y, z);
        else world.setBlockMetadataWithNotify(x, y, z, metadata(eggs - 1, hatchStage(meta)), 2);
    }

    private static boolean canDestroyEgg(World world, Entity entity) {
        if (entity instanceof EntitySeaTurtle || entity instanceof EntityBat
                || !(entity instanceof EntityLivingBase)) return false;
        return entity instanceof EntityPlayer
                || world.getGameRules().getGameRuleBooleanValue("mobGriefing");
    }

    private static boolean isHatchingTime(World world) {
        float day = world.getCelestialAngle(1.0F);
        return day > 0.65F && day < 0.69F || world.rand.nextInt(500) == 0;
    }
}
