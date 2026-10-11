import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import java.lang.reflect.*;

public final class LlamaFlight95Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(world,new net.minecraft.profiler.Profiler());
        EntityLlama112 owner=new EntityLlama112(world);owner.setPosition(0,10,0);world.loadedEntityList.add(owner);
        EntityLlamaSpit112 spit=new EntityLlamaSpit112(world,owner);
        spit.setPosition(0,10,0);spit.motionX=0;spit.motionY=0;spit.motionZ=0;
        Method leave=EntityLlamaSpit112.class.getDeclaredMethod("hasLeftOwner");leave.setAccessible(true);
        Method hit=EntityLlamaSpit112.class.getDeclaredMethod("findHit");hit.setAccessible(true);
        Field left=EntityLlamaSpit112.class.getDeclaredField("leftOwner");left.setAccessible(true);
        check(!(Boolean)leave.invoke(spit),"owner excluded while near, independent of age");
        spit.ticksExisted=100;
        check(hit.invoke(spit)==null,"old five-tick timeout removed");
        Target rider=new Target(world);rider.setPosition(2,10,0);rider.ridingEntity=owner;world.loadedEntityList.add(rider);
        spit.setPosition(2,10,0);
        check(!(Boolean)leave.invoke(spit),"passenger keeps owner group exclusion");
        spit.setPosition(20,10,0);check((Boolean)leave.invoke(spit),"leaves owner group");
        left.setBoolean(spit,true);
        NBTTagCompound tag=new NBTTagCompound();spit.writeEntityToNBT(tag);
        EntityLlamaSpit112 loaded=new EntityLlamaSpit112(world);loaded.readEntityFromNBT(tag);
        check(loaded.getThrower()==owner&&left.getBoolean(loaded),"UUID owner and leftOwner NBT roundtrip");
        owner.isDead=true;check(loaded.getThrower()==null,"dead owner not resolved");owner.isDead=false;
        check(loaded.getThrower()==owner,"owner re-resolves when loaded");

        world.loadedEntityList.clear();
        Target far=new Target(world);far.setPosition(4,10,0);
        Target near=new Target(world);near.setPosition(2,10,0);
        world.loadedEntityList.add(far);world.loadedEntityList.add(near);
        spit.setPosition(0,10,0);spit.motionX=6;spit.motionY=0;spit.motionZ=0;
        check(((MovingObjectPosition)hit.invoke(spit)).entityHit==near,"nearest hit independent of iteration order");
        near.noClip=true;
        check(((MovingObjectPosition)hit.invoke(spit)).entityHit==far,"noClip target excluded");near.noClip=false;
        world.rayHit=new MovingObjectPosition(1,10,0,4,Vec3.createVectorHelper(1,10,0));
        check(((MovingObjectPosition)hit.invoke(spit)).entityHit==null,"block occludes entities");world.rayHit=null;
        world.loadedEntityList.clear();
        spit.setPosition(0,10,0);spit.motionX=1;spit.motionY=.5;spit.motionZ=-.25;
        spit.onUpdate();
        close(spit.posX,1,"movement uses old X velocity");close(spit.posY,10.5,"movement uses old Y velocity");
        close(spit.motionX,.99F,"air drag");close(spit.motionY,.5*.99F-.06F,"gravity after drag");
        close(spit.motionZ,-.25*.99F,"Z drag");
        EntityLlamaSpit112 solid=new EntityLlamaSpit112(world);solid.setPosition(.5,10.2,.5);
        world.setBlock(0,10,0,Blocks.stone,0,3);solid.onUpdate();check(solid.isDead,"all non-air blocks discard");
        world.setBlockToAir(0,10,0);
        EntityLlamaSpit112 edge=new EntityLlamaSpit112(world);edge.setPosition(.95,10.2,.5);
        world.setBlock(0,10,0,Blocks.stone,0,3);edge.onUpdate();check(!edge.isDead,"partly in air survives");
        System.out.println("PASS: owner group, >5 ticks, UUID/leftOwner persistence, closest entity, wall, noClip, drag/gravity/order, enclosed/partial air");
    }
    static class Target extends LlamaSpit94Fixture.Target {
        Target(World w){super(w);setSize(.6F,1.8F);}
        public boolean canBeCollidedWith(){return true;}
    }
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void close(double value,double expected,String message){check(Math.abs(value-expected)<.00001,message+": "+value);}
}
