package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGoat120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityTraderLlama120;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Mod-local spawn eggs which do not consume the scarce global 1.7 entity-ID table. */
public final class ItemBackport120SpawnEgg extends Item {
    public static final int GOAT=0,CAMEL=1,PARROT=2,WARDEN=3,WANDERING_TRADER=4,TRADER_LLAMA=5,CAT=6,FOX=7,MOOSHROOM=8,BEE=9,PIGLIN=10,ZOMBIFIED_PIGLIN=11,PIGLIN_BRUTE=12,HOGLIN=13,ZOGLIN=14,STRIDER=15;
    private static final String[] NAMES={"goat","camel","parrot","warden","wandering_trader","trader_llama","cat","fox","mooshroom","bee","piglin","zombified_piglin","piglin_brute","hoglin","zoglin","strider"};
    private static final int[] PRIMARY={0xAFAF9E,0xF2D6A7,0x0E7E25,0x0F4649,0x456296,0xE7E7E7,0xEFC88E,0xD5B69C,0xA00F10,0xEDC343,0x995F40,0xEA9393,0x592A10,0xC66E55,0xC66E55,0x9C3436};
    private static final int[] SECONDARY={0x59493E,0xCB8A54,0xE6D53A,0x39D6E0,0xEAA430,0x456296,0x957256,0xCC6920,0xB7B7B7,0x43241B,0xF9F3A4,0x4C7129,0xF9F3A4,0x5F6464,0xE6E6E6,0x4D494D};
    @SideOnly(Side.CLIENT) private IIcon overlay;

    public ItemBackport120SpawnEgg(){
        setUnlocalizedName("backport_1_20.spawn_egg");
        setHasSubtypes(true);setMaxDamage(0);setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override public String getUnlocalizedName(ItemStack stack){
        return super.getUnlocalizedName()+"."+NAMES[variant(stack.getItemDamage())];
    }

    @SideOnly(Side.CLIENT) @Override public void registerIcons(IIconRegister register){
        itemIcon=register.registerIcon("spawn_egg");overlay=register.registerIcon("spawn_egg_overlay");
    }
    @SideOnly(Side.CLIENT) @Override public boolean requiresMultipleRenderPasses(){return true;}
    @SideOnly(Side.CLIENT) @Override public int getRenderPasses(int metadata){return 2;}
    @SideOnly(Side.CLIENT) @Override public IIcon getIconFromDamageForRenderPass(int metadata,int pass){
        return pass==0?itemIcon:overlay;
    }
    @SideOnly(Side.CLIENT) @Override public int getColorFromItemStack(ItemStack stack,int pass){
        int index=variant(stack.getItemDamage());return pass==0?PRIMARY[index]:SECONDARY[index];
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    @Override public void getSubItems(Item item,CreativeTabs tab,List list){
        for(int metadata=0;metadata<NAMES.length;metadata++)list.add(new ItemStack(item,1,metadata));
    }

    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,
                                       int side,float hitX,float hitY,float hitZ){
        if(stack.stackSize<1||side<0||side>5)return false;
        if(variant(stack.getItemDamage())==BEE){com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBeehive115 hive=com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBeehive115.tile(world,x,y,z);
            if(hive!=null){if(hive.occupants()>=3||!player.canPlayerEdit(x,y,z,side,stack))return false;if(world.isRemote)return true;
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115 bee=new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115(world);bee.setPosition(x+.5,y+.5,z+.5);bee.setHome(x,y,z);if(!hive.enter(bee))return false;if(!player.capabilities.isCreativeMode)--stack.stackSize;return true;}
        }
        x+=Facing.offsetsXForSide[side];y+=Facing.offsetsYForSide[side];z+=Facing.offsetsZForSide[side];
        if(!world.blockExists(x,y,z)||!player.canPlayerEdit(x,y,z,side,stack))return false;
        if(world.isRemote)return true;
        EntityLiving entity=create(world,stack.getItemDamage());
        entity.setLocationAndAngles(x+.5D,y,z+.5D,world.rand.nextFloat()*360F,0F);
        entity.onSpawnWithEgg(null);
        if(stack.hasDisplayName())entity.setCustomNameTag(stack.getDisplayName());
        if(!world.spawnEntityInWorld(entity))return false;
        entity.playLivingSound();
        if(!player.capabilities.isCreativeMode)--stack.stackSize;
        return true;
    }

    public static EntityLiving create(World world,int metadata){
        switch(variant(metadata)){
            case CAMEL:return new EntityCamel120(world);
            case PARROT:return new EntityParrot120(world);
            case WARDEN:return new EntityWarden120(world);
            case WANDERING_TRADER:return new EntityWanderingTrader120(world);
            case TRADER_LLAMA:return new EntityTraderLlama120(world);
            case CAT:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCat114(world);
            case FOX:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114(world);
            case STRIDER:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116(world);
            case HOGLIN:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityHoglin116(world);
            case ZOGLIN:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZoglin116(world);
            case PIGLIN_BRUTE:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglinBrute116(world);
            case PIGLIN:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116(world);
            case ZOMBIFIED_PIGLIN:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZombifiedPiglin116(world);
            case BEE:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115(world);
            case MOOSHROOM:return new com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityMooshroom114(world);
            default:return new EntityGoat120(world);
        }
    }

    public static Class<? extends EntityLiving> entityType(int metadata){
        switch(variant(metadata)){
            case CAMEL:return EntityCamel120.class;
            case PARROT:return EntityParrot120.class;
            case WARDEN:return EntityWarden120.class;
            case WANDERING_TRADER:return EntityWanderingTrader120.class;
            case TRADER_LLAMA:return EntityTraderLlama120.class;
            case CAT:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCat114.class;
            case FOX:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114.class;
            case STRIDER:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116.class;
            case HOGLIN:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityHoglin116.class;
            case ZOGLIN:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZoglin116.class;
            case PIGLIN_BRUTE:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglinBrute116.class;
            case PIGLIN:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116.class;
            case ZOMBIFIED_PIGLIN:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZombifiedPiglin116.class;
            case BEE:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115.class;
            case MOOSHROOM:return com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityMooshroom114.class;
            default:return EntityGoat120.class;
        }
    }

    private static int variant(int metadata){return metadata<0||metadata>=NAMES.length?0:metadata;}
}
