package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.*;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Convert only dyed vanilla oak signs; existing text and metadata survive the transaction. */
public final class SignDye114 {
    public static final BlockBackport120Sign OAK_STANDING=new BlockBackport120Sign("oak_sign",true),OAK_WALL=new BlockBackport120Sign("oak_wall_sign",false);
    private static final int[] RGB={0x1D1D21,0xB02E26,0x5E7C16,0x835432,0x3C44AA,0x8932B8,0x169C9C,0x9D9D97,0x474F52,0xF38BAA,0x80C71F,0xFED83D,0x3AB3DA,0xC74EBD,0xF9801D,0xF9FFFE};
    private static final int[] TEXT={0,0xFF0000,0x00FF00,9127187,255,10494192,65535,0xD3D3D3,0x808080,16738740,0xBFFF00,0xFFFF00,10141901,0xFF00FF,16738335,0xFFFFFF};
    public static int collarColor(int dye){return RGB[Math.max(0,Math.min(15,dye))];}
    public static int textColor(int dye){int c=TEXT[Math.max(0,Math.min(15,dye))];return ((int)(((c>>16)&255)*.4F)<<16)|((int)(((c>>8)&255)*.4F)<<8)|(int)((c&255)*.4F);}
    public static void init(){OAK_STANDING.setSignItem(Items.sign);OAK_WALL.setSignItem(Items.sign);GameRegistry.registerBlock(OAK_STANDING,ItemBlockBackport120Technical.class,"backport_1_14_dyed_oak_sign");GameRegistry.registerBlock(OAK_WALL,ItemBlockBackport120Technical.class,"backport_1_14_dyed_oak_wall_sign");net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new SignDye114());}
    public static boolean apply(World world,int x,int y,int z,int side,EntityPlayer player){
        ItemStack held=player.getHeldItem();int color=VillageWorkstationLogic120.dyeColor(held);
        TileEntity old=world.getTileEntity(x,y,z);Block block=world.getBlock(x,y,z);
        if(color<0||held.stackSize<1||!(old instanceof TileEntitySign)||!player.canPlayerEdit(x,y,z,side,held))return false;
        if(!(old instanceof TileEntityBackport120Sign)&&block!=Blocks.standing_sign&&block!=Blocks.wall_sign)return false;
        int current=old instanceof TileEntityBackport120Sign?((TileEntityBackport120Sign)old).getColor():0;
        if(current==color)return false;
        if(world.isRemote)return true;
        if(!(old instanceof TileEntityBackport120Sign)){
            String[] text=((TileEntitySign)old).signText.clone();int meta=world.getBlockMetadata(x,y,z);
            if(!world.setBlock(x,y,z,block==Blocks.standing_sign?OAK_STANDING:OAK_WALL,meta,3))return false;
            TileEntityBackport120Sign replacement=new TileEntityBackport120Sign();System.arraycopy(text,0,replacement.signText,0,4);world.setTileEntity(x,y,z,replacement);old=replacement;
        }
        ((TileEntityBackport120Sign)old).setColor(color);
        if(!player.capabilities.isCreativeMode)--held.stackSize;
        world.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:item.dye.use",1,1);return true;
    }
    @SubscribeEvent public void interact(PlayerInteractEvent event){if(event.action==PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK&&apply(event.world,event.x,event.y,event.z,event.face,event.entityPlayer))event.setCanceled(true);}
}
