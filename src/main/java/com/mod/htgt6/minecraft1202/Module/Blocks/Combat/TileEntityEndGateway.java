package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EndMechanics;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public final class TileEntityEndGateway extends TileEntity {
    private long age;
    private int cooldown,exitX,exitY,exitZ; private boolean hasExit,exact;
    @Override public void updateEntity() {
        ++age;
        if(cooldown>0)--cooldown;
        else if(worldObj!=null&&!worldObj.isRemote&&age%2400==0)triggerCooldown();
    }
    @Override public net.minecraft.util.AxisAlignedBB getRenderBoundingBox(){return INFINITE_EXTENT_AABB;}
    public boolean isSpawning(){return age<200;}
    public boolean isCoolingDown(){return cooldown>0;}
    public float spawnProgress(float partial){return Math.max(0,Math.min(1,(age+partial)/200F));}
    public float cooldownProgress(float partial){return 1-Math.max(0,Math.min(1,(cooldown-partial)/40F));}
    private void triggerCooldown(){cooldown=40;markDirty();if(worldObj!=null){worldObj.addBlockEvent(xCoord,yCoord,zCoord,getBlockType(),1,0);worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}}
    @Override public boolean receiveClientEvent(int id,int value){if(id==1){cooldown=40;return true;}return super.receiveClientEvent(id,value);}
    @Override public net.minecraft.network.Packet getDescriptionPacket(){net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,8,tag);}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}
    public static int[] findExit(World world,int x,int y,int z,int radius,boolean includeCenter){
        int[] best=null;
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(!includeCenter&&dx==0&&dz==0)continue;
            for(int by=world.getHeight()-1;by>(best==null?0:best[1]);by--){
                net.minecraft.block.Block block=world.getBlock(x+dx,by,z+dz);
                if(block.isNormalCube()&&(includeCenter||block!=Blocks.bedrock)){best=new int[]{x+dx,by,z+dz};break;}
            }
        }
        return best==null?new int[]{x,y,z}:best;
    }
    public void setExit(int x,int y,int z,boolean exact){exitX=x;exitY=y;exitZ=z;hasExit=true;this.exact=exact;markDirty();}
    public void teleport(Entity entity){
        if(worldObj.isRemote||cooldown>0||entity.isDead)return;
        if(!hasExit&&worldObj.provider.dimensionId==1)createExit();
        if(hasExit){
            int[] destination=exact?new int[]{exitX,exitY,exitZ}:findExit(worldObj,exitX,exitY+2,exitZ,5,false);
            int y=destination[1]+(exact?0:1);
            if(entity instanceof EntityPlayerMP)((EntityPlayerMP)entity).playerNetServerHandler.setPlayerLocation(destination[0]+.5,y,destination[2]+.5,entity.rotationYaw,entity.rotationPitch);
            else {entity.setPosition(destination[0]+.5,y,destination[2]+.5);entity.velocityChanged=true;}
            entity.fallDistance=0;
            if(entity instanceof EntityPlayerMP)com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete((EntityPlayerMP)entity,"enter_end_gateway");
            entity.getEntityData().setLong("HTGT6EndGateway",worldObj.getTotalWorldTime()+40L);
        }
        triggerCooldown();
    }
    private void createExit(){
        double length=Math.sqrt((double)xCoord*xCoord+(double)zCoord*zCoord);
        double dx=length<1?0:xCoord/length,dz=length<1?0:zCoord/length;
        double px=dx*1024,pz=dz*1024;
        for(int i=0;i<16&&!emptyChunk(px,pz);i++){px-=dx*16;pz-=dz*16;}
        for(int i=0;i<16&&emptyChunk(px,pz);i++){px+=dx*16;pz+=dz*16;}
        int cx=((int)Math.floor(px))>>4,cz=((int)Math.floor(pz))>>4;
        int[] base=null;double nearest=Double.MAX_VALUE;
        for(int x=cx*16;x<cx*16+16;x++)for(int z=cz*16;z<cz*16+16;z++)for(int y=30;y<worldObj.getHeight()-2;y++){
            if(worldObj.getBlock(x,y,z)!=Blocks.end_stone||worldObj.getBlock(x,y+1,z).isNormalCube()||worldObj.getBlock(x,y+2,z).isNormalCube())continue;
            double distance=(x+.5)*(x+.5)+(y+.5)*(y+.5)+(z+.5)*(z+.5);
            if(distance<nearest){nearest=distance;base=new int[]{x,y,z};}
        }
        if(base==null){
            base=new int[]{(int)Math.floor(px+.5),75,(int)Math.floor(pz+.5)};
            // Bounded fallback island using the native End-island radius decay.
            java.util.Random random=new java.util.Random(worldObj.rand.nextLong());
            float radius=random.nextInt(3)+4;
            for(int y=0;radius>.5F;y--){
                for(int x=-(int)Math.ceil(radius);x<=(int)Math.ceil(radius);x++)for(int z=-(int)Math.ceil(radius);z<=(int)Math.ceil(radius);z++)
                    if(x*x+z*z<=(radius+1)*(radius+1))worldObj.setBlock(base[0]+x,base[1]+y,base[2]+z,Blocks.end_stone,0,2);
                radius-=random.nextInt(2)+.5F;
            }
        }
        int[] top=findExit(worldObj,base[0],base[1],base[2],16,true);
        int gy=Math.min(worldObj.getHeight()-3,top[1]+10);
        EndMechanics.generateGateway(worldObj,top[0],gy,top[2],xCoord,yCoord,zCoord,false);
        setExit(top[0],gy,top[2],false);
    }
    private boolean emptyChunk(double x,double z){
        net.minecraft.world.chunk.Chunk chunk=worldObj.getChunkFromChunkCoords(((int)Math.floor(x))>>4,((int)Math.floor(z))>>4);
        for(net.minecraft.world.chunk.storage.ExtendedBlockStorage section:chunk.getBlockStorageArray())if(section!=null&&!section.isEmpty())return false;
        return true;
    }
    @Override public void writeToNBT(net.minecraft.nbt.NBTTagCompound tag) { super.writeToNBT(tag);tag.setLong("Age",age);tag.setInteger("Cooldown",cooldown);tag.setBoolean("HasExit",hasExit);tag.setInteger("ExitX",exitX);tag.setInteger("ExitY",exitY);tag.setInteger("ExitZ",exitZ);tag.setBoolean("ExactTeleport",exact);if(hasExit){net.minecraft.nbt.NBTTagCompound exit=new net.minecraft.nbt.NBTTagCompound();exit.setInteger("X",exitX);exit.setInteger("Y",exitY);exit.setInteger("Z",exitZ);tag.setTag("ExitPortal",exit);} }
    @Override public void readFromNBT(net.minecraft.nbt.NBTTagCompound tag) { super.readFromNBT(tag);age=tag.getLong("Age");cooldown=tag.getInteger("Cooldown");hasExit=tag.getBoolean("HasExit");exitX=tag.getInteger("ExitX");exitY=tag.getInteger("ExitY");exitZ=tag.getInteger("ExitZ");exact=tag.getBoolean("ExactTeleport");if(tag.hasKey("ExitPortal",10)){net.minecraft.nbt.NBTTagCompound exit=tag.getCompoundTag("ExitPortal");exitX=exit.getInteger("X");exitY=exit.getInteger("Y");exitZ=exit.getInteger("Z");hasExit=true;} }
}
