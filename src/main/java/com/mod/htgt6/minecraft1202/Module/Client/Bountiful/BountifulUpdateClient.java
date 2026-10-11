package com.mod.htgt6.minecraft1202.Module.Client.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.RenderBountifulEndermite;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.RenderBountifulGuardian;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.RenderBountifulArmorStand;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.RenderBountifulRabbit;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelBountifulGuardian;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelBountifulRabbit;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulEndermite;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulRabbit;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.client.registry.ClientRegistry;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BountifulUpdateClient {
    public static void init(){EntityBackportValidator.texture("textures/entity/bountiful/endermite.png",64,32);for(String type:new String[]{"brown","white","black","white_splotched","gold","salt","toast","caerbannog"})EntityBackportValidator.texture("textures/entity/bountiful/rabbit/"+type+".png",64,32);EntityBackportValidator.texture("textures/entity/bountiful/guardian.png",64,64);EntityBackportValidator.texture("textures/entity/bountiful/guardian_elder.png",64,64);EntityBackportValidator.texture("textures/entity/bountiful/guardian_beam.png",32,32);EntityBackportValidator.texture("textures/entity/bountiful/armor_stand.png",64,64);EntityBackportValidator.texture("textures/entity/bountiful/banner/base.png",64,64);EntityBackportValidator.modelParts(new ModelBountifulRabbit(),"body","head","nose","left_ear","right_ear","left_foot","right_foot","left_thigh","right_thigh","left_arm","right_arm","tail");EntityBackportValidator.modelParts(new ModelBountifulGuardian(),"body","eye","tail","spike_0","spike_7");RenderingRegistry.registerEntityRenderingHandler(EntityBountifulRabbit.class,new RenderBountifulRabbit());RenderingRegistry.registerEntityRenderingHandler(EntityBountifulEndermite.class,new RenderBountifulEndermite());RenderingRegistry.registerEntityRenderingHandler(EntityBountifulGuardian.class,new RenderBountifulGuardian());RenderingRegistry.registerEntityRenderingHandler(EntityBountifulArmorStand.class,new RenderBountifulArmorStand());TileEntityBountifulBannerRenderer bannerRenderer=new TileEntityBountifulBannerRenderer();ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBountifulBanner.class,bannerRenderer);MinecraftForgeClient.registerItemRenderer(BountifulContent.banner,bannerRenderer);cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new com.mod.htgt6.minecraft1202.Module.Client.Shared.GuardianBeamSoundHandler112());}
    private BountifulUpdateClient(){}
}
