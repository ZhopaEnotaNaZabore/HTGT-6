package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RenderPillager;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RenderRavager;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RenderVillager120;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RenderVillageBell120;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.entity.passive.EntityVillager;

/** Client-only render registration for Village & Pillage. */
@SideOnly(Side.CLIENT)
public final class VillageAndPillageUpdateClient {
    public static void init() {
        RaidHud150.register();
        ((net.minecraft.client.resources.IReloadableResourceManager)net.minecraft.client.Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(new VillagerHatMetadata120());
        ((net.minecraft.client.resources.IReloadableResourceManager)net.minecraft.client.Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ArmorTrimTexture120());
        RenderingRegistry.registerEntityRenderingHandler(net.minecraft.entity.passive.EntityHorse.class,new RenderHorse151());
        RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRaidWitch151.class, new net.minecraft.client.renderer.entity.RenderWitch());
        CrossbowItemRenderer139 crossbowRenderer=new CrossbowItemRenderer139();
        crossbowRenderer.setJsonRenderer(VillageItemModels114.village());
        net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(VillagePillageContent.crossbow, crossbowRenderer);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(crossbowRenderer);
        EntityBackportValidator.texture("textures/entity/village_pillage/pillager.png", 64, 64);
        EntityBackportValidator.texture("textures/entity/village_pillage/ravager.png", 128, 128);
        RenderingRegistry.registerEntityRenderingHandler(EntityPillager.class, new RenderPillager());
        RenderingRegistry.registerEntityRenderingHandler(EntityRavager.class, new RenderRavager());
        RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143.class,
                new RenderCrossbowRocket143());
        RenderingRegistry.registerEntityRenderingHandler(EntityVillager.class, new RenderVillager120());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBell120.class, new RenderVillageBell120());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityCampfire149.class,new RenderCampfireItems149());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBarrel120.class,
            new net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer(){
                public void renderTileEntityAt(net.minecraft.tileentity.TileEntity tile,double x,double y,double z,float partial){}
            });
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageLectern120.class,new RenderLecternBook120());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityColoredBed120.class,new RenderColoredBed120());
        ColoredBedItemRenderer120 bedItems=new ColoredBedItemRenderer120();
        for(Block bed:VillagePillageContent.coloredBeds)net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(net.minecraft.item.Item.getItemFromBlock(bed),bedItems);
        bindPoiModels();
    }

    private static void bindPoiModels() {
        JsonBlockModelLoader loader = JsonBlockModelLoader.install();
        VillagePillageContent.setJsonRenderId(loader.getRenderId());
        JsonBlockModelLoader.TextureResolver textures = new JsonBlockModelLoader.TextureResolver() {
            @Override public ResourceLocation resolve(String source) {
                int colon = source.indexOf(':');
                String path = colon >= 0 ? source.substring(colon + 1) : source;
                if (path.startsWith("block/")) path = path.substring(6);
                if (path.startsWith("blocks/")) path = path.substring(7);
                return new ResourceLocation("htgt6", "village_pillage/" + path);
            }
        };
        JsonBlockModelLoader.StateMapper normal = new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                return "normal";
            }
        };
        bind(loader, textures, new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata) {
                return "hanging="+((metadata&1)!=0);
            }
        }, VillagePillageContent.lantern, "lantern");
        bind(loader,textures,new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int meta){
                return "facing="+new String[]{"south","west","north","east"}[meta&3]+",lit="+com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149.lit(meta);
            }
        },VillagePillageContent.campfire,"campfire");
        bind(loader,textures,new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int meta){
                // Inventory/NEI has no world; modern scaffolding items use the stable model.
                return "bottom="+(world!=null&&((com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockScaffolding152)VillagePillageContent.scaffolding).bottom(world,x,y,z));
            }
        },VillagePillageContent.scaffolding,"scaffolding");
        bind(loader, textures, normal, VillagePillageContent.cartographyTable, "cartography_table");
        bind(loader, textures, new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata) {
                return "level="+(metadata&15);
            }
        }, VillagePillageContent.composter, "composter");
        bind(loader, textures, normal, VillagePillageContent.fletchingTable, "fletching_table");
        bind(loader, textures, normal, VillagePillageContent.smithingTable, "smithing_table");
        bindFurnace(loader, textures, VillagePillageContent.blastFurnace, "blast_furnace");
        bindFurnace(loader, textures, VillagePillageContent.smoker, "smoker");
        bind(loader,textures,new JsonBlockModelLoader.StateMapper(){
            public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                return "facing="+new String[]{"north","east","south","west","up","down","north","north"}[metadata&7]+",open="+((metadata&8)!=0);
            }
        },VillagePillageContent.barrel,"barrel");
        bindFacing(loader, textures, VillagePillageContent.lectern, "lectern", "");
        bindFacing(loader, textures, VillagePillageContent.stonecutter, "stonecutter", "");
        bindFacing(loader, textures, VillagePillageContent.loom, "loom", "");
        bindAttachment(loader, textures, VillagePillageContent.grindstone, "grindstone",
                new String[] {"floor", "wall", "ceiling", "floor"}, "face");
        bindAttachment(loader, textures, VillagePillageContent.bell, "bell",
                new String[] {"floor", "ceiling", "single_wall", "double_wall"}, "attachment");
    }

    private static void bind(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
            JsonBlockModelLoader.StateMapper mapper, Block block, String name) {
        loader.register(block, new ResourceLocation("htgt6", "village_pillage/" + name), mapper, textures);
    }

    private static void bindFacing(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
            Block block, String name, final String extra) {
        bind(loader, textures, new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                String state = "facing=" + new String[] {"north", "east", "south", "west"}[metadata & 3];
                return extra.length() == 0 ? state : (extra + "," + state);
            }
        }, block, name);
    }

    private static void bindFurnace(JsonBlockModelLoader loader,JsonBlockModelLoader.TextureResolver textures,
                                    Block block,String name){
        bind(loader,textures,new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                return "facing="+new String[]{"north","east","south","west"}[metadata&3]
                        +",lit="+((metadata&4)!=0);
            }
        },block,name);
    }

    private static void bindAttachment(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
            Block block, String name, final String[] attachments, final String property) {
        bind(loader, textures, new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                return property + "=" + attachments[(metadata >>> 2) & 3] + ",facing="
                        + new String[] {"north", "east", "south", "west"}[metadata & 3];
            }
        }, block, name);
    }

    private VillageAndPillageUpdateClient() { }
}
