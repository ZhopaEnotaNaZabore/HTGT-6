package com.mod.htgt6.common.handler;


import com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks.HighTierComponents;
import com.mod.htgt6.common.GTHooks.TurbineRotorHooks.GT6TurbineRotorHooks;
import com.mod.htgt6.common.Migaration.LegacyCoreMaterialItems;
import com.mod.htgt6.common.item.greg.materials.arcanite.*;
import com.mod.htgt6.common.item.avaritia.singularies.InfiSing;
import com.mod.htgt6.common.item.galaxy.alloys.*;
import com.mod.htgt6.common.item.galaxy.schemas.RocketSchema1;
import com.mod.htgt6.common.item.greg.circuits.UVCirc;
import com.mod.htgt6.common.item.greg.circuits.ZPMcirc;
import com.mod.htgt6.common.item.greg.coils.*;
import com.mod.htgt6.common.item.greg.technological.ZPMSocket;
import com.mod.htgt6.common.item.htgt6.ModLogo;
import com.mod.htgt6.common.item.htgt6.govno;
import com.mod.htgt6.common.item.htgt6.govnoebanoe.ItemUniversalCanister;
import com.mod.htgt6.common.item.htgt6.Tools.ItemHolographicProjector;
import com.mod.htgt6.common.item.nova.NovaCrystall;
import com.mod.htgt6.common.item.nova.NovaCrystallAlloy;
import com.mod.htgt6.common.item.nova.NovaCrystallDust;
import cpw.mods.fml.common.registry.GameRegistry;
import com.mod.htgt6.common.item.greg.technological.misc.*;

public class ModItems {

    public static final govno GOVNO = new govno();
    public static final CompositeT1 COMPOSITE_T_1 = new CompositeT1();
    public static final CompositeT2 COMPOSITE_T_2 = new CompositeT2();
    public static final CompositeT3 COMPOSITE_T_3 = new CompositeT3();
    public static final CompositeT4 COMPOSITE_T_4 = new CompositeT4();
    public static final CompositeT5 COMPOSITE_T_5 = new CompositeT5();

    public static final CompositeT6 COMPOSITE_T_6 = new CompositeT6();
    public static final ReinforcedCompositePlate REINFORCED_COMPOSITE_PLATE_T_1 = new ReinforcedCompositePlate(1);
    public static final ReinforcedCompositePlate REINFORCED_COMPOSITE_PLATE_T_2 = new ReinforcedCompositePlate(2);
    public static final ReinforcedCompositePlate REINFORCED_COMPOSITE_PLATE_T_3 = new ReinforcedCompositePlate(3);
    public static final ReinforcedCompositePlate REINFORCED_COMPOSITE_PLATE_T_4 = new ReinforcedCompositePlate(4);
    public static final ReinforcedCompositePlate REINFORCED_COMPOSITE_PLATE_T_5 = new ReinforcedCompositePlate(5);
    public static final ReinforcedCompositePlate REINFORCED_COMPOSITE_PLATE_T_6 = new ReinforcedCompositePlate(6);
    public static final ZPMcirc ZP_MCIRC = new ZPMcirc();

    public static final InfiSing INFI_SING = new InfiSing();

    public static final NovaCrystallAlloy NOVA_CRYSTALL_ALLOY = new NovaCrystallAlloy();
    public static final NovaCrystallDust NOVA_CRYSTALL_DUST = new NovaCrystallDust();
    public static final ZPMSocket ZPM_SOCKET = new ZPMSocket();

    public static final NovaCrystall NOVA_CRYSTALL = new NovaCrystall();
    public static final ModLogo MOD_LOGO = new ModLogo();
    public static final UVCirc UV_CIRC = new UVCirc();

    public static final RocketSchema1 ROCKET_SCHEMA_1 = new RocketSchema1();
    public static final ULVcoil UL_VCOIL = new ULVcoil();
    public static final LVCOIL LVCOIL = new LVCOIL();
    public static final MVcoil M_VCOIL = new MVcoil();
    public static final HVcoil H_VCOIL = new HVcoil();
    public static final EVcoil E_VCOIL = new EVcoil();
    public static final IVcoil I_VCOIL = new IVcoil();
    public static final LUVcoil LU_VCOIL = new LUVcoil();
    public static final ZPMcoil ZP_MCOIL = new ZPMcoil();
    public static final UVcoil U_VCOIL = new UVcoil();
    public static final PUV1coil PUV_1_COIL = new PUV1coil();
    public static final InfiCoil INFI_COIL = new InfiCoil();
    public static final net.minecraft.item.Item UX_COIL = atlasCoil("UXcoil");
    public static final net.minecraft.item.Item OLV_COIL = atlasCoil("OLVcoil");
    public static final net.minecraft.item.Item OMV_COIL = atlasCoil("OMVcoil");
    public static final net.minecraft.item.Item OHV_COIL = atlasCoil("OHVcoil");
    public static final net.minecraft.item.Item OEV_COIL = atlasCoil("OEVcoil");
    public static final net.minecraft.item.Item OIV_COIL = atlasCoil("OIVcoil");
    public static final net.minecraft.item.Item MAX_COIL = atlasCoil("MAXcoil");

    private static net.minecraft.item.Item atlasCoil(String name) {
        return new com.mod.htgt6.common.item.htgt6.AtlasItem().setUnlocalizedName(name)
                .setTextureName(com.mod.htgt6.HTGT6.MOD_ID + ":" + name)
                .setCreativeTab(ModTab.INSTANCE);
    }
    public static final Qeye QEYE= new Qeye();
public static final ingotArcanite INGOT_ARCANITE = new ingotArcanite();
public static final ItemUniversalCanister UNIVERSAL_CANISTER = new ItemUniversalCanister();
public static final ItemHolographicProjector HOLOGRAPHIC_PROJECTOR = new ItemHolographicProjector();


    public static void register() {
        // These seven Build 13 names belong to HTGT6 itself. Register them
        // before new content so Forge can retain old-world item mappings.
        LegacyCoreMaterialItems.register();
        HighTierComponents.register();
        GameRegistry.registerItem(UNIVERSAL_CANISTER, "UniversalCanister");

        GameRegistry.registerItem(INGOT_ARCANITE, "ingotArcanite");
        GameRegistry.registerItem(INFI_COIL, "INFIcoil");
        GameRegistry.registerItem(PUV_1_COIL, "PUV1coil");
        GameRegistry.registerItem(U_VCOIL, "UVcoil");
        GameRegistry.registerItem(ZP_MCOIL, "ZPMcoil");
        GameRegistry.registerItem(LU_VCOIL, "LUVcoil");
        GameRegistry.registerItem(I_VCOIL, "IVcoil");
        GameRegistry.registerItem(E_VCOIL, "EVcoil");
        GameRegistry.registerItem(H_VCOIL, "HVcoil");
        GameRegistry.registerItem(M_VCOIL, "MVcoil");
        GameRegistry.registerItem(LVCOIL, "LVCOIL");
        GameRegistry.registerItem(UL_VCOIL, "ULVcoil");
        GameRegistry.registerItem(ROCKET_SCHEMA_1, "RocketSchema1");
        GameRegistry.registerItem(UV_CIRC, "UVCirc");
        GameRegistry.registerItem(MOD_LOGO, "ModLogo");
        GameRegistry.registerItem(NOVA_CRYSTALL, "NovaCrystall");



        GameRegistry.registerItem(ZPM_SOCKET, "ZPMSocket");


        GameRegistry.registerItem(GOVNO, "Govno");
        GameRegistry.registerItem(COMPOSITE_T_1, "CompositeT1");
        GameRegistry.registerItem(COMPOSITE_T_2, "CompositeT2");
        GameRegistry.registerItem(COMPOSITE_T_3, "CompositeT3");
        GameRegistry.registerItem(COMPOSITE_T_4, "CompositeT4");
        GameRegistry.registerItem(COMPOSITE_T_5, "CompositeT5");
        GameRegistry.registerItem(COMPOSITE_T_6, "CompositeT6");
        GameRegistry.registerItem(ZP_MCIRC, "ZPMCirc");
        GameRegistry.registerItem(NOVA_CRYSTALL_ALLOY, "NovaCrystalAlloy");
        GameRegistry.registerItem(NOVA_CRYSTALL_DUST, "NovaCrystalDust");
        GameRegistry.registerItem(QEYE, "Qeye");
        // New IDs remain appended so existing 1.7.10 item mappings stay stable.
        GameRegistry.registerItem(HOLOGRAPHIC_PROJECTOR, "holographic_projector");
        GT6TurbineRotorHooks.registerItem();
        GameRegistry.registerItem(UX_COIL, "UXcoil");
        GameRegistry.registerItem(OLV_COIL, "OLVcoil");
        GameRegistry.registerItem(OMV_COIL, "OMVcoil");
        GameRegistry.registerItem(OHV_COIL, "OHVcoil");
        GameRegistry.registerItem(OEV_COIL, "OEVcoil");
        GameRegistry.registerItem(OIV_COIL, "OIVcoil");
        GameRegistry.registerItem(MAX_COIL, "MAXcoil");



    }

    /** Called after all existing preInit registrations to preserve their ordering. */
    public static void registerReinforcedCompositePlates() {
        GameRegistry.registerItem(REINFORCED_COMPOSITE_PLATE_T_1, "ReinforcedCompositePlateT1");
        GameRegistry.registerItem(REINFORCED_COMPOSITE_PLATE_T_2, "ReinforcedCompositePlateT2");
        GameRegistry.registerItem(REINFORCED_COMPOSITE_PLATE_T_3, "ReinforcedCompositePlateT3");
        GameRegistry.registerItem(REINFORCED_COMPOSITE_PLATE_T_4, "ReinforcedCompositePlateT4");
        GameRegistry.registerItem(REINFORCED_COMPOSITE_PLATE_T_5, "ReinforcedCompositePlateT5");
        GameRegistry.registerItem(REINFORCED_COMPOSITE_PLATE_T_6, "ReinforcedCompositePlateT6");
    }

}
