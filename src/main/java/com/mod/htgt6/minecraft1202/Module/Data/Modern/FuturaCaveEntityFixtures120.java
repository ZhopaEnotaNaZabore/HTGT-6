package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAxolotl120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGlowSquid120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelAxolotl120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelGlowSquid120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaCaveCreatureSpawns120;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.world.biome.BiomeGenBase;

/** Headless contract for the 1.20 cave mobs, drops, textures and 3D spawn tables. */
public final class FuturaCaveEntityFixtures120 {
    public static void main(String[] ignored)throws Exception{
        require(EntityAxolotl120.class.getSuperclass()==EntityWaterMob.class,"axolotl aquatic base");
        require(EntityGlowSquid120.class.getSuperclass()==EntitySquid.class,"glow squid vanilla movement base");
        require(Backport120Entities.AXOLOTL_MOD_ID==28&&Backport120Entities.GLOW_SQUID_MOD_ID==29,"stable network IDs");
        require(Backport120Content.glowInkSac.getUnlocalizedName().endsWith("glow_ink_sac"),"glow ink drop item");
        require(FuturaCaveCreatureSpawns120.LUSH_WATER.size()==2,"lush cave water spawn table");
        require(FuturaCaveCreatureSpawns120.DRIPSTONE_WATER.size()==1,"dripstone water spawn table");
        require(entry(FuturaCaveCreatureSpawns120.LUSH_WATER,EntityAxolotl120.class,4,6),"axolotl group size");
        require(entry(FuturaCaveCreatureSpawns120.LUSH_WATER,EntityGlowSquid120.class,4,6),"lush glow squid group size");
        require(entry(FuturaCaveCreatureSpawns120.DRIPSTONE_WATER,EntityGlowSquid120.class,4,6),"dripstone glow squid group size");
        for(String name:new String[]{"lucy","wild","gold","cyan","blue"})image("assets/htgt6/textures/entity/axolotl/axolotl_"+name+".png",64,64);
        image("assets/htgt6/textures/entity/squid/glow_squid.png",64,32);
        image("assets/htgt6/textures/items/1_20_backport/glow_ink_sac.png",16,16);
        ModelAxolotl120 axolotl=new ModelAxolotl120();
        require(axolotl.body.cubeList.size()==2&&axolotl.body.childModels.size()==6,
                "axolotl 1.20 body mesh/hierarchy");
        require(axolotl.head.childModels.size()==3,"axolotl gill hierarchy");
        net.minecraft.client.model.ModelBox tail=(net.minecraft.client.model.ModelBox)axolotl.tail.cubeList.get(0);
        require(tail.posX1==tail.posX2,"axolotl tail must remain a texture plane");
        require(((net.minecraft.client.model.ModelBox)axolotl.topGills.cubeList.get(0)).posZ1
                        ==((net.minecraft.client.model.ModelBox)axolotl.topGills.cubeList.get(0)).posZ2,
                "axolotl gills must remain texture planes");
        ModelGlowSquid120 squid=new ModelGlowSquid120();
        require(squid.body.cubeList.size()==1&&squid.tentacles.length==8,"glow squid 1.20 mesh");
        System.out.println("Futura cave entities OK; eggs=2, axolotl variants=5, 3D cave spawn tables=2");
    }
    private static boolean entry(java.util.List list,Class type,int min,int max){for(Object raw:list){BiomeGenBase.SpawnListEntry e=(BiomeGenBase.SpawnListEntry)raw;if(e.entityClass==type&&e.minGroupCount==min&&e.maxGroupCount==max)return true;}return false;}
    private static void image(String path,int width,int height)throws Exception{InputStream in=FuturaCaveEntityFixtures120.class.getClassLoader().getResourceAsStream(path);require(in!=null,path);try{BufferedImage image=ImageIO.read(in);require(image!=null&&image.getWidth()==width&&image.getHeight()==height,path+" dimensions");}finally{in.close();}}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private FuturaCaveEntityFixtures120(){}
}
