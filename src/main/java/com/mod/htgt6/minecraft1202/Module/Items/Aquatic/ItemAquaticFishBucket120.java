package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/** 1.20-style water-filled mob bucket for all four Aquatic fish identities. */
public final class ItemAquaticFishBucket120 extends Item {
    private static final String[] NAMES={"cod","salmon","pufferfish","tropical_fish"};
    private final IIcon[] icons=new IIcon[4];

    public ItemAquaticFishBucket120(){
        setUnlocalizedName("fish_bucket");setTextureName("htgt6:cod_bucket");
        setHasSubtypes(true);setMaxDamage(0);setMaxStackSize(1);setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override public String getUnlocalizedName(ItemStack stack){return "item."+NAMES[clampSpecies(stack.getItemDamage())]+"_bucket";}
    @Override @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister register){for(int i=0;i<4;i++)icons[i]=register.registerIcon("htgt6:"+NAMES[i]+"_bucket");itemIcon=icons[0];}
    @Override @SideOnly(Side.CLIENT) public IIcon getIconFromDamage(int meta){return icons[clampSpecies(meta)];}
    @Override @SuppressWarnings({"rawtypes","unchecked"}) public void getSubItems(Item item,CreativeTabs tab,List list){for(int i=0;i<4;i++)list.add(new ItemStack(item,1,i));}

    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){
        if(stack==null||stack.stackSize<=0)return stack;
        MovingObjectPosition hit=getMovingObjectPositionFromPlayer(world,player,true);
        if(hit==null||hit.typeOfHit!=MovingObjectPosition.MovingObjectType.BLOCK)return stack;
        int x=hit.blockX+Facing.offsetsXForSide[hit.sideHit];
        int y=hit.blockY+Facing.offsetsYForSide[hit.sideHit];
        int z=hit.blockZ+Facing.offsetsZForSide[hit.sideHit];
        if(!player.canPlayerEdit(x,y,z,hit.sideHit,stack)||!canPlaceWater(world,x,y,z))return stack;
        if(world.isRemote)return stack;

        Block previousBlock=world.getBlock(x,y,z);
        int previousMetadata=world.getBlockMetadata(x,y,z);
        boolean waterPlaced,placedNewWater=false;
        if(world.provider.isHellWorld){
            waterPlaced=false;
            world.playSoundEffect(x+.5D,y+.5D,z+.5D,"random.fizz",.5F,2.6F+(world.rand.nextFloat()-world.rand.nextFloat())*.8F);
            for(int i=0;i<8;i++)world.spawnParticle("largesmoke",x+world.rand.nextDouble(),y+world.rand.nextDouble(),z+world.rand.nextDouble(),0,0,0);
        }else{
            Block old=world.getBlock(x,y,z);
            boolean existingSource=old==Blocks.water&&world.getBlockMetadata(x,y,z)==0;
            waterPlaced=existingSource||world.setBlock(x,y,z,Blocks.water,0,3);
            placedNewWater=waterPlaced&&!existingSource;
            if(!waterPlaced)return stack;
        }

        EntityAquaticFish120 fish=create(world,stack.getItemDamage());
        fish.readFromBucket(stack);
        fish.setLocationAndAngles(x+.5D,y+.5D,z+.5D,world.rand.nextFloat()*360F,0);
        if(!world.spawnEntityInWorld(fish)){
            if(placedNewWater && world.getBlock(x,y,z)==Blocks.water && world.getBlockMetadata(x,y,z)==0)
                world.setBlock(x,y,z,previousBlock,previousMetadata,3);
            return stack;
        }
        world.playSoundAtEntity(fish,"game.neutral.swim",1.0F,1.0F);
        return player.capabilities.isCreativeMode?stack:new ItemStack(Items.bucket);
    }

    private static boolean canPlaceWater(World world,int x,int y,int z){
        Block block=world.getBlock(x,y,z);Material material=block.getMaterial();
        return world.isAirBlock(x,y,z)||material==Material.water||block.isReplaceable(world,x,y,z);
    }

    public static EntityAquaticFish120 create(World world,int meta){switch(clampSpecies(meta)){
        case 1:return new EntityAquaticFish120.Salmon(world);
        case 2:return new EntityAquaticFish120.Pufferfish(world);
        case 3:return new EntityAquaticFish120.Tropical(world);
        default:return new EntityAquaticFish120.Cod(world);
    }}
    public static Class<? extends EntityAquaticFish120> entityClassForSpecies(int meta){switch(clampSpecies(meta)){
        case 1:return EntityAquaticFish120.Salmon.class;
        case 2:return EntityAquaticFish120.Pufferfish.class;
        case 3:return EntityAquaticFish120.Tropical.class;
        default:return EntityAquaticFish120.Cod.class;
    }}
    public static int clampSpecies(int meta){return Math.max(0,Math.min(3,meta));}
}
