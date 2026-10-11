package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PlayerItemUse120;
import java.lang.reflect.Field;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

/** Prevent client-only player API calls from re-entering shared shield code. */
public final class ShieldServerApiFixture120 {
    public static void main(String[] args) throws Exception {
        verifySharedCalls();
        Class<?> unsafeClass=Class.forName("sun.misc.Unsafe");
        Field singleton=unsafeClass.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        EntityPlayer player=(EntityPlayer)unsafeClass.getMethod("allocateInstance",Class.class)
                .invoke(singleton.get(null),EntityPlayerMP.class);
        require(PlayerItemUse120.item(player)==null && PlayerItemUse120.duration(player)==0,"idle player");
        Field item=cpw.mods.fml.relauncher.ReflectionHelper.findField(EntityPlayer.class,"itemInUse","field_71074_e");
        Field count=cpw.mods.fml.relauncher.ReflectionHelper.findField(EntityPlayer.class,"itemInUseCount","field_71072_f");
        ItemStack bow=new ItemStack(new net.minecraft.item.ItemBow());item.set(player,bow);
        for(int ticks:new int[]{0,4,5,20}) {
            count.setInt(player,bow.getMaxItemUseDuration()-ticks);
            require(PlayerItemUse120.item(player)==bow && PlayerItemUse120.duration(player)==ticks,"use duration "+ticks);
        }
        item.set(player,null);require(PlayerItemUse120.duration(player)==0,"cleared item ignores old timer");
        System.out.println("Shield shared API OK: no client-only calls; idle/active/cleared state and 4/5-tick boundary");
    }
    public static void verifySharedCalls() throws Exception {
        String root="com/mod/htgt6/minecraft1202/Module/";
        for(String path:new String[]{"Gameplay/Combat/PlayerItemUse120","Gameplay/Combat/ShieldMechanics120","Events/Combat/CombatEventHandler"}) {
            ClassNode node=new ClassNode();
            java.io.InputStream stream=ShieldServerApiFixture120.class.getClassLoader().getResourceAsStream(root+path+".class");
            require(stream!=null,"missing shared class "+path);
            try { new ClassReader(stream).accept(node,0); } finally { stream.close(); }
            for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
                if(instruction instanceof MethodInsnNode) {
                    String name=((MethodInsnNode)instruction).name;
                    require(!name.equals("getItemInUse")&&!name.equals("getItemInUseCount")&&!name.equals("getItemInUseDuration")
                            &&!name.equals("func_71011_bu")&&!name.equals("func_71052_bv")&&!name.equals("func_71057_bx"),
                            "client-only player call in "+path+"."+method.name+": "+name);
                }
        }
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
