package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.IVillagePoiBlock120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageWorkstationType120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Minecraft1202;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import java.util.List;
import java.util.Random;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

/** A distinct 1.20 job-site block. Its identity is also the POI type. */
public final class BlockVillagePoi120 extends Block implements IVillagePoiBlock120 {
    private final String poiType;
    private final String kind;

    public BlockVillagePoi120(String name, String poiType, Material material, float hardness) {
        super(material);
        this.poiType = poiType;
        this.kind = name;
        setBlockName("village_pillage_" + name);
        setHardness(hardness);
        setResistance(Backport120Content.legacyResistance(hardness));
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockTextureName("htgt6:village_pillage/" + fallbackTexture(name));
    }

    public String getPoiType() { return poiType; }

    @Override public int getRenderType() { return VillagePillageContent.getJsonRenderId(); }

    @Override public boolean hasTileEntity(int metadata) { return "bell".equals(kind)||"lectern".equals(kind)||"composter".equals(kind); }
    @Override public TileEntity createTileEntity(World world, int metadata) {
        if("bell".equals(kind))return new TileEntityVillageBell120();
        if("lectern".equals(kind))return new TileEntityVillageLectern120();
        return "composter".equals(kind)?new TileEntityVillageComposter120():null;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = supportsFacing() ? MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3 : 0;
        if ("bell".equals(kind) || "grindstone".equals(kind)) {
            int placed = world.getBlockMetadata(x, y, z);
            facing = PoiAttachment120.withPlacerFacing("bell".equals(kind), placed, facing);
        }
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
        VillagePoiData120.register(world, x, y, z, poiType);
    }

    @Override public int onBlockPlaced(World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ, int metadata) {
        if ("grindstone".equals(kind)) return PoiAttachment120.grindstone(side, 0);
        if ("bell".equals(kind)) return Math.max(0, PoiAttachment120.bell(side, 0, supportMask(world, x, y, z)));
        return super.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, metadata);
    }

    @Override public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        return "bell".equals(kind) ? super.canPlaceBlockAt(world, x, y, z)
                && PoiAttachment120.bell(side, 0, supportMask(world, x, y, z)) >= 0
                : super.canPlaceBlockOnSide(world, x, y, z, side);
    }

    private static int supportMask(World world, int x, int y, int z) {
        int mask = 0;
        for (net.minecraftforge.common.util.ForgeDirection direction : net.minecraftforge.common.util.ForgeDirection.VALID_DIRECTIONS)
            if (world.isSideSolid(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ,
                    direction.getOpposite())) mask |= 1 << direction.ordinal();
        return mask;
    }

    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block changed) {
        super.onNeighborBlockChange(world, x, y, z, changed);
        if (!"bell".equals(kind) || world.isRemote) return;
        int old = world.getBlockMetadata(x, y, z);
        int next = PoiAttachment120.updateBell(old, supportMask(world, x, y, z));
        if (next < 0) {
            dropBlockAsItem(world, x, y, z, old, 0);
            world.setBlockToAir(x, y, z);
        } else {
            if (next != old) world.setBlockMetadataWithNotify(x, y, z, next, 3);
            TileEntity tile=world.getTileEntity(x,y,z);
            if(tile instanceof TileEntityVillageBell120
                    &&((TileEntityVillageBell120)tile).updatePower(world.isBlockIndirectlyGettingPowered(x,y,z)))
                ringBell(world,x,y,z,-1);
        }
    }

    @Override public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        VillagePoiData120.register(world, x, y, z, poiType);
    }

    @Override public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        VillagePoiData120.remove(world, x, y, z);
        if("lectern".equals(kind)){
            TileEntity tile=world.getTileEntity(x,y,z);
            if(tile instanceof TileEntityVillageLectern120){ItemStack book=((TileEntityVillageLectern120)tile).getStackInSlot(0);if(book!=null)dropBlockAsItem(world,x,y,z,book);}
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override public boolean isOpaqueCube(){return !isShaped();}
    @Override public boolean renderAsNormalBlock(){return !isShaped();}
    @Override public void addCollisionBoxesToList(World world,int x,int y,int z,AxisAlignedBB mask,List boxes,net.minecraft.entity.Entity entity){
        if(!"composter".equals(kind)){
            if("stonecutter".equals(kind))setBlockBoundsBasedOnState(world,x,y,z);
            super.addCollisionBoxesToList(world,x,y,z,mask,boxes,entity);return;
        }
        addBox(world,x,y,z,mask,boxes,0,0,0,1,.125F,1);
        addBox(world,x,y,z,mask,boxes,0,0,0,.125F,1,1);
        addBox(world,x,y,z,mask,boxes,.875F,0,0,1,1,1);
        addBox(world,x,y,z,mask,boxes,.125F,0,0,.875F,1,.125F);
        addBox(world,x,y,z,mask,boxes,.125F,0,.875F,.875F,1,1);
        setBlockBounds(0,0,0,1,1,1);
    }
    private void addBox(World world,int x,int y,int z,AxisAlignedBB mask,List boxes,float minX,float minY,float minZ,float maxX,float maxY,float maxZ){setBlockBounds(minX,minY,minZ,maxX,maxY,maxZ);super.addCollisionBoxesToList(world,x,y,z,mask,boxes,null);}
    @Override public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world,int x,int y,int z){
        if("stonecutter".equals(kind))setBlockBounds(0,0,0,1,.5625F,1);
        else if("grindstone".equals(kind))setBlockBounds(.125F,0,.125F,.875F,1,.875F);
        else if("bell".equals(kind))setBlockBounds(.25F,0,.25F,.75F,.875F,.75F);
        else if("lectern".equals(kind))setBlockBounds(0,0,0,1,1,1);
        else if("composter".equals(kind))setBlockBounds(0,0,0,1,1,1);
        else setBlockBounds(0,0,0,1,1,1);
    }

    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hitX,float hitY,float hitZ){
        if("composter".equals(kind))return compost(world,x,y,z,player);
        if("bell".equals(kind)){
            if(!PoiAttachment120.canRing(world.getBlockMetadata(x,y,z),side,hitY))return false;
            ringBell(world,x,y,z,side);
            return true;
        }
        if("lectern".equals(kind)){
            TileEntity tile=world.getTileEntity(x,y,z);
            if(!(tile instanceof TileEntityVillageLectern120))return false;
            TileEntityVillageLectern120 lectern=(TileEntityVillageLectern120)tile;
            if(lectern.getStackInSlot(0)==null){
                ItemStack held=player.getCurrentEquippedItem();
                if(!lectern.isItemValidForSlot(0,held))return false;
                if(!world.isRemote){ItemStack placed=held.copy();placed.stackSize=1;lectern.setInventorySlotContents(0,placed);if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);player.inventory.markDirty();world.notifyBlocksOfNeighborChange(x,y,z,this);}
                return true;
            }
        }
        VillageWorkstationType120 menu=VillageWorkstationType120.byBlockName(kind);
        if(menu!=null){if(!world.isRemote)player.openGui(Minecraft1202.instance,menu.guiId,world,x,y,z);return true;}
        return false;
    }

    private static void ringBell(World world,int x,int y,int z,int side) {
        if(world.isRemote)return;
        TileEntity tile=world.getTileEntity(x,y,z);
        if(!(tile instanceof TileEntityVillageBell120))return;
        ((TileEntityVillageBell120)tile).ring(side,world.getBlockMetadata(x,y,z)&3);
        world.playSoundEffect(x+.5D,y+.5D,z+.5D,"htgt6:block.bell.use",2F,1F);
    }
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        if(!world.isRemote&&"composter".equals(kind)&&(world.getBlockMetadata(x,y,z)&15)==7
                &&world.setBlockMetadataWithNotify(x,y,z,8,3))composterSound(world,x,y,z,"ready");
    }

    @Override public boolean hasComparatorInputOverride(){return "composter".equals(kind)||"lectern".equals(kind);}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){
        if("composter".equals(kind))return world.getBlockMetadata(x,y,z)&15;
        TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityVillageLectern120
                ?((TileEntityVillageLectern120)tile).comparatorLevel():0;
    }
    @Override public int damageDropped(int metadata){return 0;}

    private boolean compost(World world,int x,int y,int z,EntityPlayer player){
        int level=world.getBlockMetadata(x,y,z)&15;
        if(level==8){
            if(!world.isRemote){
                // Modern composters eject produce at the block, including in creative.
                net.minecraft.entity.item.EntityItem meal=new net.minecraft.entity.item.EntityItem(world,
                        x+.5D+(world.rand.nextFloat()-.5D)*.7D,
                        y+1.01D+(world.rand.nextFloat()-.5D)*.7D,
                        z+.5D+(world.rand.nextFloat()-.5D)*.7D,new ItemStack(Items.dye,1,15));
                meal.delayBeforeCanPickup=10;
                world.spawnEntityInWorld(meal);
                emptyComposter(world,x,y,z);
                composterSound(world,x,y,z,"empty");
            }
            return true;
        }
        ItemStack held=player.inventory.getCurrentItem();float chance=compostChance(held);if(chance<0F||level>7)return false;
        // Level seven still handles the click, but cannot consume another input.
        if(!world.isRemote&&level<7){insertCompost(world,x,y,z,held);if(!player.capabilities.isCreativeMode){--held.stackSize;if(held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}}
        return true;
    }

    static void insertCompost(World world,int x,int y,int z,ItemStack stack){
        if(world==null||world.isRemote||stack==null||stack.stackSize<=0)return;int level=world.getBlockMetadata(x,y,z)&15;float chance=compostChance(stack);
        if(level>=7||chance<0F)return;
        boolean grew=false;
        if(level==0||world.rand.nextDouble()<chance){
            int next=level+1;grew=world.setBlockMetadataWithNotify(x,y,z,next,3);
            if(grew&&next==7)world.scheduleBlockUpdate(x,y,z,VillagePillageContent.composter,20);
        }
        composterSound(world,x,y,z,grew?"fill_success":"fill");
        world.addBlockEvent(x,y,z,VillagePillageContent.composter,1,world.getBlockMetadata(x,y,z)&15);
    }
    @Override public boolean onBlockEventReceived(World world,int x,int y,int z,int id,int level){
        if(!"composter".equals(kind)||id!=1||level<0||level>8)return super.onBlockEventReceived(world,x,y,z,id,level);
        if(world.isRemote){
            double floor=Math.max(2,1+2*Math.min(level,7))/16D+.03125D;
            for(int i=0;i<10;i++){
                double vx=world.rand.nextGaussian()*.02D,vy=world.rand.nextGaussian()*.02D,vz=world.rand.nextGaussian()*.02D;
                world.spawnParticle("happyVillager",x+.13125D+.7375D*world.rand.nextFloat(),
                        y+floor+world.rand.nextFloat()*(1D-floor),z+.13125D+.7375D*world.rand.nextFloat(),vx,vy,vz);
            }
        }
        return true;
    }
    private static void composterSound(World world,int x,int y,int z,String event){
        world.playSoundEffect(x+.5D,y+.5D,z+.5D,"htgt6:block.composter."+event,1F,1F);
    }
    static void emptyComposter(World world,int x,int y,int z){if(world!=null)world.setBlockMetadataWithNotify(x,y,z,0,3);}

    static float compostChance(ItemStack stack){
        if(stack==null)return -1F;
        net.minecraft.item.Item item=stack.getItem();Block block=Block.getBlockFromItem(item);
        if(block==com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.warpedWart||block==com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.netherWartBlock)return .85F;
        if(block==com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.shroomlight)return .65F;
        if(block instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockNetherPlant116)return ((com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockNetherPlant116)block).sprouts?.5F:.65F;
        if(block instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockNetherVine116)return .5F;
        if(item==Items.cake||item==Items.pumpkin_pie)return 1F;
        if(item==Items.bread||item==Items.baked_potato||item==Items.cookie||block==Blocks.hay_block
                ||block==Blocks.brown_mushroom_block||block==Blocks.red_mushroom_block
                ||block==Backport120Content.floweringAzalea)return .85F;
        if(item==Items.apple||item==Items.carrot||item==Items.potato||item==Items.wheat
                ||block==Blocks.pumpkin||block==Blocks.melon_block||block==Blocks.red_flower
                ||block==Blocks.yellow_flower||block==Blocks.brown_mushroom||block==Blocks.red_mushroom
                ||block==Blocks.waterlily||item==Items.nether_wart||item==Items.dye&&stack.getItemDamage()==3
                ||block==Backport120Content.azalea||block==Backport120Content.bigDripleaf
                ||block==Backport120Content.bigDripleafStem||block==Backport120Content.sporeBlossom
                ||block==Backport120Content.mossBlock||block==OceanContent.marinePlant&&stack.getItemDamage()>=8)return .65F;
        if(item==Items.melon||item==Items.reeds||block==Blocks.cactus||block==Blocks.tallgrass||block==Blocks.vine||block==Backport120Content.glowLichen
                ||block==Backport120Content.caveVines||block==Backport120Content.caveVinesPlant
                ||block==Backport120Content.floweringAzaleaLeaves||block==OceanContent.driedKelpBlock)return .5F;
        if(item==Items.wheat_seeds||item==Items.melon_seeds||item==Items.pumpkin_seeds||block==Blocks.sapling
                ||block==Blocks.leaves||block==Blocks.leaves2||block==Backport120Content.mossCarpet
                ||block==Backport120Content.mangroveRoots||block==Backport120Content.cherrySapling
                ||block==Backport120Content.hangingRoots||block==Backport120Content.smallDripleaf
                ||block==Backport120Content.pinkPetals||block==OceanContent.kelp||block==OceanContent.kelpPlant
                ||block==OceanContent.marinePlant&&stack.getItemDamage()<=2||block==Backport120Content.azaleaLeaves
                ||item==Backport120Content.sweetBerries||item==Backport120Content.glowBerries||item==OceanContent.driedKelp)return .3F;
        return -1F;
    }
    /** Canonical 1.20.2 tier lookup used by the headless contract fixture. */
    public static float compostChanceForKey(String key){
        if("cake".equals(key)||"pumpkin_pie".equals(key))return 1F;
        if("bread".equals(key)||"baked_potato".equals(key)||"cookie".equals(key)||"hay_block".equals(key)
                ||"mushroom_block".equals(key)||"flowering_azalea".equals(key))return .85F;
        if("apple".equals(key)||"carrot".equals(key)||"potato".equals(key)||"wheat".equals(key)
                ||"flower".equals(key)||"pumpkin".equals(key)||"melon_block".equals(key)||"big_dripleaf".equals(key)
                ||"nether_wart".equals(key)||"cocoa_beans".equals(key))return .65F;
        if("melon_slice".equals(key)||"sugar_cane".equals(key)||"cactus".equals(key)||"vine".equals(key)
                ||"glow_lichen".equals(key)||"flowering_azalea_leaves".equals(key))return .5F;
        if("wheat_seeds".equals(key)||"melon_seeds".equals(key)||"pumpkin_seeds".equals(key)
                ||"sapling".equals(key)||"leaves".equals(key)||"kelp".equals(key)||"moss_carpet".equals(key)
                ||"small_dripleaf".equals(key)||"sweet_berries".equals(key))return .3F;
        return -1F;
    }
    private boolean supportsFacing(){return "lectern".equals(kind)||"stonecutter".equals(kind)||"loom".equals(kind)
            ||"grindstone".equals(kind)||"bell".equals(kind);}
    private boolean isShaped(){return "composter".equals(kind)||"lectern".equals(kind)||"stonecutter".equals(kind)
            ||"grindstone".equals(kind)||"bell".equals(kind);}
    private static String fallbackTexture(String name){
        if("cartography_table".equals(name))return "cartography_table_side1";
        if("lectern".equals(name))return "lectern_sides";
        return name+"_side";
    }
}
