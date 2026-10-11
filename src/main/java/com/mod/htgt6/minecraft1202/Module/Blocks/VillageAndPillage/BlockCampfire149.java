package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.block.Block;import net.minecraft.block.BlockContainer;import net.minecraft.block.material.Material;
import net.minecraft.world.*;import net.minecraft.tileentity.TileEntity;import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.enchantment.EnchantmentHelper;import net.minecraft.util.*;import java.util.Random;
/** facing 0..3, lit bit4, retained water bit8. Water has explicit bucket support. */
public final class BlockCampfire149 extends BlockContainer implements com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 {
 public final boolean soul;
 public BlockCampfire149(){this(false);}
 public BlockCampfire149(boolean soul){super(Material.wood);this.soul=soul;setBlockName("village_pillage_campfire");setBlockTextureName("htgt6:village_pillage/campfire_log");setHardness(2F);setResistance(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.legacyResistance(2F));setStepSound(soundTypeWood);setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);setBlockBounds(0,0,0,1,7F/16,1);if(soul){setBlockName("backport_1_20.soul_campfire");setBlockTextureName("htgt6:1_20_backport/soul_campfire_log_lit");}}
 @Override public boolean isWaterlogged(IBlockAccess w,int x,int y,int z){return waterlogged(w.getBlockMetadata(x,y,z));}
 @Override public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){int meta=w.getBlockMetadata(x,y,z);if(w.isRemote||waterlogged(meta)==wet)return false;boolean accepted=updateState(w,x,y,z,wet?(meta|8)&~4:meta&~8);if(accepted&&wet&&lit(meta))dowse(w,x,y,z);return accepted;}
 public static boolean lit(int meta){return(meta&4)!=0&&(meta&8)==0;}public static boolean waterlogged(int meta){return(meta&8)!=0;}
 @Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}
 @Override public int getRenderType(){return VillagePillageContent.getJsonRenderId();}
 @Override public TileEntity createNewTileEntity(World w,int meta){return new TileEntityCampfire149();}
 @Override public int getLightValue(IBlockAccess w,int x,int y,int z){return lit(w.getBlockMetadata(x,y,z))?(soul?10:15):0;}
 @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase e,ItemStack stack){int facing=MathHelper.floor_double(e.rotationYaw*4F/360F+.5D)&3;w.setBlockMetadataWithNotify(x,y,z,(w.getBlockMetadata(x,y,z)&12)|facing,3);}
 public boolean setLit(World w,int x,int y,int z,boolean burning){int meta=w.getBlockMetadata(x,y,z);if(w.isRemote||burning&&waterlogged(meta))return false;return updateState(w,x,y,z,burning?meta|4:meta&~4);}
 @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){
  ItemStack held=p.getHeldItem();if(held==null||!p.canPlayerEdit(x,y,z,side,held))return false;Item item=held.getItem();int meta=w.getBlockMetadata(x,y,z);
  if(item==Items.water_bucket&&!waterlogged(meta)){
   if(w.isRemote)return true;
   if(w.provider.isHellWorld){if(!p.capabilities.isCreativeMode)p.inventory.setInventorySlotContents(p.inventory.currentItem,new ItemStack(Items.bucket));dowse(w,x,y,z);return true;}
   if(!updateState(w,x,y,z,(meta|8)&~4))return false;
   if(!p.capabilities.isCreativeMode)p.inventory.setInventorySlotContents(p.inventory.currentItem,new ItemStack(Items.bucket));dowse(w,x,y,z);return true;
  }
  if(item==Items.bucket&&waterlogged(meta)){
   if(w.isRemote)return true;if(!updateState(w,x,y,z,meta&~8))return false;
   if(!p.capabilities.isCreativeMode){held.stackSize--;ItemStack water=new ItemStack(Items.water_bucket);if(held.stackSize<=0)p.inventory.setInventorySlotContents(p.inventory.currentItem,water);else if(!p.inventory.addItemStackToInventory(water))p.dropPlayerItemWithRandomChoice(water,false);}return true;
  }
  if(!lit(meta)&&!waterlogged(meta)&&(item==Items.flint_and_steel||item==Items.fire_charge)){
   if(w.isRemote)return true;if(!setLit(w,x,y,z,true))return false;
   if(!p.capabilities.isCreativeMode){if(item==Items.flint_and_steel)held.damageItem(1,p);else held.stackSize--;}
   w.playSoundEffect(x+.5,y+.5,z+.5,"fire.ignite",1F,w.rand.nextFloat()*.4F+.8F);return true;
  }
  if(lit(meta)&&(item instanceof ItemSpade||item.getToolClasses(held).contains("shovel"))){if(w.isRemote)return true;if(!setLit(w,x,y,z,false))return false;dowse(w,x,y,z);if(!p.capabilities.isCreativeMode)held.damageItem(1,p);return true;}
  TileEntity tile=w.getTileEntity(x,y,z);if(!(tile instanceof TileEntityCampfire149)||com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.CampfireRecipes149.result(held)==null)return false;
  return w.isRemote||((TileEntityCampfire149)tile).insert(held,p.capabilities.isCreativeMode);
 }
 private static boolean updateState(World w,int x,int y,int z,int meta){boolean before=lit(w.getBlockMetadata(x,y,z));if(!w.setBlockMetadataWithNotify(x,y,z,meta,3))return false;if(before!=lit(meta))w.updateLightByType(EnumSkyBlock.Block,x,y,z);return true;}
 private static void dowse(World w,int x,int y,int z){w.playSoundEffect(x+.5,y+.5,z+.5,"random.fizz",.5F,2.6F+(w.rand.nextFloat()-w.rand.nextFloat())*.8F);}
 @Override public void onEntityCollidedWithBlock(World w,int x,int y,int z,Entity e){
  if(w.isRemote)return;int meta=w.getBlockMetadata(x,y,z);
  if(!lit(meta)&&e instanceof net.minecraft.entity.projectile.EntityArrow&&e.isBurning()){
   Entity shooter=((net.minecraft.entity.projectile.EntityArrow)e).shootingEntity;
   // Modern Projectile.mayInteract: player protection, mobGriefing for mobs,
   // and ownerless projectiles (for example dispensers) remain allowed.
   boolean allowed=shooter instanceof EntityPlayer?w.canMineBlock((EntityPlayer)shooter,x,y,z)
     :shooter==null||w.getGameRules().getGameRuleBooleanValue("mobGriefing");
   if(allowed)setLit(w,x,y,z,true);return;
  }
  if(lit(meta)&&e instanceof EntityLivingBase&&!e.isImmuneToFire()&&EnchantmentHelper.getEnchantmentLevel(CombatContent.frostWalker.effectId,((EntityLivingBase)e).getEquipmentInSlot(1))==0)e.attackEntityFrom(DamageSource.inFire,soul?2F:1F);
 }
 @Override public void breakBlock(World w,int x,int y,int z,Block old,int meta){TileEntity t=w.getTileEntity(x,y,z);if(t instanceof TileEntityCampfire149)((TileEntityCampfire149)t).dropContents();super.breakBlock(w,x,y,z,old,meta);}
 @Override public void onBlockDestroyedByPlayer(World w,int x,int y,int z,int meta){if(!w.isRemote&&waterlogged(meta)&&w.isAirBlock(x,y,z))w.setBlock(x,y,z,Blocks.water,0,3);}
 @Override protected boolean canSilkHarvest(){return true;}
 @Override public Item getItemDropped(int meta,Random r,int fortune){return soul?Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.soulSoil):Items.coal;}@Override public int damageDropped(int meta){return soul?0:1;}@Override public int quantityDropped(Random r){return soul?1:2;}
 @Override protected ItemStack createStackedBlock(int meta){return new ItemStack(this);}
 @Override public void randomDisplayTick(World w,int x,int y,int z,Random r){if(!lit(w.getBlockMetadata(x,y,z)))return;
  if(r.nextFloat()<.11F)w.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:block.campfire.crackle",.5F+r.nextFloat(),.6F+r.nextFloat()*.7F);
 }
 public static boolean isSmokeSource(World w,int x,int y,int z){return w.getBlock(x,y,z) instanceof BlockCampfire149&&lit(w.getBlockMetadata(x,y,z));}
}
