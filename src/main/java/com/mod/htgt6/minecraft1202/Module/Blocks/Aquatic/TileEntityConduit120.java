package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.network.NetworkManager;import net.minecraft.network.Packet;import net.minecraft.network.play.server.S35PacketUpdateTileEntity;import net.minecraft.tileentity.TileEntity;import net.minecraft.util.AxisAlignedBB;import java.util.List;
/** Functional 1.13 conduit: prismarine frame, composite Conduit Power and hostile-water attack. */
public final class TileEntityConduit120 extends TileEntity {private boolean active;private int frame;private java.util.UUID targetUuid;
 @Override public void updateEntity(){if(worldObj==null||worldObj.isRemote||worldObj.getTotalWorldTime()%40L!=0)return;if(!worldObj.checkChunksExist(xCoord-2,yCoord-2,zCoord-2,xCoord+2,yCoord+2,zCoord+2))return;int count=waterEnclosed()?countFrame():0;boolean next=count>=16;boolean changed=next!=active||frame!=count;frame=count;if(next!=active){active=next;worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,next?"htgt6:block.conduit.activate":"htgt6:block.conduit.deactivate",1F,1F);}if(changed){markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}if(!active){setTarget(null);return;}int range=effectRangeForFrame(count);@SuppressWarnings("unchecked") List<EntityPlayer> players=worldObj.getEntitiesWithinAABB(EntityPlayer.class,AxisAlignedBB.getBoundingBox(xCoord-range,yCoord-range,zCoord-range,xCoord+range+1,yCoord+range+1,zCoord+range+1));for(EntityPlayer p:players)if(canAffect(p,range)){com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.AquaticEffects120.grantConduitPower(p);}updateAttackTarget(count);}
 private void updateAttackTarget(int count){
  if(count<42){setTarget(null);return;}
  @SuppressWarnings("unchecked") List<net.minecraft.entity.EntityLivingBase> nearby=worldObj.getEntitiesWithinAABB(
   net.minecraft.entity.EntityLivingBase.class,AxisAlignedBB.getBoundingBox(xCoord-8,yCoord-8,zCoord-8,xCoord+9,yCoord+9,zCoord+9));
  java.util.ArrayList<net.minecraft.entity.EntityLivingBase> candidates=new java.util.ArrayList<net.minecraft.entity.EntityLivingBase>();
  net.minecraft.entity.EntityLivingBase target=null;
  for(net.minecraft.entity.EntityLivingBase entity:nearby){
   if(!(entity instanceof net.minecraft.entity.monster.IMob)||!canAffect(entity,8))continue;
   candidates.add(entity);
   if(entity.getUniqueID().equals(targetUuid))target=entity;
  }
  if(target==null&&!candidates.isEmpty())target=candidates.get(worldObj.rand.nextInt(candidates.size()));
  setTarget(target==null?null:target.getUniqueID());
  if(target!=null){
   target.attackEntityFrom(net.minecraft.util.DamageSource.magic,4F);
   worldObj.playSoundEffect(target.posX,target.posY,target.posZ,"htgt6:block.conduit.attack.target",1F,1F);
  }
 }
 private void setTarget(java.util.UUID value){
  if(java.util.Objects.equals(targetUuid,value))return;
  targetUuid=value;markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);
 }
 public java.util.UUID getTargetUuid(){return targetUuid;}

 /** Modern conduit compares block positions, not corners of the query cube. */
 public boolean canAffect(net.minecraft.entity.EntityLivingBase entity,int range){
  if(entity==null||entity.worldObj!=worldObj||!entity.isEntityAlive()||!entity.isWet())return false;
  double dx=net.minecraft.util.MathHelper.floor_double(entity.posX)-(double)xCoord;
  double dy=net.minecraft.util.MathHelper.floor_double(entity.posY)-(double)yCoord;
  double dz=net.minecraft.util.MathHelper.floor_double(entity.posZ)-(double)zCoord;
  return dx*dx+dy*dy+dz*dz<(double)range*range;
 }
 private boolean waterEnclosed(){for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++)if((dx!=0||dy!=0||dz!=0)&&!BlockKelp.isWater(worldObj.getBlock(xCoord+dx,yCoord+dy,zCoord+dz)))return false;return true;}
 private int countFrame(){int count=0;for(int dx=-2;dx<=2;dx++)for(int dy=-2;dy<=2;dy++)for(int dz=-2;dz<=2;dz++)if(isFrameOffset(dx,dy,dz)&&isFrameBlock(worldObj.getBlock(xCoord+dx,yCoord+dy,zCoord+dz)))count++;return count;}
 public static boolean isFrameBlock(net.minecraft.block.Block block){return block==BountifulContent.prismarine||block==BountifulContent.seaLantern;}
 public static boolean isFrameOffset(int dx,int dy,int dz){if(Math.max(Math.max(Math.abs(dx),Math.abs(dy)),Math.abs(dz))!=2)return false;return dx==0||dy==0||dz==0;}
 public static int effectRangeForFrame(int count){return Math.max(0,Math.min(42,count))/7*16;}
 public boolean isActive(){return active;}public int getFrameCount(){return frame;}@Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setBoolean("Active",active);n.setByte("Frame",(byte)frame);if(targetUuid!=null)n.setString("Target",targetUuid.toString());else n.removeTag("Target");}@Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);active=n.getBoolean("Active");frame=Math.min(42,n.getByte("Frame")&255);targetUuid=null;if(n.hasKey("Target",8))try{targetUuid=java.util.UUID.fromString(n.getString("Target"));}catch(IllegalArgumentException ignored){}}@Override public Packet getDescriptionPacket(){NBTTagCompound n=new NBTTagCompound();writeToNBT(n);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,0,n);}@Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}}
