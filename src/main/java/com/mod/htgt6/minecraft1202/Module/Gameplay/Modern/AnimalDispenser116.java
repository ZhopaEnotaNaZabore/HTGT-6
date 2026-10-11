package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.LeatherHorseArmor151;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.*;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.item.*;
import net.minecraft.util.*;
import java.lang.reflect.*;
/** One-item equipment transactions; previous mod dispenser behavior remains the fallback. */
public final class AnimalDispenser116 {
    private static boolean registered;
    private static Field horseInventory;
    private static Method rebuildHorseInventory;
    public static void register(){if(registered)return;registered=true;
        for(Item item:new Item[]{Items.saddle,Items.iron_horse_armor,Items.golden_horse_armor,Items.diamond_horse_armor,LeatherHorseArmor151.ITEM,Item.getItemFromBlock(Blocks.chest)}){
            final IBehaviorDispenseItem previous=(IBehaviorDispenseItem)BlockDispenser.dispenseBehaviorRegistry.getObject(item);
            BlockDispenser.dispenseBehaviorRegistry.putObject(item,new IBehaviorDispenseItem(){public ItemStack dispense(IBlockSource source,ItemStack stack){
                if(source.getWorld().isRemote)return stack;
                EnumFacing face=BlockDispenser.func_149937_b(source.getBlockMetadata());int x=source.getXInt()+face.getFrontOffsetX(),y=source.getYInt()+face.getFrontOffsetY(),z=source.getZInt()+face.getFrontOffsetZ();
                for(Object value:source.getWorld().getEntitiesWithinAABB(EntityLivingBase.class,AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1)))if(equip((EntityLivingBase)value,stack)){
                    source.getWorld().playAuxSFX(1000,source.getXInt(),source.getYInt(),source.getZInt(),0);source.getWorld().playAuxSFX(2000,source.getXInt(),source.getYInt(),source.getZInt(),face.getFrontOffsetX()+1+(face.getFrontOffsetZ()+1)*3);return stack;}
                return previous.dispense(source,stack);
            }});
        }
    }
    public static AnimalChest inventory(EntityHorse horse){try{
        if(horseInventory==null){for(Field f:EntityHorse.class.getDeclaredFields())if(f.getType()==AnimalChest.class){f.setAccessible(true);horseInventory=f;break;}if(horseInventory==null)throw new NoSuchFieldException("EntityHorse AnimalChest");}
        return (AnimalChest)horseInventory.get(horse);
    }catch(ReflectiveOperationException e){throw new IllegalStateException("Horse inventory adapter unavailable",e);}}
    public static boolean equip(EntityLivingBase target,ItemStack stack){
        if(stack==null||stack.stackSize<=0||target.worldObj.isRemote||!target.isEntityAlive())return false;
        Item item=stack.getItem();boolean adult=!(target instanceof EntityAgeable)||!((EntityAgeable)target).isChild();String sound=null;
        if(item==Items.saddle&&adult){
            if(target instanceof EntityStrider116){EntityStrider116 s=(EntityStrider116)target;if(s.isSaddled())return false;s.setSaddled(true);sound="strider.saddle";}
            else if(target instanceof EntityPig){EntityPig pig=(EntityPig)target;if(pig.getSaddled())return false;pig.setSaddled(true);sound="pig.saddle";}
            else if(target instanceof EntityCamel120){EntityCamel120 camel=(EntityCamel120)target;if(camel.isSaddled())return false;camel.setSaddled(true);sound="horse.saddle";}
            else if(target instanceof EntityHorse){EntityHorse horse=(EntityHorse)target;if(!horse.isTame()||horse.isHorseSaddled())return false;AnimalChest inv=inventory(horse);if(inv.getStackInSlot(0)!=null)return false;ItemStack copy=stack.copy();copy.stackSize=1;inv.setInventorySlotContents(0,copy);sound=horse.ticksExisted>20?"":"horse.saddle";}
        }else if(target instanceof EntityHorse){EntityHorse horse=(EntityHorse)target;if(!horse.isTame())return false;
            if(item==Item.getItemFromBlock(Blocks.chest)&&(horse.getHorseType()==1||horse.getHorseType()==2)&&!horse.isChested()){
                try{if(rebuildHorseInventory==null){rebuildHorseInventory=EntityHorse.class.getDeclaredMethod("func_110226_cD");rebuildHorseInventory.setAccessible(true);}horse.setChested(true);rebuildHorseInventory.invoke(horse);}catch(ReflectiveOperationException e){throw new IllegalStateException("Horse chest resize unavailable",e);}sound="donkey.chest";
            }else if(horse.getHorseType()==0&&(item==Items.iron_horse_armor||item==Items.golden_horse_armor||item==Items.diamond_horse_armor||item==LeatherHorseArmor151.ITEM)){
                AnimalChest inv=inventory(horse);if(inv.getStackInSlot(1)!=null)return false;ItemStack copy=stack.copy();copy.stackSize=1;inv.setInventorySlotContents(1,copy);sound=horse.ticksExisted>20&&item!=LeatherHorseArmor151.ITEM?"":"horse.armor";
            }
        }
        if(sound==null)return false;stack.stackSize--;if(!sound.isEmpty())target.playSound("htgt6:entity."+sound,.5F,1);return true;
    }
    private AnimalDispenser116(){}
}
