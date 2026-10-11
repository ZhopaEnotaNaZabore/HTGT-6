package com.mod.htgt6.common.turbine;

import com.mod.htgt6.common.multiblock.BlockMultiblockTrim;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public final class SteamTurbineContent {
    public static final Block controller=new BlockSteamTurbineController();
    public static final Block casing=new BlockMultiblockTrim("steam_turbine_casing","hullUlv");
    public static final Block rib=new BlockMultiblockTrim("steam_turbine_rib","hull_advanced_side");
    private SteamTurbineContent() {}
    public static void register() {
        GameRegistry.registerBlock(controller,SteamTurbineItem.class,"steam_turbine");
        GameRegistry.registerBlock(casing,"steam_turbine_casing");
        GameRegistry.registerBlock(rib,"steam_turbine_rib");
        GameRegistry.registerTileEntity(MultiTileSteamTurbine.class,"htgt6:steam_turbine");
    }
    public static final class SteamTurbineItem extends net.minecraft.item.ItemBlock {
        public SteamTurbineItem(Block block){super(block);}
        @Override @SuppressWarnings("unchecked")
        public void addInformation(net.minecraft.item.ItemStack stack,net.minecraft.entity.player.EntityPlayer player,java.util.List lines,boolean advanced){
            lines.add(net.minecraft.util.StatCollector.translateToLocal("tooltip.htgt6.steam_turbine.structure"));
            lines.add(net.minecraft.util.StatCollector.translateToLocal("tooltip.htgt6.steam_turbine.ports"));
            lines.add(net.minecraft.util.StatCollector.translateToLocal("tooltip.htgt6.steam_turbine.rotor"));
        }
    }
}
