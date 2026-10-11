package com.mod.htgt6.common.multiblock.glass;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** One registered block/item shared by every multiblock. */
public final class ObservationGlassContent {
    public static final Block glass = new UniversalGlass(false);
    public static final Block[] legacy = new Block[7];
    private static final String[] LEGACY_NAMES = {"chemical_plant_glass", "electronic_factory_glass",
            "industrial_mixer_glass", "industrial_centrifuge_glass", "super_massive_compressor_glass",
            "fluid_solidifier_glass", "formed_mining_unit_glass"};
    private ObservationGlassContent() {}
    public static void register() {
        GameRegistry.registerBlock(glass, "unbreakable_glass");
        // Forge 1.7.10 cannot safely remap several numeric block IDs onto one object.
        for (int i=0;i<legacy.length;i++) {
            legacy[i] = new UniversalGlass(true);
            GameRegistry.registerBlock(legacy[i], LegacyGlassItem.class, LEGACY_NAMES[i]);
            GameRegistry.addShapelessRecipe(new ItemStack(glass), new ItemStack(legacy[i],1,32767));
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ObservationGlassMigration());
    }

    public static final class LegacyGlassItem extends net.minecraft.item.ItemBlock {
        public LegacyGlassItem(Block block) { super(block); }
        @Override public boolean placeBlockAt(ItemStack stack, net.minecraft.entity.player.EntityPlayer player,
                net.minecraft.world.World world, int x, int y, int z, int side,
                float hitX, float hitY, float hitZ, int metadata) {
            return world.setBlock(x,y,z,glass,0,3);
        }
        @Override @SideOnly(Side.CLIENT) @SuppressWarnings("rawtypes")
        public void getSubItems(Item item, CreativeTabs tab, List list) { }
    }

    private static final class UniversalGlass extends BlockObservationGlass {
        private final boolean legacy;
        private UniversalGlass(boolean legacy) {
            super(true);
            this.legacy = legacy;
            setBlockName("unbreakable_glass");
            setHardness(6F);
            setResistance(30F);
            setLightOpacity(0);
            setCreativeTab(legacy ? null : CreativeTabs.tabRedstone);
        }
        @Override public int damageDropped(int metadata) { return 0; }
        @Override public int quantityDropped(java.util.Random random) { return 1; }
        @Override public Item getItemDropped(int metadata, java.util.Random random, int fortune) {
            return Item.getItemFromBlock(glass);
        }
        @Override public ItemStack getPickBlock(net.minecraft.util.MovingObjectPosition target, net.minecraft.world.World world,
                int x,int y,int z) { return new ItemStack(glass); }
        @Override @SideOnly(Side.CLIENT)
        public int colorMultiplier(net.minecraft.world.IBlockAccess world, int x, int y, int z) { return 0xB8CDD4; }
        @Override @SideOnly(Side.CLIENT) public int getRenderColor(int metadata) { return 0xB8CDD4; }
        @Override @SideOnly(Side.CLIENT) @SuppressWarnings("unchecked")
        public void getSubBlocks(Item item, CreativeTabs tab, List list) { if (!legacy) list.add(new ItemStack(item, 1, 0)); }
    }
}
