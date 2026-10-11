package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ModelCombatShulker;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.RenderCombatShulker;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.RenderCombatShulkerBullet;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.RenderCombatInvisible;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.EndRodRenderHandler;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ChorusRenderHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityLingeringCloud;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityLingeringPotion;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulkerBullet;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityDragonBreathFireball;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoatSeat;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.RenderCombatZombieVillager;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.client.registry.ClientRegistry;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ElytraClientHandler;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.AttackIndicator112;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.AttackTooltip112;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ElytraPlayerRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.MinecraftForgeClient;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ShieldItemRenderer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CombatUpdateClient {
    public static void init() {
        EntityBackportValidator.texture("textures/gui/icons_1_12.png", 256, 256);
        EntityBackportValidator.texture("textures/entity/combat_update/sweep.png", 128, 32);
        EntityBackportValidator.texture("textures/blocks/combat_update/end_rod.png", 16, 16);
        EntityBackportValidator.texture("textures/entity/combat_update/shulker/shulker.png", 64, 64);
        EntityBackportValidator.texture("textures/entity/combat_update/shulker/spark.png", 64, 32);
        for (String wood : new String[]{"spruce", "birch", "jungle", "acacia", "dark_oak"}) {
            EntityBackportValidator.texture("textures/items/combat_update/" + wood + "_boat.png", 16, 16);
            EntityBackportValidator.texture("textures/entity/combat_update/boat/" + wood + ".png", 128, 64);
        }
        EntityBackportValidator.texture("textures/entity/combat_update/projectiles/tipped_arrow.png", 32, 32);
        EntityBackportValidator.texture("textures/entity/combat_update/projectiles/spectral_arrow.png", 32, 32);
        EntityBackportValidator.texture("textures/entity/combat_update/dragon_fireball.png", 16, 16);
        for (String profession : new String[]{"farmer", "librarian", "priest", "smith", "butcher"}) {
            EntityBackportValidator.texture("textures/entity/combat_update/zombie_villager/zombie_"
                    + profession + ".png", 64, 64);
        }
        EntityBackportValidator.texture(
                "textures/entity/combat_update/zombie_villager/zombie_villager.png", 64, 64);
        ModelCombatShulker model = new ModelCombatShulker();
        if (model.base == null || model.lid == null || model.head == null) {
            throw new IllegalStateException("Minecraft 1.9 Shulker model named-part validation failed");
        }
        int endRodRenderId = RenderingRegistry.getNextAvailableRenderId();
        CombatContent.setEndRodRenderId(endRodRenderId);
        EndRodRenderHandler endRodRenderer=new EndRodRenderHandler(endRodRenderId);
        if(!endRodRenderer.shouldRender3DInInventory(endRodRenderId)
                ||CombatContent.endRod.getRenderType()!=endRodRenderId)
            throw new IllegalStateException("End Rod inventory renderer is not connected to its block render ID");
        RenderingRegistry.registerBlockHandler(endRodRenderer);
        int chorusRenderId = RenderingRegistry.getNextAvailableRenderId();
        CombatContent.setChorusRenderId(chorusRenderId);
        RenderingRegistry.registerBlockHandler(new ChorusRenderHandler(chorusRenderId));
        RenderingRegistry.registerEntityRenderingHandler(EntityCombatShulker.class, new RenderCombatShulker());
        RenderingRegistry.registerEntityRenderingHandler(EntityCombatShulkerBullet.class, new RenderCombatShulkerBullet());
        RenderingRegistry.registerEntityRenderingHandler(EntityCombatArrow.class, new RenderCombatArrow());
        RenderingRegistry.registerEntityRenderingHandler(EntityLingeringCloud.class, new RenderCombatInvisible());
        RenderingRegistry.registerEntityRenderingHandler(EntityLingeringPotion.class,
                new net.minecraft.client.renderer.entity.RenderSnowball(CombatContent.lingeringPotion));
        RenderingRegistry.registerEntityRenderingHandler(EntityCombatZombieVillager.class,
                new RenderCombatZombieVillager());
        RenderingRegistry.registerEntityRenderingHandler(EntityCombatBoat.class, new RenderCombatBoat());
        RenderingRegistry.registerEntityRenderingHandler(EntityDragonBreathFireball.class,
                new RenderDragonBreathFireball());
        RenderingRegistry.registerEntityRenderingHandler(EntityCombatBoatSeat.class,
                new RenderCombatInvisible());
        ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityDragonHead.class,
                new RenderDragonHead120());
        ClientRegistry.bindTileEntitySpecialRenderer(com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityEndGateway.class,new RenderEndGateway120());
        ElytraClientHandler elytra = new ElytraClientHandler();
        FMLCommonHandler.instance().bus().register(elytra);
        MinecraftForge.EVENT_BUS.register(new ElytraPlayerRenderer());
        MinecraftForge.EVENT_BUS.register(new DragonHeadPlayerRenderer156());
        MinecraftForge.EVENT_BUS.register(new AttackIndicator112());
        MinecraftForge.EVENT_BUS.register(new AttackTooltip112());
        MinecraftForge.EVENT_BUS.register(new GlowingOutlineRenderer120());
        MinecraftForgeClient.registerItemRenderer(CombatContent.shield, new ShieldItemRenderer());
        MinecraftForgeClient.registerItemRenderer(net.minecraft.item.Item.getItemFromBlock(CombatContent.dragonHead),
                new DragonHeadItemRenderer120());
    }
    private CombatUpdateClient() { }
}
