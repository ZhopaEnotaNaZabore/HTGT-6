package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderEvokerFangs112;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderIllager112;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderVex112;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderLlama112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvoker112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvokerFangs112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVex112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ExplorationUpdate112Client {
    public static void init() {
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.TileEntityShulkerBox112.class, new RenderShulkerBox112());
        ShulkerBoxItemRenderer112 boxItems = new ShulkerBoxItemRenderer112();
        EntityBackportValidator.texture("textures/entity/exploration_update/shulker_box/undyed.png", 64, 64);
        net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(net.minecraft.item.Item.getItemFromBlock(
                com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.undyedShulkerBox), boxItems);
        for (com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112 box
                : com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.shulkerBoxes) {
            EntityBackportValidator.texture("textures/entity/exploration_update/shulker_box/" + box.getColorName() + ".png", 64, 64);
            net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(net.minecraft.item.Item.getItemFromBlock(box), boxItems);
        }
        for (String texture : new String[] {"evoker", "vindicator", "vex", "vex_charging"})
            EntityBackportValidator.texture("textures/entity/exploration_update/illager/" + texture + ".png", 64, 64);
        EntityBackportValidator.texture("textures/entity/exploration_update/illager/fangs.png", 64, 32);
        RenderingRegistry.registerEntityRenderingHandler(EntityVindicator112.class, new RenderIllager112("vindicator"));
        RenderingRegistry.registerEntityRenderingHandler(EntityEvoker112.class, new RenderIllager112("evoker"));
        RenderingRegistry.registerEntityRenderingHandler(EntityVex112.class, new RenderVex112());
        RenderingRegistry.registerEntityRenderingHandler(EntityEvokerFangs112.class, new RenderEvokerFangs112());
        RenderingRegistry.registerEntityRenderingHandler(EntityLlama112.class, new RenderLlama112());
        EntityBackportValidator.texture("textures/entity/exploration_update/llama/spit.png", 64, 32);
        RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlamaSpit112.class, new RenderLlamaSpit112());
        com.mod.htgt6.minecraft1202.Module.Client.Exploration.TotemActivationRenderer112 totemRenderer =
                new com.mod.htgt6.minecraft1202.Module.Client.Exploration.TotemActivationRenderer112();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(totemRenderer);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(totemRenderer);
    }
    private ExplorationUpdate112Client() { }
}
