package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ModelCombatShulker;
import java.lang.reflect.Method;
import net.minecraft.client.model.ModelBox;

/** Exercises the actual watcher, tick interpolation and model pose without an OpenGL context. */
public final class ShulkerAnimationFixture120 {
    public static void main(String[] args) throws Exception {
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid world = com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
        world.isRemote=true;
        EntityCombatShulker entity = new EntityCombatShulker(world);
        Method tick = EntityCombatShulker.class.getDeclaredMethod("updatePeekAmount");
        tick.setAccessible(true);
        ModelCombatShulker model = new ModelCombatShulker();
        pose(model, entity, 0);
        close(model.lid.rotationPointY, 24, "closed lid pivot");
        close(((ModelBox) model.lid.cubeList.get(0)).posY1, -16, "reference lid geometry");
        close(((ModelBox) model.head.cubeList.get(0)).posY1, 0, "reference head geometry");
        entity.getDataWatcher().updateObject(21, Byte.valueOf((byte) 100));
        tick.invoke(entity);
        close(entity.getPeekAmount(), 1, "authoritative target immediately open");
        close(entity.getClientPeekAmount(0), 0, "previous frame stays closed");
        close(entity.getClientPeekAmount(0.5F), 0.025F, "partial tick interpolation");
        close(entity.getClientPeekAmount(1), 0.05F, "first opening step");
        for (int i = 1; i < 20; i++) tick.invoke(entity);
        close(entity.getClientPeekAmount(1), 1, "fully open after twenty ticks");
        tick.invoke(entity);
        pose(model, entity, 0);
        close(model.lid.rotationPointY, 8, "open lid must not return to closed position");
        close(model.lid.rotateAngleY, (float) Math.PI * 2, "full reference lid turn");
        close(model.head.rotationPointY, 12, "head does not rise with lid");
        entity.ticksExisted = 10;
        pose(model, entity, 10);
        close(model.lid.rotationPointY, 8 + net.minecraft.util.MathHelper.sin(1) * 0.7F, "open lid wobble");
        entity.getDataWatcher().updateObject(21, Byte.valueOf((byte) 0));
        tick.invoke(entity);
        close(entity.getClientPeekAmount(0.5F), 0.975F, "smooth reverse after watcher update");
        for (int i = 1; i < 20; i++) tick.invoke(entity);
        close(entity.getClientPeekAmount(1), 0, "closes without undershooting");
        entity.getDataWatcher().updateObject(21, Byte.valueOf((byte) 30));
        for (int i = 0; i < 8; i++) tick.invoke(entity);
        close(entity.getClientPeekAmount(1), 0.3F, "intermediate network target");
        pose(model, entity, 10);
        close(model.lid.rotateAngleY, 0, "no turn at peek threshold");
        System.out.println("ShulkerAnimationFixture120: OK");
    }
    private static void pose(ModelCombatShulker model, EntityCombatShulker entity, float age) {
        model.setRotationAngles(0, 0, age, 0, 0, 0.0625F, entity);
    }
    private static void close(float actual, float expected, String label) {
        if (Math.abs(actual - expected) > 0.0001F) throw new AssertionError(label + ": " + actual + " != " + expected);
    }
}
