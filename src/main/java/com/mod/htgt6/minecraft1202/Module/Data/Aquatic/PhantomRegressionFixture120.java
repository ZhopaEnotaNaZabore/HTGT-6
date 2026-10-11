package com.mod.htgt6.minecraft1202.Module.Data.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityPhantom120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.PhantomSpawnHandler120;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;

/** Regression scenarios: real entity collision/flight, target loss, and independent spawn statistics. */
public final class PhantomRegressionFixture120 {
    public static void main(String[] args) throws Exception {
        // Same standalone Forge bootstrap as the existing world/structure fixtures.
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        java.lang.reflect.Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,unsafe.getMethod("allocateInstance",Class.class).invoke(singleton.get(null),cpw.mods.fml.common.Loader.class));
        net.minecraft.init.Bootstrap.func_151354_b();
        Grid world=new Grid();
        EntityPhantom120 phantom=new EntityPhantom120(world);
        phantom.setPosition(0,80,0);
        require(!phantom.noClip,"phantoms must collide with blocks");
        world.wall=AxisAlignedBB.getBoundingBox(2,0,-100,3,256,100);
        phantom.motionX=5; phantom.motionY=phantom.motionZ=0;
        phantom.moveEntityWithHeading(0,0);
        require(phantom.boundingBox.maxX<=2.000001,"high-speed motion crossed a solid wall");
        require(phantom.isCollidedHorizontally,"wall collision was not reported");

        world.wall=null;
        phantom.setPosition(0,80,0);phantom.motionX=phantom.motionY=phantom.motionZ=0;
        Method ai=EntityPhantom120.class.getDeclaredMethod("updateAITasks");ai.setAccessible(true);
        phantom.func_110163_bv(); // Do not let the fixture's playerless world despawn the subject.
        double travelled=0;
        for(int i=0;i<240;i++) {
            double x=phantom.posX,z=phantom.posZ;
            ai.invoke(phantom);phantom.moveEntityWithHeading(0,0);
            travelled+=Math.hypot(phantom.posX-x,phantom.posZ-z);
        }
        require(travelled>20,"targetless phantom hovered instead of circling");
        EntityPhantom120 hidden=new EntityPhantom120(world);hidden.setPosition(phantom.posX+5,phantom.posY,phantom.posZ);
        phantom.setAttackTarget(hidden);world.hidden=true;
        ai.invoke(phantom);
        require(phantom.getAttackTarget()==null,"occluded target retained for wall-diving");
        require(Math.hypot(phantom.motionX,phantom.motionZ)>.01,"lost target stopped flight");
        world.hidden=false;
        NBTTagCompound save=new NBTTagCompound();phantom.writeEntityToNBT(save);
        phantom.noClip=true;phantom.readEntityFromNBT(save);
        require(!phantom.noClip&&save.hasKey("AX"),"reload lost collision or circle anchor");

        Random random=new Random(1202);
        for(int i=0;i<10000;i++) {
            require(!PhantomSpawnHandler120.insomniaRoll(random,72000),"spawn before three complete days");
            int delay=PhantomSpawnHandler120.nextDelay(random);
            require(delay>=1200&&delay<=2380&&delay%20==0,"attempt delay outside 60..119 seconds");
            for(int difficulty=1;difficulty<=3;difficulty++) {
                int count=PhantomSpawnHandler120.groupSize(random,difficulty);
                require(count>=1&&count<=difficulty+1,"group size independent of difficulty");
            }
        }
        int shortlyAfter=0,twice=0;
        for(int i=0;i<100000;i++) {
            if(PhantomSpawnHandler120.insomniaRoll(random,73000))shortlyAfter++;
            if(PhantomSpawnHandler120.insomniaRoll(random,144000))twice++;
        }
        require(shortlyAfter>1100&&shortlyAfter<1650,"expected ~1.37% insomnia gate at 73000 ticks");
        require(twice>49000&&twice<51000,"expected 50% insomnia gate at six days");
        require(PhantomSpawnHandler120.nextRest(Integer.MAX_VALUE,false)==Integer.MAX_VALUE,"rest overflow");
        require(PhantomSpawnHandler120.nextRest(90000,true)==0,"sleep/death must reset insomnia");
        require(PhantomSpawnHandler120.localDifficulty(0,9999999,9999999,1)==0,"peaceful local difficulty");
        require(PhantomSpawnHandler120.localDifficulty(2,0,0,0)==1.5F,"new normal world difficulty");
        require(PhantomSpawnHandler120.localDifficulty(3,9999999,9999999,1)==6.75F,"max hard local difficulty");
        world.difficultySetting=EnumDifficulty.HARD;world.skylightSubtracted=7;world.setWorldTime(9999999);
        Player first=new Player(world,1),second=new Player(world,2);
        for(Player player:new Player[]{first,second}) {
            player.setPosition(0,80,0);player.getEntityData().setInteger(PhantomSpawnHandler120.REST_KEY,Integer.MAX_VALUE);
            world.playerEntities.add(player);
        }
        cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent tick=new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(
                cpw.mods.fml.relauncher.Side.SERVER,cpw.mods.fml.common.gameevent.TickEvent.Phase.END,world);
        // Force independent attempts; actual scheduler can only make these less frequent.
        for(int i=0;i<20;i++)new PhantomSpawnHandler120().worldTick(tick);
        require(world.spawned.size()==4,"overlapping sleepless players exceeded/shared no local hard cap");
        world.spawned.clear();first.capabilities.disableDamage=second.capabilities.disableDamage=true;
        for(int i=0;i<20;i++)new PhantomSpawnHandler120().worldTick(tick);
        require(world.spawned.isEmpty(),"creative players spawned phantoms");
        first.capabilities.disableDamage=second.capabilities.disableDamage=false;
        world.getGameRules().setOrCreateGameRule("doInsomnia","false");
        for(int i=0;i<20;i++)new PhantomSpawnHandler120().worldTick(tick);
        require(world.spawned.isEmpty(),"doInsomnia=false ignored");
        System.out.println("Phantom regression OK: swept wall collision, 240 targetless flight ticks, hidden target, reload, insomnia probabilities and difficulty groups");
    }
    private static void require(boolean value,String why){if(!value)throw new AssertionError(why);}
    private static final class Player extends net.minecraft.entity.player.EntityPlayer {
        Player(World world,int id){super(world,new com.mojang.authlib.GameProfile(new UUID(1202,id),"PhantomTest"+id));}
        @Override public void addChatMessage(IChatComponent message){}
        @Override public boolean canCommandSenderUseCommand(int level,String command){return false;}
        @Override public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,80,0);}
    }
    private static final class Grid extends World {
        AxisAlignedBB wall;boolean hidden;
        final List<Entity> spawned=new ArrayList<Entity>();
        Grid(){super(null,"phantom fixture",new WorldProviderSurface(),new WorldSettings(1202,WorldSettings.GameType.SURVIVAL,true,false,WorldType.DEFAULT),new net.minecraft.profiler.Profiler());provider.registerWorld(this);chunkProvider=createChunkProvider();}
        @Override public Block getBlock(int x,int y,int z){return Blocks.air;}
        @Override public int getBlockMetadata(int x,int y,int z){return 0;}
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
