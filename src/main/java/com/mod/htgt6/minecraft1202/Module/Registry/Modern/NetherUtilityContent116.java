package com.mod.htgt6.minecraft1202.Module.Registry.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockRespawnAnchor116;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SoulSpeed116;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;

/** Utility content is owned by Backport, independently of the Nether world generator. */
public final class NetherUtilityContent116 {
    public static final BlockRespawnAnchor116 anchor=new BlockRespawnAnchor116();
    public static final com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149 soulCampfire=new com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149(true);
    private static boolean registered;
    public static void register(){
        if(registered)return;SoulSpeed116.init();SoulSpeed116.registerSurface(EtFVanillaGapContent120.soulSoil);
        GameRegistry.registerBlock(soulCampfire,com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockCampfire149.class,"backport_1_20_soul_campfire");
        for(net.minecraft.block.Block base:new net.minecraft.block.Block[]{Blocks.soul_sand,EtFVanillaGapContent120.soulSoil})
            GameRegistry.addRecipe(new net.minecraftforge.oredict.ShapedOreRecipe(new ItemStack(soulCampfire)," S ","SBS","LLL",
                'S',net.minecraft.init.Items.stick,'B',base,'L',"logWood"));
        GameRegistry.registerBlock(anchor,"backport_1_20_respawn_anchor");
        GameRegistry.addRecipe(new ItemStack(anchor),"OOO","GGG","OOO",'O',EtFVanillaGapContent120.cryingObsidian,'G',Blocks.glowstone);
        final Item item=Item.getItemFromBlock(Blocks.glowstone);
        final IBehaviorDispenseItem previous=(IBehaviorDispenseItem)BlockDispenser.dispenseBehaviorRegistry.getObject(item);
        BlockDispenser.dispenseBehaviorRegistry.putObject(item,new IBehaviorDispenseItem(){
            public ItemStack dispense(IBlockSource source,ItemStack stack){
                EnumFacing facing=BlockDispenser.func_149937_b(source.getBlockMetadata());
                int x=source.getXInt()+facing.getFrontOffsetX(),y=source.getYInt()+facing.getFrontOffsetY(),z=source.getZInt()+facing.getFrontOffsetZ();
                if(stack.stackSize>0&&anchor.charge(source.getWorld(),x,y,z)){if(!source.getWorld().isRemote)stack.stackSize--;return stack;}
                return previous.dispense(source,stack);
            }
        });
        MinecraftForge.EVENT_BUS.register(new SoulSpeed116());MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.AnchorExplosion116());registered=true;
    }
    private NetherUtilityContent116(){}
}
