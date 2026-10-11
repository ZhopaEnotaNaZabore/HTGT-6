package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.network.*;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115;

/** The stored NBT is authoritative until the world accepts a released entity. */
public final class TileEntityBeehive115 extends TileEntity {
    private final List<NBTTagCompound> bees=new ArrayList<NBTTagCompound>();
    private int honey;
    private NBTTagCompound flower=new NBTTagCompound();
    public int honey(){return honey;}
    public int occupants(){return bees.size();}
    public void setHoney(int value){honey=Math.max(0,Math.min(5,value));changed();}
    private void changed(){markDirty();if(worldObj!=null){worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);worldObj.func_147453_f(xCoord,yCoord,zCoord,getBlockType());}}
    public boolean enter(EntityBee115 bee){
        if(worldObj==null||worldObj.isRemote||bees.size()>=3||bee.isDead||bee.worldObj!=worldObj)return false;
        NBTTagCompound entity=new NBTTagCompound();bee.writeToNBT(entity);
        for(NBTTagCompound stored:bees)if(stored.getCompoundTag("EntityData").getLong("UUIDMost")==entity.getLong("UUIDMost")&&stored.getCompoundTag("EntityData").getLong("UUIDLeast")==entity.getLong("UUIDLeast"))return false;
        NBTTagCompound entry=new NBTTagCompound();entry.setTag("EntityData",entity);entry.setInteger("TicksInHive",0);entry.setInteger("MinOccupationTicks",bee.hasNectar()?2400:600);
        if(entity.getInteger("FlowerY")>=0){flower.setInteger("FlowerX",entity.getInteger("FlowerX"));flower.setInteger("FlowerY",entity.getInteger("FlowerY"));flower.setInteger("FlowerZ",entity.getInteger("FlowerZ"));}
        bees.add(entry);changed();bee.setDead();sound("enter");return true;
    }
    public void seed(int count,Random random){
        if(worldObj==null||worldObj.isRemote)return;
        for(int i=0;i<count&&bees.size()<3;i++){
            EntityBee115 bee=new EntityBee115(worldObj);bee.setPosition(xCoord+.5,yCoord+.5,zCoord+.5);bee.setHome(xCoord,yCoord,zCoord);
            NBTTagCompound data=new NBTTagCompound();bee.writeToNBT(data);
            NBTTagCompound entry=new NBTTagCompound();entry.setTag("EntityData",data);entry.setInteger("TicksInHive",random.nextInt(599));entry.setInteger("MinOccupationTicks",600);bees.add(entry);
        }changed();
    }
    @Override public void updateEntity(){
        if(worldObj==null||worldObj.isRemote)return;
        boolean emergency=nearFire();
        if(!bees.isEmpty()&&worldObj.rand.nextDouble()<.005)worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,"htgt6:block.beehive.work",1F,1F);
        for(int i=bees.size()-1;i>=0;i--){NBTTagCompound entry=bees.get(i);int ticks=entry.getInteger("TicksInHive");entry.setInteger("TicksInHive",ticks==Integer.MAX_VALUE?ticks:ticks+1);
            if(emergency||ticks>=entry.getInteger("MinOccupationTicks")&&worldObj.getTotalWorldTime()%20==0)release(i,emergency,null);
        }
        if(!bees.isEmpty())markDirty();
    }
    public boolean nearFire(){for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++)if(worldObj.getBlock(xCoord+dx,yCoord+dy,zCoord+dz)==net.minecraft.init.Blocks.fire)return true;return false;}
    private boolean release(int index,boolean emergency,EntityPlayer target){
        if(!emergency&&(!worldObj.isDaytime()||worldObj.isRaining()))return false;
        int facing=BlockBeehive115.side(getBlockMetadata());double dx=Facing.offsetsXForSide[facing],dz=Facing.offsetsZForSide[facing];
        EntityBee115 bee=new EntityBee115(worldObj);NBTTagCompound entry=bees.get(index);NBTTagCompound data=(NBTTagCompound)entry.getCompoundTag("EntityData").copy();if(data.getInteger("FlowerY")<0&&flower.hasKey("FlowerY")){data.setInteger("FlowerX",flower.getInteger("FlowerX"));data.setInteger("FlowerY",flower.getInteger("FlowerY"));data.setInteger("FlowerZ",flower.getInteger("FlowerZ"));}bee.readFromNBT(data);
        bee.setLocationAndAngles(xCoord+.5+dx*.9,yCoord+.2,zCoord+.5+dz*.9,0,0);
        if(!worldObj.getCollidingBoundingBoxes(bee,bee.boundingBox).isEmpty()){
            if(!emergency)return false;bee.setPosition(xCoord+.5,yCoord+.2,zCoord+.5);
        }
        int age=bee.getGrowingAge(),elapsed=entry.getInteger("TicksInHive");bee.setGrowingAge(age<0?Math.min(0,age+elapsed):Math.max(0,age-elapsed));
        bee.setHome(xCoord,yCoord,zCoord);bee.hiveCooldown=400;
        boolean produced=bee.hasNectar()&&!emergency;
        if(produced)bee.setNectar(false);
        if(target!=null)bee.angerAt(target);
        if(!worldObj.spawnEntityInWorld(bee))return false;
        bees.remove(index);if(produced)honey=Math.min(5,honey+(worldObj.rand.nextInt(100)==0?2:1));changed();sound("exit");return true;
    }
    public void releaseAll(EntityPlayer target){if(worldObj==null||worldObj.isRemote)return;for(int i=bees.size()-1;i>=0;i--)release(i,true,target);}
    /** Used only after ownership has transferred into an ItemStack. */
    public void clearStored(){bees.clear();changed();}
    private void sound(String name){worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,"htgt6:block.beehive."+name,1F,1F);}
    public NBTTagCompound payload(){NBTTagCompound n=new NBTTagCompound();writeToNBT(n);n.removeTag("x");n.removeTag("y");n.removeTag("z");n.removeTag("id");return n;}
    public void restore(NBTTagCompound n){int x=xCoord,y=yCoord,z=zCoord;readFromNBT(n);xCoord=x;yCoord=y;zCoord=z;changed();}
    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setTag("Flower",flower.copy());n.setInteger("HoneyLevel",honey);NBTTagList list=new NBTTagList();for(NBTTagCompound b:bees)list.appendTag(b.copy());n.setTag("Bees",list);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);flower=(NBTTagCompound)n.getCompoundTag("Flower").copy();honey=Math.max(0,Math.min(5,n.getInteger("HoneyLevel")));bees.clear();NBTTagList list=n.getTagList("Bees",10);for(int i=0;i<Math.min(3,list.tagCount());i++){
        NBTTagCompound e=(NBTTagCompound)list.getCompoundTagAt(i).copy();e.setInteger("TicksInHive",Math.max(0,e.getInteger("TicksInHive")));e.setInteger("MinOccupationTicks",Math.max(600,e.getInteger("MinOccupationTicks")));bees.add(e);
    }}
    @Override public Packet getDescriptionPacket(){NBTTagCompound n=new NBTTagCompound();n.setInteger("HoneyLevel",honey);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,0,n);}
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet){honey=Math.max(0,Math.min(5,packet.func_148857_g().getInteger("HoneyLevel")));worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
}
