package com.mod.htgt6.asm;

import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.BoatMotion120;
import net.minecraft.entity.item.EntityBoat;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.*;

public final class BoatMotion79Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        EntityCombatBoat modern=new EntityCombatBoat(world);
        EntityBoat legacy=new EntityBoat(world);
        modern.isCollidedHorizontally=legacy.isCollidedHorizontally=true;
        check(!BoatMotion120.breakOnCollision(modern),"ported boat collision destruction");
        check(modern.isCollidedHorizontally,"collision state erased");
        check(BoatMotion120.breakOnCollision(legacy),"legacy behavior changed");
        for(int yaw=-180;yaw<180;yaw+=45){
            double r=Math.toRadians(yaw),dx=-Math.sin(r),dz=Math.cos(r);
            double old=Math.atan2(-dz,-dx),actual=BoatMotion120.heading(old,modern);
            check(Math.abs(Math.sin(actual)+dx)<1e-6&&Math.abs(Math.cos(actual)-dz)<1e-6,"heading "+yaw);
            check(BoatMotion120.heading(old,legacy)==old,"legacy heading changed");
        }
        InputStream in=EntityBoat.class.getResourceAsStream("EntityBoat.class");
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
        while((n=in.read(buf))>=0)out.write(buf,0,n);in.close();
        final byte[] transformed=CombatBoatMotionASM.transform(out.toByteArray());
        Class<?> verified=new ClassLoader(BoatMotion79Fixture.class.getClassLoader()) {
            Class<?> loadBoat(){return defineClass("net.minecraft.entity.item.EntityBoat",transformed,0,transformed.length);}
        }.loadBoat();
        verified.getDeclaredMethods(); // JVM verification of the modified method bodies
        ClassNode node=new ClassNode();new ClassReader(transformed).accept(node,0);
        int hooks=0;
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
            if(instruction instanceof MethodInsnNode&&((MethodInsnNode)instruction).owner.endsWith("/BoatMotion120"))hooks++;
        check(hooks==2,"missing injected hooks");
        System.out.println("PASS: 8 headings, collision survives with collision flag intact, vanilla unchanged, transformed EntityBoat JVM verification");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
