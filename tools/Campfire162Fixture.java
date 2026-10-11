package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityCampfire149;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.ArrayList;

/** Written before reading the second ChatGPT response. */
public final class Campfire162Fixture {
    private static int passed, failed;
    private static void check(boolean ok,String name) {
        System.out.println((ok?"PASS ":"FAIL ")+name);
        if(ok)passed++;else failed++;
    }
    private static NBTTagCompound entry(int slot,int count,int time) {
        NBTTagCompound n=new NBTTagCompound();
        new ItemStack(Items.beef,count).writeToNBT(n);
        n.setInteger("Slot",slot);n.setInteger("CookingTime",time);return n;
    }
    private static void load(TileEntityCampfire149 tile,NBTTagCompound... entries) {
        NBTTagCompound n=new NBTTagCompound();NBTTagList list=new NBTTagList();
        for(NBTTagCompound e:entries)list.appendTag(e);n.setTag("Items",list);tile.readFromNBT(n);
    }
    public static void main(String[] args)throws Exception {
        BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
        cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(TileEntityCampfire149.class,"fixture_campfire162");
        TileEntityCampfire149 tile=new TileEntityCampfire149();
        for(int bad:new int[]{-1,4,256,257,Integer.MAX_VALUE}) {
            load(tile,entry(bad,1,50));boolean empty=true;
            for(int i=0;i<4;i++)empty&=tile.item(i)==null;
            check(empty,"reject slot "+bad);
        }
        NBTTagCompound missing=entry(0,1,10);missing.removeTag("Slot");load(tile,missing);
        check(tile.item(0)==null,"missing slot is not zero");
        missing.setString("Slot","0");load(tile,missing);check(tile.item(0)==null,"string slot rejected");
        for(int count:new int[]{0,-1}) {
            load(tile,entry(0,count,10));check(tile.item(0)==null,"reject count "+count);
        }
        load(tile,entry(1,8,900));check(tile.item(1)!=null&&tile.item(1).stackSize==1&&tile.progress(1)==600,"normalize count and high time");
        NBTTagCompound legacy=entry(2,1,-5);legacy.setByte("Slot",(byte)2);load(tile,legacy);
        check(tile.item(2)!=null&&tile.progress(2)==0,"legacy byte slot and negative time");
        legacy.removeTag("CookingTime");load(tile,legacy);check(tile.progress(2)==0,"missing time");
        load(tile,entry(0,1,45),entry(0,1,90));check(tile.progress(0)==45,"first valid duplicate wins");
        load(tile,entry(0,0,45),entry(0,1,90));check(tile.progress(0)==90,"invalid record does not reserve slot");
        load(tile,entry(0,1,45),entry(0,0,90));check(tile.progress(0)==45,"invalid duplicate preserves valid record");
        ItemStack custom=new ItemStack(Items.stick,5,7);NBTTagCompound customTag=new NBTTagCompound();customTag.setString("owner","test");custom.setTagCompound(customTag);
        NBTTagCompound saved=new NBTTagCompound();custom.writeToNBT(saved);saved.setByte("Slot",(byte)3);load(tile,saved);
        check(tile.item(3)!=null&&tile.item(3).getItem()==Items.stick&&tile.item(3).getItemDamage()==7&&"test".equals(tile.item(3).getTagCompound().getString("owner")),"preserve legacy nonrecipe and custom NBT");
        NBTTagCompound roundtrip=new NBTTagCompound();tile.writeToNBT(roundtrip);TileEntityCampfire149 copy=new TileEntityCampfire149();copy.readFromNBT(roundtrip);
        check(copy.item(3)!=null&&copy.item(3).stackSize==1,"roundtrip remains one item");
        tile.readFromNBT(new NBTTagCompound());check(tile.item(3)==null&&tile.progress(3)==0,"reload clears previous state");
        load(tile,entry(0,1,5));NBTTagCompound wrong=new NBTTagCompound();wrong.setString("Items","bad");tile.readFromNBT(wrong);check(tile.item(0)==null,"wrong Items type clears state");

        sun.misc.Unsafe unsafe=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
        Campfire149Fixture.TestWorld w=(Campfire149Fixture.TestWorld)unsafe.allocateInstance(Campfire149Fixture.TestWorld.class);
        for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
        Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;
        w.block=VillagePillageContent.campfire;w.meta=4;w.spawns=new ArrayList();tile.setWorldObj(w);tile.yCoord=10;w.tile=tile;
        check(!tile.insert(null,false),"null input rejected");
        for(int count:new int[]{0,-1}){ItemStack bad=new ItemStack(Items.beef,count);check(!tile.insert(bad,false)&&bad.stackSize==count,"invalid insert unchanged "+count);}
        ItemStack input=new ItemStack(Items.beef,3);input.setTagCompound((NBTTagCompound)customTag.copy());
        check(tile.insert(input,false)&&input.stackSize==2&&tile.item(0).stackSize==1&&tile.progress(0)==0,"survival takes one");
        input.getTagCompound().setString("owner","changed");check("test".equals(tile.item(0).getTagCompound().getString("owner")),"deep copy on insert");
        check(tile.insert(input,true)&&input.stackSize==2,"creative does not consume");
        tile.insert(input,true);tile.insert(input,true);check(!tile.insert(input,false)&&input.stackSize==2,"full does not consume");
        load(tile);w.isRemote=true;check(!tile.insert(input,false)&&tile.item(0)==null,"client rejects insert");
        w.isRemote=false;tile.setWorldObj(null);check(!tile.insert(input,false),"missing world rejects insert");
        System.out.println("CAMPFIRE162 passed="+passed+" failed="+failed);
        if(failed>0)throw new AssertionError("Failed checks: "+failed);
    }
}
