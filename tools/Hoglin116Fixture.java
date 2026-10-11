import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import java.util.*;

public final class Hoglin116Fixture {
    static int checks;
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
    static final class Victim extends EntityCow {
        float damage;Victim(Grid w){super(w);setPosition(2,80,0);}
        public boolean attackEntityFrom(DamageSource source,float value){damage=value;return true;}
    }
    public static void main(String[] args)throws Exception{
        Grid w=B11605Fixture.initialize();w.loaded=true;w.provider.dimensionId=-1;w.difficultySetting=net.minecraft.world.EnumDifficulty.NORMAL;
        NetherVegetationContent116.register();
        EntityHoglin116 hog=new EntityHoglin116(w);hog.setPosition(.5,80,.5);
        check(hog.getMaxHealth()==40&&Math.abs(hog.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue()-.6)<1E-6,"health and fractional resistance");
        for(int i=0;i<100;i++){Victim victim=new Victim(w);check(hog.attackEntityAsMob(victim)&&victim.damage>=3&&victim.damage<=8,"adult randomized hit 3..8");check(victim.motionY>=0&&victim.motionY<=.5&&Math.sqrt(victim.motionX*victim.motionX+victim.motionZ*victim.motionZ)<=.700001,"bounded knockback impulse");}
        hog.setGrowingAge(-24000);Victim babyVictim=new Victim(w);hog.attackEntityAsMob(babyVictim);check(babyVictim.damage==.5F&&babyVictim.motionY==0,"baby damage and no throw");
        check(hog.width<.71F&&!hog.canBeHunted(),"baby hitbox and hunting exclusion");
        B11605Fixture.Player player=new B11605Fixture.Player(w);player.inventory.mainInventory[0]=new ItemStack(NetherVegetationContent116.crimsonFungus,3);
        check(hog.interact(player)&&hog.getGrowingAge()==-21600&&player.getHeldItem().stackSize==2&&hog.isNoDespawnRequired(),"baby feeding grows ten percent and persists");
        hog.setGrowingAge(0);check(hog.interact(player)&&hog.isInLove(),"adult breeding uses crimson fungus");
        check(!hog.isBreedingItem(new ItemStack(NetherVegetationContent116.warpedFungus)),"warped fungus not breeding food");
        check(((EntityHoglin116)hog.createChild(hog)).isNoDespawnRequired(),"bred offspring persistent");
        Victim resistant=new Victim(w);resistant.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1);HoglinCombat116.throwTarget(hog,resistant);check(resistant.motionX==0&&resistant.motionY==0&&resistant.motionZ==0,"full resistance prevents throw");
        check(EntityHoglin116.repellent(NetherVegetationContent116.warpedFungus)&&EntityHoglin116.repellent(NetherUtilityContent116.anchor)&&EntityHoglin116.repellent(Blocks.portal)&&!EntityHoglin116.repellent(NetherVegetationContent116.crimsonFungus),"repellent identities");
        java.lang.reflect.Method scan=EntityHoglin116.class.getDeclaredMethod("scanRepellents");scan.setAccessible(true);EntityHoglin116 afraid=new EntityHoglin116(w);afraid.setPosition(.5,80,.5);w.setBlock(1,80,0,NetherVegetationContent116.warpedFungus,0,3);
        for(int i=0;i<120;i++){afraid.ticksExisted=i;scan.invoke(afraid);}NBTTagCompound fear=new NBTTagCompound();afraid.writeEntityToNBT(fear);check(fear.getInteger("Pacified")==200,"incremental search detects nearby fungus");
        EntityHoglin116 converting=new EntityHoglin116(w);converting.setGrowingAge(-12000);converting.setCustomNameTag("Young hoglin");converting.func_110163_bv();
        for(int i=0;i<350;i++)converting.tickConversion();check(!converting.isDead,"safe in Nether");w.provider.dimensionId=0;for(int i=0;i<150;i++)converting.tickConversion();NBTTagCompound n=new NBTTagCompound();converting.writeEntityToNBT(n);
        EntityHoglin116 restored=new EntityHoglin116(w);restored.readEntityFromNBT(n);for(int i=150;i<300;i++)restored.tickConversion();check(!restored.isDead,"conversion waits more than 300 ticks");restored.tickConversion();
        check(restored.isDead&&w.lastSpawn instanceof EntityZoglin116,"conversion at tick 301");EntityZoglin116 zombie=(EntityZoglin116)w.lastSpawn;
        check(zombie.isChild()&&zombie.isNoDespawnRequired()&&"Young hoglin".equals(zombie.getCustomNameTag())&&zombie.isPotionActive(net.minecraft.potion.Potion.confusion),"conversion retains baby/name/persistence and adds nausea");
        check(zombie.getCreatureAttribute()==EnumCreatureAttribute.UNDEAD&&zombie.isImmuneToFire()&&!zombie.getCanSpawnHere(),"zoglin undead, fire immune, no natural spawning");
        check(!zombie.canTarget(new EntityZoglin116(w))&&!zombie.canTarget(new EntityCreeper(w))&&zombie.canTarget(new EntityCow(w))&&zombie.canTarget(hog),"zoglin target exclusions");
        player.capabilities.isCreativeMode=true;check(!zombie.canTarget(player),"creative immune to targeting");player.capabilities.isCreativeMode=false;
        zombie.setChild(false);Victim adultZoglinVictim=new Victim(w);zombie.attackEntityAsMob(adultZoglinVictim);check(adultZoglinVictim.damage>=3&&adultZoglinVictim.damage<=8,"zoglin adult combat");
        java.lang.reflect.Method drops=EntityHoglin116.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);drops.setAccessible(true);hog.captureDrops=true;
        for(int i=0;i<30;i++){hog.capturedDrops.clear();drops.invoke(hog,true,3);int pork=0,leather=0;for(EntityItem e:hog.capturedDrops){ItemStack stack=e.getEntityItem();if(stack.getItem()==Items.porkchop)pork+=stack.stackSize;else if(stack.getItem()==Items.leather)leather+=stack.stackSize;else throw new AssertionError("unexpected loot");}check(pork>=2&&pork<=7&&leather>=0&&leather<=4,"hoglin looting bounds");}
        hog.setGrowingAge(-1);hog.capturedDrops.clear();drops.invoke(hog,true,3);check(hog.capturedDrops.isEmpty(),"baby has no pork/leather drops");
        w.provider.dimensionId=-1;ArrayList entries=new ArrayList();entries.add(new net.minecraft.world.biome.BiomeGenBase.SpawnListEntry(EntityPigZombie.class,100,4,4));net.minecraftforge.event.world.WorldEvent.PotentialSpawns event=new net.minecraftforge.event.world.WorldEvent.PotentialSpawns(w,EnumCreatureType.monster,0,80,0,entries);HoglinNether116 handler=new HoglinNether116();handler.potentialSpawns(event);handler.potentialSpawns(event);
        check(event.list.size()==2&&((net.minecraft.world.biome.BiomeGenBase.SpawnListEntry)event.list.get(0)).entityClass==EntityZombifiedPiglin116.class,"spawn adapter idempotent and upgrades legacy zombie pigmen");check(((net.minecraft.world.biome.BiomeGenBase.SpawnListEntry)event.list.get(0)).itemWeight==100,"zombie spawn weight unchanged");
        com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelHoglin116 model=new com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelHoglin116();check(model.textureWidth==128&&model.textureHeight==64&&model.head.childModels.size()==4&&model.body.childModels.size()==1,"official mesh ears, tusks, mane");
        model.isChild=false;hog.handleHealthUpdate((byte)4);model.setRotationAngles(0,0,0,0,0,.0625F,hog);check(Math.abs(model.head.rotateAngleX-.87266463F)<1E-5,"attack animation starts neutral");
        B11605Fixture.field(EntityHoglin116.class,hog,"attackTicks",5);model.setRotationAngles(0,0,0,0,0,.0625F,hog);check(Math.abs(model.head.rotateAngleX+.34906584F)<1E-5,"attack animation peaks at tick five");
        w.loadedEntityList.clear();EntityPiglin116 hunter=new EntityPiglin116(w),partner=new EntityPiglin116(w);hunter.setPosition(0,80,0);partner.setPosition(1,80,0);EntityHoglin116 prey=new EntityHoglin116(w);prey.setPosition(3,80,0);
        w.loadedEntityList.add(hunter);w.loadedEntityList.add(partner);w.loadedEntityList.add(prey);
        check(hunter.selectMobTarget()==prey&&partner.getAttackTarget()==prey,"hunt selects visible adult and rallies group");
        partner.setAttackTarget(null);check(partner.selectMobTarget()==null,"shared hunt cooldown prevents continuous hunting");
        NBTTagCompound huntState=new NBTTagCompound();hunter.writeEntityToNBT(huntState);check(huntState.getInteger("HuntCooldown")>=600&&huntState.getInteger("HuntCooldown")<=2400,"hunt cooldown 30..120 seconds persisted");
        check(NetherHerd116.piglinsOutnumber(prey)&&!NetherHerd116.hoglinsOutnumber(hunter),"herd counts include observer exactly once");
        prey.setAttackTarget(hunter);prey.attackEntityAsMob(hunter);check(prey.getAttackTarget()==null,"outnumbered hoglin retreats after striking piglin");
        w.loadedEntityList.clear();EntityPiglin116 lone=new EntityPiglin116(w);lone.setPosition(0,80,0);EntityHoglin116 h1=new EntityHoglin116(w),h2=new EntityHoglin116(w);h1.setPosition(1,80,0);h2.setPosition(2,80,0);w.loadedEntityList.add(lone);w.loadedEntityList.add(h1);w.loadedEntityList.add(h2);
        check(NetherHerd116.hoglinsOutnumber(lone),"two hoglins outnumber one piglin");lone.attackEntityFrom(DamageSource.causeMobDamage(h1),1);check(lone.getAttackTarget()==null&&lone.selectMobTarget()==null,"outnumbered piglin retreats and suspends hunt");
        h2.setGrowingAge(-100);check(!NetherHerd116.hoglinsOutnumber(lone),"babies excluded from herd advantage");
        EntityPiglinBrute116 brute=new EntityPiglinBrute116(w);check(!brute.canHunt(),"brutes never initiate hunts");
        System.out.println("Hoglin 116: "+checks+" checks passed");
    }
}
