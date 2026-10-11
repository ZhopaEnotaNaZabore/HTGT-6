package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.*;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.*;
import net.minecraft.world.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherUtilityContent116;

/** One global anchor target, persisted in player data; legacy beds retain their own normal path. */
public final class RespawnAnchor116 {
    public static final String TAG="HTGT6RespawnAnchor116";
    private static final Set<Integer> DIMENSIONS=new HashSet<Integer>(Arrays.asList(-1));
    private static final Map<EntityPlayer,Plan> PLANS=new WeakHashMap<EntityPlayer,Plan>();
    private static final int[][] AROUND={{0,-1},{-1,0},{0,1},{1,0},{-1,-1},{1,-1},{-1,1},{1,1}};
    private static final class Plan {
        final World world; final ChunkCoordinates anchor; final Vec3 safe;
        Plan(World w,ChunkCoordinates p,Vec3 s){world=w;anchor=p;safe=s;}
    }
    /** Explicit opt-in for another dimension provider; no dependency on a Nether mod. */
    public static void registerDimension(int dimension){DIMENSIONS.add(dimension);}
    public static boolean works(World w){return DIMENSIONS.contains(w.provider.dimensionId);}
    public static NBTTagCompound persisted(EntityPlayer p){
        NBTTagCompound data=p.getEntityData();
        if(!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG,10))data.setTag(EntityPlayer.PERSISTED_NBT_TAG,new NBTTagCompound());
        return data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }
    public static boolean bind(EntityPlayer p,int x,int y,int z){
        NBTTagCompound data=persisted(p), old=data.getCompoundTag(TAG);
        if(data.hasKey(TAG,10)&&old.getInteger("Dimension")==p.dimension&&old.getInteger("X")==x&&old.getInteger("Y")==y&&old.getInteger("Z")==z)return false;
        p.setSpawnChunk(new ChunkCoordinates(x,y,z),false,p.dimension);
        NBTTagCompound target=new NBTTagCompound();target.setInteger("Dimension",p.dimension);
        target.setInteger("X",x);target.setInteger("Y",y);target.setInteger("Z",z);data.setTag(TAG,target);
        p.addChatMessage(new ChatComponentTranslation("block.minecraft.set_spawn"));return true;
    }
    /** Called from the Forge dimension-aware setSpawnChunk, including vanilla bed/command changes. */
    public static void spawnChanged(EntityPlayer p,ChunkCoordinates pos,boolean forced,int dim){
        NBTTagCompound data=persisted(p);if(!data.hasKey(TAG,10))return;
        NBTTagCompound t=data.getCompoundTag(TAG);
        if(forced||pos==null||dim!=t.getInteger("Dimension")||pos.posX!=t.getInteger("X")||pos.posY!=t.getInteger("Y")||pos.posZ!=t.getInteger("Z")||p.dimension==dim&&p.worldObj.getBlock(pos.posX,pos.posY,pos.posZ)!=NetherUtilityContent116.anchor)data.removeTag(TAG);
    }
    public static int prepare(EntityPlayerMP p,int dimension,boolean endReturn){
        PLANS.remove(p);NBTTagCompound data=persisted(p);if(!data.hasKey(TAG,10))return dimension;
        NBTTagCompound t=data.getCompoundTag(TAG);int dim=t.getInteger("Dimension");
        WorldServer w=net.minecraftforge.common.DimensionManager.getWorld(dim);
        if(w==null&&net.minecraftforge.common.DimensionManager.isDimensionRegistered(dim)){
            net.minecraftforge.common.DimensionManager.initDimension(dim);w=net.minecraftforge.common.DimensionManager.getWorld(dim);
        }
        ChunkCoordinates pos=new ChunkCoordinates(t.getInteger("X"),t.getInteger("Y"),t.getInteger("Z"));Vec3 safe=null;
        if(w!=null&&works(w)&&Math.abs((long)pos.posX)<30000000&&Math.abs((long)pos.posZ)<30000000&&pos.posY>=0&&pos.posY<w.getHeight()){
            for(int x=(pos.posX-3)>>4;x<=((pos.posX+3)>>4);x++)for(int z=(pos.posZ-3)>>4;z<=((pos.posZ+3)>>4);z++)w.getChunkFromChunkCoords(x,z);
            if(w.getBlock(pos.posX,pos.posY,pos.posZ)==NetherUtilityContent116.anchor&&w.getBlockMetadata(pos.posX,pos.posY,pos.posZ)>0)safe=findSafe(w,pos,p);
        }
        PLANS.put(p,new Plan(w,pos,safe));
        if(safe==null){data.removeTag(TAG);return 0;}return dim;
    }
    /** Explicit Bukkit destinations and End portal transfers retain their own transaction. */
    public static int prepareHybrid(EntityPlayerMP old,int dimension,boolean endReturn,Object location){
        if(location!=null||endReturn){PLANS.remove(old);return dimension;}
        return prepare(old,dimension,false);
    }
    public static double hybridCoordinate(double proposed,EntityPlayerMP old,int axis){
        Plan plan=PLANS.get(old);
        if(plan==null||plan.safe==null)return proposed;
        return axis==0?plan.safe.xCoord:axis==1?plan.safe.yCoord:plan.safe.zCoord;
    }
    public static void finishHybrid(EntityPlayerMP next,EntityPlayerMP old,boolean endReturn){
        Plan plan=PLANS.get(old);
        // Crucible can return null or the old player on cancellation/disconnection.
        // A plugin may also send the new player to a different destination.
        if(next==null||next==old||plan!=null&&plan.safe!=null&&
            (next.worldObj!=plan.world||Math.abs(next.posX-plan.safe.xCoord)>.01||
             Math.abs(next.posY-plan.safe.yCoord)>.01||Math.abs(next.posZ-plan.safe.zCoord)>.01)){
            PLANS.remove(old);return;
        }
        finish(next,old,endReturn);
    }
    public static boolean canRespawn(WorldProvider provider,EntityPlayerMP p){Plan plan=PLANS.get(p);return plan!=null&&plan.safe!=null||provider.canRespawnHere();}
    public static ChunkCoordinates spawnPosition(EntityPlayerMP p,int dim){Plan plan=PLANS.get(p);return plan==null?p.getBedLocation(dim):plan.safe==null?null:plan.anchor;}
    public static ChunkCoordinates resolve(World world,ChunkCoordinates pos,boolean forced,EntityPlayerMP p){
        Plan plan=PLANS.get(p);return plan==null?EntityPlayer.verifyRespawnCoordinates(world,pos,forced):plan.safe==null?null:new ChunkCoordinates(MathHelper.floor_double(plan.safe.xCoord),MathHelper.floor_double(plan.safe.yCoord),MathHelper.floor_double(plan.safe.zCoord));
    }
    public static void place(EntityPlayerMP next,double x,double y,double z,float yaw,float pitch,EntityPlayerMP old){
        Plan plan=PLANS.get(old);
        if(plan!=null&&plan.safe!=null){x=plan.safe.xCoord;y=plan.safe.yCoord;z=plan.safe.zCoord;yaw=(float)(Math.atan2(plan.anchor.posZ+.5-z,plan.anchor.posX+.5-x)*180/Math.PI)-90;pitch=0;}
        next.setLocationAndAngles(x,y,z,yaw,pitch);
    }
    public static void finish(EntityPlayerMP next,EntityPlayerMP old,boolean endReturn){
        Plan plan=PLANS.remove(old);if(plan==null)return;
        if(plan.safe==null){next.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0,0));return;}
        if(!endReturn&&plan.world.getBlock(plan.anchor.posX,plan.anchor.posY,plan.anchor.posZ)==NetherUtilityContent116.anchor){
            int charge=plan.world.getBlockMetadata(plan.anchor.posX,plan.anchor.posY,plan.anchor.posZ);
            if(charge>0){plan.world.setBlock(plan.anchor.posX,plan.anchor.posY,plan.anchor.posZ,NetherUtilityContent116.anchor,charge-1,3);
                plan.world.playSoundEffect(plan.anchor.posX+.5,plan.anchor.posY+.5,plan.anchor.posZ+.5,"htgt6:block.respawn_anchor.deplete",1,1);}
        }
    }
    public static Vec3 findSafe(World w,ChunkCoordinates anchor,EntityPlayer p){
        for(int strict=0;strict<2;strict++){
            for(int dy:new int[]{0,1,-1})for(int[] offset:AROUND){Vec3 safe=safe(w,anchor.posX+offset[0],anchor.posY+dy,anchor.posZ+offset[1],p,strict==0);if(safe!=null)return safe;}
            Vec3 top=safe(w,anchor.posX,anchor.posY+1,anchor.posZ,p,strict==0);if(top!=null)return top;
        }return null;
    }
    private static Vec3 safe(World w,int x,int y,int z,EntityPlayer p,boolean strict){
        if(y<1||y>=w.getHeight())return null;
        Block feet=w.getBlock(x,y,z),below=w.getBlock(x,y-1,z);
        if(feet.getMaterial().isLiquid()||below.getMaterial().isLiquid()||hazard(feet)||strict&&hazard(below))return null;
        AxisAlignedBB ground=below.getCollisionBoundingBoxFromPool(w,x,y-1,z);
        AxisAlignedBB current=feet.getCollisionBoundingBoxFromPool(w,x,y,z);
        double floor=current!=null?current.maxY:ground!=null?ground.maxY:Double.NaN;
        if(Double.isNaN(floor)||floor<y-1||floor>y+1||floor+1.8>w.getHeight())return null;
        double half=.3;AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x+.5-half,floor+.001,z+.5-half,x+.5+half,floor+1.8,z+.5+half);
        if(!w.getCollidingBoundingBoxes(p,box).isEmpty()||w.isAnyLiquid(box))return null;
        for(int head=MathHelper.floor_double(floor);head<=MathHelper.floor_double(floor+1.799);head++)if(hazard(w.getBlock(x,head,z)))return null;
        return Vec3.createVectorHelper(x+.5,floor,z+.5);
    }
    private static boolean hazard(Block b){String name=String.valueOf(Block.blockRegistry.getNameForObject(b));return b==Blocks.fire||b==Blocks.cactus||name.contains("campfire")||name.contains("magma")||name.contains("sweet_berry")||name.contains("soul_fire");}
    private RespawnAnchor116(){}
}
