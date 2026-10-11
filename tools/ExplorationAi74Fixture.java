import java.lang.reflect.Method;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.profiler.Profiler;

/** Runs the actual ported spell goals together in the Forge 1.7.10 scheduler. */
public final class ExplorationAi74Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        Method enabled = EntityLiving.class.getDeclaredMethod("isAIEnabled");
        enabled.setAccessible(true);
        for (EntityLiving mob : new EntityLiving[] {new EntityLlama112(world), new EntityEvoker112(world),
                new EntityVindicator112(world), new EntityVex112(world)}) {
            require((Boolean) enabled.invoke(mob), mob.getClass().getSimpleName() + " AI disabled");
            require(!mob.tasks.taskEntries.isEmpty() && !mob.targetTasks.taskEntries.isEmpty(), "missing goals");
        }

        EntityEvoker112 evoker = new EntityEvoker112(world);
        EntitySheep target = new EntitySheep(world);
        target.setPosition(8, 1, 0);
        evoker.setAttackTarget(target);
        EntityAITasks summon = spellTasks(evoker, "SummonSpellAI");
        for (int tick = 0; tick < 19; tick++) summon.onUpdateTasks();
        require(world.lastSpawn == null, "summoned before warmup");
        summon.onUpdateTasks();
        require(world.lastSpawn instanceof EntityVex112, "casting pose interrupted summon warmup");
        Object summoned = world.lastSpawn;
        for (int tick = 0; tick < 40; tick++) summon.onUpdateTasks();
        require(world.lastSpawn == summoned, "spell repeated during cooldown");

        world.lastSpawn = null;
        EntityEvoker112 cancelled = new EntityEvoker112(world);
        cancelled.setAttackTarget(target);
        EntityAITasks interrupted = spellTasks(cancelled, "SummonSpellAI");
        interrupted.onUpdateTasks();
        target.setDead();
        for (int tick = 0; tick < 25; tick++) interrupted.onUpdateTasks();
        require(world.lastSpawn == null, "spell continued against dead target");

        EntityEvoker112 wololo = new EntityEvoker112(world);
        EntitySheep blue = new EntitySheep(world);
        blue.setPosition(1, 0, 0);
        blue.setFleeceColor(11);
        world.loadedEntityList.add(blue);
        EntityAITasks dye = spellTasks(wololo, "WololoSpellAI");
        for (int tick = 0; tick < 39; tick++) dye.onUpdateTasks();
        require(blue.getFleeceColor() == 11, "wololo before warmup");
        dye.onUpdateTasks();
        require(blue.getFleeceColor() == 14, "casting pose interrupted wololo");
        System.out.println("PASS: four AI opt-ins; real scheduler summon/wololo warmups, cooldown and dead-target cancellation");
    }

    private static EntityAITasks spellTasks(EntityEvoker112 evoker, String spell) {
        EntityAITasks scheduler = new EntityAITasks(new Profiler());
        int added = 0;
        for (Object value : evoker.tasks.taskEntries) {
            EntityAITasks.EntityAITaskEntry entry = (EntityAITasks.EntityAITaskEntry) value;
            String name = entry.action.getClass().getSimpleName();
            if (name.equals("CastingAI") || name.equals(spell)) {
                scheduler.addTask(entry.priority, entry.action);
                added++;
            }
        }
        require(added == 2, "spell/pose goals missing");
        return scheduler;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
