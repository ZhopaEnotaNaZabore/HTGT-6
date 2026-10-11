package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/** Four oxidation stages plus their waxed forms, packed into legacy metadata. */
public final class BlockBackport120CopperFamily extends Block {
    private static final String[] STAGES={"","exposed_","weathered_","oxidized_"};
    private final String family;
    private final boolean transparent;
    @SideOnly(Side.CLIENT) private IIcon[] icons;

    public BlockBackport120CopperFamily(String family,boolean transparent){
        super(Material.iron);this.family=family;this.transparent=transparent;
        setBlockName("backport_1_20."+family+"_family");setHardness(3.0F);
        setResistance(Backport120Content.legacyResistance(6.0F));setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe",1);setCreativeTab(CreativeTabs.tabBlock);setTickRandomly(true);
        if(transparent)setLightOpacity(0);
    }
    public String variantName(int metadata){
        int meta=metadata&7,stage=meta&3;
        return (meta>=4?"waxed_":"")+textureName(stage);
    }
    private String textureName(int stage){return family.equals("copper")&&stage==0?"copper_block":STAGES[stage]+family;}
    @Override public int damageDropped(int metadata){return metadata&7;}
    @Override public boolean isOpaqueCube(){return !transparent;}
    @Override public int getRenderBlockPass(){return transparent?1:0;}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register){
        icons=new IIcon[4];for(int i=0;i<4;i++)icons[i]=register.registerIcon("htgt6:1_20_backport/"+textureName(i));blockIcon=icons[0];
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side,int metadata){return icons[metadata&3];}
    @SuppressWarnings({"rawtypes","unchecked"}) @SideOnly(Side.CLIENT)
    @Override public void getSubBlocks(Item item,CreativeTabs tab,List list){for(int i=0;i<8;i++)list.add(new ItemStack(item,1,i));}
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        int meta=world.getBlockMetadata(x,y,z)&7;
        if(world.isRemote||meta>=3||random.nextFloat()>=0.05688889F)return;
        int same=0,older=0;
        // ChangeOverTimeBlock: Manhattan radius four, excluding the block itself.
        for(int dx=-4;dx<=4;dx++)for(int dy=-4;dy<=4;dy++)for(int dz=-4;dz<=4;dz++){
            int distance=Math.abs(dx)+Math.abs(dy)+Math.abs(dz);
            if(distance==0||distance>4||y+dy<0||y+dy>=world.getHeight())continue;
            // Do not load neighboring chunks from a random tick in the legacy engine.
            if(!world.getChunkProvider().chunkExists((x+dx)>>4,(z+dz)>>4))return;
            if(!(world.getBlock(x+dx,y+dy,z+dz) instanceof BlockBackport120CopperFamily))continue;
            int neighbor=world.getBlockMetadata(x+dx,y+dy,z+dz)&7;
            if(neighbor>=4)continue; // Waxed copper is not a weathering block in vanilla.
            if(neighbor<meta)return;
            if(neighbor==meta)same++;else older++;
        }
        float ratio=(older+1F)/(older+same+1F);
        if(random.nextFloat()<ratio*ratio*(meta==0?0.75F:1F))
            world.setBlockMetadataWithNotify(x,y,z,meta+1,3);
    }
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,
            int side,float hitX,float hitY,float hitZ){
        ItemStack held=player.getCurrentEquippedItem();if(held==null)return false;
        int meta=world.getBlockMetadata(x,y,z)&7;
        if(held.getItem()==EtFVanillaGapContent120.honeycomb&&meta<4){
            if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,meta+4,3);consume(player,held);}return true;
        }
        if(held.getItem() instanceof ItemAxe){
            int changed=meta>=4?meta-4:meta>0?meta-1:meta;
            if(changed!=meta){if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,changed,3);held.damageItem(1,player);}return true;}
        }
        return false;
    }
    private static void consume(EntityPlayer player,ItemStack stack){if(!player.capabilities.isCreativeMode&&--stack.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}
}
