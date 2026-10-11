package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelParrot120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.RenderParrot120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120SpawnEgg;
import com.mod.htgt6.minecraft1202.Module.Registry.Aquatic.AquaticUpdate;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatUpdate;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Registration, rendering, NBT and dedicated-egg contract for the parrot backport. */
public final class ModernParrotEntityFixture120 {
    public static void main(String[] ignored)throws Exception{
        require(EntityParrot120.class.getSuperclass()==EntityTameable.class,"legacy tameable base");
        require(EntityParrot120.VARIANT_WATCHER>=0&&EntityParrot120.VARIANT_WATCHER<=31,"watcher range");
        require(EntityParrot120.class.getMethod("createChild",EntityAgeable.class)!=null,"ageable bridge");
        require(EntityParrot120.class.getMethod("writeEntityToNBT",NBTTagCompound.class)!=null,"NBT writer");
        require(RenderLiving.class.isAssignableFrom(RenderParrot120.class),"legacy renderer");
        ModelParrot120 model=new ModelParrot120();
        require(model.body.childModels == null || model.body.childModels.isEmpty(),
                "parrot root parts must not inherit body offset");
        require(model.head.childModels.size()==4,"parrot head, two-part beak and feather hierarchy");
        require(model.body.rotationPointY==16.5F&&model.body.rotationPointZ==-3.0F
                &&model.leftLeg.rotationPointY==22.0F,"official parrot root pivots");
        require(Backport120Entities.PARROT_MOD_ID==42,"non-conflicting mod entity id");
        requireModernNetworkIdsDoNotAliasAquaticEntities();
        require(Backport120Entities.PARROT_SPAWN_BIOMES.length==2,"jungle natural spawn list");
        require(Backport120Entities.CAMEL_SPAWN_BIOMES.length==0,"camel spawns only with desert village generation");
        require(Backport120Content.spawnEgg instanceof ItemBackport120SpawnEgg,"mod-local egg item");
        require("item.backport_1_20.spawn_egg.goat".equals(new ItemStack(Backport120Content.spawnEgg,1,0)
                .getUnlocalizedName()),"goat egg metadata");
        require("item.backport_1_20.spawn_egg.camel".equals(new ItemStack(Backport120Content.spawnEgg,1,1)
                .getUnlocalizedName()),"camel egg metadata");
        require("item.backport_1_20.spawn_egg.parrot".equals(new ItemStack(Backport120Content.spawnEgg,1,2)
                .getUnlocalizedName()),"parrot egg metadata");
        require("item.backport_1_20.spawn_egg.warden".equals(new ItemStack(Backport120Content.spawnEgg,1,3)
                .getUnlocalizedName()),"warden egg metadata");
        require("item.backport_1_20.spawn_egg.wandering_trader".equals(new ItemStack(Backport120Content.spawnEgg,1,4)
                .getUnlocalizedName()),"wandering trader egg metadata");
        require("item.backport_1_20.spawn_egg.trader_llama".equals(new ItemStack(Backport120Content.spawnEgg,1,5)
                .getUnlocalizedName()),"trader llama egg metadata");
        require(ItemBackport120SpawnEgg.entityType(ItemBackport120SpawnEgg.WARDEN)
                ==com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120.class,
                "warden egg factory");
        for(String variant:new String[]{"red_blue","blue","green","yellow_blue","grey"})
            image("assets/htgt6/textures/entity/parrot/parrot_"+variant+".png");
        System.out.println("Modern dedicated goat/camel/parrot/warden/wandering-trader/trader-llama spawn eggs OK");
    }
    private static void requireModernNetworkIdsDoNotAliasAquaticEntities(){
        int[] modern={Backport120Entities.GOAT_MOD_ID,Backport120Entities.ALLAY_MOD_ID,
                Backport120Entities.CAMEL_MOD_ID,Backport120Entities.PARROT_MOD_ID,
                Backport120Entities.WARDEN_MOD_ID,Backport120Entities.PANDA_MOD_ID,
                Backport120Entities.WANDERING_TRADER_MOD_ID,Backport120Entities.TRADER_LLAMA_MOD_ID};
        int[] aquatic={AquaticUpdate.DROWNED_MOD_ID,AquaticUpdate.TRIDENT_MOD_ID,
                AquaticUpdate.TURTLE_MOD_ID,AquaticUpdate.COD_MOD_ID,AquaticUpdate.SALMON_MOD_ID,
                AquaticUpdate.PUFFERFISH_MOD_ID,AquaticUpdate.TROPICAL_FISH_MOD_ID,
                AquaticUpdate.DOLPHIN_MOD_ID,AquaticUpdate.PHANTOM_MOD_ID};
        for(int i=0;i<modern.length;i++){
            for(int j=i+1;j<modern.length;j++)require(modern[i]!=modern[j],"unique modern network IDs");
            for(int aquaticId:aquatic)require(modern[i]!=aquaticId,
                    "modern network ID must not instantiate an aquatic entity");
            require(modern[i]!=CombatUpdate.COMBAT_BOAT_MOD_ID
                    &&modern[i]!=CombatUpdate.DRAGON_FIREBALL_MOD_ID
                    &&modern[i]!=CombatUpdate.COMBAT_BOAT_SEAT_MOD_ID,
                    "modern network ID must not instantiate a Combat entity");
        }
    }
    private static void image(String path)throws Exception{InputStream in=ModernParrotEntityFixture120.class
            .getClassLoader().getResourceAsStream(path);require(in!=null,path);BufferedImage image;
        try{image=ImageIO.read(in);}finally{in.close();}require(image!=null&&image.getWidth()==32&&image.getHeight()==32,
                path+" dimensions");}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private ModernParrotEntityFixture120(){}
}
