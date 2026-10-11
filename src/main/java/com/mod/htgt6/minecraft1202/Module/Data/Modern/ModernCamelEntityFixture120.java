package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelCamel120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.RenderCamel120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamelSeat120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.CamelMountSystem120;
import com.mod.htgt6.minecraft1202.Module.Network.Modern.PacketCamelDash120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/** Triple contract for Camel reference behavior, legacy mount safety and desert-village wiring. */
public final class ModernCamelEntityFixture120 {
    public static void main(String[] ignored)throws Exception{
        // 1: Minecraft 1.20.2 constants, feed identity and exact client assets.
        require(EntityCamel120.MAX_HEALTH==32.0D&&EntityCamel120.MOVEMENT_SPEED==0.09D,"1.20.2 attributes");
        require(EntityCamel120.DASH_COOLDOWN_TICKS==55,"dash cooldown");
        require(Math.abs(EntityCamel120.dashHorizontalStrength(0)-1.9D)<1.0E-9D
                &&Math.abs(EntityCamel120.dashHorizontalStrength(90)-2.3D)<1.0E-9D,"dash charge range");
        require(Math.abs(EntityCamel120.dashVerticalStrength(0)-.65D)<1.0E-9D
                &&Math.abs(EntityCamel120.dashVerticalStrength(90)-.75D)<1.0E-9D,"dash vertical range");
        require(EntityCamel120.isCamelFood(new ItemStack(Blocks.cactus)),"cactus feed/breeding item");
        require(EntityCamel120.isCamelSpawnGroundKey("minecraft:sand")
                &&EntityCamel120.isCamelSpawnGroundKey("minecraft:sandstone")
                &&!EntityCamel120.isCamelSpawnGroundKey("minecraft:grass"),"desert natural-spawn ground");
        require("IsSitting".equals(EntityCamel120.NBT_SITTING)
                &&"LastPoseTick".equals(EntityCamel120.NBT_LAST_POSE_TICK)
                &&"DashCooldown".equals(EntityCamel120.NBT_DASH_COOLDOWN),"pose/dash NBT keys");
        image("assets/htgt6/textures/entity/camel/camel.png",128,128,
                "cc9c69ed055f3c98da53bb1f0c8093d58e3775bbbd2ea8217287394d56879487");
        sounds();

        // 2: native 1.7 animal, saddle, two seats and dedicated dash input (no horse state leakage).
        require(EntityCamel120.class.getSuperclass()==EntityAnimal.class,"native animal base");
        require(EntityCamel120.STATE_WATCHER==16&&EntityCamel120.DASH_COOLDOWN_WATCHER==17,
                "camel-owned watcher range");
        require(!classContains(EntityCamel120.class,"EntityHorse"),"horse implementation leaked into Camel");
        require(EntityCamel120.class.getMethod("requestDash",int.class)!=null,"native dash endpoint");
        require(EntityCamel120.class.getMethod("isSaddled")!=null,"camel saddle state");
        require(EntityCamelSeat120.class.getSuperclass()==net.minecraft.entity.Entity.class,"second passenger adapter");
        require("MC1202_Camel".equals(CamelMountSystem120.CHANNEL),"dedicated mount network");
        require(PacketCamelDash120.class.getConstructor(int.class)!=null,"dash packet");
        require(EntityCamel120.class.getMethod("createChild",EntityAgeable.class)!=null,"legacy breeding bridge");
        require(EntityCamel120.class.getMethod("writeEntityToNBT",NBTTagCompound.class)!=null,"NBT writer");
        require(EntityCamel120.class.getMethod("readEntityFromNBT",NBTTagCompound.class)!=null,"NBT reader");
        require(RenderLiving.class.isAssignableFrom(RenderCamel120.class),"legacy renderer base");
        ModelCamel120 model=new ModelCamel120();
        require(model.body.childModels.size()==4,"camel body/head/hump/tail/saddle hierarchy");
        require(model.head.childModels.size()==4&&model.head.cubeList.size()==3,
                "camel ears/head/neck/muzzle/bridle hierarchy");
        require(model.body.rotationPointY==4.0F&&model.body.rotationPointZ==9.5F
                &&model.leftFrontLeg.rotationPointY==1.0F,
                "official camel root pivots; legs must not inherit the body offset");

        // 3: registry/network and official single Camel village marker.
        require(Backport120Entities.CAMEL_MOD_ID==41,"non-conflicting mod entity id");
        require(Backport120Entities.CAMEL_SEAT_MOD_ID==50,"second seat entity id");
        require(Backport120Entities.CAMEL_TRACKING_RANGE==80&&Backport120Entities.CAMEL_UPDATE_FREQUENCY==3
                &&Backport120Entities.CAMEL_SENDS_VELOCITY,
                "tracking and velocity packet contract");
        require(FuturaAncientCityGenerator120.supportsTemplateEntity("minecraft:camel"),"template entity adapter");
        require(classContains(FuturaAncientCityGenerator120.class,"EntityCamel120"),
                "template factory must construct Camel adapter instead of legacy Horse placeholder");
        marker();
        System.out.println("Modern Camel OK; reference=1.20.2, sounds=47, entityId=41, desertMarkers=1");
    }

    private static void marker()throws Exception{
        InputStream in=ModernCamelEntityFixture120.class.getClassLoader().getResourceAsStream(
                "assets/htgt6/structures/village_1_20/desert/camel_spawn.nbt");require(in!=null,"camel_spawn.nbt");
        NBTTagCompound root;try{root=CompressedStreamTools.readCompressed(in);}finally{in.close();}
        NBTTagList entities=root.getTagList("entities",10);int camels=0;
        for(int i=0;i<entities.tagCount();i++)if("minecraft:camel".equals(
                entities.getCompoundTagAt(i).getCompoundTag("nbt").getString("id")))camels++;
        require(camels==1,"official desert camel marker count");
    }

    private static void sounds()throws Exception{
        String[][] groups={{"ambient","8"},{"dash","6"},{"dash_ready","1"},{"death","2"},{"eat","5"},
                {"hurt","4"},{"sit","4"},{"stand","5"},{"step","6"},{"step_sand","6"}};int total=0;
        for(String[] group:groups){int count=Integer.parseInt(group[1]);for(int i=1;i<=count;i++){
            String path="assets/htgt6/sounds/mob/camel/"+group[0]+i+".ogg";
            InputStream in=ExternalSoundFixture179.open(path);require(in!=null,path);
            try{require(in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',path+" OGG");}finally{in.close();}total++;}}
        require(total==47,"official Camel sound count");InputStream jsonIn=
                ModernCamelEntityFixture120.class.getClassLoader().getResourceAsStream("assets/htgt6/sounds.json");
        require(jsonIn!=null,"sounds.json");try{String json=new String(read(jsonIn),"UTF-8");
            for(String event:new String[]{"ambient","dash","dash_ready","death","eat","hurt","sit","stand","step","step_sand"})
                require(json.contains("\"entity.camel."+event+"\""),"sound "+event);}finally{jsonIn.close();}
    }
    private static void image(String path,int width,int height,String expected)throws Exception{
        InputStream in=ModernCamelEntityFixture120.class.getClassLoader().getResourceAsStream(path);require(in!=null,path);
        byte[] bytes;try{bytes=read(in);}finally{in.close();}BufferedImage image=ImageIO.read(new ByteArrayInputStream(bytes));
        require(image!=null&&image.getWidth()==width&&image.getHeight()==height,path+" dimensions");
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder hex=new StringBuilder();
        for(byte value:digest)hex.append(String.format("%02x",value&255));require(expected.equals(hex.toString()),path+" SHA-256");
    }
    private static byte[] read(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))>=0)out.write(buffer,0,count);return out.toByteArray();}
    private static boolean classContains(Class<?> type,String token)throws Exception{
        InputStream in=type.getResourceAsStream(type.getSimpleName()+".class");require(in!=null,type.getName()+" bytecode");
        try{return new String(read(in),"ISO-8859-1").contains(token);}finally{in.close();}
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private ModernCamelEntityFixture120(){}
}
