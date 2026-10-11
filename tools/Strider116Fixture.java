import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.*;
public final class Strider116Fixture {
    static int checks;
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
    public static void main(String[] args)throws Exception{
        Grid w=B11605Fixture.initialize();w.loaded=true;w.provider.dimensionId=-1;
        NetherVegetationContent116.register();StriderContent116.register();
        EntityStrider116 s=new EntityStrider116(w);s.setPosition(.5,80,.5);
        check(s.isImmuneToFire()&&s.width==.9F&&s.height==1.7F,"adult dimensions and fire immunity");
        double warm=s.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
        for(int i=0;i<100;i++)s.setCold(true);
        check(Math.abs(s.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()-warm*.66)<1E-7,"cold modifier never stacks");
        check(Math.abs(s.riddenSpeed()-warm*.66*.35)<1E-7,"cold mounted speed");
        s.setCold(false);check(Math.abs(s.riddenSpeed()-warm*.55)<1E-7,"warm speed restored");
        B11605Fixture.Player p=new B11605Fixture.Player(w);p.inventory=new B11605Fixture.OffhandInventory(p);p.inventory.mainInventory[0]=new ItemStack(Items.saddle);
        check(s.interact(p)&&s.isSaddled()&&p.getHeldItem()==null,"saddle consumed exactly once");
        check(s.interact(p)&&p.ridingEntity==s,"mount saddled adult");
        ItemStack stick=new ItemStack(StriderContent116.fungusStick);p.inventory.mainInventory[0]=stick;
        check(s.canBeSteered(),"main hand steering");
        StriderContent116.fungusStick.onItemRightClick(stick,w,p);
        check(stick.getItemDamage()==1&&!s.boost(),"boost costs one durability and cannot restart");
        int duration=s.getDataWatcher().getWatchableObjectInt(21);check(duration>=140&&duration<=980,"boost duration bounds");
        w.time=duration/2;check(s.boostMultiplier()>2.149F&&s.boostMultiplier()<=2.15F,"smooth boost peak");
        w.time=duration+1;check(s.boostMultiplier()==1,"boost expires");
        stick.setItemDamage(100);stick.setStackDisplayName("Kept name");ItemStack rod=StriderContent116.fungusStick.onItemRightClick(stick,w,p);
        check(rod.getItem()==Items.fishing_rod&&rod.hasDisplayName(),"broken stick returns rod with NBT");
        B11605Fixture.OffhandInventory off=new B11605Fixture.OffhandInventory(p);p.inventory=off;off.offhand[0]=new ItemStack(StriderContent116.fungusStick);
        check(s.canBeSteered(),"offhand steering");off.offhand[0]=null;check(!s.canBeSteered(),"no automatic control without stick");
        NBTTagCompound n=new NBTTagCompound();s.writeEntityToNBT(n);EntityStrider116 saved=new EntityStrider116(w);saved.readEntityFromNBT(n);check(saved.isSaddled(),"saddle survives save");
        EntityStrider116 baby=new EntityStrider116(w);baby.setGrowingAge(-24000);p.inventory.mainInventory[0]=new ItemStack(Items.saddle);baby.interact(p);check(!baby.isSaddled(),"baby cannot be saddled");
        p.inventory.mainInventory[0]=new ItemStack(NetherVegetationContent116.warpedFungus,2);check(baby.interact(p)&&baby.getGrowingAge()==-21600&&p.getHeldItem().stackSize==1,"baby fungus feeding");
        check(!s.isBreedingItem(new ItemStack(NetherVegetationContent116.crimsonFungus)),"crimson fungus not food");
        p.ridingEntity=null;s.riddenByEntity=null;s.setPosition(.5,79.5,.5);
        for(int x=-5;x<=8;x++)for(int z=-5;z<=5;z++)w.setBlock(x,79,z,Blocks.lava,0,3);
        check(s.lavaSurface()==79.5&&s.warm(),"source lava has half block stable surface");
        StriderNavigation116 nav=(StriderNavigation116)s.getNavigator();check(nav.walkable(0,80,0),"lava surface navigable");
        for(int y=80;y<84;y++)w.setBlock(2,y,0,Blocks.stone,0,3);
        check(!nav.walkable(2,80,0),"solid wall rejected");PathEntity path=nav.getPathToXYZ(4.5,80,.5);check(path!=null,"route around wall");
        boolean detour=false;for(int i=0;i<path.getCurrentPathLength();i++){PathPoint point=path.getPathPointFromIndex(i);check(!(point.xCoord==2&&point.zCoord==0),"path never crosses wall");detour|=point.zCoord!=0;}check(detour,"wall causes detour");
        w.setBlock(1,79,0,Blocks.water,0,3);check(!nav.walkable(1,80,0),"water excluded");
        w.loaded=false;check(!nav.walkable(0,80,0),"unloaded terrain excluded");w.loaded=true;
        check(nav.getPathToXYZ(100,80,0)==null,"search radius bounded");
        com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelStrider116 model=new com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelStrider116();check(model.textureWidth==64&&model.textureHeight==128,"official texture layout");
        w.playerEntities.clear();w.loadedEntityList.clear();p.setPosition(0,80,0);w.playerEntities.add(p);
        check(StriderNether116.remainingCapacity(w)==10,"single player passive cap");
        B11605Fixture.Player p2=new B11605Fixture.Player(w);p2.setPosition(0,80,0);w.playerEntities.add(p2);
        check(StriderNether116.remainingCapacity(w)==10,"overlapping players do not double cap");
        p2.setPosition(1024,80,0);check(StriderNether116.remainingCapacity(w)==20,"distant player adds independent cap");
        for(int i=0;i<10;i++){EntityStrider116 wild=new EntityStrider116(w);wild.setPosition(0,80,i);w.loadedEntityList.add(wild);}
        check(!StriderNether116.localCapacity(w,0,0)&&StriderNether116.localCapacity(w,1024,0),"distant spare budget cannot crowd existing herd");
        w.loadedEntityList.add(new EntityHoglin116(w));check(StriderNether116.remainingCapacity(w)==10,"hostile hoglins excluded from passive budget");
        check(new EntityHoglin116(w).isCreatureType(net.minecraft.entity.EnumCreatureType.monster,true)&&!new EntityHoglin116(w).isCreatureType(net.minecraft.entity.EnumCreatureType.creature,true),"hoglins only count as monsters");
        System.out.println("Strider 116: "+checks+" checks passed");
    }
}
