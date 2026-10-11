package com.mod.htgt6.minecraft1202.Module.Data.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.WardenAnger120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SculkWarningManager120;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
public final class WardenRegressionFixture120 {
 public static void main(String[] args)throws Exception {
  Class<?> unsafe=Class.forName("sun.misc.Unsafe");Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
  Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
  loader.set(null,unsafe.getMethod("allocateInstance",Class.class).invoke(singleton.get(null),cpw.mods.fml.common.Loader.class));
  net.minecraft.init.Bootstrap.func_151354_b();
  WardenAnger120 anger=new WardenAnger120();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
  anger.increase(a,70);anger.increase(b,35);require(anger.get(a)==70&&anger.get(b)==35&&anger.highest()==70,"cross-player anger transfer");
  NBTTagCompound saved=new NBTTagCompound();anger.write(saved);WardenAnger120 loaded=new WardenAnger120();loaded.read(saved);loaded.decay();
  require(loaded.get(a)==69&&loaded.get(b)==34,"per-suspect reload/decay");
  Grid world=new Grid();EntityWarden120 warden=new EntityWarden120(world);warden.setPosition(0,80,0);
  net.minecraft.entity.passive.EntityPig first=new net.minecraft.entity.passive.EntityPig(world),second=new net.minecraft.entity.passive.EntityPig(world);
  first.setPosition(4,80,0);second.setPosition(10,80,0);world.spawned.add(first);world.spawned.add(second);
  Method increase=EntityWarden120.class.getDeclaredMethod("increaseAnger",net.minecraft.entity.EntityLivingBase.class,int.class);increase.setAccessible(true);
  increase.invoke(warden,first,70);increase.invoke(warden,second,35);
  require(warden.getAttackTarget()==null,"second suspect inherited first suspect anger");
  Method sniff=EntityWarden120.class.getDeclaredMethod("sniff");sniff.setAccessible(true);sniff.invoke(warden);
  require(warden.getAttackTarget()==first,"sniff failed to find nearby silent suspect");
  require(warden.getWardenState()==EntityWarden120.ROARING,"first acquired angry target must roar");
  Method state=EntityWarden120.class.getDeclaredMethod("setState",int.class,int.class);state.setAccessible(true);state.invoke(warden,EntityWarden120.IDLE,0);
  Method refresh=EntityWarden120.class.getDeclaredMethod("refreshAnger");refresh.setAccessible(true);refresh.invoke(warden);
  require(warden.getWardenState()==EntityWarden120.IDLE,"same target repeatedly restarted roaring");
  NBTTagCompound tag=new NBTTagCompound();warden.writeEntityToNBT(tag);
  EntityWarden120 restored=new EntityWarden120(world);restored.setPosition(0,80,0);restored.readEntityFromNBT(tag);refresh.invoke(restored);
  require(restored.getAttackTarget()==first,"reload forgot suspect identity");
  Grid spawnWorld=new Grid();spawnWorld.floor=79;spawnWorld.difficultySetting=EnumDifficulty.NORMAL;
  Player player=new Player(spawnWorld,1);player.setPosition(.5,80,.5);spawnWorld.spawned.add(player);
  require(SculkWarningManager120.warn(player,0,80,0)==1,"first warning at world tick zero");
  require(SculkWarningManager120.warn(player,0,80,0)==0,"cooldown at tick zero");
  for(int i=2;i<=4;i++){spawnWorld.time+=200;require(SculkWarningManager120.warn(player,0,80,0)==i,"warning escalation");}
  require(spawnWorld.spawned.size()==1,"warn spawned before shriek completion");
  require(SculkWarningManager120.spawnWarden(spawnWorld,0,80,0),"valid floor rejected");
  EntityWarden120 summoned=(EntityWarden120)spawnWorld.spawned.get(1);
  require(summoned.getWardenState()==EntityWarden120.EMERGING&&summoned.posY==80,"spawn/emerging pose");
  require(Math.abs(summoned.posX-.5)<=5&&Math.abs(summoned.posZ-.5)<=5,"spawn search escaped horizontal radius");
  require(SculkWarningManager120.level(player)==4,"summoning reset warning level");
  spawnWorld.spawned.remove(summoned);spawnWorld.wall=AxisAlignedBB.getBoundingBox(-20,80,-20,20,84,20);
  require(!SculkWarningManager120.spawnWarden(spawnWorld,0,80,0),"spawn inside collision box");
  spawnWorld.wall=null;spawnWorld.floor=60;
  require(!SculkWarningManager120.spawnWarden(spawnWorld,0,80,0),"spawn searched outside vertical range");
  spawnWorld.floor=79;spawnWorld.getGameRules().setOrCreateGameRule("doWardenSpawning","false");
  require(!SculkWarningManager120.spawnWarden(spawnWorld,0,80,0),"doWardenSpawning ignored");
  spawnWorld.time+=12000;require(SculkWarningManager120.level(player)==3,"warning decay");
  spawnWorld.getGameRules().setOrCreateGameRule("doWardenSpawning","true");
  require(SculkWarningManager120.warn(player,0,80,0)==4,"decay incorrectly restarted cooldown");
  com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelWarden120 model=new com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelWarden120();
  require(model.body.childModels.contains(model.leftArm)&&model.head.childModels.contains(model.leftTendril),"model hierarchy");
  net.minecraft.client.model.ModelRenderer armMesh=(net.minecraft.client.model.ModelRenderer)model.leftArm.childModels.get(0);
  net.minecraft.client.model.ModelBox arm=(net.minecraft.client.model.ModelBox)armMesh.cubeList.get(0);
  require(arm.posX2-arm.posX1==8&&arm.posY2-arm.posY1==28,"official arm dimensions");
  require(model.bone.rotationPointY+model.body.rotationPointY+model.head.rotationPointY== -10,"head pivot");
  com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;
  com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
  com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;
  com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
  Method mapping=net.minecraft.tileentity.TileEntity.class.getDeclaredMethod("addMapping",Class.class,String.class);mapping.setAccessible(true);mapping.invoke(null,TileEntityBackport120SculkShrieker.class,"fixture_shrieker");
  Grid shrine=new Grid();shrine.floor=79;shrine.difficultySetting=EnumDifficulty.NORMAL;
  shrine.tile=new TileEntityBackport120SculkShrieker();shrine.tile.setWorldObj(shrine);shrine.meta=1;
  Player visitor=new Player(shrine,2);visitor.setPosition(.5,80,.5);shrine.spawned.add(visitor);
  BlockBackport120SculkShrieker shrieker=(BlockBackport120SculkShrieker)Backport120Content.sculkShrieker;
  for(int i=1;i<=4;i++){
   shrine.time=i*200;shrieker.shriek(shrine,0,80,0,visitor);
   require(shrine.delay==90&&shrine.meta==3,"shriek duration/metadata");
   require(shrine.spawned.size()==1,"early spawn during shriek");
   NBTTagCompound pending=new NBTTagCompound();shrine.tile.writeToNBT(pending);
   shrine.tile=new TileEntityBackport120SculkShrieker();shrine.tile.setWorldObj(shrine);shrine.tile.readFromNBT(pending);
   shrieker.updateTick(shrine,0,80,0,shrine.rand);
  }
  require(shrine.spawned.size()==2&&shrine.meta==1,"persisted fourth shriek failed to summon");
  Method growth=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaDeepDarkRuntime120.class.getDeclaredMethod("placeGrowth",World.class,Random.class,int.class,int.class,int.class,boolean.class);growth.setAccessible(true);
  for(boolean natural:new boolean[]{false,true}){
   int count=0;Grid patch=new Grid();patch.floor=79;patch.floorBlock=Backport120Content.sculk;
   for(int seed=0;seed<200;seed++){
    patch.placed=null;growth.invoke(null,patch,new Random(seed),0,79,0,natural);
    if(patch.placed==Backport120Content.sculkShrieker){count++;require(patch.meta==(natural?1:0),"worldgen/catalyst can_summon flag");}
   }
   require(count>0,"no shrieker generated in sampled patches");
  }
  System.out.println("Warden regression OK: separate suspects, decay, silent sniff target, no repeated roar, NBT target identity, warning cooldown/decay, bounded collision-safe spawn, gamerule, model geometry, delayed shrieker NBT, generated/catalyst shriekers");
 }
 private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static final class Player extends net.minecraft.entity.player.EntityPlayer {
        Player(World world,int id){super(world,new com.mojang.authlib.GameProfile(new UUID(1202,id),"WardenTest"+id));}
        @Override public void addChatMessage(IChatComponent message){}
        @Override public boolean canCommandSenderUseCommand(int level,String command){return false;}
        @Override public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,80,0);}
    }
    private static final class Grid extends World {
        AxisAlignedBB wall;boolean hidden;int floor=-1;long time;Block floorBlock=Blocks.stone,placed;int meta,delay;TileEntityBackport120SculkShrieker tile;
        @Override public net.minecraft.tileentity.TileEntity getTileEntity(int x,int y,int z){return tile;}
        @Override public boolean setBlockMetadataWithNotify(int x,int y,int z,int value,int flags){meta=value;return true;}
        @Override public boolean setBlock(int x,int y,int z,Block block,int value,int flags){placed=block;meta=value;return true;}
        @Override public void scheduleBlockUpdate(int x,int y,int z,Block block,int ticks){delay=ticks;}
        @Override public void playSoundEffect(double x,double y,double z,String sound,float volume,float pitch){}
        @Override public long getTotalWorldTime(){return time;}
        @Override public boolean checkNoEntityCollision(AxisAlignedBB box){return true;}
        @Override public void playSoundAtEntity(Entity entity,String sound,float volume,float pitch){}
        final List<Entity> spawned=new ArrayList<Entity>();
        Grid(){super(null,"phantom fixture",new WorldProviderSurface(),new WorldSettings(1202,WorldSettings.GameType.SURVIVAL,true,false,WorldType.DEFAULT),new net.minecraft.profiler.Profiler());provider.registerWorld(this);chunkProvider=createChunkProvider();}
        @Override public Block getBlock(int x,int y,int z){return y==floor?floorBlock:Blocks.air;}
        @Override public int getBlockMetadata(int x,int y,int z){return meta;}
        @Override public boolean checkChunksExist(int x,int y,int z,int xx,int yy,int zz){return true;}
        @Override public boolean blockExists(int x,int y,int z){return true;}
        @Override public List getCollidingBoundingBoxes(Entity entity,AxisAlignedBB box){
            List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();
            if(wall!=null&&wall.intersectsWith(box))boxes.add(wall);
            return boxes;
        }
        @Override public MovingObjectPosition rayTraceBlocks(Vec3 from,Vec3 to){return hidden?new MovingObjectPosition(0,80,0,0,from):null;}
        @Override public boolean isAnyLiquid(AxisAlignedBB box){return false;}
        @Override public boolean canBlockSeeTheSky(int x,int y,int z){return true;}
        @Override public boolean spawnEntityInWorld(Entity entity){spawned.add(entity);return true;}
        @Override public List getEntitiesWithinAABB(Class type,AxisAlignedBB box){
            List<Entity> result=new ArrayList<Entity>();
            for(Entity entity:spawned)if(type.isInstance(entity)&&entity.boundingBox.intersectsWith(box))result.add(entity);
            return result;
        }
        @Override protected int func_152379_p(){return 0;}
        @Override public Entity getEntityByID(int id){return null;}
        @Override protected IChunkProvider createChunkProvider(){
            return (IChunkProvider)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{IChunkProvider.class},(proxy,method,args)->{
                if(method.getName().equals("chunkExists"))return true;
                if(method.getName().equals("provideChunk"))return new Chunk(this,(Integer)args[0],(Integer)args[1]);
                if(method.getReturnType()==boolean.class)return false;
                if(method.getReturnType()==int.class)return 0;
                return null;
            });
        }
    }
}
