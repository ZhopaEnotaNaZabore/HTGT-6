package com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage;
import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.item.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;
public final class ItemBlockCampfire149 extends ItemBlock {
 public ItemBlockCampfire149(Block block){super(block);}
 @Override public boolean placeBlockAt(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz,int metadata){
  int state=world.getBlock(x,y,z).getMaterial()==Material.water?8:4;
  return super.placeBlockAt(stack,player,world,x,y,z,side,hx,hy,hz,state);
 }
 @Override public int getMetadata(int damage){return 4;}
 @Override public int getSpriteNumber(){return 1;}
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister register){itemIcon=register.registerIcon(((com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149)field_150939_a).soul?"htgt6:1_20_backport/soul_campfire":"htgt6:village_pillage/campfire");}
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 public net.minecraft.util.IIcon getIconFromDamage(int damage){return itemIcon;}
}
