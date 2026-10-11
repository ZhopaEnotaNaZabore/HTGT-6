import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import java.lang.reflect.Method;

public final class LlamaSpit94Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        java.lang.reflect.Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);
        profiler.set(world,new net.minecraft.profiler.Profiler());
        EntityLlama112 owner = new EntityLlama112(world);
        owner.setPosition(10, 20, 30);
        for (int yaw = 0; yaw < 360; yaw += 90) {
            owner.renderYawOffset = yaw;
            owner.rotationYaw = yaw + 45; // Body yaw, not head yaw, determines muzzle offset.
            EntityLlamaSpit112 spit = new EntityLlamaSpit112(world, owner);
            double reach = (owner.width + 1F) * .5;
            near(spit.posX, owner.posX - reach * Math.sin(Math.toRadians(yaw)), "muzzle X");
            near(spit.posY, owner.posY + owner.getEyeHeight() - .1, "muzzle Y");
            near(spit.posZ, owner.posZ + reach * Math.cos(Math.toRadians(yaw)), "muzzle Z");
            check(spit.getThrower() == owner, "owner retained");
        }
        Method impact = EntityLlamaSpit112.class.getDeclaredMethod("onImpact", MovingObjectPosition.class);
        impact.setAccessible(true);
        Target target = new Target(world);
        EntityLlamaSpit112 spit = new EntityLlamaSpit112(world, owner);
        impact.invoke(spit, new MovingObjectPosition(target));
        check(target.hits == 1 && target.damage == 1 && target.source.getEntity() == owner && !spit.isDead,
                "entity damage is attributed and does not discard spit");
        impact.invoke(new EntityLlamaSpit112(world), new MovingObjectPosition(target));
        check(target.hits == 1, "orphan does not deal damage");
        world.isRemote = true;
        impact.invoke(spit, new MovingObjectPosition(target));
        check(target.hits == 1 && !spit.isDead, "client cannot apply impact damage");
        world.isRemote = false;
        impact.invoke(spit, new MovingObjectPosition(0, 0, 0, 1, Vec3.createVectorHelper(0, 0, 0)));
        check(spit.isDead, "server block hit discards");
        for (boolean client : new boolean[]{false, true}) {
            world.isRemote = client;
            world.setBlock(0, 10, 0, Blocks.water, 0, 3);
            EntityLlamaSpit112 wet = new EntityLlamaSpit112(world);
            wet.setPosition(.5, 10.2, .5);
            wet.onUpdate();
            check(wet.isDead, "water discards on both sides");
        }
        System.out.println("PASS: four muzzle directions, owner, entity/block impacts, orphan/client guards, water removal");
    }
    static class Target extends Entity {
        int hits; float damage; DamageSource source;
        Target(World world) { super(world); }
        protected void entityInit() { }
        protected void readEntityFromNBT(NBTTagCompound n) { }
        protected void writeEntityToNBT(NBTTagCompound n) { }
        public boolean attackEntityFrom(DamageSource source, float amount) {
            hits++; damage=amount; this.source=source; return true;
        }
    }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static void near(double value, double expected, String message) { check(Math.abs(value-expected)<.00001, message); }
}
