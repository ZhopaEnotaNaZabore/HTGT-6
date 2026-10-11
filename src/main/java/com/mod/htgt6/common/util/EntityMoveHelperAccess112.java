package com.mod.htgt6.common.util;
import cpw.mods.fml.relauncher.ReflectionHelper;import java.lang.reflect.Field;import net.minecraft.entity.EntityLiving;import net.minecraft.entity.ai.EntityMoveHelper;
/** Installs a 1.12-style MoveHelper into the private 1.7 EntityLiving slot. */
public final class EntityMoveHelperAccess112{
 private static final Field FIELD=ReflectionHelper.findField(EntityLiving.class,"moveHelper","field_70765_h");
 public static void install(EntityLiving entity,EntityMoveHelper helper){try{FIELD.set(entity,helper);}catch(IllegalAccessException e){throw new IllegalStateException("Cannot install entity move helper",e);}}
 private EntityMoveHelperAccess112(){}
}
