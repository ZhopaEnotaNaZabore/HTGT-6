package com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements;

import java.util.LinkedHashMap;
import java.util.Map;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

/** Modern criteria exposed as ordinary, saved and synchronized vanilla achievements. */
public final class BackportAchievements120 {
    private static final Map<String,Achievement> ENTRIES=new LinkedHashMap<String,Achievement>();
    private static final String DATA="HTGT6BackportAchievements";
    private static final Map<EntityPlayer,double[]> LEVITATING=new java.util.WeakHashMap<EntityPlayer,double[]>();
    private static boolean initialized;

    public static synchronized void init(){
        if(initialized||!com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999)return;
        registerAchievements();
        BackportAchievements120 listener=new BackportAchievements120();
        FMLCommonHandler.instance().bus().register(listener);MinecraftForge.EVENT_BUS.register(listener);
        initialized=true;
    }

    private static void registerAchievements(){
        if(!ENTRIES.isEmpty())return;
        add("bee_our_guest",com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.honeyBottle,AchievementList.openInventory,12,9,false);
        add("total_beelocation",com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.honeycomb,AchievementList.openInventory,14,9,false);
        add("sticky_situation",Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.honeyBlock),AchievementList.openInventory,16,9,false);
        add("iron_tools",Items.iron_pickaxe,AchievementList.acquireIron,10,3,false);
        add("obtain_armor",Items.iron_chestplate,AchievementList.acquireIron,12,3,false);
        add("deflect_arrow",CombatContent.shield,ENTRIES.get("obtain_armor"),14,3,false);
        add("lava_bucket",Items.lava_bucket,AchievementList.acquireIron,10,5,false);
        add("form_obsidian",Item.getItemFromBlock(Blocks.obsidian),ENTRIES.get("lava_bucket"),12,5,false);
        add("shiny_gear",Items.diamond_chestplate,AchievementList.diamonds,14,5,false);
        add("dragon_breath",CombatContent.dragonBreath,AchievementList.theEnd2,3,16,false);
        add("dragon_egg",Item.getItemFromBlock(Blocks.dragon_egg),AchievementList.theEnd2,5,16,false);
        add("elytra",CombatContent.elytra,AchievementList.theEnd2,7,16,false);
        add("enter_end_gateway",Items.ender_pearl,AchievementList.theEnd2,1,16,false);
        add("respawn_dragon",Items.ender_eye,AchievementList.theEnd2,-1,16,false);
        add("levitate",ExplorationContent112.shulkerShell,AchievementList.theEnd2,9,16,true);
        add("totem_of_undying",ExplorationContent112.totemOfUndying,AchievementList.killEnemy,12,-1,false);
        add("shoot_arrow",Items.bow,AchievementList.killEnemy,10,-1,false);
        add("country_lode_take_me_home",com.mod.htgt6.minecraft1202.Module.Registry.Modern.LodestoneContent116.compass,ENTRIES.get("shoot_arrow"),15,-5,false);
        add("bullseye",Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.target),ENTRIES.get("shoot_arrow"),14,-5,true);
        add("throw_trident",OceanContent.trident,AchievementList.killEnemy,12,-3,false);
        add("sleep_in_bed",Items.bed,AchievementList.openInventory,10,-5,false);
        add("tactical_fishing",OceanContent.fishBucket,AchievementList.cookFish,10,7,false);
        add("get_wither_skull",Items.skull,AchievementList.portal,-7,8,false);
        ENTRIES.get("get_wither_skull").theItemStack.setItemDamage(1);
        add("uneasy_alliance",Items.ghast_tear,AchievementList.ghast,-7,10,true);
        add("obtain_ancient_debris",Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.ancientDebris),AchievementList.portal,-10,8,false);
        add("netherite_armor",com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.netheriteIngot,ENTRIES.get("obtain_ancient_debris"),-12,8,true);
        add("distract_piglin",Items.gold_ingot,AchievementList.portal,-10,10,false);
        add("ride_strider",com.mod.htgt6.minecraft1202.Module.Registry.Modern.StriderContent116.fungusStick,AchievementList.portal,-12,10,false);
        add("charge_respawn_anchor",Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherUtilityContent116.anchor),AchievementList.portal,-14,10,false);
        // No AchievementPage: Forge displays these on the vanilla page automatically.
    }

    private static void add(String id,Item icon,Achievement parent,int x,int y,boolean special){
        // Leave existing vanilla/mod coordinates and stat IDs intact.
        boolean occupied;
        do{occupied=false;for(Object object:AchievementList.achievementList){Achievement a=(Achievement)object;
            if(a.displayColumn==x&&a.displayRow==y){occupied=true;y+=2;break;}}
        }while(occupied);
        Achievement a=new Achievement("achievement.htgt6.backport."+id,"htgt6.backport."+id,x,y,new ItemStack(icon),parent);
        if(special)a.setSpecial();a.registerStat();ENTRIES.put(id,a);
    }

    public static void complete(EntityPlayer player,String id){
        if(!initialized||!(player instanceof EntityPlayerMP)||player.worldObj.isRemote||!ENTRIES.containsKey(id))return;
        Achievement achievement=ENTRIES.get(id);
        if(((EntityPlayerMP)player).func_147099_x().hasAchievementUnlocked(achievement))return;
        NBTTagCompound data=data(player);data.setBoolean(id,true);
        flush(player,((EntityPlayerMP)player).func_147099_x(),data);
    }

    private static NBTTagCompound data(EntityPlayer player){
        NBTTagCompound root=player.getEntityData();
        if(!root.hasKey(EntityPlayer.PERSISTED_NBT_TAG,10))root.setTag(EntityPlayer.PERSISTED_NBT_TAG,new NBTTagCompound());
        NBTTagCompound persisted=root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if(!persisted.hasKey(DATA,10))persisted.setTag(DATA,new NBTTagCompound());
        return persisted.getCompoundTag(DATA);
    }

    // Criteria completed before their vanilla parent remain pending across death/relogin.
    static void flush(EntityPlayer player,StatFileWriter stats,NBTTagCompound data){
        for(Map.Entry<String,Achievement> entry:ENTRIES.entrySet()){
            Achievement a=entry.getValue();if(!data.getBoolean(entry.getKey()))continue;
            if(stats.hasAchievementUnlocked(a)){data.removeTag(entry.getKey());continue;}
            if(stats.canUnlockAchievement(a)){
                player.triggerAchievement(a);
                if(stats.hasAchievementUnlocked(a)){
                    data.removeTag(entry.getKey());
                    if("levitate".equals(entry.getKey()))player.addExperience(50);
                    else if("uneasy_alliance".equals(entry.getKey())||"netherite_armor".equals(entry.getKey()))player.addExperience(100);
                }
            }
        }
    }

    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.player instanceof EntityPlayerMP)||event.player.worldObj.isRemote)return;
        EntityPlayerMP p=(EntityPlayerMP)event.player;
        NBTTagCompound state=data(p);
        if(p.isEntityAlive()&&p.isPotionActive(CombatContent.levitation)){
            double[] start=LEVITATING.get(p);
            if(start==null||start[1]!=p.dimension){start=new double[]{p.posY,p.dimension};LEVITATING.put(p,start);}
            if(Math.abs(p.posY-start[0])>=50D)complete(p,"levitate");
        }else LEVITATING.remove(p);
        if(p.isPlayerSleeping())complete(p,"sleep_in_bed");
        if(p.ticksExisted%20!=0)return;
        for(int i=0;i<p.inventory.getSizeInventory();i++){
            String id=inventoryCriterion(p.inventory.getStackInSlot(i));if(id!=null)complete(p,id);
        }
        if(hasNetheriteArmor(p))complete(p,"netherite_armor");
        flush(p,p.func_147099_x(),state);
    }

    @SubscribeEvent public void crafted(cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event){inventory(event.player,event.crafting);}
    @SubscribeEvent public void smelted(cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent event){inventory(event.player,event.smelting);}
    @SubscribeEvent public void pickedUp(cpw.mods.fml.common.gameevent.PlayerEvent.ItemPickupEvent event){inventory(event.player,event.pickedUp.getEntityItem());}
    @SubscribeEvent public void logout(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event){LEVITATING.remove(event.player);}
    private static void inventory(EntityPlayer player,ItemStack stack){String id=inventoryCriterion(stack);if(id!=null)complete(player,id);}

    public static boolean hasNetheriteArmor(EntityPlayer player){
        java.util.Map<Item,Item> upgrades=com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgrades;
        Item[] legacy={Items.diamond_helmet,Items.diamond_chestplate,Items.diamond_leggings,Items.diamond_boots};int found=0;
        for(int slot=0;slot<player.inventory.getSizeInventory();slot++){ItemStack stack=player.inventory.getStackInSlot(slot);if(stack==null||stack.stackSize<=0)continue;for(int i=0;i<4;i++)if(stack.getItem()==upgrades.get(legacy[i]))found|=1<<i;}
        return found==15;
    }
    public static void distracted(EntityPlayer player,com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116 piglin){
        if(player!=null&&!piglin.isChild()&&!com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116.wearsGold(player))complete(player,"distract_piglin");
    }
    static String inventoryCriterion(ItemStack stack){
        if(stack==null||stack.stackSize<=0)return null;Item item=stack.getItem();
        if(item==Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.ancientDebris))return "obtain_ancient_debris";
        if(item==Items.iron_pickaxe)return "iron_tools";
        if(item==Items.iron_helmet||item==Items.iron_chestplate||item==Items.iron_leggings||item==Items.iron_boots)return "obtain_armor";
        if(item==Items.diamond_helmet||item==Items.diamond_chestplate||item==Items.diamond_leggings||item==Items.diamond_boots)return "shiny_gear";
        if(item==Items.lava_bucket)return "lava_bucket";
        if(item==Item.getItemFromBlock(Blocks.obsidian))return "form_obsidian";
        if(item==CombatContent.dragonBreath)return "dragon_breath";
        if(item==CombatContent.elytra)return "elytra";
        if(item==Item.getItemFromBlock(Blocks.dragon_egg))return "dragon_egg";
        if(item==Items.skull&&stack.getItemDamage()==1)return "get_wither_skull";
        return null;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST) public void hurt(LivingHurtEvent event){
        if(event.ammount>0&&event.source.getSourceOfDamage() instanceof EntityArrow&&event.source.getEntity() instanceof EntityPlayer)
            complete((EntityPlayer)event.source.getEntity(),"shoot_arrow");
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public void death(LivingDeathEvent event){
        if(event.entityLiving instanceof EntityGhast&&event.entityLiving.dimension==0&&event.source.getEntity() instanceof EntityPlayer)
            complete((EntityPlayer)event.source.getEntity(),"uneasy_alliance");
    }
}
