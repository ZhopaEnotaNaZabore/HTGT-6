package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelAllay120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.RenderAllay120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAIAllayCollect120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAllay120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/** Reference, legacy-runtime and outpost-marker contract for the 1.20.2 Allay port. */
public final class ModernAllayEntityFixture120 {
    public static void main(String[] ignored)throws Exception{
        // 1: official 1.20.2 behavior/data/assets.
        require(EntityAllay120.MAX_HEALTH==20.0D&&EntityAllay120.MOVEMENT_SPEED==0.10D,"1.20.2 attributes");
        require(EntityAllay120.DUPLICATION_COOLDOWN==6000,"duplication cooldown");
        require("Inventory".equals(EntityAllay120.NBT_INVENTORY)
                &&"DuplicationCooldown".equals(EntityAllay120.NBT_DUPLICATION_COOLDOWN)
                &&"CanDuplicate".equals(EntityAllay120.NBT_CAN_DUPLICATE),"modern NBT keys");
        Item appleItem=new Item(),carrotItem=new Item();
        ItemStack apple=new ItemStack(appleItem,1,0),same=new ItemStack(appleItem,8,0);
        require(EntityAllay120.matchesFilter(apple,same),"held-item collection filter");
        require(!EntityAllay120.matchesFilter(apple,new ItemStack(carrotItem)),"different item filter rejection");
        image("assets/htgt6/textures/entity/allay/allay.png",32,32,
                "0cf5bbd580b1cf1817452450b88c8cf63a665b9430b65f48659873743c7a09bf");
        sounds();

        // 2: legal 1.7 entity/watcher/AI/network/client implementation.
        require(EntityAllay120.class.getSuperclass()==EntityCreature.class,"legacy creature base");
        require(EntityAllay120.FLAGS_WATCHER>=0&&EntityAllay120.FLAGS_WATCHER<=31,"DataWatcher slot");
        require((EntityAllay120.FLAG_DANCING|EntityAllay120.FLAG_CAN_DUPLICATE)==3,"watcher flag packing");
        require(EntityAIAllayCollect120.class.getSuperclass()==EntityAIBase.class,"legacy collect task");
        require(EntityAIAllayCollect120.SEARCH_RANGE==32.0D,"pickup search radius");
        require(EntityAllay120.class.getMethod("writeEntityToNBT",NBTTagCompound.class)!=null,"NBT writer");
        require(EntityAllay120.class.getMethod("readEntityFromNBT",NBTTagCompound.class)!=null,"NBT reader");
        require(RenderLiving.class.isAssignableFrom(RenderAllay120.class),"legacy renderer base");
        ModelAllay120 model=new ModelAllay120();require(model.body.childModels.size()==5,"allay model hierarchy");
        net.minecraft.client.model.ModelBox wing=(net.minecraft.client.model.ModelBox)model.rightWing.cubeList.get(0);
        require(wing.posX1==wing.posX2,"allay wing texture plane");

        // 3: stable registration and the official outpost entity marker.
        require(Backport120Entities.ALLAY_MOD_ID==40,"non-conflicting entity id");
        require(Backport120Entities.ALLAY_TRACKING_RANGE==64&&Backport120Entities.ALLAY_UPDATE_FREQUENCY==3,
                "tracking/velocity packet contract");
        require(FuturaAncientCityGenerator120.supportsTemplateEntity("minecraft:allay"),"template entity adapter");
        marker();
        System.out.println("Modern Allay OK; reference=1.20.2, sounds=22, entityId=40, outpostMarkers=2");
    }

    private static void marker()throws Exception{
        InputStream in=ModernAllayEntityFixture120.class.getClassLoader().getResourceAsStream(
                "assets/htgt6/structures/pillager_outpost/feature_cage_with_allays.nbt");
        require(in!=null,"allay cage template");NBTTagCompound root;try{root=CompressedStreamTools.readCompressed(in);}finally{in.close();}
        NBTTagList entities=root.getTagList("entities",10);int allays=0;
        for(int i=0;i<entities.tagCount();i++){NBTTagCompound data=entities.getCompoundTagAt(i).getCompoundTag("nbt");
            if("minecraft:allay".equals(data.getString("id"))){require(data.getBoolean("PersistenceRequired"),
                    "cage allay persistence");allays++;}}
        require(allays==2,"official feature_cage_with_allays marker count");
    }

    private static void sounds()throws Exception{
        String[][] groups={{"idle_with_item","4"},{"idle_without_item","4"},{"hurt","2"},{"death","2"},
                {"item_given","4"},{"item_taken","4"},{"item_thrown","1"}};int total=0;
        for(String[] group:groups){int count=Integer.parseInt(group[1]);for(int i=1;i<=count;i++){
            InputStream in=ExternalSoundFixture179.open(
                    "assets/htgt6/sounds/mob/allay/"+group[0]+i+".ogg");require(in!=null,group[0]+i);
            try{require(in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',group[0]+i+" OGG");}
            finally{in.close();}total++;}}
        require(total==21,"official Allay sound count");
        InputStream shimmer=ExternalSoundFixture179.open(
                "assets/htgt6/sounds/block/amethyst/shimmer.ogg");require(shimmer!=null,"duplication chime");shimmer.close();
        InputStream jsonIn=ModernAllayEntityFixture120.class.getClassLoader().getResourceAsStream("assets/htgt6/sounds.json");
        require(jsonIn!=null,"sounds.json");try{String json=new String(read(jsonIn),"UTF-8");
            for(String event:new String[]{"ambient_with_item","ambient_without_item","hurt","death","item_given",
                    "item_taken","item_thrown","duplicate"})require(json.contains("\"entity.allay."+event+"\""),"sound "+event);
        }finally{jsonIn.close();}
    }

    private static void image(String path,int width,int height,String expected)throws Exception{
        InputStream in=ModernAllayEntityFixture120.class.getClassLoader().getResourceAsStream(path);require(in!=null,path);
        byte[] bytes;try{bytes=read(in);}finally{in.close();}BufferedImage image=ImageIO.read(new ByteArrayInputStream(bytes));
        require(image!=null&&image.getWidth()==width&&image.getHeight()==height,path+" dimensions");
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder hex=new StringBuilder();
        for(byte value:digest)hex.append(String.format("%02x",value&255));require(expected.equals(hex.toString()),path+" SHA-256");
    }
    private static byte[] read(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))>=0)out.write(buffer,0,count);return out.toByteArray();}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private ModernAllayEntityFixture120(){}
}
