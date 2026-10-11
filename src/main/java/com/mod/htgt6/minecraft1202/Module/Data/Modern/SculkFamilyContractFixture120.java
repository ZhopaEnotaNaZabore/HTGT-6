package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120CalibratedSculkSensor;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Candle;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SculkWarningManager120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120SculkSensor;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120SculkShrieker;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120SculkVein;
import com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120SculkHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Headless contract for the complete HTGT6-owned 1.20 sculk family. */
public final class SculkFamilyContractFixture120 {
    public static void main(String[] args)throws Exception{
        require(Backport120Content.sculk!=null&&Backport120Content.sculkVein!=null
                &&Backport120Content.sculkCatalyst!=null&&Backport120Content.sculkSensor!=null
                &&Backport120Content.sculkShrieker!=null,"base sculk family registration");
        require(Backport120Content.calibratedSculkSensor instanceof BlockBackport120CalibratedSculkSensor,
                "Minecraft 1.20 calibrated sensor registration");
        require(Backport120Content.candle instanceof BlockBackport120Candle
                &&Backport120Content.whiteCandle instanceof BlockBackport120Candle,"native candles");
        require(Backport120Content.soulFire instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SoulFire,
                "native soul fire");
        require(BlockBackport120Candle.count(BlockBackport120Candle.metadata(4,true))==4,
                "four-candle state encoding");
        require(EntityWarden120.MAX_HEALTH==500D&&EntityWarden120.ATTACK_DAMAGE==30D
                &&EntityWarden120.SONIC_HIT_TICK==34,"native 1.20 Warden combat constants");
        require(SculkWarningManager120.MAX_WARNING==4&&SculkWarningManager120.WARNING_COOLDOWN==200
                &&SculkWarningManager120.WARNING_DECAY==12000,"shared warning tracker constants");
        require(Backport120Content.sculkSensor.hasTileEntity(0)
                &&Backport120Content.calibratedSculkSensor.hasTileEntity(0)
                &&Backport120Content.sculkShrieker.hasTileEntity(0)
                &&Backport120Content.sculkVein.hasTileEntity(0),"persistent sculk block entities");
        cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(TileEntityBackport120SculkSensor.class,
                "htgt6:fixture_sculk_sensor");
        TileEntityBackport120SculkSensor source=new TileEntityBackport120SculkSensor();source.setFacing(5);
        NBTTagCompound saved=new NBTTagCompound();source.writeToNBT(saved);
        TileEntityBackport120SculkSensor restored=new TileEntityBackport120SculkSensor();restored.readFromNBT(saved);
        require(restored.getFacing()==5,"calibrated sensor orientation NBT");
        require(new TileEntityBackport120SculkShrieker()!=null&&new TileEntityBackport120SculkVein()!=null,
                "shrieker/vein state containers");
        String[] events={"step","projectile_land","projectile_shoot","roar","equip","interact","damage",
                "eat","close","open","block_change","destroy","place","teleport","die"};
        for(int i=0;i<events.length;i++)require(Backport120SculkHandler.frequencyForFixture(events[i])==i+1,
                "vibration frequency "+(i+1)+": "+events[i]);
        require(Backport120SculkHandler.frequencyForUseAction(EnumAction.eat)==8
                &&Backport120SculkHandler.frequencyForUseAction(EnumAction.drink)==8,
                "food and drink completion frequency");
        require(Backport120SculkHandler.frequencyForUseAction(EnumAction.bow)==3
                &&Backport120SculkHandler.frequencyForUseAction(EnumAction.block)==3,
                "non-consumable item completion frequency");
        resource("blockstates/1_20_backport/calibrated_sculk_sensor.json");
        resource("models/block/1_20_backport/calibrated_sculk_sensor.json");
        resource("models/block/1_20_backport/calibrated_sculk_sensor_active.json");
        resource("models/block/1_20_backport/calibrated_sculk_sensor_inactive.json");
        resource("models/item/1_20_backport/calibrated_sculk_sensor.json");
        resource("models/item/1_20_backport/sculk_vein.json");
        resource("textures/blocks/1_20_backport/calibrated_sculk_sensor_top.png");
        resource("textures/blocks/1_20_backport/calibrated_sculk_sensor_amethyst.png");
        resource("textures/blocks/1_20_backport/calibrated_sculk_sensor_input_side.png");
        resource("textures/entity/warden/warden.png");
        resource("textures/entity/warden/warden_bioluminescent_layer.png");
        resource("textures/entity/warden/warden_pulsating_spots.png");
        resource("textures/blocks/1_20_backport/soul_fire_0.png");
        resource("textures/blocks/1_20_backport/soul_fire_1.png");
        resource("sounds/entity/sculk_horn/sonic_boom1.ogg");
        resource("sounds/block/warden_altar/shriek1.ogg");
        for(String path:args){String text=new String(Files.readAllBytes(new File(path).toPath()),StandardCharsets.UTF_8);
            require(text.contains("B:enableSculk=false"),"Et Futurum sculk must remain disabled: "+path);}
        System.out.println("Sculk family 1.20 contract OK: native Warden, candles, warning tracker, frequencies 1..15, persistent states");
    }
    private static void resource(String path){try(java.io.InputStream input=ExternalSoundFixture179.open("assets/htgt6/"+path)){require(input!=null,"missing "+path);}catch(java.io.IOException e){throw new IllegalStateException("Missing validated resource "+path,e);}}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private SculkFamilyContractFixture120(){ }
}
