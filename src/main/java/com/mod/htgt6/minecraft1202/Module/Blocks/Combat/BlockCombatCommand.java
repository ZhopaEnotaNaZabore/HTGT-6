package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Directional repeating/chain command block compatible with the 1.7 command GUI and executor. */
public final class BlockCombatCommand extends BlockCommandBlock {
    public static final int CHAIN = 0, REPEATING = 1;
    private final int mode;
    @SideOnly(Side.CLIENT) private IIcon front, back, side, conditional;

    public BlockCombatCommand(int mode) {
        this.mode = mode;
        String name = mode == CHAIN ? "chainCommandBlock" : "repeatingCommandBlock";
        setBlockName(name); setHardness(-1.0F); setResistance(6000000.0F);
        setCreativeTab(CreativeTabs.tabRedstone);
    }
    public int getMode() { return mode; }
    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        TileEntityCombatCommandBlock tile = new TileEntityCombatCommandBlock();
        tile.setAutomatic(mode == CHAIN);
        return tile;
    }
    @Override public int tickRate(World world) { return 1; }
    @Override public int damageDropped(int metadata) { return metadata & 8; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return Item.getItemFromBlock(this); }
    @Override public int quantityDropped(Random random) { return 1; }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, placer, stack);
        int facing = BlockPistonBase.determineOrientation(world, x, y, z, placer);
        world.setBlockMetadataWithNotify(x, y, z, facing | (stack.getItemDamage() & 8), 2);
        TileEntityCombatCommandBlock tile = tile(world, x, y, z);
        if (tile != null) tile.setAutomatic(mode == CHAIN);
        if (mode == REPEATING) world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        TileEntityCombatCommandBlock tile = tile(world, x, y, z);
        if (tile == null) return;
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        if (powered && !tile.isPowered() && mode == REPEATING){
            tile.captureCondition();
            world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
        }
        tile.setPowered(powered);
    }
    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote || mode != REPEATING) return;
        TileEntityCombatCommandBlock tile = tile(world, x, y, z);
        if (tile == null || (!tile.isPowered() && !tile.isAutomatic())) return;
        int metadata=world.getBlockMetadata(x,y,z);
        boolean condition=tile.isConditionMet();tile.captureCondition();
        if((metadata&8)==0||condition){
            if(execute(tile, world))executeChain(world, x, y, z, facing(metadata));
        }else tile.clearSuccessCount();
        world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
        world.func_147453_f(x,y,z,this);
    }
    public static boolean conditionMet(World world,int x,int y,int z){int metadata=world.getBlockMetadata(x,y,z);return (metadata&8)==0||predecessorSucceeded(world,x,y,z,facing(metadata));}
    private static boolean execute(TileEntityCombatCommandBlock tile, World world) {
        if(!tile.claimExecution(world.getTotalWorldTime()))return false;
        if(tile.func_145993_a().func_145753_i().trim().isEmpty())tile.clearSuccessCount();
        else tile.func_145993_a().func_145755_a(world);
        tile.markDirty();
        world.markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
        return true;
    }
    public static int facing(int metadata){int direction=metadata&7;return direction<6?direction:0;}
    public static int chainLimit(net.minecraft.world.GameRules rules){
        if(!rules.hasRule("maxCommandChainLength"))return 65536;
        try{return Math.max(0,Integer.parseInt(rules.getGameRuleStringValue("maxCommandChainLength")));}
        catch(NumberFormatException invalid){return 0;}
    }
    private static void executeChain(World world, int x, int y, int z, int direction) {
        for (int count = 0,limit=chainLimit(world.getGameRules()); count < limit; ++count) {
            x += Facing.offsetsXForSide[direction]; y += Facing.offsetsYForSide[direction];
            z += Facing.offsetsZForSide[direction];
            Block block = world.getBlock(x, y, z);
            if (!(block instanceof BlockCombatCommand) || ((BlockCombatCommand) block).mode != CHAIN) return;
            TileEntityCombatCommandBlock tile = tile(world, x, y, z);
            if (tile == null) return;
            int metadata = world.getBlockMetadata(x, y, z);
            if(tile.isAutomatic()||tile.isPowered()){
                if ((metadata & 8) == 0 || predecessorSucceeded(world, x, y, z, facing(metadata))){
                    if(!execute(tile, world))return;
                }else tile.clearSuccessCount();
                world.func_147453_f(x,y,z,block);
            }
            direction = facing(metadata);
        }
    }
    private static boolean predecessorSucceeded(World world, int x, int y, int z, int direction) {
        TileEntity previous = world.getTileEntity(x - Facing.offsetsXForSide[direction],
                y - Facing.offsetsYForSide[direction], z - Facing.offsetsZForSide[direction]);
        return previous instanceof net.minecraft.tileentity.TileEntityCommandBlock
                && ((net.minecraft.tileentity.TileEntityCommandBlock) previous).func_145993_a().func_145760_g() > 0;
    }
    private static TileEntityCombatCommandBlock tile(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityCombatCommandBlock ? (TileEntityCombatCommandBlock) tile : null;
    }

    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register) {
        String prefix = mode == CHAIN ? "chain" : "repeating";
        front = register.registerIcon("htgt6:combat_update/" + prefix + "_command_block_front");
        back = register.registerIcon("htgt6:combat_update/" + prefix + "_command_block_back");
        side = register.registerIcon("htgt6:combat_update/" + prefix + "_command_block_side");
        conditional = register.registerIcon("htgt6:combat_update/" + prefix + "_command_block_conditional");
        blockIcon = side;
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int blockSide, int metadata) {
        int facing = facing(metadata);
        if (blockSide == facing) return front;
        if (blockSide == Facing.oppositeSide[facing]) return back;
        return (metadata & 8) != 0 ? conditional : side;
    }
}
