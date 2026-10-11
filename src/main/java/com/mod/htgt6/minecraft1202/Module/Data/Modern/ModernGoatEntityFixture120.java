package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelGoat120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGoat120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAILongJump120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.security.MessageDigest;

/** Triple contract for the 1.20.2 goat reference, legacy runtime and complete registration. */
public final class ModernGoatEntityFixture120 {
    public static void main(String[] ignored) throws Exception {
        // 1: stable facts from the local 1.20.2 entity/data contract.
        require(EntityGoat120.MAX_HEALTH==10.0D&&EntityGoat120.MOVEMENT_SPEED==0.20D,"1.20.2 attributes");
        require(EntityGoat120.SCREAMING_CHANCE==50,"1.20.2 screaming goat probability");
        require(EntityGoat120.MIN_RAM_COOLDOWN==600&&EntityGoat120.MAX_RAM_COOLDOWN==6000,"ram interval");
        require(EntityGoat120.SCREAMING_MIN_RAM_COOLDOWN==100&&EntityGoat120.SCREAMING_MAX_RAM_COOLDOWN==300,
                "screaming ram interval");
        require(EntityGoat120.instrumentMetadata(false,0)==0&&EntityGoat120.instrumentMetadata(false,3)==3,
                "normal horn instrument set");
        require(EntityGoat120.instrumentMetadata(true,0)==4&&EntityGoat120.instrumentMetadata(true,3)==7,
                "screaming horn instrument set");
        require(EntityGoat120.nextRamCooldown(-1)==600&&EntityGoat120.nextRamCooldown(5401)==6000,
                "ram interval clamping");
        require(EntityGoat120.nextRamCooldown(true,-1)==100&&EntityGoat120.nextRamCooldown(true,201)==300,
                "screaming ram interval clamping");

        // 2: legal 1.7.10 entity protocol: AI animal, age/breeding, watcher slots, NBT and interaction hooks.
        require(EntityGoat120.class.getSuperclass()==EntityAnimal.class,"legacy EntityAnimal base");
        require(EntityGoat120.FLAGS_WATCHER>=0&&EntityGoat120.RAMMING_WATCHER<=31
                &&EntityGoat120.FLAGS_WATCHER!=EntityGoat120.RAMMING_WATCHER,"1.7 DataWatcher ids");
        require((EntityGoat120.FLAG_LEFT_HORN|EntityGoat120.FLAG_RIGHT_HORN)==6,"independent horn flags");
        require(EntityGoat120.class.getMethod("createChild",EntityAgeable.class)!=null,"legacy breeding bridge");
        require(EntityGoat120.class.getMethod("interact",EntityPlayer.class)!=null,"legacy milk interaction");
        require(EntityGoat120.class.getMethod("writeEntityToNBT",NBTTagCompound.class)!=null,"NBT writer");
        require(EntityGoat120.class.getMethod("readEntityFromNBT",NBTTagCompound.class)!=null,"NBT reader");
        require(new ItemStack(Backport120Content.goatHorn,1,7).getItemDamage()==7,"horn metadata transport");
        require(EntityGoat120.isVanillaHornBreakKey("log")&&EntityGoat120.isVanillaHornBreakKey("log2")
                &&EntityGoat120.isVanillaHornBreakKey("stone")&&EntityGoat120.isVanillaHornBreakKey("packed_ice")
                &&EntityGoat120.isVanillaHornBreakKey("iron_ore")&&EntityGoat120.isVanillaHornBreakKey("coal_ore")
                &&EntityGoat120.isVanillaHornBreakKey("emerald_ore")&&EntityGoat120.canBreakHornOn(Backport120Content.copperOre)
                &&EntityGoat120.canBreakHornOn(Backport120Content.cherryLog)
                &&EntityGoat120.canBreakHornOn(Backport120Content.mangroveLog),"snaps_goat_horn mapping");
        require(!EntityGoat120.canBreakHornOn(null)&&!EntityGoat120.isVanillaHornBreakKey("cobblestone")
                &&!EntityGoat120.canBreakHornOn(Backport120Content.mossBlock),
                "solid blocks outside snaps_goat_horn must retain horns");
        require(EntityAILongJump120.class.getSuperclass()==EntityAIBase.class,"legacy long-jump AI base");
        double distance=4.4D,deltaY=2.0D,time=distance/EntityAILongJump120.HORIZONTAL_SPEED;
        double launch=EntityAILongJump120.verticalVelocity(distance,deltaY,EntityAILongJump120.HORIZONTAL_SPEED,
                EntityAILongJump120.GRAVITY);
        require(launch>0.2D&&launch<EntityAILongJump120.MAX_VERTICAL_SPEED,"safe long-jump launch speed");
        require(Math.abs(EntityAILongJump120.sampleHeight(launch,time,EntityAILongJump120.GRAVITY)-deltaY)<1.0E-9D,
                "ballistic trajectory must reach selected landing height");

        // 3: registry/tracking, biome spawn and client mesh/resource contract.
        require(Backport120Entities.GOAT_MOD_ID==39,"non-conflicting mod entity id");
        require(Backport120Entities.GOAT_TRACKING_RANGE==80&&Backport120Entities.GOAT_UPDATE_FREQUENCY==3,
                "spawn packet tracking contract");
        require(Backport120Entities.GOAT_SPAWN_BIOMES.length==0,"legacy mountains must not spawn goats");
        ModelGoat120 model=new ModelGoat120();
        require(model.body.childModels == null || model.body.childModels.isEmpty(),
                "goat legs/head must use root pivots, not inherit body offset");
        require(model.head.childModels.size()==3,"nose and independent horn hierarchy");
        require(model.head.cubeList.size()==3&&model.nose.cubeList.size()==1,"official ears, beard and nose mesh");
        require(model.body.rotationPointY==24.0F&&model.head.rotationPointX==1.0F
                &&model.head.rotationPointY==14.0F,"official goat root pivots");
        image("assets/htgt6/textures/entity/goat/goat.png",64,64,
                "9ea8a5129b13f9a275975004cf8a3a392600c7f2a8232a6731c53a7ad42eb231");
        sounds();
        System.out.println("Modern goat OK; reference=1.20.2, watchers=20/21, entityId=39, mountainBiomes=3");
    }

    private static void sounds()throws Exception{
        String[][] groups={{"idle", "8"},{"scream", "9"},{"hurt", "4"},{"screaming_hurt", "3"},
                {"death", "5"},{"screaming_death", "3"},{"pre_ram", "4"},{"screaming_pre_ram", "5"},
                {"impact", "3"},{"horn_break", "4"},{"jump", "2"},{"screaming_milk", "5"}};
        int total=0;for(String[] group:groups){int count=Integer.parseInt(group[1]);for(int i=1;i<=count;i++){
            String path="assets/htgt6/sounds/mob/goat/"+group[0]+i+".ogg";
            InputStream in=ExternalSoundFixture179.open(path);
            require(in!=null,path);try{require(in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',path+" OGG header");}
            finally{in.close();}total++;}}
        require(total==55,"official goat sound object count");
        InputStream in=ModernGoatEntityFixture120.class.getClassLoader().getResourceAsStream("assets/htgt6/sounds.json");
        require(in!=null,"sounds.json");try{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
            byte[] buffer=new byte[4096];int read;while((read=in.read(buffer))>=0)out.write(buffer,0,read);
            String json=new String(out.toByteArray(),"UTF-8");
            for(String event:new String[]{"ambient","scream","hurt","screaming_hurt","death","screaming_death",
                    "pre_ram","screaming_pre_ram","ram_impact","horn_break","long_jump","screaming_milk"})
                require(json.contains("\"entity.goat."+event+"\""),"sound event "+event);
        }finally{in.close();}
    }

    private static void image(String path,int width,int height,String expectedSha256)throws Exception{
        InputStream in=ModernGoatEntityFixture120.class.getClassLoader().getResourceAsStream(path);
        require(in!=null,path);byte[] bytes;try{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
            byte[] buffer=new byte[4096];int read;while((read=in.read(buffer))>=0)out.write(buffer,0,read);bytes=out.toByteArray();
        }finally{in.close();}
        BufferedImage image=ImageIO.read(new java.io.ByteArrayInputStream(bytes));
        require(image!=null&&image.getWidth()==width&&image.getHeight()==height,path+" dimensions");
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder hex=new StringBuilder();
        for(byte value:digest)hex.append(String.format("%02x",value&255));
        require(expectedSha256.equals(hex.toString()),path+" must be byte-identical to local 1.20.2");
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private ModernGoatEntityFixture120(){}
}
