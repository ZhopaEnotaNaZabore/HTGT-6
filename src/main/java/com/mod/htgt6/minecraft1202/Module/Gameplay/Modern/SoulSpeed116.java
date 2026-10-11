package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import java.util.*;
import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

/** Treasure enchantment and movement contract. Nether loot owners may use book(level). */
public final class SoulSpeed116 {
    public static final UUID MODIFIER=UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
    public static Enchantment enchantment;
    private static final Set<Block> SURFACES=Collections.newSetFromMap(new IdentityHashMap<Block,Boolean>());
    private static final Map<EntityLivingBase,Step> STEPS=new WeakHashMap<EntityLivingBase,Step>();
    private static final class Step {int x,y,z,level,dimension;boolean ground;Step(int x,int y,int z,int l,int d,boolean g){this.x=x;this.y=y;this.z=z;level=l;dimension=d;ground=g;}}
    public static synchronized void init(){
        if(enchantment!=null)return;int id=HTGT6Config.SOUL_SPEED_ENCHANTMENT_ID;
        if(id<0||id>=Enchantment.enchantmentsList.length||Enchantment.enchantmentsList[id]!=null)throw new IllegalStateException("Soul Speed enchantment ID "+id+" occupied or invalid; configure nether_enchantment_ids.SoulSpeed identically on server and client");
        enchantment=new Enchantment(id,1,EnumEnchantmentType.armor_feet){
            @Override public int getMaxLevel(){return 3;}
            @Override public int getMinEnchantability(int level){return level*10;}
            @Override public int getMaxEnchantability(int level){return getMinEnchantability(level)+15;}
            @Override public boolean canApplyAtEnchantingTable(ItemStack stack){return false;}
            @Override public boolean isAllowedOnBooks(){return false;}
        }.setName("soul_speed");registerSurface(Blocks.soul_sand);
    }
    /** Explicit block identity registration, never a loose unlocalized-name match. */
    public static void registerSurface(Block block){if(block==null)throw new IllegalArgumentException("surface");SURFACES.add(block);}
    public static boolean isSurface(Block block){return SURFACES.contains(block);}
    public static ItemStack book(int level){init();if(level<1||level>3)throw new IllegalArgumentException("Soul Speed level");return net.minecraft.init.Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(enchantment,level));}
    public static int level(EntityLivingBase e){return enchantment==null?0:Math.max(0,Math.min(3,EnchantmentHelper.getEnchantmentLevel(enchantment.effectId,e.getEquipmentInSlot(1))));}
    public static boolean bypassSlowdown(Entity e){return e instanceof EntityLivingBase&&level((EntityLivingBase)e)>0;}
    public static double boost(int level){return (double)(.03F*(1+level*.35F));}
    @SubscribeEvent public void update(LivingUpdateEvent event){tick(event.entityLiving);}
    public static void tick(EntityLivingBase e){
        int level=level(e);boolean flying=e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.isFlying;
        if(e.worldObj.isRemote)return;
        IAttributeInstance speed=e.getEntityAttribute(SharedMonsterAttributes.movementSpeed);if(speed==null)return;
        int x=MathHelper.floor_double(e.posX),y=MathHelper.floor_double(e.boundingBox.minY-.2),z=MathHelper.floor_double(e.posZ);
        Block block=e.worldObj.getBlock(x,y,z);Step old=STEPS.get(e);
        boolean changed=old==null||old.x!=x||old.y!=y||old.z!=z||old.level!=level||old.dimension!=e.dimension||!old.ground&&e.onGround;
        boolean surface=isSurface(block),solid=!block.isAir(e.worldObj,x,y,z);
        AttributeModifier modifier=speed.getModifier(MODIFIER);
        if(level==0||flying||e.isDead||changed&&(solid||e.isInWater())){
            if(modifier!=null){speed.removeModifier(modifier);modifier=null;}
        }
        if(level>0&&!flying&&!e.isDead&&surface&&(changed||modifier==null)){
            if(modifier!=null)speed.removeModifier(modifier);
            speed.applyModifier(new AttributeModifier(MODIFIER,"Soul speed boost",boost(level),0).setSaved(false));
            if(e.getRNG().nextFloat()<.04F){ItemStack boots=e.getEquipmentInSlot(1);if(boots!=null)boots.damageItem(1,e);}
        }
        if(level==0||e.isDead)STEPS.remove(e);else STEPS.put(e,new Step(x,y,z,level,e.dimension,e.onGround));
    }
}
