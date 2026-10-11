package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;

/** Server-owned bee lifecycle. Flight always uses legacy collision resolution. */
public final class EntityBee115 extends EntityAnimal {
    private int homeX,homeY=-1,homeZ,flowerX,flowerY=-1,flowerZ;
    private int pollenTicks,searchTicks,angerTicks,stingTicks,crops,babyTicks,failedTrips;
    public int hiveCooldown;
    private UUID angerTarget;
    private double targetX,targetY,targetZ;
    private int goalTicks,underWaterTicks;
    private List<int[]> path=Collections.emptyList();private int pathIndex,repath;
    public EntityBee115(World w){super(w);setSize(.7F,.6F);experienceValue=0;targetX=posX;targetY=posY;targetZ=posZ;}
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Byte.valueOf((byte)0));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3);getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32);}
    @Override protected boolean isAIEnabled(){return true;}
    @Override protected boolean canDespawn(){return false;}
    @Override protected void fall(float distance){}
    public boolean hasNectar(){return(dataWatcher.getWatchableObjectByte(20)&1)!=0;}
    public boolean angry(){return(dataWatcher.getWatchableObjectByte(20)&2)!=0;}
    public boolean stung(){return(dataWatcher.getWatchableObjectByte(20)&4)!=0;}
    private void flag(int bit,boolean value){int f=dataWatcher.getWatchableObjectByte(20);dataWatcher.updateObject(20,Byte.valueOf((byte)(value?f|bit:f&~bit)));}
    public void setNectar(boolean value){flag(1,value);if(!value){crops=0;pollenTicks=0;}}
    public void setHome(int x,int y,int z){homeX=x;homeY=y;homeZ=z;}
    public void angerAt(EntityLivingBase target){if(stung()||target instanceof EntityPlayer&&((EntityPlayer)target).capabilities.isCreativeMode)return;angerTarget=target.getUniqueID();angerTicks=400+rand.nextInt(400);flag(2,true);pollenTicks=0;}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){boolean hit=super.attackEntityFrom(source,amount);if(hit&&!worldObj.isRemote&&source.getEntity() instanceof EntityLivingBase){EntityLivingBase enemy=(EntityLivingBase)source.getEntity();for(Object e:worldObj.getEntitiesWithinAABB(EntityBee115.class,boundingBox.expand(8,6,8)))((EntityBee115)e).angerAt(enemy);}return hit;}
    @Override public EntityAnimal createChild(EntityAgeable other){return new EntityBee115(worldObj);}
    public static boolean flower(Block b,int meta){return b instanceof BlockFlower||b==Blocks.double_plant&&meta<=5&&meta!=2&&meta!=3;}
    @Override public boolean isBreedingItem(ItemStack stack){return stack!=null&&flower(Block.getBlockFromItem(stack.getItem()),stack.getItemDamage());}
    private boolean flowerAt(int x,int y,int z){return worldObj.blockExists(x,y,z)&&flower(worldObj.getBlock(x,y,z),worldObj.getBlockMetadata(x,y,z));}
    @Override protected String getHurtSound(){return "htgt6:entity.bee.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.bee.death";}
    @Override protected String getLivingSound(){return null;}
    @Override public void moveEntityWithHeading(float strafe,float forward){
        // No gravity, but moveEntity resolves walls, floors and entities normally.
        moveEntity(motionX,motionY,motionZ);motionX*=.8;motionY*=.8;motionZ*=.8;
    }
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();fallDistance=0;
        if(worldObj.isRemote)return;
        underWaterTicks=isInWater()?underWaterTicks+1:0;
        if(underWaterTicks>20)attackEntityFrom(DamageSource.drown,1);
        if(isInWater())motionY+=.04;
        if(hiveCooldown>0)hiveCooldown--;
        if(angerTicks>0&&--angerTicks==0){angerTarget=null;flag(2,false);}
        if(stung()){++stingTicks;if(stingTicks%5==0&&rand.nextInt(MathHelper.clamp_int(1200-stingTicks,1,1200))==0){attackEntityFrom(DamageSource.generic,getHealth());return;}}
        if(ticksExisted%40==0&&!onGround)playSound("htgt6:entity.bee."+(angry()?"loop_aggressive":"loop"),.3F,1F);
        EntityLivingBase enemy=enemy();
        if(enemy!=null&&!stung()){
            go(enemy.posX,enemy.posY+enemy.height*.5,enemy.posZ);
            if(getDistanceSqToEntity(enemy)<1.2&&canEntityBeSeen(enemy)&&enemy.attackEntityFrom(DamageSource.causeMobDamage(this),2)){
                int seconds=worldObj.difficultySetting==net.minecraft.world.EnumDifficulty.HARD?18:worldObj.difficultySetting==net.minecraft.world.EnumDifficulty.NORMAL?10:0;
                if(seconds>0)enemy.addPotionEffect(new PotionEffect(Potion.poison.id,seconds*20,0));flag(4,true);flag(2,false);angerTicks=0;angerTarget=null;playSound("htgt6:entity.bee.sting",1,1);
            }fly();return;
        }
        if(!stung()&&isInLove()){
            EntityBee115 mate=null;for(Object raw:worldObj.getEntitiesWithinAABB(EntityBee115.class,boundingBox.expand(8,8,8))){EntityBee115 b=(EntityBee115)raw;if(b!=this&&b.isInLove()&&b.getGrowingAge()==0){mate=b;break;}}
            if(mate!=null){go(mate.posX,mate.posY,mate.posZ);if(getDistanceSqToEntity(mate)<4&&++babyTicks>=60){EntityBee115 child=new EntityBee115(worldObj);child.setGrowingAge(-24000);child.setPosition(posX,posY,posZ);
                if(worldObj.spawnEntityInWorld(child)){setGrowingAge(6000);mate.setGrowingAge(6000);resetInLove();mate.resetInLove();if(worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))worldObj.spawnEntityInWorld(new net.minecraft.entity.item.EntityXPOrb(worldObj,posX,posY,posZ,1+rand.nextInt(7)));}babyTicks=0;
            }fly();return;}
        }else babyTicks=0;
        for(Object raw:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)raw;if(getDistanceSqToEntity(p)<100&&isBreedingItem(p.getHeldItem())&&!angry()){go(p.posX,p.posY+1,p.posZ);if(getDistanceSqToEntity(p)<4){motionX*=.5;motionZ*=.5;}fly();return;}}
        if(++searchTicks%100==0){
            if(homeY>=0&&worldObj.blockExists(homeX,homeY,homeZ)&&BlockBeehive115.tile(worldObj,homeX,homeY,homeZ)==null)homeY=-1;
            if(homeY<0)findHive();
        }
        boolean returnHome=hasNectar()||worldObj.isRaining()||!worldObj.isDaytime()||searchTicks>3600;
        if(returnHome&&homeY>=0&&hiveCooldown==0&&!stung()){
            if(!worldObj.blockExists(homeX,homeY,homeZ)){wander();fly();return;}
            TileEntityBeehive115 hive=BlockBeehive115.tile(worldObj,homeX,homeY,homeZ);
            if(hive==null||hive.occupants()>=3||hive.nearFire()){homeY=-1;hiveCooldown=200;}
            else {int side=BlockBeehive115.side(worldObj.getBlockMetadata(homeX,homeY,homeZ));go(homeX+.5+Facing.offsetsXForSide[side],homeY+.2,homeZ+.5+Facing.offsetsZForSide[side]);
                if(getDistanceSq(targetX,targetY,targetZ)<.6&&hive.enter(this))return;
                pollinateCrops();fly();return;}
        }
        if(!hasNectar()&&!stung()&&worldObj.isDaytime()&&!worldObj.isRaining()){
            if(flowerY>=0&&!flowerAt(flowerX,flowerY,flowerZ)){flowerY=-1;pollenTicks=0;}
            if(flowerY<0&&searchTicks%100==0)findFlower();
            if(flowerY>=0){go(flowerX+.5,flowerY+.65,flowerZ+.5);
                if(getDistanceSq(targetX,targetY,targetZ)<.5){motionX*=.5;motionY*=.5;motionZ*=.5;if(++pollenTicks%60==0)playSound("htgt6:entity.bee.pollinate",1,1);if(pollenTicks>=400){setNectar(true);searchTicks=0;}}
                fly();return;}
        }
        wander();fly();
    }
    private EntityLivingBase enemy(){if(angerTarget==null)return null;for(Object raw:worldObj.loadedEntityList)if(raw instanceof EntityLivingBase){EntityLivingBase e=(EntityLivingBase)raw;if(angerTarget.equals(e.getUniqueID())&&e.isEntityAlive()&&getDistanceSqToEntity(e)<1024&&!(e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.isCreativeMode))return e;}return null;}
    private void findHive(){double nearest=Double.MAX_VALUE;for(Object raw:worldObj.loadedTileEntityList)if(raw instanceof TileEntityBeehive115){TileEntityBeehive115 t=(TileEntityBeehive115)raw;double d=getDistanceSq(t.xCoord+.5,t.yCoord+.5,t.zCoord+.5);if(d<=100&&d<nearest&&t.occupants()<3&&!t.nearFire()){setHome(t.xCoord,t.yCoord,t.zCoord);nearest=d;}}}
    private void findFlower(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);double best=Double.MAX_VALUE;for(int dx=-5;dx<=5;dx++)for(int dy=-5;dy<=5;dy++)for(int dz=-5;dz<=5;dz++)if(flowerAt(x+dx,y+dy,z+dz)){double d=dx*dx+dy*dy+dz*dz;if(d<best){flowerX=x+dx;flowerY=y+dy;flowerZ=z+dz;best=d;}}}
    private void pollinateCrops(){if(!hasNectar()||crops>=10||ticksExisted%30!=0||!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return;int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);for(int d=1;d<=2;d++){Block b=worldObj.getBlock(x,y-d,z);if(b instanceof BlockCrops||b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.sweetBerryBush){int max=b instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockBeetrootCrop||b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.sweetBerryBush?3:7;int m=worldObj.getBlockMetadata(x,y-d,z);if(m<max&&worldObj.setBlockMetadataWithNotify(x,y-d,z,m+1,3)){crops++;worldObj.playAuxSFX(2005,x,y-d,z,0);break;}}}}
    private void wander(){if(--goalTicks<=0||getDistanceSq(targetX,targetY,targetZ)<.8){goalTicks=60+rand.nextInt(60);go(posX+rand.nextInt(13)-6,Math.max(1,posY+rand.nextInt(7)-3),posZ+rand.nextInt(13)-6);}}
    private void go(double x,double y,double z){if(Math.abs(targetX-x)+Math.abs(targetY-y)+Math.abs(targetZ-z)>1){repath=0;}targetX=x;targetY=y;targetZ=z;}
    private boolean free(double x,double y,double z){int ix=MathHelper.floor_double(x),iy=MathHelper.floor_double(y),iz=MathHelper.floor_double(z);if(iy<1||iy>254||!worldObj.blockExists(ix,iy,iz))return false;AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x-width*.5,y,z-width*.5,x+width*.5,y+height,z+width*.5);return worldObj.getCollidingBoundingBoxes(this,box).isEmpty()&&!worldObj.isAnyLiquid(box);}
    private void fly(){
        if(--repath<=0){repath=40;path=findPath();pathIndex=0;if(path.isEmpty()&&++failedTrips>5){homeY=-1;flowerY=-1;goalTicks=0;hiveCooldown=200;failedTrips=0;}else if(!path.isEmpty())failedTrips=0;}
        double x=targetX,y=targetY,z=targetZ;
        if(pathIndex<path.size()){int[] n=path.get(pathIndex);x=n[0]+.5;y=n[1]+.1;z=n[2]+.5;if(getDistanceSq(x,y,z)<.4)pathIndex++;}
        else if(getDistanceSq(x,y,z)>4){motionX*=.5;motionY*=.5;motionZ*=.5;return;}
        double dx=x-posX,dy=y-posY,dz=z-posZ,len=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(len>.1){double speed=angry()?.09:.055;motionX+=dx/len*speed;motionY+=dy/len*speed;motionZ+=dz/len*speed;rotationYaw=(float)(Math.atan2(-dx,dz)*180/Math.PI);renderYawOffset=rotationYaw;}
        if(isCollidedHorizontally)repath=0;
    }
    /** Bounded 3-D breadth-first search; only loaded, collision-free cells are admitted. */
    private List<int[]> findPath(){
        int sx=MathHelper.floor_double(posX),sy=MathHelper.floor_double(posY),sz=MathHelper.floor_double(posZ),tx=MathHelper.floor_double(targetX),ty=MathHelper.floor_double(targetY),tz=MathHelper.floor_double(targetZ);
        ArrayList<int[]> nodes=new ArrayList<int[]>();ArrayList<Integer> parents=new ArrayList<Integer>();Set<String> seen=new HashSet<String>();nodes.add(new int[]{sx,sy,sz});parents.add(-1);seen.add(sx+","+sy+","+sz);int best=0;double distance=Double.MAX_VALUE;
        int[][] dirs={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        for(int head=0;head<nodes.size()&&head<512;head++){int[] p=nodes.get(head);double d=(p[0]-tx)*(p[0]-tx)+(p[1]-ty)*(p[1]-ty)+(p[2]-tz)*(p[2]-tz);if(d<distance){best=head;distance=d;}if(d==0)break;
            for(int[] dir:dirs){int x=p[0]+dir[0],y=p[1]+dir[1],z=p[2]+dir[2];if(Math.abs(x-sx)>10||Math.abs(y-sy)>8||Math.abs(z-sz)>10||!seen.add(x+","+y+","+z)||!free(x+.5,y+.1,z+.5))continue;nodes.add(new int[]{x,y,z});parents.add(head);}
        }
        LinkedList<int[]> result=new LinkedList<int[]>();for(int i=best;i>0;i=parents.get(i))result.addFirst(nodes.get(i));return result;
    }
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setByte("BeeFlags",dataWatcher.getWatchableObjectByte(20));n.setInteger("HiveX",homeX);n.setInteger("HiveY",homeY);n.setInteger("HiveZ",homeZ);n.setInteger("FlowerX",flowerX);n.setInteger("FlowerY",flowerY);n.setInteger("FlowerZ",flowerZ);n.setInteger("AngerTicks",angerTicks);n.setInteger("StingTicks",stingTicks);n.setInteger("HiveCooldown",hiveCooldown);n.setInteger("Crops",crops);n.setInteger("SearchTicks",searchTicks);if(angerTarget!=null)n.setString("AngryAt",angerTarget.toString());}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);dataWatcher.updateObject(20,Byte.valueOf(n.getByte("BeeFlags")));homeX=n.getInteger("HiveX");homeY=n.hasKey("HiveY")?n.getInteger("HiveY"):-1;homeZ=n.getInteger("HiveZ");flowerX=n.getInteger("FlowerX");flowerY=n.hasKey("FlowerY")?n.getInteger("FlowerY"):-1;flowerZ=n.getInteger("FlowerZ");angerTicks=Math.max(0,n.getInteger("AngerTicks"));stingTicks=Math.max(0,n.getInteger("StingTicks"));hiveCooldown=Math.max(0,n.getInteger("HiveCooldown"));crops=Math.max(0,n.getInteger("Crops"));searchTicks=Math.max(0,n.getInteger("SearchTicks"));try{angerTarget=UUID.fromString(n.getString("AngryAt"));}catch(IllegalArgumentException ignored){angerTarget=null;}targetX=posX;targetY=posY;targetZ=posZ;}
}
