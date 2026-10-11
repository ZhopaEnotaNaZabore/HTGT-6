package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.Random;
import cpw.mods.fml.common.eventhandler.*;
import net.minecraftforge.event.entity.player.BonemealEvent;

/** Player-triggered vegetation; never installs a biome or terrain generator. */
public final class NetherVegetation116 {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void pot(net.minecraftforge.event.entity.player.PlayerInteractEvent e){
        if(e.action!=net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK||e.useBlock==Event.Result.DENY||e.useItem==Event.Result.DENY||e.world.getBlock(e.x,e.y,e.z)!=Blocks.flower_pot)return;
        net.minecraft.tileentity.TileEntity te=e.world.getTileEntity(e.x,e.y,e.z);
        if(!(te instanceof net.minecraft.tileentity.TileEntityFlowerPot)||((net.minecraft.tileentity.TileEntityFlowerPot)te).getFlowerPotItem()!=null)return;
        net.minecraft.item.ItemStack held=e.entityPlayer.getHeldItem();if(held==null||!e.entityPlayer.canPlayerEdit(e.x,e.y,e.z,e.face,held))return;
        for(BlockPottedNether116 pot:NetherVegetationContent116.POTS)if(held.getItem()==net.minecraft.item.Item.getItemFromBlock(pot.plant)){
            if(e.world.isRemote||e.world.setBlock(e.x,e.y,e.z,pot,0,3)){
                if(!e.world.isRemote&&!e.entityPlayer.capabilities.isCreativeMode&&--held.stackSize<=0)e.entityPlayer.inventory.setInventorySlotContents(e.entityPlayer.inventory.currentItem,null);
                e.useBlock=Event.Result.DENY;e.useItem=Event.Result.DENY;e.setCanceled(true);
            }return;
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void bonemeal(BonemealEvent e){
        if(e.getResult()!=Event.Result.DEFAULT||e.block!=Blocks.netherrack||e.world.getBlock(e.x,e.y+1,e.z).getLightOpacity(e.world,e.x,e.y+1,e.z)>=15)return;
        boolean crimson=false,warped=false;
        for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++){
            if(!e.world.blockExists(e.x+dx,e.y+dy,e.z+dz))continue;Block b=e.world.getBlock(e.x+dx,e.y+dy,e.z+dz);
            crimson|=b==NetherVegetationContent116.crimsonNylium;warped|=b==NetherVegetationContent116.warpedNylium;
        }
        if(!crimson&&!warped)return;
        if(e.world.isRemote||e.world.setBlock(e.x,e.y,e.z,warped&&(!crimson||e.world.rand.nextBoolean())?NetherVegetationContent116.warpedNylium:NetherVegetationContent116.crimsonNylium,0,3))e.setResult(Event.Result.ALLOW);
    }
    public static BlockNetherPlant116 vegetation(boolean warped,int roll){
        if(warped)return roll<85?NetherVegetationContent116.warpedRoots:roll<86?NetherVegetationContent116.crimsonRoots:roll<99?NetherVegetationContent116.warpedFungus:NetherVegetationContent116.crimsonFungus;
        return roll<87?NetherVegetationContent116.crimsonRoots:roll<98?NetherVegetationContent116.crimsonFungus:NetherVegetationContent116.warpedFungus;
    }
    public static void spread(World w,Random r,int x,int y,int z,boolean warped){
        scatter(w,r,x,y,z,warped,false);
        if(warped){scatter(w,r,x,y,z,true,true);if(r.nextInt(8)==0)twistingPatch(w,r,x,y,z);}
    }
    private static void scatter(World w,Random r,int x,int y,int z,boolean warped,boolean sprouts){
        for(int i=0;i<9;i++){
            int px=x+r.nextInt(3)-r.nextInt(3),py=y+r.nextInt(1)-r.nextInt(1),pz=z+r.nextInt(3)-r.nextInt(3);
            BlockNetherPlant116 plant=sprouts?NetherVegetationContent116.sprouts:vegetation(warped,r.nextInt(warped?100:99));
            if(py>0&&w.blockExists(px,py,pz)&&w.isAirBlock(px,py,pz)&&plant.canBlockStay(w,px,py,pz))w.setBlock(px,py,pz,plant,0,3);
        }
    }
    private static void twistingPatch(World w,Random r,int x,int y,int z){
        for(int i=0;i<9;i++){
            int px=x+r.nextInt(7)-3,py=y+r.nextInt(3)-1,pz=z+r.nextInt(7)-3;
            while(py>0&&w.blockExists(px,py-1,pz)&&w.isAirBlock(px,py-1,pz))py--;
            if(py<=0||!w.blockExists(px,py,pz)||!w.isAirBlock(px,py,pz))continue;
            Block base=w.getBlock(px,py-1,pz);if(base!=Blocks.netherrack&&base!=NetherVegetationContent116.warpedNylium&&base!=NetherVegetationContent116.warpedWart)continue;
            int length=1+r.nextInt(3);if(r.nextInt(6)==0)length*=2;if(r.nextInt(5)==0)length=1;
            vine(w,r,px,py,pz,length,1,17);
        }
    }
    public static void vine(World w,Random r,int x,int y,int z,int length,int direction,int minAge){
        BlockNetherVine116[] family=direction>0?NetherVegetationContent116.twisting:NetherVegetationContent116.weeping;
        for(int i=1;i<=length;i++,y+=direction){
            if(y<0||y>=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.height(w)||!w.blockExists(x,y,z)||!w.isAirBlock(x,y,z))return;
            if(i==length||!w.blockExists(x,y+direction,z)||!w.isAirBlock(x,y+direction,z)){family[0].placeAge(w,x,y,z,minAge+r.nextInt(26-minAge));return;}
            if(!w.setBlock(x,y,z,family[2],0,2))return;
        }
    }
    private static boolean replaceable(World w,int x,int y,int z,boolean stem){
        Block b=w.getBlock(x,y,z);return !b.getMaterial().isLiquid()&&w.getTileEntity(x,y,z)==null&&(b.isReplaceable(w,x,y,z)||stem&&b instanceof BlockBush);
    }
    private static void place(World w,int x,int y,int z,Block b){
        // The planted vanilla feature drops replaceable plants over a non-air support.
        Block old=w.getBlock(x,y,z);int metadata=w.getBlockMetadata(x,y,z);boolean drops=old!=Blocks.air&&!w.isAirBlock(x,y-1,z);
        if(w.setBlock(x,y,z,b,0,3)&&drops)old.dropBlockAsItem(w,x,y,z,metadata,0);
    }
    public static boolean growFungus(World w,Random r,int x,int y,int z,boolean warped){
        if(w.isRemote||w.getBlock(x,y-1,z)!=(warped?NetherVegetationContent116.warpedNylium:NetherVegetationContent116.crimsonNylium))return false;
        int height=4+r.nextInt(10);if(r.nextInt(12)==0)height*=2;
        if(y<1||y+height>=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.height(w)||!w.checkChunksExist(x-3,y-1,z-3,x+3,y+height,z+3))return false;
        if(!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(w,r,x,y,z))return false;
        Block stem=warped?NetherWoodContent116.WARPED.stem:NetherWoodContent116.CRIMSON.stem;
        Block hat=warped?NetherVegetationContent116.warpedWart:FrostburnContent.netherWartBlock;
        if(!w.setBlockToAir(x,y,z))return false;
        for(int dy=0;dy<height;dy++)if(replaceable(w,x,y+dy,z,true))place(w,x,y+dy,z,stem);
        int crown=Math.min(r.nextInt(1+height/3)+5,height),bottom=height-crown;
        for(int dy=bottom;dy<=height;dy++){
            int radius=dy<height-r.nextInt(3)?2:1;if(crown>8&&dy<bottom+4)radius=3;
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
                int px=x+dx,py=y+dy,pz=z+dz;boolean edgeX=Math.abs(dx)==radius,edgeZ=Math.abs(dz)==radius,inside=!edgeX&&!edgeZ&&dy!=height,corner=edgeX&&edgeZ;
                if(!replaceable(w,px,py,pz,false))continue;
                if(dy<bottom+3){
                    if(inside)continue;
                    if(w.getBlock(px,py-1,pz)==hat)place(w,px,py,pz,hat);
                    else if(r.nextFloat()<.15F){place(w,px,py,pz,hat);if(!warped&&r.nextInt(11)==0)weeping(w,r,px,py-1,pz);}
                }else{
                    float lightChance=inside?.1F:corner?.01F:.0005F,hatChance=inside?.2F:corner?.7F:.98F,vineChance=warped?0:inside?.1F:corner?.083F:.07F;
                    if(r.nextFloat()<lightChance)place(w,px,py,pz,NetherVegetationContent116.shroomlight);
                    else if(r.nextFloat()<hatChance){place(w,px,py,pz,hat);if(r.nextFloat()<vineChance)weeping(w,r,px,py-1,pz);}
                }
            }
        }
        return true;
    }
    private static void weeping(World w,Random r,int x,int y,int z){if(!w.isAirBlock(x,y,z))return;int length=1+r.nextInt(5);if(r.nextInt(7)==0)length*=2;vine(w,r,x,y,z,length,-1,23);}
}
