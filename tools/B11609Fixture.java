import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.*;

public final class B11609Fixture {
    static int checks;
    static void check(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
    public static void main(String[] args)throws Exception {
        Grid w=B11605Fixture.initialize();
        Block[] sources={EtFVanillaGapContent120.basalt,Backport120Content.polishedBasalt,EtFVanillaGapContent120.polishedBlackstone,EtFVanillaGapContent120.quartzBricks};
        for(int i=0;i<sources.length;i++)GameRegistry.registerBlock(sources[i],"masonry_source_"+i);
        NetherMasonry116.register();int count=CraftingManager.getInstance().getRecipeList().size();NetherMasonry116.register();check(count==CraftingManager.getInstance().getRecipeList().size(),"idempotent registration");
        for(Block basalt:new Block[]{sources[0],sources[1]}) {
            for(int face=0;face<6;face++) {
                int m=basalt.onBlockPlaced(w,0,80,0,face,.5F,.5F,.5F,0);
                check(m==(face<2?0:face<4?8:4),"pillar placement on face "+face);
                check(basalt.damageDropped(m)==0,"axis never leaks into item damage");
            }
            w.setBlock(0,80,0,basalt,0,3);
            check(basalt.rotateBlock(w,0,80,0,ForgeDirection.EAST)&&w.getBlockMetadata(0,80,0)==8,"rotate about X");
            check(!basalt.rotateBlock(w,0,80,0,ForgeDirection.NORTH),"rotation around own axis is unchanged");
            check(!basalt.rotateBlock(w,0,80,0,ForgeDirection.UNKNOWN),"invalid rotation");
            check(basalt.stepSound.getBreakSound().equals("htgt6:block.basalt.break"),"external basalt sound");
        }
        InventoryCrafting craft=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
        craft.setInventorySlotContents(0,new ItemStack(sources[0]));craft.setInventorySlotContents(1,new ItemStack(sources[0]));craft.setInventorySlotContents(3,new ItemStack(sources[0]));craft.setInventorySlotContents(4,new ItemStack(sources[0]));
        result(craft,w,Backport120Content.polishedBasalt,4);
        clear(craft);craft.setInventorySlotContents(1,new ItemStack(Blocks.stone_slab,1,6));craft.setInventorySlotContents(4,new ItemStack(Blocks.stone_slab,1,6));result(craft,w,NetherMasonry116.chiseledNetherBricks,1);
        craft.setInventorySlotContents(1,new ItemStack(Blocks.stone_slab,1,7));check(CraftingManager.getInstance().findMatchingRecipe(craft,w)==null,"wrong slab rejected");
        clear(craft);craft.setInventorySlotContents(8,new ItemStack(sources[2]));result(craft,w,NetherMasonry116.button,1);
        clear(craft);craft.setInventorySlotContents(4,new ItemStack(sources[2]));craft.setInventorySlotContents(5,new ItemStack(sources[2]));result(craft,w,NetherMasonry116.pressurePlate,1);
        ItemStack smelt=FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(Blocks.nether_brick));check(smelt!=null&&Block.getBlockFromItem(smelt.getItem())==NetherMasonry116.crackedNetherBricks,"cracked brick smelting");
        for(Block source:new Block[]{sources[0],Blocks.nether_brick,Blocks.quartz_block}) {
            List<ItemStack> out=new ArrayList<ItemStack>();NetherMasonry116.appendStonecutting(new ItemStack(source),out);check(out.size()==1&&out.get(0).stackSize==1,"stonecut yield");NetherMasonry116.appendStonecutting(new ItemStack(source),out);check(out.size()==1,"deduplicated stonecut");
            out.clear();NetherMasonry116.appendStonecutting(new ItemStack(source,1,1),out);check(out.isEmpty(),"metadata-specific ingredients");
        }
        BlockBackport120Button button=NetherMasonry116.button;
        check(!button.getTickRandomly()&&button.tickRate(w)==20,"stone scheduled ticks only");
        BlockBackport120Button wood=new BlockBackport120Button("fixture_wood_button","oak_planks");
        check(!wood.getTickRandomly()&&wood.tickRate(w)==30,"wood scheduled ticks only");
        w.setBlock(0,80,0,button,0,3);TileEntityBackport120Button tile=new TileEntityBackport120Button();w.tiles.put("0,80,0",tile);
        EntityArrow arrow=new EntityArrow(w,.5,80.05,.5);w.loadedEntityList.add(arrow);
        button.onEntityCollidedWithBlock(w,0,80,0,arrow);check(w.getBlockMetadata(0,80,0)==0,"stone button ignores arrows");
        button.onBlockActivated(w,0,80,0,null,1,.5F,.5F,.5F);check(w.getBlockMetadata(0,80,0)==1&&w.lastDelay==20,"stone pulse 20 ticks");
        check(button.isProvidingWeakPower(w,0,80,0,0)==15,"button signal");
        button.updateTick(w,0,80,0,w.rand);check(w.getBlockMetadata(0,80,0)==0,"release even with nearby arrow");
        for(int face:new int[]{TileEntityBackport120Button.FLOOR,TileEntityBackport120Button.CEILING})for(int direction=0;direction<4;direction++) {
            tile.setOrientation(face,direction);button.setBlockBoundsBasedOnState(w,0,80,0);
            check(Math.abs((button.getBlockBoundsMaxX()-button.getBlockBoundsMinX())-((direction&1)==0?.375:.25))<1e-6,"rotated button X bounds");
            check(Math.abs((button.getBlockBoundsMaxZ()-button.getBlockBoundsMinZ())-((direction&1)==0?.25:.375))<1e-6,"rotated button Z bounds");
        }
        BlockBlackstonePressurePlate116 plate=NetherMasonry116.pressurePlate;check(!plate.getTickRandomly(),"plate scheduled ticks only");w.setBlock(0,80,0,plate,0,3);
        plate.onEntityCollidedWithBlock(w,0,80,0,arrow);check(w.getBlockMetadata(0,80,0)==0,"plate ignores arrows");
        net.minecraft.entity.item.EntityItem drop=new net.minecraft.entity.item.EntityItem(w,.5,80.05,.5,new ItemStack(Items.stick));w.loadedEntityList.add(drop);plate.onEntityCollidedWithBlock(w,0,80,0,drop);check(w.getBlockMetadata(0,80,0)==0,"plate ignores dropped items");
        net.minecraft.entity.passive.EntityPig pig=new net.minecraft.entity.passive.EntityPig(w);pig.setPosition(.5,80,.5);w.loadedEntityList.add(pig);plate.onEntityCollidedWithBlock(w,0,80,0,pig);check(w.getBlockMetadata(0,80,0)==1&&w.lastDelay==20,"living mob activates plate");
        pig.setPosition(5,80,5);plate.updateTick(w,0,80,0,w.rand);check(w.getBlockMetadata(0,80,0)==0,"plate releases when mob leaves");
        System.out.println("Nether masonry 1.16: "+checks+" checks passed");
    }
    static void clear(InventoryCrafting craft){for(int i=0;i<9;i++)craft.setInventorySlotContents(i,null);}
    static void result(InventoryCrafting craft,Grid w,Block expected,int count){ItemStack s=CraftingManager.getInstance().findMatchingRecipe(craft,w);check(s!=null&&Block.getBlockFromItem(s.getItem())==expected&&s.stackSize==count,"craft "+expected.getUnlocalizedName());}
}
