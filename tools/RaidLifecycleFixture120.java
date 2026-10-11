package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.init.Blocks;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;

/** Real entity/POI/effect lifecycle regressions on Forge 1.7.10. */
public final class RaidLifecycleFixture120 {
    static Field field(Class<?> c,String n)throws Exception {Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
    static int checks;
    static void check(boolean value,String label) {if(!value)throw new AssertionError(label);checks++;}
    static final class Player extends net.minecraft.entity.player.EntityPlayer {
        Player(World w){super(w,new com.mojang.authlib.GameProfile(new UUID(1202,600),"RaidFixture"));}
        public void addChatMessage(net.minecraft.util.IChatComponent text){}
        public boolean canCommandSenderUseCommand(int level,String command){return false;}
        public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,64,0);}
    }
    static final class AuditWorld extends World {
        boolean doorRemoved;
        AuditWorld(){super(null,"audit",(WorldProvider)null,(WorldSettings)null,null);}
        public boolean checkChunksExist(int a,int b,int c,int d,int e,int f){return true;}
        public net.minecraft.block.Block getBlock(int x,int y,int z){return x==0&&y==64&&z==0?Blocks.bed:Blocks.air;}
        public int getBlockMetadata(int x,int y,int z){return 8;}
        public int getTopSolidOrLiquidBlock(int x,int z){return 64;}
        public boolean setBlockToAir(int x,int y,int z){doorRemoved=true;return true;}
        public void destroyBlockInWorldPartially(int id,int x,int y,int z,int progress){}
        public void playAuxSFX(int effect,int x,int y,int z,int data){}
        public List getEntitiesWithinAABB(Class type,net.minecraft.util.AxisAlignedBB box){List list=new ArrayList();for(Object value:loadedEntityList)if(type.isInstance(value)&&((Entity)value).boundingBox.intersectsWith(box))list.add(value);return list;}
        public net.minecraft.world.chunk.IChunkProvider getChunkProvider(){return (net.minecraft.world.chunk.IChunkProvider)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{net.minecraft.world.chunk.IChunkProvider.class},new InvocationHandler(){public Object invoke(Object p,Method m,Object[] a){return m.getReturnType()==boolean.class?Boolean.TRUE:m.getReturnType()==int.class?Integer.valueOf(0):null;}});}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public Entity getEntityByID(int id){return null;}
    }
    public static void main(String[] args)throws Exception {
        World base=BountifulMechanicsFixture120.initialize();
        sun.misc.Unsafe unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);
        AuditWorld w=(AuditWorld)unsafe.allocateInstance(AuditWorld.class);
        for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
        field(World.class,"perWorldStorage").set(w,new net.minecraft.world.storage.MapStorage(null));
        field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
        w.loadedEntityList=new ArrayList();w.playerEntities=new ArrayList();w.rand=new Random(1);w.provider.worldObj=w;
        w.villageCollectionObj=new net.minecraft.village.VillageCollection(w);
        Class<?> rc=Class.forName(RaidAndPatrolHandler.class.getName()+"$Raid");
        Constructor<?> ctor=rc.getDeclaredConstructor(ChunkCoordinates.class,int.class,EnumDifficulty.class);ctor.setAccessible(true);
        Object raid=ctor.newInstance(new ChunkCoordinates(0,64,0),1,EnumDifficulty.NORMAL);
        Method living=rc.getDeclaredMethod("livingRaiders",World.class);living.setAccessible(true);
        Set ids=(Set)field(rc,"members").get(raid);
        EntityPillager mob=new EntityPillager(w);mob.setPosition(200,64,0);w.loadedEntityList.add(mob);ids.add(mob.getUniqueID());
        check((Integer)living.invoke(raid,w)==0,"far raider retires");
        mob.setPosition(0,64,0);ids.add(mob.getUniqueID());mob.setDead();
        check(mob.getHealth()>0&&(Integer)living.invoke(raid,w)==0,"removed living entity retires");
        w.loadedEntityList.clear();ids.add(mob.getUniqueID());
        for(int i=0;i<29;i++)living.invoke(raid,w);
        check(ids.size()==1,"unloaded UUID gets 600 active ticks grace");
        living.invoke(raid,w);check(ids.isEmpty(),"missing UUID expires at grace boundary");
        mob=new EntityPillager(w);mob.setPosition(0,64,0);ids.add(mob.getUniqueID());
        for(int i=0;i<20;i++)living.invoke(raid,w);
        w.loadedEntityList.add(mob);living.invoke(raid,w);w.loadedEntityList.clear();
        for(int i=0;i<20;i++)living.invoke(raid,w);check(ids.size()==1,"loaded return resets missing timer");
        VillagePoiData120.registerIfPoi(w,0,64,0);
        check(VillagePoiData120.findVillageCenter(w,0,64,0,48)==null,"unclaimed bed is not village");
        VillagePoiData120.get(w).claimNearestHome(w,0,64,0,"owner");
        check(VillagePoiData120.findVillageCenter(w,0,64,0,48)!=null,"claimed home forms village");
        VillagePoiData120.get(w).releaseOwner("owner");
        check(VillagePoiData120.findVillageCenter(w,0,64,0,48)==null,"released home no longer forms village");
        RaidAndPatrolHandler.RaidData data=new RaidAndPatrolHandler.RaidData();
        ((List)field(data.getClass(),"active").get(data)).add(raid);w.perWorldStorage.setData("HTGT6Raids120",data);
        net.minecraft.nbt.NBTTagCompound save=new net.minecraft.nbt.NBTTagCompound();data.writeToNBT(save);
        RaidAndPatrolHandler.RaidData copy=new RaidAndPatrolHandler.RaidData();copy.readFromNBT(save);
        Object restored=((List)field(copy.getClass(),"active").get(copy)).get(0);
        for(int i=0;i<9;i++)living.invoke(restored,w);
        check(((Set)field(rc,"members").get(restored)).size()==1,"saved grace resumes without reset");
        living.invoke(restored,w);check(((Set)field(rc,"members").get(restored)).isEmpty(),"saved grace expires exactly");
        Method has=RaidAndPatrolHandler.class.getDeclaredMethod("hasRaidAt",World.class,ChunkCoordinates.class);has.setAccessible(true);
        check(!(Boolean)has.invoke(new RaidAndPatrolHandler(),w,new ChunkCoordinates(65536,64,0)),"far coordinate does not overflow");
        check(!(Boolean)has.invoke(new RaidAndPatrolHandler(),w,new ChunkCoordinates(0,300,0)),"raid lookup respects altitude");
        Field registry=net.minecraft.potion.Potion.class.getDeclaredField("potionTypes");
        unsafe.putObject(unsafe.staticFieldBase(registry),unsafe.staticFieldOffset(registry),Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536));
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.init();
        Player player=new Player(w);player.setPosition(0,64,0);w.playerEntities.add(player);w.difficultySetting=EnumDifficulty.NORMAL;
        VillagePoiData120.get(w).claimNearestHome(w,0,64,0,"owner");
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.captainKilled(player);
        Method start=RaidAndPatrolHandler.class.getDeclaredMethod("startPendingRaids",World.class);start.setAccessible(true);
        RaidAndPatrolHandler handler=new RaidAndPatrolHandler();start.invoke(handler,w);
        check(field(rc,"omen").getInt(raid)==2&&field(rc,"totalWaves").getInt(raid)==6,"existing raid absorbs omen and adds bonus wave");
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.badOmenLevel(player)==0,"absorbed effect removed");
        for(int i=0;i<5;i++)com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.captainKilled(player);
        start.invoke(handler,w);check(field(rc,"omen").getInt(raid)==5&&field(rc,"totalWaves").getInt(raid)==6,"omen capped and bonus not duplicated");
        field(rc,"status").setInt(raid,1);com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.captainKilled(player);start.invoke(handler,w);
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.badOmenLevel(player)==0,"celebrating raid consumes residual omen");
        field(rc,"status").setInt(raid,0);
        EntityPillager entrant=new EntityPillager(w);entrant.setPosition(5,64,0);entrant.ticksExisted=20;
        w.loadedEntityList.add(entrant);handler.raiderTick(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(entrant));
        check(ids.contains(entrant.getUniqueID()),"patrol member joins nearby raid");
        float health=field(rc,"totalHealth").getFloat(raid);handler.raiderTick(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(entrant));
        check(field(rc,"totalHealth").getFloat(raid)==health,"joining is idempotent");
        net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn despawn=new net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn(entrant);
        handler.preventRaidDespawn(despawn);check(despawn.getResult()==cpw.mods.fml.common.eventhandler.Event.Result.DENY,"active raid protects natural despawn");
        entrant.setPatrolTarget(new ChunkCoordinates(450,300,-400));
        net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();entrant.writeEntityToNBT(tag);
        EntityPillager reloaded=new EntityPillager(w);reloaded.readEntityFromNBT(tag);
        check(reloaded.isPatrolling()&&reloaded.getPatrolTarget().equals(new ChunkCoordinates(450,300,-400)),"patrol route survives NBT");
        check(!new com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.PatrolGoal120(entrant).shouldExecute(),"raid overrides patrol route");
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidMechanics120.enchantOdds(5)==.75F,"omen V enchant chance");
        field(Entity.class,"rand").set(entrant,new Random(){public float nextFloat(){return 0;}});
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidMechanics120.applyBuffs(entrant,7,5);
        check(net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
            com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.CrossbowEnchantments140.quickCharge.effectId,entrant.getHeldItem())==2,
            "late-wave crossbow receives quick charge II");
        check(net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
            com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.CrossbowEnchantments140.multishot.effectId,entrant.getHeldItem())==1,
            "enchanted raid crossbow receives multishot");
        Random fixed=new Random(){public int nextInt(int bound){return bound-1;}};
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.HeroGiftGoal120.gift("mason",false,fixed).getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.clay),"mason gift is clay block");
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.HeroGiftGoal120.gift("shepherd",false,fixed).getItemDamage()==15,"shepherd includes black wool");
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.HeroGiftGoal120.gift("fletcher",false,fixed).getItemDamage()==8196,"fletcher includes poison arrow");
        ((List)field(data.getClass(),"active").get(data)).clear();
        despawn=new net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn(entrant);handler.preventRaidDespawn(despawn);
        check(despawn.getResult()==cpw.mods.fml.common.eventhandler.Event.Result.DEFAULT,"ended raid releases despawn protection");
        EntityPillager follower=new EntityPillager(w);follower.setPosition(6,64,0);w.loadedEntityList.add(follower);
        entrant.setCaptain(true);entrant.setPatrolTarget(new ChunkCoordinates(100,64,0));
        Raid152Fixture.Nav nav=new Raid152Fixture.Nav(entrant,w);field(EntityLiving.class,"navigator").set(entrant,nav);
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.PatrolGoal120 patrol=new com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.PatrolGoal120(entrant);
        check(patrol.shouldExecute(),"patrol resumes outside raid");patrol.updateTask();
        check(nav.calls==1&&nav.speed==.595,"captain makes local patrol step");
        check(follower.isPatrolling()&&follower.getPatrolTarget()!=null,"captain shares waypoint with follower");
        entrant.setPosition(100,64,0);entrant.ticksExisted=601;entrant.getEntityData().setInteger("HTGT6RaidIdle",2420);
        ids.clear();ids.add(entrant.getUniqueID());
        for(int i=0;i<29;i++)living.invoke(raid,w);check(ids.size()==1,"idle eviction waits 30 cleanup checks");
        living.invoke(raid,w);check(ids.isEmpty(),"idle outsider retires even when legacy entityAge is reset");
        EntityPillager captain=new EntityPillager(w);captain.setCaptain(true);captain.setPosition(0,64,0);
        w.getGameRules().setOrCreateGameRule("disableRaids","true");
        handler.death(new net.minecraftforge.event.entity.living.LivingDeathEvent(captain,net.minecraft.util.DamageSource.causePlayerDamage(player)));
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.badOmenLevel(player)==0,"disabled raids block captain omen");
        w.getGameRules().setOrCreateGameRule("disableRaids","false");captain.setCurrentItemOrArmor(4,null);
        handler.death(new net.minecraftforge.event.entity.living.LivingDeathEvent(captain,net.minecraft.util.DamageSource.causePlayerDamage(player)));
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.badOmenLevel(player)==0,"captain must wear real banner");
        captain.setCaptain(true);handler.death(new net.minecraftforge.event.entity.living.LivingDeathEvent(captain,net.minecraft.util.DamageSource.causePlayerDamage(player)));
        check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.badOmenLevel(player)==1,"eligible captain grants omen");
        com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112 vindicator=new com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112(w);
        vindicator.setPosition(0,64,0);ids.add(vindicator.getUniqueID());((List)field(data.getClass(),"active").get(data)).add(raid);
        Class<?> doorClass=Class.forName("com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidMechanics120$RaidBreakDoor");
        Constructor<?> doorCtor=doorClass.getDeclaredConstructor(EntityLiving.class);doorCtor.setAccessible(true);
        net.minecraft.entity.ai.EntityAIBase door=(net.minecraft.entity.ai.EntityAIBase)doorCtor.newInstance(vindicator);
        field(net.minecraft.entity.ai.EntityAIDoorInteract.class,"field_151504_e").set(door,Blocks.wooden_door);
        field(net.minecraft.entity.ai.EntityAIDoorInteract.class,"entityPosY").setInt(door,64);
        w.getGameRules().setOrCreateGameRule("mobGriefing","true");door.startExecuting();
        for(int i=0;i<239;i++)door.updateTask();check(!w.doorRemoved,"vindicator respects modern 240-tick minimum");
        door.updateTask();check(w.doorRemoved,"vindicator breaks door on normal at completion");
        w.doorRemoved=false;door.startExecuting();w.getGameRules().setOrCreateGameRule("mobGriefing","false");
        for(int i=0;i<240;i++)door.updateTask();check(!w.doorRemoved,"mobGriefing false prevents door destruction");
        System.out.println("PASS RaidLifecycle: "+checks);
    }
}
