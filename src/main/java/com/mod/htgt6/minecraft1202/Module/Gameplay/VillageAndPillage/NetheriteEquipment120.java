package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import net.minecraft.entity.item.EntityItem;import net.minecraft.item.ItemStack;import net.minecraft.util.DamageSource;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120;
/** Equipment properties absent from the legacy Item API. */
public final class NetheriteEquipment120 {
    private static final java.util.UUID RESISTANCE=java.util.UUID.fromString("3ad3769b-26c4-4c7b-a2c8-6c7df748f40f");
    public static boolean isNetherite(ItemStack stack){return stack!=null&&SmithingContent120.upgrades.containsValue(stack.getItem());}
    /** Item fire immunity is not equipment classification (and never an armor bonus). */
    public static boolean isFireproof(ItemStack stack){
        if(stack==null||stack.stackSize<=0)return false;
        net.minecraft.item.Item item=stack.getItem();
        return isNetherite(stack)
                ||item==com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.netheriteScrap
                ||item==com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.netheriteIngot
                ||item==net.minecraft.item.Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.netheriteBlock)
                ||item==net.minecraft.item.Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.ancientDebris);
    }
    public static boolean isNetheriteArmor(ItemStack stack){return isNetherite(stack)&&stack.stackSize>0&&stack.getItem() instanceof net.minecraft.item.ItemArmor;}
    public static boolean immune(EntityItem entity,DamageSource source){return entity!=null&&source!=null&&source.isFireDamage()&&isFireproof(entity.getEntityItem());}
    public static void afterItemTick(EntityItem entity){if(entity!=null&&isFireproof(entity.getEntityItem())&&entity.isBurning())entity.extinguish();}
    @cpw.mods.fml.common.eventhandler.SubscribeEvent public void update(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event){
        if(event.entityLiving.worldObj.isRemote)return;int pieces=0;
        for(int slot=1;slot<=4;slot++)if(isNetheriteArmor(event.entityLiving.getEquipmentInSlot(slot))
                &&((net.minecraft.item.ItemArmor)event.entityLiving.getEquipmentInSlot(slot).getItem()).armorType==4-slot)pieces++;
        net.minecraft.entity.ai.attributes.IAttributeInstance attribute=event.entityLiving.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.knockbackResistance);
        if(attribute==null)return;net.minecraft.entity.ai.attributes.AttributeModifier current=attribute.getModifier(RESISTANCE);
        double amount=pieces*.1;if(current!=null&&Math.abs(current.getAmount()-amount)<1E-8)return;
        if(current!=null)attribute.removeModifier(current);
        if(pieces>0)attribute.applyModifier(new net.minecraft.entity.ai.attributes.AttributeModifier(RESISTANCE,"Netherite armor",amount,0).setSaved(false));
    }
}
