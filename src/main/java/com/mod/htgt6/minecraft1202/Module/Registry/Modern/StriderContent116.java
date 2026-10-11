package com.mod.htgt6.minecraft1202.Module.Registry.Modern;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemWarpedFungusStick116;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.*;
import net.minecraft.init.Items;
public final class StriderContent116 {
    public static final ItemWarpedFungusStick116 fungusStick=new ItemWarpedFungusStick116();
    private static boolean registered;
    public static void register(){if(registered)return;GameRegistry.registerItem(fungusStick,"backport_1_20_warped_fungus_on_a_stick");GameRegistry.addRecipe(new ItemStack(fungusStick),"R "," F",'R',new ItemStack(Items.fishing_rod,1,32767),'F',NetherVegetationContent116.warpedFungus);com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.AnimalDispenser116.register();registered=true;}
    private StriderContent116(){}
}
