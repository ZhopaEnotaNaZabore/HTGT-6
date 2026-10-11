import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.DamageSource;
import java.util.Random;

public final class SoulCampfire116Fixture {
    static int checks;
    static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);checks++;}
    static final class Victim extends EntityCow {
        float damage;
        Victim(Grid w){super(w);}
        public boolean attackEntityFrom(DamageSource source,float amount){damage+=amount;return true;}
    }
    public static void main(String[] args)throws Exception{
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        Grid w=B11605Fixture.initialize();
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(EtFVanillaGapContent120.soulSoil,"fixture_soul_soil");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(EtFVanillaGapContent120.cryingObsidian,"fixture_crying_obsidian");
        cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(TileEntityCampfire149.class,"fixture_campfire");
        NetherUtilityContent116.register();NetherUtilityContent116.register();NetherWoodContent116.register();
        BlockCampfire149 b=NetherUtilityContent116.soulCampfire;
        for(int state=0;state<16;state++){
            w.setBlock(0,80,0,b,state,3);
            boolean lit=(state&4)!=0&&(state&8)==0;
            check(b.getLightValue(w,0,80,0)==(lit?10:0),"light follows lit/water state "+state);
            Victim v=new Victim(w);b.onEntityCollidedWithBlock(w,0,80,0,v);
            check(v.damage==(lit?2:0),"two damage only when lit "+state);
        }
        check(b.getItemDropped(4,new Random(),3)==Item.getItemFromBlock(EtFVanillaGapContent120.soulSoil)&&b.quantityDropped(new Random())==1&&b.damageDropped(4)==0,"one soul soil without silk touch");
        java.lang.reflect.Method silk=BlockCampfire149.class.getDeclaredMethod("createStackedBlock",int.class);silk.setAccessible(true);
        check(((ItemStack)silk.invoke(b,12)).getItem()==Item.getItemFromBlock(b),"silk touch keeps campfire item");
        for(net.minecraft.block.Block base:new net.minecraft.block.Block[]{Blocks.soul_sand,EtFVanillaGapContent120.soulSoil}){
            ItemStack result=NetherWood116Fixture.craft(w,null,new ItemStack(Items.stick),null,new ItemStack(Items.stick),new ItemStack(base),new ItemStack(Items.stick),new ItemStack(NetherWoodContent116.CRIMSON.stem),new ItemStack(NetherWoodContent116.WARPED.hyphae),new ItemStack(NetherWoodContent116.CRIMSON.strippedStem));
            check(result!=null&&result.getItem()==Item.getItemFromBlock(b)&&result.stackSize==1,"both soil recipes with mixed Nether logs");
        }
        w.setBlock(0,80,0,b,4,3);TileEntityCampfire149 tile=new TileEntityCampfire149();tile.setWorldObj(w);tile.yCoord=80;w.tiles.put("0,80,0",tile);
        ItemStack food=new ItemStack(Items.beef,4);for(int i=0;i<4;i++)check(tile.insert(food,false),"four cooking slots");
        for(int i=0;i<100;i++)tile.updateEntity();NBTTagCompound saved=new NBTTagCompound();tile.writeToNBT(saved);
        TileEntityCampfire149 restored=new TileEntityCampfire149();restored.readFromNBT(saved);restored.setWorldObj(w);restored.yCoord=80;
        check(restored.progress(0)==100&&restored.item(3)!=null,"cooking state survives reload");
        w.setBlockMetadataWithNotify(0,80,0,8,3);restored.updateEntity();check(restored.progress(0)==98,"water extinguishes and cools food");
        check(!b.setLit(w,0,80,0,true),"cannot light waterlogged campfire");
        w.setBlockMetadataWithNotify(0,80,0,4,3);for(int i=98;i<600;i++)restored.updateEntity();
        for(int i=0;i<4;i++)check(restored.item(i)==null,"cooked output delivered once");
        check(((net.minecraft.entity.item.EntityItem)w.lastSpawn).getEntityItem().getItem()==Items.cooked_beef,"correct output");
        System.out.println("Soul campfire 116: "+checks+" checks passed");
    }
}
