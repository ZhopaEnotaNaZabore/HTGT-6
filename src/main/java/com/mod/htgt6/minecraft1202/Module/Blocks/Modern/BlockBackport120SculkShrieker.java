package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120SculkHandler;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SculkWarningManager120;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.Random;

/** Bit 0 preserves the world-generated can-summon state; bit 1 is the active warning period. */
public final class BlockBackport120SculkShrieker extends BlockBackport120Sculk {
    static final int CAN_SUMMON = 1, SHRIEKING = 2;
    public BlockBackport120SculkShrieker() {
        super("sculk_shrieker", 3.0F, 5);
        setBlockBounds(0, 0, 0, 1, 0.9375F, 1);
    }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean hasTileEntity(int metadata){return true;}
    @Override public TileEntity createTileEntity(World world,int metadata){return new TileEntityBackport120SculkShrieker();}
    @Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if(world.isRemote||!(entity instanceof EntityPlayer)||entity.isSneaking())return;
        Backport120SculkHandler.emit(world,entity.posX,entity.posY,entity.posZ,1,entity,false);
    }
    public boolean receiveVibration(World world,int x,int y,int z,int distance,Entity source){
        TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityBackport120SculkShrieker
                &&((TileEntityBackport120SculkShrieker)tile).receive(distance,source);
    }
    public void shriek(World world,int x,int y,int z,EntityPlayer player){
        if(world.isRemote)return;
        int state = world.getBlockMetadata(x, y, z);
        if ((state & SHRIEKING) != 0) return;
        int warning=0;
        if((state&CAN_SUMMON)!=0&&SculkWarningManager120.canSummon(world)){
            warning=SculkWarningManager120.warn(player,x,y,z);
            if(warning==0)return;
        }
        TileEntity tile=world.getTileEntity(x,y,z);
        if(tile instanceof TileEntityBackport120SculkShrieker)((TileEntityBackport120SculkShrieker)tile).setWarningLevel(warning);
        world.setBlockMetadataWithNotify(x,y,z,state|SHRIEKING,3);
        world.playSoundEffect(x+.5D,y+.5D,z+.5D,"htgt6:block.sculk_shrieker.shriek",2F,1F);
        world.scheduleBlockUpdate(x, y, z, this, 90);
    }
    @Override public int damageDropped(int metadata){return 0;}
    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        if(world.isRemote||(world.getBlockMetadata(x,y,z)&SHRIEKING)==0)return;
        int state=world.getBlockMetadata(x,y,z);
        world.setBlockMetadataWithNotify(x,y,z,state&CAN_SUMMON,3);
        TileEntity tile=world.getTileEntity(x,y,z);
        int warning=tile instanceof TileEntityBackport120SculkShrieker?((TileEntityBackport120SculkShrieker)tile).takeWarningLevel():0;
        if((state&CAN_SUMMON)==0||warning==0||!SculkWarningManager120.canSummon(world))return;
        boolean spawned=warning>=4&&SculkWarningManager120.spawnWarden(world,x,y,z);
        if(!spawned)world.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:entity.warden.listening",5F,1F);
        @SuppressWarnings("unchecked") java.util.List<EntityPlayer> nearby=world.getEntitiesWithinAABB(EntityPlayer.class,
                AxisAlignedBB.getBoundingBox(x-40,y-40,z-40,x+41,y+41,z+41));
        for(EntityPlayer target:nearby)if(!target.capabilities.disableDamage&&target.getDistanceSq(x+.5,y+.5,z+.5)<1600)
            target.addPotionEffect(new PotionEffect(Backport120Content.darkness.id,260,0));
    }
}
