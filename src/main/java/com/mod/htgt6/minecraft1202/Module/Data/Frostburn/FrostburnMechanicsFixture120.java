package com.mod.htgt6.minecraft1202.Module.Data.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.*;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnStructureVoid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.potion.Potion;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.util.DamageSource;

public final class FrostburnMechanicsFixture120 {
    public static void main(String[] args)throws Exception {
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;
        com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;
        com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        Grid w=BountifulMechanicsFixture120.initialize();
        java.lang.reflect.Field accesses=World.class.getDeclaredField("worldAccesses");
        accesses.setAccessible(true);accesses.set(w,new java.util.ArrayList());
        w.provider.worldObj=w;
        java.lang.reflect.Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);
        info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        w.biome=net.minecraft.world.biome.BiomeGenBase.frozenOcean;w.light=15;
        w.sky=true;w.skyFromY=12;
        require(!EntityFrostburnStray.canSeeSkyForSpawn(w,0,10,0),"stray requires sky without powder snow");
        w.setBlock(0,11,0,com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.powderSnow,0,2);
        w.setBlock(0,12,0,com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.powderSnow,0,2);
        require(EntityFrostburnStray.canSeeSkyForSpawn(w,0,10,0),"stray sky query skips powder snow column");
        w.skyFromY=0;w.setBlock(0,11,0,Blocks.air,0,2);w.setBlock(0,12,0,Blocks.air,0,2);
        EntityFrostburnPolarBear bear=new EntityFrostburnPolarBear(w);bear.setPosition(0,10,0);
        near(com.mod.htgt6.asm.HTGT6Transformer.depthDrag120(.8D,bear),.98D,"polar bear modern horizontal water drag");
        near(com.mod.htgt6.asm.HTGT6Transformer.depthAcceleration120(.04F,bear),.02D,"polar bear modern water acceleration");
        w.setBlock(0,9,0,Blocks.ice,0,2);
        require(bear.getCanSpawnHere(),"bear spawns on frozen-ocean ice");
        w.setBlock(0,9,0,Blocks.stone,0,2);require(!bear.getCanSpawnHere(),"bear rejects ocean stone");
        w.setBlock(0,9,0,Blocks.ice,0,2);w.light=8;require(!bear.getCanSpawnHere(),"bear requires adequate light");w.light=15;
        net.minecraft.world.biome.BiomeGenBase previousOcean=com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry.deepFrozenOcean;
        try {
            com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry.deepFrozenOcean=net.minecraft.world.biome.BiomeGenBase.deepOcean;
            w.biome=net.minecraft.world.biome.BiomeGenBase.deepOcean;
            require(bear.getCanSpawnHere(),"deep frozen ocean uses ice spawn rule too");
        } finally {
            com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry.deepFrozenOcean=previousOcean;
            w.biome=net.minecraft.world.biome.BiomeGenBase.frozenOcean;
        }
        EntityFrostburnHusk husk=new EntityFrostburnHusk(w);
        require(!EntityFrostburnHusk.solarBurnAllowed(true,husk),"husk rejects solar ignition");
        require(EntityFrostburnHusk.solarBurnAllowed(true,new EntityZombie(w)),"ordinary zombie still burns");
        husk.setFire(8);require(husk.isBurning(),"ordinary fire still ignites husk");
        near(EntityFrostburnHusk.effectiveDifficulty(0,9999999,9999999,1),0,"peaceful difficulty");
        near(EntityFrostburnHusk.effectiveDifficulty(3,0,0,1),2.25,"new hard world");
        near(EntityFrostburnHusk.effectiveDifficulty(3,9999999,9999999,1),6.75,"inhabited hard world");
        BlockFrostburnStructureVoid empty=new BlockFrostburnStructureVoid();
        near(empty.getBlockBoundsMinX(),5D/16D,"structure void selection min");
        near(empty.getBlockBoundsMaxY(),11D/16D,"structure void selection max");
        require(empty.getCollisionBoundingBoxFromPool(w,0,0,0)==null,"structure void noncolliding");
        EntityFrostburnStrayArrow arrow=new EntityFrostburnStrayArrow(w);
        Victim victim=new Victim(w);victim.accept=false;
        require(!EntityCombatArrow.damageTarget(victim,DamageSource.generic,1,arrow),"cancelled hit returns false");
        require(!victim.isPotionActive(Potion.moveSlowdown),"cancelled hit applies no effect");
        victim.accept=true;require(EntityCombatArrow.damageTarget(victim,DamageSource.generic,1,arrow),"accepted hit returns true");
        require(victim.getActivePotionEffect(Potion.moveSlowdown).getDuration()==600,"stray applies exactly thirty seconds");
        NBTTagCompound saved=new NBTTagCompound();arrow.writeEntityToNBT(saved);
        EntityFrostburnStrayArrow restored=new EntityFrostburnStrayArrow(w);restored.readEntityFromNBT(saved);
        require(restored.hitEffects().size()==1&&restored.hitEffects().get(0).getDuration()==600,"stray arrow NBT effects preserved");
        saved.setByte("inGround",(byte)1);restored.readEntityFromNBT(saved);
        for(int i=0;i<600;i++)restored.tickPotionState();
        require(restored.getCombatType()==0,"stray arrow uses modern custom-effect decay");
        NBTTagCompound old=new NBTTagCompound();restored.readEntityFromNBT(old);
        require(restored.hitEffects().size()==1,"pre-migration stray arrow restores slowness");
        com.mod.htgt6.minecraft1202.Module.World.Aquatic.ZombieDrowningHandler drowning=
                new com.mod.htgt6.minecraft1202.Module.World.Aquatic.ZombieDrowningHandler();
        husk.getEntityData().setInteger("HTGT6DrownedConversion",899);w.rejectSpawns=true;
        husk.func_110163_bv();husk.setCanPickUpLoot(true);husk.setHealth(3F);
        husk.setCurrentItemOrArmor(0,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_sword));
        husk.setEquipmentDropChance(0,.75F);
        drowning.onLivingUpdate(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(husk));
        require(!husk.isDead,"failed conversion spawn preserves original husk");
        w.rejectSpawns=false;
        drowning.onLivingUpdate(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(husk));
        require(husk.isDead&&w.lastSpawn.getClass()==EntityZombie.class,"conversion completes out of water once started and preserves intermediate zombie");
        EntityZombie converted=(EntityZombie)w.lastSpawn;
        require(converted.isNoDespawnRequired()&&converted.canPickUpLoot(),"conversion preserves persistence and loot pickup");
        NBTTagCompound convertedTag=new NBTTagCompound();converted.writeEntityToNBT(convertedTag);
        near(convertedTag.getTagList("DropChances",5).func_150308_e(0),.75,"conversion preserves equipment drop chance");
        require(converted.getHeldItem()!=husk.getHeldItem()&&converted.getHeldItem().getItem()==net.minecraft.init.Items.iron_sword,"equipment is copied");
        near(converted.getHealth(),converted.getMaxHealth(),"modern conversion creates full-health target");
        Object model=new com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelFrostburnPolarBear();
        java.lang.reflect.Field standing=EntityFrostburnPolarBear.class.getDeclaredField("standingScale");standing.setAccessible(true);standing.setFloat(bear,6F);
        java.lang.reflect.Field previous=EntityFrostburnPolarBear.class.getDeclaredField("previousStandingScale");previous.setAccessible(true);previous.setFloat(bear,6F);
        java.lang.reflect.Method animate=model.getClass().getDeclaredMethod("setupAnim",EntityFrostburnPolarBear.class,float.class,float.class,float.class,float.class,float.class);animate.setAccessible(true);
        java.lang.reflect.Field headField=model.getClass().getDeclaredField("head");headField.setAccessible(true);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportModelPart head=(com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportModelPart)headField.get(model);
        animate.invoke(model,bear,0F,0F,0F,0F,0F);near(head.y(),-14,"adult standing head Y");near(head.z(),-3,"adult standing head Z");
        bear.setGrowingAge(-24000);animate.invoke(model,bear,0F,0F,0F,0F,0F);near(head.y(),-9,"cub standing head Y");near(head.z(),-7,"cub standing head Z");
        loot(w);
        bearHit(w);
        anger(w);
        tools(w);
        magma();
        spawner(w);
        columns(w);
        particles(w);
        System.out.println("Frostburn mechanics OK: ice spawn, sunlight hook, real fire, local difficulty, void bounds, accepted arrow damage, NBT migration and decay");
    }
    private static final class Victim extends EntityPig {
        boolean accept;float lastDamage;
        Victim(World w){super(w);}
        @Override public boolean attackEntityFrom(DamageSource source,float amount){lastDamage=amount;return accept;}
    }
    private static void loot(Grid w) throws Exception {
        EntityFrostburnHusk husk=new EntityFrostburnHusk(w);
        EntityFrostburnStray stray=new EntityFrostburnStray(w);
        java.lang.reflect.Method huskDrop=EntityFrostburnHusk.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);huskDrop.setAccessible(true);
        java.lang.reflect.Method strayDrop=EntityFrostburnStray.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);strayDrop.setAccessible(true);
        java.lang.reflect.Method rare=EntityFrostburnHusk.class.getDeclaredMethod("dropRareDrop",int.class);rare.setAccessible(true);
        husk.captureDrops=true;stray.captureDrops=true;husk.setFire(100);husk.getRNG().setSeed(6210);
        int rareCount=0,baked=0;
        for(int i=0;i<5000;i++) {
            husk.capturedDrops.clear();huskDrop.invoke(husk,true,3);
            for(net.minecraft.entity.item.EntityItem drop:husk.capturedDrops) {
                net.minecraft.item.ItemStack stack=drop.getEntityItem();
                if(stack.getItem()==net.minecraft.init.Items.rotten_flesh)require(stack.stackSize<=5,"husk flesh count respects table maximum");
                else {rareCount++;require(stack.stackSize==1,"one rare item");}
                require(stack.getItem()!=net.minecraft.init.Items.potato,"burning husk smelts potato");
                if(stack.getItem()==net.minecraft.init.Items.baked_potato)baked++;
            }
            int size=husk.capturedDrops.size();rare.invoke(husk,1);require(husk.capturedDrops.size()==size,"legacy rare-drop hook cannot duplicate table loot");
        }
        require(rareCount>220&&rareCount<335&&baked>50,"Looting III rare probability is 5.5 percent and all entries occur");
        for(int i=0;i<100;i++) {
            husk.capturedDrops.clear();huskDrop.invoke(husk,false,100);
            for(net.minecraft.entity.item.EntityItem drop:husk.capturedDrops)require(drop.getEntityItem().getItem()==net.minecraft.init.Items.rotten_flesh,"rare loot requires player kill");
        }
        for(int i=0;i<100;i++) {
            long seed=2000+i;stray.getRNG().setSeed(seed);stray.capturedDrops.clear();strayDrop.invoke(stray,true,3);
            java.util.Random reference=new java.util.Random(seed);
            int arrows=reference.nextInt(3)+Math.round(reference.nextFloat()*3);
            int bones=reference.nextInt(3)+Math.round(reference.nextFloat()*3);
            int tipped=Math.min(1,reference.nextInt(2)+Math.round(reference.nextFloat()*3));
            int actualArrows=0,actualBones=0,actualTipped=0;
            for(net.minecraft.entity.item.EntityItem drop:stray.capturedDrops) {
                net.minecraft.item.ItemStack stack=drop.getEntityItem();
                if(stack.getItem()==net.minecraft.init.Items.arrow)actualArrows+=stack.stackSize;
                else if(stack.getItem()==net.minecraft.init.Items.bone)actualBones+=stack.stackSize;
                else {actualTipped+=stack.stackSize;require(stack.getItemDamage()==8202,"slowness tipped loot");}
            }
            require(actualArrows==arrows&&actualBones==bones&&actualTipped==tipped,"stray table functions replace legacy uniform Looting count");
        }
        System.out.println("Frostburn loot OK: modern counts, rare rate, smelting, player condition, no duplicate rare hook");
    }
    private static void bearHit(Grid w) throws Exception {
        EntityFrostburnPolarBear bear=new EntityFrostburnPolarBear(w);Victim victim=new Victim(w);
        bear.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.attackDamage).setBaseValue(6.9D);
        bear.setPosition(0,10,0);victim.setPosition(1,10,0);victim.accept=true;
        FixtureSenses senses=new FixtureSenses(bear);
        for(java.lang.reflect.Field field:net.minecraft.entity.EntityLiving.class.getDeclaredFields())
            if(field.getType()==net.minecraft.entity.ai.EntitySenses.class){field.setAccessible(true);field.set(bear,senses);}
        require(bear.canPerformMeleeAttack120(victim),"close visible target within modern attack box");
        senses.visible=false;require(!bear.canPerformMeleeAttack120(victim),"opaque wall prevents bear attack");senses.visible=true;
        victim.setPosition(1,13,0);require(!bear.canPerformMeleeAttack120(victim),"target above bear has no vertical overlap");
        victim.setPosition(2.2,10,0);require(!bear.canPerformMeleeAttack120(victim),"outside modern horizontal reach");
        victim.setPosition(1,10,0);
        int free=-1;
        for(int i=net.minecraft.enchantment.Enchantment.enchantmentsList.length-1;i>=0;i--)
            if(net.minecraft.enchantment.Enchantment.enchantmentsList[i]==null){free=i;break;}
        require(free>=0,"fixture enchantment slot");
        HitEnchantment enchantment=new HitEnchantment(free);
        try {
            net.minecraft.item.ItemStack armor=new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_chestplate);armor.addEnchantment(enchantment,1);victim.setCurrentItemOrArmor(3,armor);
            victim.accept=false;require(!bear.attackEntityAsMob(victim)&&enchantment.callbacks==0,"rejected bear damage must not trigger enchantment callbacks");
            victim.accept=true;require(bear.attackEntityAsMob(victim)&&enchantment.callbacks>0,"accepted bear attack applies armor enchantment callback");
            near(victim.lastDamage,6,"polar bear uses reference integer attack attribute");
        } finally {net.minecraft.enchantment.Enchantment.enchantmentsList[free]=null;}
    }
    private static final class FixtureSenses extends net.minecraft.entity.ai.EntitySenses {
        boolean visible=true;
        FixtureSenses(net.minecraft.entity.EntityLiving entity){super(entity);}
        @Override public boolean canSee(net.minecraft.entity.Entity entity){return visible;}
    }
    private static final class HitEnchantment extends net.minecraft.enchantment.Enchantment {
        int callbacks;
        HitEnchantment(int id){super(id,1,net.minecraft.enchantment.EnumEnchantmentType.armor);}
        @Override public void func_151367_b(net.minecraft.entity.EntityLivingBase user,net.minecraft.entity.Entity other,int level){callbacks++;}
        @Override public void func_151368_a(net.minecraft.entity.EntityLivingBase user,net.minecraft.entity.Entity other,int level){callbacks++;}
    }
    private static void anger(Grid w) throws Exception {
        EntityFrostburnPolarBear bear=new EntityFrostburnPolarBear(w);
        Victim attacker=new Victim(w);bear.setAttackTarget(attacker);bear.tickAnger();
        require(bear.getAngerTime()>=399&&bear.getAngerTime()<=779,"anger starts in 20-39 second range then ticks");
        require(bear.isAngryAt(attacker),"anger remembers attacker UUID");
        for(Object value:bear.targetTasks.taskEntries) {
            net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry=(net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry)value;
            if(entry.action.getClass().getSimpleName().equals("ProtectCubsTarget")) {
                entry.action.shouldExecute();require(bear.getAttackTarget()==attacker,"cub goal must not erase retaliation target");
            }
        }
        NBTTagCompound tag=new NBTTagCompound();bear.writeEntityToNBT(tag);
        EntityFrostburnPolarBear restored=new EntityFrostburnPolarBear(w);restored.readEntityFromNBT(tag);
        require(restored.getAngerTime()==bear.getAngerTime()&&restored.isAngryAt(attacker),"anger UUID and timer survive NBT");
        w.isRemote=true;int ticks=restored.getAngerTime();restored.tickAnger();require(restored.getAngerTime()==ticks,"client cannot count down authoritative anger");w.isRemote=false;
        for(int i=0;i<ticks;i++)restored.tickAnger();
        require(restored.getAngerTime()==0&&restored.getAngryAt()==null&&!restored.isAngryAt(attacker),"unengaged anger expires and clears UUID");
        tag.setIntArray("AngryAt",new int[]{1});tag.setInteger("AngerTime",-10);restored.readEntityFromNBT(tag);
        require(restored.getAngerTime()==0&&restored.getAngryAt()==null,"malformed anger save handled");
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");java.lang.reflect.Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        TargetPlayer player=(TargetPlayer)unsafe.getMethod("allocateInstance",Class.class).invoke(singleton.get(null),TargetPlayer.class);
        player.alive=true;bear.setAttackTarget(player);bear.tickAnger();int activeTicks=bear.getAngerTime();
        for(int i=0;i<40;i++)bear.tickAnger();require(bear.getAngerTime()==activeTicks,"anger pauses during active player combat");
        player.alive=false;bear.tickAnger();require(bear.getAngerTime()==0&&bear.getAttackTarget()==null,"default forgiveDeadPlayers clears dead player target");
        w.loadedEntityList.add(attacker);
        restored.readEntityFromNBT(tagFor(attacker,100));
        require(restored.getAITarget()==attacker,"loaded anger restores last attacker");
        restored.tickAnger();require(restored.getAngerTime()==0,"loaded non-player without combat target clears anger");
        w.loadedEntityList.remove(attacker);
        w.rules.setOrCreateGameRule("universalAnger","true");
        player.alive=true;bear.ticksExisted=100;bear.setRevengeTarget(player);bear.setAttackTarget(player);bear.tickAnger();
        require(bear.getAngryAt()==null&&bear.getAngerTime()>0&&bear.isAngryAt(player),"universal anger forgets UUID but targets players");
        w.rules.setOrCreateGameRule("universalAnger","false");
    }
    private static void spawner(World world) {
        SpawnProbe probe=new SpawnProbe(world);
        require(!FrostburnSpawnRules120.fromSpawner(probe),"natural spawn has no spawner reason");
        require(FrostburnSpawnRules120.canSpawnFromSpawner(probe),"spawner reason is scoped to spawn check");
        require(!FrostburnSpawnRules120.fromSpawner(probe),"spawner reason cleared after check");
        probe.fail=true;
        try { FrostburnSpawnRules120.canSpawnFromSpawner(probe);throw new AssertionError("probe did not throw"); }
        catch(IllegalStateException expected) { }
        require(!FrostburnSpawnRules120.fromSpawner(probe),"spawner reason cleared on exception");
    }
    private static void columns(Grid world) {
        com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120 bridge=new com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120();
        for(int y=5;y<=12;y++)world.setBlock(40,y,40,Blocks.water,0,2);
        world.setBlock(40,4,40,Blocks.soul_sand,0,2);
        EntityPig pig=new EntityPig(world);pig.setPosition(40.5,6,40.5);pig.setAir(20);
        require(com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120.airSupply(pig,20)==24,"column replenishes four air per tick");
        require(com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120.airSupply(pig,299)==300,"column air cap");
        world.loadedEntityList.add(pig);pig.motionY=0;
        bridge.onWorldTick(new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(cpw.mods.fml.relauncher.Side.SERVER,cpw.mods.fml.common.gameevent.TickEvent.Phase.END,world));
        near(pig.motionY,.06D,"column works in ordinary world");
        world.setBlock(40,4,40,com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.magmaBlock,0,2);pig.motionY=0;
        bridge.onWorldTick(new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(cpw.mods.fml.relauncher.Side.SERVER,cpw.mods.fml.common.gameevent.TickEvent.Phase.END,world));
        near(pig.motionY,-.03D,"magma pulls down");
        pig.addPotionEffect(new net.minecraft.potion.PotionEffect(Potion.waterBreathing.id,100));pig.setAir(20);
        bridge.onWorldTick(new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(cpw.mods.fml.relauncher.Side.SERVER,cpw.mods.fml.common.gameevent.TickEvent.Phase.END,world));
        require(pig.getAir()==24,"water-breathing entities still replenish air in columns");
        world.setBlock(40,5,40,Blocks.water,1,2);
        require(com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120.airSupply(pig,20)==Integer.MIN_VALUE,"flowing water breaks column and retains ordinary breathing");
        world.loaded=false;
        require(com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120.directionAt(world,40,6,40)==0,"column does not query unloaded chunk");
        world.loaded=true;world.loadedEntityList.remove(pig);
    }
    private static void particles(Grid world) throws Exception {
        Class<?> type=Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Frostburn.BubbleColumnClient120$Bubble");
        java.lang.reflect.Constructor<?> constructor=type.getDeclaredConstructor(World.class,double.class,double.class,double.class,boolean.class);constructor.setAccessible(true);
        net.minecraft.entity.Entity down=(net.minecraft.entity.Entity)constructor.newInstance(world,40.5,7.5,40.5,true);
        down.onUpdate();near(down.motionX,.6F*.07D,"downward particle spiral X");near(down.motionZ,0,"downward particle initial Z");near(down.motionY,-.05D,"downward particle speed");
        net.minecraft.entity.Entity up=(net.minecraft.entity.Entity)constructor.newInstance(world,40.5,7.5,40.5,false);
        up.noClip=true;double original=up.motionY;up.onUpdate();near(up.motionY,(original+.005D)*.85F,"upward particle acceleration and drag");
        world.setBlock(40,7,40,Blocks.air,0,2);down.onUpdate();require(down.isDead,"column particle expires outside water");
    }
    private static final class SpawnProbe extends EntityPig {
        boolean fail;
        SpawnProbe(World world){super(world);}
        @Override public boolean getCanSpawnHere() {
            if(fail)throw new IllegalStateException("fixture");
            require(!FrostburnSpawnRules120.fromSpawner(new EntityPig(worldObj)),"reason does not leak to another mob");
            return FrostburnSpawnRules120.fromSpawner(this);
        }
    }
    private static void tools(Grid w) {
        com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnSimple wart =
            (com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnSimple)
            com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.netherWartBlock;
        MiningPlayer p=new MiningPlayer(w);p.onGround=true;
        w.setBlock(5,10,5,wart,0,2);
        net.minecraft.item.ItemStack hoe=new net.minecraft.item.ItemStack(net.minecraft.init.Items.diamond_hoe);
        p.inventory.mainInventory[0]=hoe;
        near(wart.getPlayerRelativeBlockHardness(p,w,5,10,5),8D/30D,"diamond hoe wart speed");
        hoe.addEnchantment(net.minecraft.enchantment.Enchantment.efficiency,3);
        near(wart.getPlayerRelativeBlockHardness(p,w,5,10,5),18D/30D,"hoe Efficiency III");
        p.addPotionEffect(new net.minecraft.potion.PotionEffect(Potion.digSlowdown.id,100,2));
        near(wart.getPlayerRelativeBlockHardness(p,w,5,10,5),18D*.0027D/30D,"modern mining fatigue III");
        require("pickaxe".equals(com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.boneBlock.getHarvestTool(0)),"bone requires pickaxe");
        require("htgt6:block.wart_block.break".equals(wart.stepSound.getBreakSound()),"wart sound binding");
    }
    private static final class MiningPlayer extends net.minecraft.entity.player.EntityPlayer {
        MiningPlayer(World w){super(w,new com.mojang.authlib.GameProfile(new java.util.UUID(1,2),"FrostburnFixture"));}
        @Override public void addChatMessage(net.minecraft.util.IChatComponent message) { }
        @Override public boolean canCommandSenderUseCommand(int level,String command){return true;}
        @Override public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,10,0);}
    }
    private static void magma() throws Exception {
        Class<?> type=com.mod.htgt6.minecraft1202.Module.Data.Modern.FuturaWorldgenParityFixture120.OreWorld.class;
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");java.lang.reflect.Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        World world=(World)unsafe.getMethod("allocateInstance",Class.class).invoke(singleton.get(null),type);
        java.lang.reflect.Field cellsField=type.getDeclaredField("cells");cellsField.setAccessible(true);
        java.util.Map<String,net.minecraft.block.Block> cells=new java.util.HashMap<String,net.minecraft.block.Block>();cellsField.set(world,cells);
        java.lang.reflect.Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);
        provider.set(world,new net.minecraft.world.WorldProviderHell());world.provider.dimensionId=-1;
        com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnMagmaGenerator120 generator=new com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnMagmaGenerator120();
        generator.generate(new java.util.Random(12),0,0,world,null,null);
        require(cells.isEmpty(),"magma must not replace stone");
        for(int x=-10;x<26;x++)for(int z=-10;z<26;z++)for(int y=15;y<45;y++)cells.put(x+","+y+","+z,Blocks.netherrack);
        cells.put("8,30,8",Blocks.bedrock);
        generator.generate(new java.util.Random(12),0,0,world,null,null);
        int count=0;for(net.minecraft.block.Block b:cells.values())if(b==com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.magmaBlock)count++;
        require(count>0,"Nether magma veins actually place blocks");
        require(cells.get("8,30,8")==Blocks.bedrock,"magma preserves non-netherrack blocks");
        java.util.Map<String,net.minecraft.block.Block> before=new java.util.HashMap<String,net.minecraft.block.Block>(cells);
        world.provider.dimensionId=0;generator.generate(new java.util.Random(44),0,0,world,null,null);
        require(cells.equals(before),"magma generator skips Overworld");
    }
    private static NBTTagCompound tagFor(net.minecraft.entity.Entity entity,int ticks) {
        NBTTagCompound tag=new NBTTagCompound();tag.setInteger("AngerTime",ticks);
        long a=entity.getUniqueID().getMostSignificantBits(),b=entity.getUniqueID().getLeastSignificantBits();
        tag.setIntArray("AngryAt",new int[]{(int)(a>>>32),(int)a,(int)(b>>>32),(int)b});return tag;
    }
    private static final class TargetPlayer extends net.minecraft.entity.player.EntityPlayerMP {
        boolean alive;
        private TargetPlayer(){super(null,null,null,null);}
        @Override public boolean isEntityAlive(){return alive;}
        @Override public java.util.UUID getUniqueID(){return new java.util.UUID(123,456);}
    }
    private static void near(double a,double b,String label){require(Math.abs(a-b)<.00001,label+": "+a);}
    private static void require(boolean value,String label){if(!value)throw new AssertionError(label);}
}
