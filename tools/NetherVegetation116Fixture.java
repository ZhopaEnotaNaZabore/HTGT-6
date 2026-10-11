import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherVegetation116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import java.util.*;

public final class NetherVegetation116Fixture {
    static int checks;
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
    public static void main(String[] args)throws Exception{
        Grid w=B11605Fixture.initialize();w.loaded=true;
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(EtFVanillaGapContent120.soulSoil,"fixture_soul_soil");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(FrostburnContent.netherWartBlock,"fixture_nether_wart_block");
        NetherWoodContent116.register();NetherVegetationContent116.register();NetherVegetationContent116.register();
        for(Block b:NetherVegetationContent116.BLOCKS)check(Item.getItemFromBlock(b)!=null,"registered "+b.getUnlocalizedName());
        for(boolean warped:new boolean[]{false,true}){
            BlockNylium116 soil=warped?NetherVegetationContent116.warpedNylium:NetherVegetationContent116.crimsonNylium;
            BlockNetherPlant116 fungus=warped?NetherVegetationContent116.warpedFungus:NetherVegetationContent116.crimsonFungus;
            w.setBlock(0,79,0,soil,0,3);w.setBlockToAir(0,80,0);
            check(soil.func_149851_a(w,0,79,0,false),"bone meal accepts exposed nylium");
            soil.updateTick(w,0,79,0,new Random());check(w.getBlock(0,79,0)==soil,"exposed nylium survives");
            w.setBlock(0,80,0,Blocks.stone,0,3);soil.updateTick(w,0,79,0,new Random());check(w.getBlock(0,79,0)==Blocks.netherrack,"covered nylium decays");
            w.setBlock(0,79,0,soil,0,3);w.setBlock(0,80,0,fungus,0,3);
            check(fungus.func_149851_a(w,0,80,0,false),"matching nylium permits fungus growth");
            w.setBlock(0,79,0,warped?NetherVegetationContent116.crimsonNylium:NetherVegetationContent116.warpedNylium,0,3);
            check(!fungus.func_149851_a(w,0,80,0,false)&&fungus.canBlockStay(w,0,80,0),"wrong nylium permits planting but not huge growth");
            int[] counts=new int[4];for(int i=0;i<(warped?100:99);i++){
                Block result=NetherVegetation116.vegetation(warped,i);
                counts[result==NetherVegetationContent116.crimsonRoots?0:result==NetherVegetationContent116.warpedRoots?1:result==NetherVegetationContent116.crimsonFungus?2:3]++;
            }
            check(Arrays.equals(counts,warped?new int[]{1,85,1,13}:new int[]{87,0,11,1}),"official vegetation weights");
            for(int seed=0;seed<20;seed++){
                int x=seed*10;w.setBlock(x,79,0,soil,0,3);w.setBlock(x,80,0,fungus,0,3);w.setBlock(x+2,85,0,Blocks.bedrock,0,3);
                check(NetherVegetation116.growFungus(w,new Random(seed),x,80,0,warped),"planted fungus grows seed "+seed);
                check(w.getBlock(x,80,0)==(warped?NetherWoodContent116.WARPED.stem:NetherWoodContent116.CRIMSON.stem),"owned stem at origin");
                check(w.getBlock(x+2,85,0)==Blocks.bedrock,"growth preserves solid structures");
            }
        }
        for(BlockNetherVine116[] family:new BlockNetherVine116[][]{NetherVegetationContent116.twisting,NetherVegetationContent116.weeping}){
            int d=family[0].direction,x=d*500,y=80;w.setBlock(x,y-d,0,Blocks.netherrack,0,3);
            for(int age=0;age<=25;age++){
                check(family[0].placeAge(w,x,y,0,age),"place all ages");BlockNetherVine116 block=(BlockNetherVine116)w.getBlock(x,y,0);
                check(block.age(w.getBlockMetadata(x,y,0))==age,"age survives four-bit metadata "+age);
                check(block.getItem(w,x,y,0)==Item.getItemFromBlock(family[0]),"technical age picks public item");
                check(block.isLadder(w,x,y,0,null)&&!block.hasTileEntity(w.getBlockMetadata(x,y,0)),"climbable without ticking tile entity");
            }
            check(family[1].getItemDropped(9,new Random(0),3)==Item.getItemFromBlock(family[0]),"Fortune III always drops");
            check(family[1].getItemDropped(9,new Random(){public float nextFloat(){return .999999F;}},3)==Item.getItemFromBlock(family[0]),"Fortune III upper boundary is 100 percent");
            check(family[2].onSheared(new ItemStack(Items.shears),w,x,y,0,0).get(0).getItem()==Item.getItemFromBlock(family[0]),"body shears public tip");
            w.setBlockToAir(x,y+d,0);family[0].placeAge(w,x,y,0,25);family[1].updateTick(w,x,y,0,new Random(0));check(w.isAirBlock(x,y+d,0),"mature head stops random growth");
            family[1].func_149853_b(w,new Random(0),x,y,0);check(family[0].same(w.getBlock(x,y+d,0)),"bone meal still grows mature vines");
            family[1].onNeighborBlockChange(w,x,y,0,w.getBlock(x,y+d,0));check(w.getBlock(x,y,0)==family[2],"extended head becomes body");
            w.setBlockToAir(x,y+d,0);family[2].onNeighborBlockChange(w,x,y,0,Blocks.air);check(((BlockNetherVine116)w.getBlock(x,y,0)).phase!=2,"broken column restores tip");
            w.setBlockToAir(x,y-d,0);((BlockNetherVine116)w.getBlock(x,y,0)).updateTick(w,x,y,0,new Random(0));check(w.isAirBlock(x,y,0),"unsupported vines break");
        }
        check(NetherVegetationContent116.sprouts.getItemDropped(0,new Random(),3)==null,"sprouts require shears");
        w.loaded=false;w.setBlock(0,79,0,NetherVegetationContent116.crimsonNylium,0,3);w.setBlock(0,80,0,NetherVegetationContent116.crimsonFungus,0,3);
        check(!NetherVegetation116.growFungus(w,new Random(0),0,80,0,false)&&w.getBlock(0,80,0)==NetherVegetationContent116.crimsonFungus,"growth never loads chunks or consumes source when unavailable");
        w.loaded=true;
        for(BlockPottedNether116 pot:NetherVegetationContent116.POTS){
            B11605Fixture.Player gardener=new B11605Fixture.Player(w);gardener.inventory.mainInventory[0]=new ItemStack(pot.plant,2);
            w.setBlock(0,80,0,Blocks.flower_pot,0,3);w.tiles.put("0,80,0",new net.minecraft.tileentity.TileEntityFlowerPot());
            net.minecraftforge.event.entity.player.PlayerInteractEvent e=new net.minecraftforge.event.entity.player.PlayerInteractEvent(gardener,net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,0,80,0,1,w);
            new NetherVegetation116().pot(e);check(e.isCanceled()&&w.getBlock(0,80,0)==pot&&gardener.getHeldItem().stackSize==1,"pot inserts exactly one plant");
            check(pot.getDrops(w,0,80,0,0,0).size()==2&&!pot.hasTileEntity(0),"pot drops container and plant without ticking tile");
            check(pot.onBlockActivated(w,0,80,0,gardener,1,0,0,0)&&w.getBlock(0,80,0)==Blocks.flower_pot&&gardener.getHeldItem().stackSize==2,"empty pot returns plant");
            e=new net.minecraftforge.event.entity.player.PlayerInteractEvent(gardener,net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,0,80,0,1,w);e.useBlock=cpw.mods.fml.common.eventhandler.Event.Result.DENY;
            new NetherVegetation116().pot(e);check(w.getBlock(0,80,0)==Blocks.flower_pot&&gardener.getHeldItem().stackSize==2,"denied planting leaves inventory intact");
        }
        check(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityHoglin116.repellent(NetherVegetationContent116.POTS[1]),"potted warped fungus repels hoglins");
        for(Item item:new Item[]{Items.wooden_hoe,Items.stone_hoe,Items.iron_hoe,Items.diamond_hoe,Items.golden_hoe}){
            ItemStack hoe=new ItemStack(item);float expected=com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoeMining116.material((ItemHoe)item).getEfficiencyOnProperMaterial();
            check(item.getDigSpeed(hoe,NetherVegetationContent116.shroomlight,0)==expected&&item.getDigSpeed(hoe,Blocks.leaves,0)==expected,"native hoes mine effective blocks at material speed");
            check(item.getIsRepairable(hoe,com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoeMining116.material((ItemHoe)item).getRepairItemStack()),"native hoe repairs with its material");
            check(item.getDigSpeed(hoe,Blocks.stone,0)==1&&item.getToolClasses(hoe).contains("hoe"),"hoe tool class without pickaxe efficiency");
            check(net.minecraft.enchantment.Enchantment.efficiency.canApply(hoe)&&net.minecraft.enchantment.Enchantment.silkTouch.canApply(hoe)&&item.getItemEnchantability()>0,"native hoes accept modern enchantments");
            B11605Fixture.Player miner=new B11605Fixture.Player(w);item.onBlockDestroyed(hoe,w,NetherVegetationContent116.shroomlight,0,80,0,miner);check(hoe.getItemDamage()==1,"native hoe mining uses one durability");
            item.hitEntity(hoe,miner,miner);check(hoe.getItemDamage()==3,"native hoe hit uses two durability");
        }
        NetherAssets116Fixture.verify();
        System.out.println("Nether vegetation 116: "+checks+" checks passed");
    }
}
