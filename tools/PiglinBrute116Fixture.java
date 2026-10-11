import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120SpawnEgg;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import java.util.UUID;

public final class PiglinBrute116Fixture {
    static int checks;
    static void check(boolean b,String reason){if(!b)throw new AssertionError(reason);checks++;}
    static final class Victim extends EntityCow {
        float damage;Victim(Grid w){super(w);}
        public boolean attackEntityFrom(DamageSource source,float amount){damage=amount;return true;}
    }
    public static void main(String[] args)throws Exception{
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        Grid w=B11605Fixture.initialize();w.provider.dimensionId=-1;w.loaded=true;
        EntityPiglinBrute116 brute=new EntityPiglinBrute116(w);brute.setPosition(.5,80,.5);brute.onSpawnWithEgg(null);
        check(brute.getMaxHealth()==50&&brute.getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue()==7,"brute attributes");
        check(brute.getHeldItem().getItem()==Items.golden_axe&&!brute.getCanSpawnHere(),"gold axe, no ordinary natural respawning");
        brute.setChild(true);check(!brute.isChild(),"brutes always adult");
        B11605Fixture.Player player=new B11605Fixture.Player(w);player.setPosition(3,80,0);player.inventory.mainInventory[0]=new ItemStack(Items.gold_ingot,5);player.inventory.armorInventory[0]=new ItemStack(Items.golden_boots);w.playerEntities.add(player);
        check(!brute.interact(player)&&player.getHeldItem().stackSize==5&&!brute.acceptsGold(),"brute rejects barter without taking gold");
        brute.beginBarter(player.getUniqueID());check(!brute.isAdmiring(),"no scripted barter entry");
        check(brute.nearestBruteTarget()==player,"gold armor does not protect against brute");
        player.capabilities.isCreativeMode=true;check(brute.nearestBruteTarget()==null,"creative player ignored");player.capabilities.isCreativeMode=false;
        player.setPosition(13,80,0);check(brute.nearestBruteTarget()==null,"idle detection limited to 12 blocks");player.setPosition(3,80,0);
        NBTTagCompound n=new NBTTagCompound();brute.writeEntityToNBT(n);check(n.getBoolean("BruteHasHome")&&n.getInteger("BruteHomeY")==80,"guard home persisted");
        EntityPiglinBrute116 restored=new EntityPiglinBrute116(w);restored.readEntityFromNBT(n);check(restored.hasHome(),"guard home restored");
        n.setBoolean("BruteHasHome",false);restored.readEntityFromNBT(n);check(!restored.hasHome(),"reused entity clears absent home");
        UUID weapon=UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
        for(boolean oldApplied:new boolean[]{false,true})for(boolean isBrute:new boolean[]{false,true}){
            EntityPiglin116 attacker=isBrute?new EntityPiglinBrute116(w):new EntityPiglin116(w);attacker.setCurrentItemOrArmor(0,new ItemStack(isBrute?Items.golden_axe:Items.golden_sword));
            IAttributeInstance attr=attacker.getEntityAttribute(SharedMonsterAttributes.attackDamage);AttributeModifier old=new AttributeModifier(weapon,"Legacy weapon",isBrute?3:4,0);if(oldApplied)attr.applyModifier(old);
            double before=attr.getAttributeValue();Victim victim=new Victim(w);attacker.attackEntityAsMob(victim);
            check(victim.damage==(isBrute?13:8),"modern equipped damage with/without equipment tick");
            check(attr.getAttributeValue()==before&&attr.getModifier(weapon)==(oldApplied?old:null),"attack preserves equipment modifier");
        }
        brute.setAttackTarget(player);brute.attackEntityFrom(DamageSource.causeMobDamage(new EntityPiglin116(w)),1);check(brute.getAttackTarget()==player,"friendly fire does not retarget fellow piglin");
        w.provider.dimensionId=0;w.lastSpawn=null;for(int i=0;i<300;i++)brute.tickConversion();check(!brute.isDead,"brute remains living at tick 300");brute.tickConversion();check(brute.isDead&&w.lastSpawn instanceof EntityZombifiedPiglin116,"brute converts at overworld tick 301");
        check(((EntityZombifiedPiglin116)w.lastSpawn).getHeldItem().getItem()==Items.golden_axe,"conversion preserves weapon");
        check(ItemBackport120SpawnEgg.create(w,ItemBackport120SpawnEgg.PIGLIN_BRUTE) instanceof EntityPiglinBrute116&&ItemBackport120SpawnEgg.entityType(12)==EntityPiglinBrute116.class,"own spawn egg identity");
        java.lang.reflect.Method despawn=EntityPiglinBrute116.class.getDeclaredMethod("canDespawn");despawn.setAccessible(true);check(!(Boolean)despawn.invoke(restored),"brute never naturally despawns");
        System.out.println("Piglin brute 116: "+checks+" checks passed");
    }
}
