package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;

/** Solid wood without Material.wood's legacy burnability. */
public final class NetherWoodMaterial116 {
    private static final java.util.Set<net.minecraft.item.Item> NON_FUEL=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<net.minecraft.item.Item,Boolean>());
    public static void markNonFuel(net.minecraft.item.Item item){if(item!=null)NON_FUEL.add(item);}
    public static int fuelTime(int original,net.minecraft.item.ItemStack stack){return stack!=null&&NON_FUEL.contains(stack.getItem())?0:original;}
    public static final Material MATERIAL=new Material(MapColor.woodColor);
    public static boolean matches(String name){return name!=null&&(name.startsWith("crimson_")||name.startsWith("warped_")||name.startsWith("stripped_crimson_")||name.startsWith("stripped_warped_"));}
    public static Material forName(String name){return matches(name)?MATERIAL:Material.wood;}
    private NetherWoodMaterial116(){}
}
