package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/** Per-world geological palettes; one shared saltwater fluid for every ocean. */
public final class SolSysGeology {
    public enum Profile {
        CHARON("Charon", 0xD0D9E4, 0.04, 22, 640),
        PROTEUS("Proteus", 0x858B99, -2, 14, 0),
        PHOBOS("Phobos", 0xC29478, -2, 12, 0),
        DEIMOS("Deimos", 0xC3A18B, -2, 8, 0),
        MIMAS("Mimas", 0xCCD0D5, 0.20, 18, 896),
        TETHYS("Tethys", 0xDCEBF3, -0.10, 12, 576),
        DIONE("Dione", 0xCBDCE1, 0.00, 26, 640),
        RHEA("Rhea", 0xD2C9BC, 0.12, 20, 768),
        IAPETUS("Iapetus", 0xB6A89A, 0.28, 32, 1024),
        UMBRIEL("Umbriel", 0x8A90A6, 0.18, 20, 896),
        TITANIA("Titania", 0xDDD2C8, -0.08, 30, 544),
        OBERON("Oberon", 0xAAA7B6, 0.12, 28, 768),
        ARIEL("Ariel", 0xD6E7EC, -0.16, 24, 480),
        HAUMEA("Haumea", 0xC9E5EF, 0.02, 34, 704),
        MAKEMAKE("Makemake", 0xD9BAA4, -0.06, 14, 832),
        ERIS("Eris", 0xDCE0F1, 0.06, 30, 768),
        DEEDEE("DeeDee", 0xB6C6CE, 0.16, 16, 960);

        public final String worldName;
        public final int colour, massifHeight, volcanoCell;
        public final double oceanThreshold;
        public Block strata, cryoFlow, cryoVent, sediment, salt;
        Profile(String name, int colour, double oceans, int height, int volcanoCell) {
            this.worldName = name; this.colour = colour; this.oceanThreshold = oceans;
            this.massifHeight = height; this.volcanoCell = volcanoCell;
        }
        public boolean hasOceans() { return volcanoCell > 0; }
    }

    public static Block saltWater;
    public static int aquaticRenderId = -1;
    private SolSysGeology() { }

    public static void register() {
        if (saltWater != null) return;
        Fluid fluid = FluidRegistry.getFluid("saltwater");
        if (fluid == null) {
            fluid = new Fluid("saltwater").setDensity(1030).setViscosity(1100).setTemperature(275);
            FluidRegistry.registerFluid(fluid);
        }
        saltWater = new SaltWaterBlock(fluid).setBlockName("SUSaltWater");
        GameRegistry.registerBlock(saltWater, "SUSaltWater");
        for (Profile p : Profile.values()) {
            p.strata = block(p, "Strata", "strata", 4.5F);
            if (p.hasOceans()) {
                p.cryoFlow = block(p, "CryoFlow", "cryo", 2.8F);
                p.cryoVent = block(p, "CryoVent", "vent", 4.0F);
                p.sediment = block(p, "OceanSediment", "sediment", 1.4F);
                p.salt = block(p, "SaltCrust", "salt", 1.8F);
            }
        }
    }

    private static Block block(Profile p, String suffix, String texture, float hardness) {
        String name = "SU" + p.worldName + suffix;
        Block block = new GeologicalBlock(p.colour).setBlockName(name)
                .setBlockTextureName("htgt6:solsys/" + texture).setHardness(hardness)
                .setResistance(hardness * 3).setCreativeTab(CreativeTabs.tabBlock);
        block.setHarvestLevel("pickaxe", hardness >= 4 ? 2 : 1);
        GameRegistry.registerBlock(block, name);
        return block;
    }

    public static Profile forWorld(String name) {
        for (Profile p : Profile.values()) if (p.worldName.equals(name)) return p;
        return null; // Neptune uses pressure layers, never solid continents.
    }

    private static final class GeologicalBlock extends Block {
        private final int colour;
        GeologicalBlock(int colour) { super(Material.rock); this.colour = colour; }
        @Override public int getBlockColor() { return colour; }
        @Override public int getRenderColor(int metadata) { return colour; }
        @Override public int colorMultiplier(IBlockAccess world, int x, int y, int z) { return colour; }
    }

    private static final class SaltWaterBlock extends BlockFluidClassic {
        SaltWaterBlock(Fluid fluid) { super(fluid, Material.water); setLightOpacity(2); }
        private boolean plant(IBlockAccess world, int x, int y, int z) {
            return world.getBlock(x, y, z) instanceof com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.BlockGlieseCPlant;
        }
        @Override public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
            return !plant(world, x, y, z) && super.canDisplace(world, x, y, z);
        }
        @Override public boolean displaceIfPossible(net.minecraft.world.World world, int x, int y, int z) {
            return !plant(world, x, y, z) && super.displaceIfPossible(world, x, y, z);
        }
        @Override public int getQuantaValue(IBlockAccess world, int x, int y, int z) {
            return plant(world, x, y, z) ? quantaPerBlock : super.getQuantaValue(world, x, y, z);
        }
        @Override public int getRenderType() { return aquaticRenderId; }
        @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister register) {
            // Vanilla owns the animated still/flow sprites; no duplicate texture.
        }
        @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side, int meta) { return Blocks.water.getIcon(side, meta); }
        @Override public int getBlockColor() { return 0x88BACB; }
        @Override public int getRenderColor(int meta) { return getBlockColor(); }
        @Override public int colorMultiplier(IBlockAccess world, int x, int y, int z) { return getBlockColor(); }
    }
}
