import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

/** Independent acceptance probes. A red result documents an open defect, not an expected success. */
public final class VillageFauna114Audit {
    private static int failed, passed;
    private static void expect(String id, boolean condition, String observed) {
        if (condition) passed++; else failed++;
        System.out.println((condition ? "PASS " : "FAIL ") + id + ": " + observed);
    }
    private static Field field(Class<?> owner, String name) throws Exception {
        Field f = owner.getDeclaredField(name); f.setAccessible(true); return f;
    }
    public static void main(String[] args) throws Exception {
        VillageFauna114Fixture.main(args);
        // Reproduce FML's real network lookup with the registration order in BackportCategory.
        Constructor<cpw.mods.fml.common.registry.EntityRegistry> constructor = cpw.mods.fml.common.registry.EntityRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        cpw.mods.fml.common.registry.EntityRegistry registry = constructor.newInstance();
        cpw.mods.fml.common.ModMetadata metadata = new cpw.mods.fml.common.ModMetadata(); metadata.modId="Minecraft_1.20.2";
        cpw.mods.fml.common.ModContainer mod = new cpw.mods.fml.common.DummyModContainer(metadata);
        com.google.common.collect.ListMultimap registrations = (com.google.common.collect.ListMultimap)field(cpw.mods.fml.common.registry.EntityRegistry.class,"entityRegistrations").get(registry);
        registrations.put(mod,registry.new EntityRegistration(mod,com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143.class,"CrossbowRocket",51,64,1,true));
        registrations.put(mod,registry.new EntityRegistration(mod,com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRaidWitch151.class,"HTGT6.RaidWitch",52,80,3,true));
        int catId=com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities.CAT_MOD_ID;
        int foxId=com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities.FOX_MOD_ID;
        registrations.put(mod,registry.new EntityRegistration(mod,EntityCat114.class,"Cat",catId,80,3,true));
        registrations.put(mod,registry.new EntityRegistration(mod,EntityFox114.class,"Fox",foxId,80,3,true));
        expect("CAT_NETWORK_ID",registry.lookupModSpawn(mod,catId).getEntityClass()==EntityCat114.class,
                "id="+catId+" resolves="+registry.lookupModSpawn(mod,catId).getEntityClass().getSimpleName());
        expect("FOX_NETWORK_ID",registry.lookupModSpawn(mod,foxId).getEntityClass()==EntityFox114.class,
                "id="+foxId+" resolves="+registry.lookupModSpawn(mod,foxId).getEntityClass().getSimpleName());
        BountifulMechanicsFixture120.Grid base = BountifulMechanicsFixture120.initialize();
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);
        VillageFauna114Fixture.W world = (VillageFauna114Fixture.W)unsafe.allocateInstance(AuditWorld.class);
        for (Field f : World.class.getDeclaredFields()) if (!Modifier.isStatic(f.getModifiers())) {
            f.setAccessible(true); f.set(world, f.get(base));
        }
        world.base = base; world.players = new ArrayList(); world.spawned = new ArrayList();
        world.playerEntities = world.players; world.loadedEntityList = new ArrayList();
        world.provider.worldObj = world;
        field(World.class,"worldInfo").set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        VillageFauna114Fixture.Player player = new VillageFauna114Fixture.Player(world);
        world.players.add(player);
        EntityCat114 cat = new EntityCat114(world);
        cat.setTamed(true); cat.func_152115_b(player.getUniqueID().toString());
        cat.onGround = true; cat.setSitting(false); cat.func_70907_r().setSitting(false);
        cat.setCollar(1); player.inventory.setInventorySlotContents(0,new ItemStack(Items.dye,2,1));
        cat.interact(player);
        expect("CAT_SAME_DYE", !cat.func_70907_r().shouldExecute(),
                "same-color dye schedules sitting="+cat.func_70907_r().shouldExecute());
        cat.func_146082_f(player);
        EntityOcelot legacy = new EntityOcelot(world);
        legacy.setTamed(true); legacy.func_146082_f(player);
        expect("CAT_SPECIES_BOUNDARY", !legacyMate(legacy,cat) && !cat.canMateWith(legacy),
                "legacy->cat="+legacyMate(legacy,cat)+", cat->legacy="+cat.canMateWith(legacy));
        EntityCat114 mate = new EntityCat114(world); mate.setTamed(true); mate.setCollar(4);
        boolean inheritedOtherCollar = false;
        for(int i=0;i<100;i++) if(cat.createChild(mate).collar()==4) inheritedOtherCollar=true;
        expect("CAT_COLLAR_INHERITANCE", inheritedOtherCollar,
                "partner collar appeared in 100 offspring="+inheritedOtherCollar);
        EntityFox114 fox = new EntityFox114(world); fox.setGrowingAge(-1000);
        player.inventory.setInventorySlotContents(0,new ItemStack(Backport120Content.sweetBerries,2));
        boolean handled = fox.interact(player);
        expect("FOX_BABY_FEED", handled && fox.getGrowingAge()>-1000 && player.getHeldItem().stackSize==1,
                "handled="+handled+", age="+fox.getGrowingAge()+", berries="+player.getHeldItem().stackSize);
        fox.setPosition(30.5,64,30.5);
        net.minecraft.block.Block[] floors={net.minecraft.init.Blocks.grass,net.minecraft.init.Blocks.snow,net.minecraft.init.Blocks.dirt,net.minecraft.init.Blocks.dirt};
        int[] floorMeta={0,0,2,1}; String[] floorNames={"GRASS_CONTROL","SNOW","PODZOL","COARSE_DIRT"};
        for(int i=0;i<floors.length;i++) {
            world.setBlock(30,63,30,floors[i],floorMeta[i],3);
            expect("FOX_SPAWN_"+floorNames[i],fox.getCanSpawnHere(),"canSpawn="+fox.getCanSpawnHere()+", light=15, empty collision/liquid");
        }
        EntityWanderingTrader120 trader = new EntityWanderingTrader120(world);
        trader.addPotionEffect(new PotionEffect(Potion.invisibility.id,1000));
        trader.addPotionEffect(new PotionEffect(Potion.poison.id,1000)); world.day=true;
        EntityAIBase drink = null;
        for(Object raw:trader.tasks.taskEntries) {
            EntityAIBase ai=((EntityAITasks.EntityAITaskEntry)raw).action;
            if(ai.getClass().getSimpleName().equals("DrinkGoal")) drink=ai;
        }
        if(drink==null || !drink.shouldExecute()) throw new AssertionError("Audit setup: missing milk goal");
        drink.startExecuting(); for(int tick=0;tick<32;tick++) drink.updateTask();
        expect("TRADER_MILK", !trader.isPotionActive(Potion.invisibility) && !trader.isPotionActive(Potion.poison),
                "invisibility="+trader.isPotionActive(Potion.invisibility)+", poison="+trader.isPotionActive(Potion.poison));
        drink.resetTask();
        additionalChecks(world,player,cat,fox,registry,mod,registrations);
        System.out.println("AUDIT acceptance: passed="+passed+", failed="+failed);
        if(failed>0) throw new AssertionError(failed+" open 1.14 acceptance defects; see audit report");
    }
    // Execute the actual transformed native method as a static bridge. This avoids replacing
    // Minecraft's loaded class in a headless JVM while preserving the method's complete body.
    private static Method mateBridge;
    private static boolean legacyMate(EntityOcelot animal,net.minecraft.entity.passive.EntityAnimal other)throws Exception {
        if(mateBridge==null){
            java.io.InputStream in=EntityOcelot.class.getResourceAsStream("/net/minecraft/entity/passive/EntityOcelot.class");
            byte[] original=com.google.common.io.ByteStreams.toByteArray(in);in.close();
            byte[] patched=com.mod.htgt6.asm.CatSpeciesASM114.transform(original);
            expect("CAT_ASM_IDEMPOTENT",Arrays.equals(patched,com.mod.htgt6.asm.CatSpeciesASM114.transform(patched)),"second transform unchanged");
            org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(patched).accept(node,0);
            org.objectweb.asm.tree.MethodNode target=null;
            for(org.objectweb.asm.tree.MethodNode method:node.methods)if(method.name.equals("canMateWith"))target=method;
            if(target==null)throw new AssertionError("Missing native mate method");
            org.objectweb.asm.tree.ClassNode srg=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(original).accept(srg,0);
            for(org.objectweb.asm.tree.MethodNode method:srg.methods)if(method.name.equals("canMateWith"))method.name="func_70878_b";
            org.objectweb.asm.ClassWriter srgWriter=new org.objectweb.asm.ClassWriter(0);srg.accept(srgWriter);
            expect("CAT_ASM_SRG",com.mod.htgt6.asm.CatSpeciesASM114.transform(srgWriter.toByteArray()).length>srgWriter.toByteArray().length,"SRG mate method guarded");
            target.access=org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC;
            target.name="mate";target.desc="(Lnet/minecraft/entity/passive/EntityOcelot;Lnet/minecraft/entity/passive/EntityAnimal;)Z";
            org.objectweb.asm.tree.ClassNode bridge=new org.objectweb.asm.tree.ClassNode();bridge.version=org.objectweb.asm.Opcodes.V1_6;
            bridge.access=org.objectweb.asm.Opcodes.ACC_PUBLIC;bridge.name="CatMateAuditBridge";bridge.superName="java/lang/Object";bridge.methods.add(target);
            org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);bridge.accept(writer);
            Class<?> type=new ClassLoader(VillageFauna114Audit.class.getClassLoader()){
                Class<?> install(byte[] bytes){return defineClass(null,bytes,0,bytes.length);}
            }.install(writer.toByteArray());
            mateBridge=type.getMethod("mate",EntityOcelot.class,net.minecraft.entity.passive.EntityAnimal.class);
        }
        return (Boolean)mateBridge.invoke(null,animal,other);
    }
    private static void additionalChecks(VillageFauna114Fixture.W world,VillageFauna114Fixture.Player player,
            EntityCat114 cat,EntityFox114 fox,cpw.mods.fml.common.registry.EntityRegistry registry,
            cpw.mods.fml.common.ModContainer mod,com.google.common.collect.ListMultimap registrations)throws Exception {
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.BackportEntityIds114.validate(registry,mod);
        expect("ENTITY_IDS_HIGH_RANGE",com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities.CAT_MOD_ID>=1000&&
                com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities.FOX_MOD_ID>=1000&&
                com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities.MOOSHROOM_MOD_ID>=1000,"all new mod IDs >= 1000");
        cpw.mods.fml.common.registry.EntityRegistry.EntityRegistration duplicate=registry.new EntityRegistration(mod,EntityMooshroom114.class,"Duplicate",1000,80,3,true);
        registrations.put(mod,duplicate);boolean rejected=false;
        try{com.mod.htgt6.minecraft1202.Module.Registry.Modern.BackportEntityIds114.validate(registry,mod);}catch(IllegalStateException expected){rejected=true;}
        registrations.remove(mod,duplicate);expect("DUPLICATE_REGISTRY_REJECTED",rejected,"duplicate rejected before world startup");
        net.minecraft.entity.Entity[] animals={cat,fox,new EntityMooshroom114(world)};
        for(int i=0;i<animals.length;i++){
            cpw.mods.fml.common.registry.EntityRegistry.EntityRegistration entry=registry.new EntityRegistration(mod,animals[i].getClass(),"PacketFixture",1000+i,80,3,true);
            cpw.mods.fml.common.network.internal.FMLMessage.EntitySpawnMessage message=new cpw.mods.fml.common.network.internal.FMLMessage.EntitySpawnMessage(entry,animals[i],mod);
            Method write=message.getClass().getDeclaredMethod("toBytes",io.netty.buffer.ByteBuf.class),read=message.getClass().getDeclaredMethod("fromBytes",io.netty.buffer.ByteBuf.class);write.setAccessible(true);read.setAccessible(true);
            io.netty.buffer.ByteBuf buf=io.netty.buffer.Unpooled.buffer();write.invoke(message,buf);
            cpw.mods.fml.common.network.internal.FMLMessage.EntitySpawnMessage copy=new cpw.mods.fml.common.network.internal.FMLMessage.EntitySpawnMessage();read.invoke(copy,buf);
            expect("SPAWN_PACKET_"+(1000+i),field(copy.getClass(),"modEntityTypeId").getInt(copy)==1000+i,"real FML packet preserves full-width mod ID");buf.release();
        }
        Field sleepTimer=field(net.minecraft.entity.player.EntityPlayer.class,"sleepTimer");
        sleepTimer.setInt(player,100);expect("CAT_WAKE_TIMER_SERVER",!player.isPlayerSleeping()&&EntityCat114.completedOwnerSleep(player),"wakeup retains sleep completion independent of entity tick order");
        sleepTimer.setInt(player,0);expect("CAT_WAKE_TIMER_RESET",!EntityCat114.completedOwnerSleep(player),"reset wake timer cannot grant gift");
        for(float angle:new float[]{0,.3F,.77F,.785F,.8F,.95F}){
            expect("CAT_GIFT_TIME_"+angle,EntityCat114.canGiveMorningGift(angle,true)==(angle>.77F&&angle<.8F),"morning interval");
            expect("CAT_GIFT_INCOMPLETE_"+angle,!EntityCat114.canGiveMorningGift(angle,false),"incomplete sleep gives no gift");
        }
        ItemStack stew=new ItemStack(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.suspiciousStew);
        NBTTagCompound effect=new NBTTagCompound();effect.setInteger("EffectId",Potion.poison.id);effect.setInteger("EffectDuration",140);stew.setTagCompound(effect);
        ItemStack bowl=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.FoxFood114.finish(fox,stew);
        expect("FOX_STEW_EFFECT_CONTAINER",fox.isPotionActive(Potion.poison)&&fox.getActivePotionEffect(Potion.poison).getDuration()==140&&bowl!=null&&bowl.getItem()==Items.bowl,"stew effect and bowl retained");
        ItemStack bread=new ItemStack(Items.bread,2);ItemStack remaining=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.FoxFood114.finish(fox,bread);
        expect("FOX_FOOD_SINGLE_CONSUMPTION",remaining==bread&&remaining.stackSize==1,"one food consumed");
        world.isRemote=true;com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.FoxFood114.finish(fox,bread);world.isRemote=false;
        expect("FOX_FOOD_SERVER_ONLY",bread.stackSize==1,"client cannot consume");
        final float[] rolls={.01F,.1F,.3F,.5F,.7F,.9F};
        net.minecraft.item.Item[] expectedItems={Items.emerald,Items.egg,com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.rabbitFoot,Items.wheat,Items.leather,Items.feather};
        for(int i=0;i<rolls.length;i++){final float roll=rolls[i];expect("FOX_SPAWN_ITEM_"+roll,EntityFox114.spawnHeldItem(new Random(){public float nextFloat(){return roll;}public boolean nextBoolean(){return true;}})==expectedItems[i],"official weighted interval");}
        expect("FOX_SPAWN_RABBIT_HIDE",EntityFox114.spawnHeldItem(new Random(){public float nextFloat(){return .3F;}public boolean nextBoolean(){return false;}})==com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.rabbitHide,"both rabbit drops available");
        fox.setPosition(0,64,0);player.setPosition(1,64,0);fox.trust(player.getUniqueID());
        VillageFauna114Fixture.Player attacker=new VillageFauna114Fixture.Player(world);attacker.setPosition(3,64,0);
        world.loadedEntityList.add(player);world.loadedEntityList.add(attacker);player.setRevengeTarget(attacker);
        net.minecraft.entity.passive.EntityWolf wolf=new net.minecraft.entity.passive.EntityWolf(world);wolf.setTamed(true);
        expect("FOX_TAME_WOLF",!fox.avoids(wolf),"tamed wolf ignored");wolf.setTamed(false);expect("FOX_WILD_WOLF",fox.avoids(wolf),"wild wolf avoided");
        EntityAIBase defend=null;for(Object raw:fox.targetTasks.taskEntries){EntityAIBase ai=((EntityAITasks.EntityAITaskEntry)raw).action;if(ai.getClass().getSimpleName().equals("DefendTrusted"))defend=ai;}
        if(defend==null||!defend.shouldExecute())throw new AssertionError("Missing defence goal");defend.startExecuting();
        expect("FOX_DEFENCE_SUPPRESSES_FLEE",fox.defending()&&fox.getAttackTarget()==attacker&&!fox.avoids(attacker)&&!fox.avoids(wolf),"defending goal takes precedence");
        defend.resetTask();expect("FOX_DEFENCE_RESET",!fox.defending()&&fox.avoids(attacker),"normal avoidance restored");
        conversionChecks(world);
    }
    private static void conversionChecks(VillageFauna114Fixture.W world)throws Exception {
        net.minecraft.entity.passive.EntityVillager source=new net.minecraft.entity.passive.EntityVillager(world,1);
        com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.initializeWatcher(source);
        NBTTagCompound full=new NBTTagCompound();source.writeEntityToNBT(full);full.setInteger("Career",1);full.setInteger("CareerLevel",4);full.setString("Profession120","librarian");full.setString("VillagerType120","snow");
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ManagedVillagerOffer145 offer=new com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ManagedVillagerOffer145(new ItemStack(Items.emerald,12),null,new ItemStack(Items.book),16,5,.2F);
        offer.tradeRank=4;offer.incrementToolUses();offer.updateDemand();net.minecraft.village.MerchantRecipeList list=new net.minecraft.village.MerchantRecipeList();list.add(offer);full.setTag("Offers",list.getRecipiesAsTags());
        NBTTagCompound economy=new NBTTagCompound();economy.setInteger("Rank",4);economy.setInteger("Xp",170);economy.setBoolean("Traded",true);economy.setLong("LevelAt",-1);economy.setInteger("Restocks",1);economy.setLong("LastRestock",2000);full.setTag("HTGT6Economy145",economy);
        source.readEntityFromNBT(full);com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.read(source,full);
        com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager zombie=new com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager(world);zombie.captureVillagerState(source);
        NBTTagCompound saved=new NBTTagCompound();zombie.writeEntityToNBT(saved);com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager reloaded=new com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager(world);reloaded.readEntityFromNBT(saved);
        NBTTagCompound again=new NBTTagCompound();reloaded.writeEntityToNBT(again);
        expect("CONVERSION_SNAPSHOT_RELOAD",again.getCompoundTag("HTGT6VillagerState114").equals(saved.getCompoundTag("HTGT6VillagerState114")),"zombie reload preserves trade snapshot");
        net.minecraft.entity.passive.EntityVillager restored=new net.minecraft.entity.passive.EntityVillager(world);com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.initializeWatcher(restored);
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerConversion114.restore(restored,again.getCompoundTag("HTGT6VillagerState114"));
        NBTTagCompound after=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerConversion114.capture(restored);
        expect("CONVERSION_OFFERS",after.getCompoundTag("Offers").equals(saved.getCompoundTag("HTGT6VillagerState114").getCompoundTag("Offers")),"prices, uses, demand and rank persist");
        expect("CONVERSION_RANK_XP",com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145.rank(restored)==4&&com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145.xp(restored)==170,"rank 4 / XP 170 restored");
        expect("CONVERSION_CAREER",after.getString("Profession120").equals("librarian")&&after.getString("VillagerType120").equals("snow"),"profession and biome type restored");
        // Headless JVM lacks the constructor ASM; install only its watcher contract for this probe.
        final ConstructWatchers watcher=new ConstructWatchers();
        cpw.mods.fml.common.eventhandler.IEventListener listener=new cpw.mods.fml.common.eventhandler.IEventListener(){
            public void invoke(cpw.mods.fml.common.eventhandler.Event event){if(event instanceof net.minecraftforge.event.entity.EntityEvent.EntityConstructing)watcher.construct((net.minecraftforge.event.entity.EntityEvent.EntityConstructing)event);}
        };
        cpw.mods.fml.common.eventhandler.ListenerList listeners=new net.minecraftforge.event.entity.EntityEvent.EntityConstructing(source).getListenerList();
        int busId=field(cpw.mods.fml.common.eventhandler.EventBus.class,"busID").getInt(net.minecraftforge.common.MinecraftForge.EVENT_BUS);
        listeners.register(busId,cpw.mods.fml.common.eventhandler.EventPriority.NORMAL,listener);
        try {
            Method cure=reloaded.getClass().getDeclaredMethod("convertToVillager");cure.setAccessible(true);
            world.reject=true;int count=world.spawned.size();cure.invoke(reloaded);
            expect("CONVERSION_REJECTED_SPAWN",!reloaded.isDead&&world.spawned.size()==count,"zombie retained on rejected spawn");
            world.reject=false;cure.invoke(reloaded);
            expect("CONVERSION_ACCEPTED_SPAWN",reloaded.isDead&&world.spawned.size()==count+1,"accepted replacement removes zombie once");
            NBTTagCompound atSpawn=((AuditWorld)world).publishedTrades;
            expect("CONVERSION_PUBLISHED_COMPLETE",atSpawn!=null&&atSpawn.getCompoundTag("Offers").equals(after.getCompoundTag("Offers"))&&atSpawn.getCompoundTag("HTGT6Economy145").getInteger("Xp")==170,"trade snapshot complete before world accepts entity");
            cure.invoke(reloaded);expect("CONVERSION_NO_DUPLICATE",world.spawned.size()==count+1,"repeated conversion cannot duplicate villager");
        } finally {world.reject=false;listeners.unregister(busId,listener);}
    }
    public static final class ConstructWatchers {
        @cpw.mods.fml.common.eventhandler.SubscribeEvent public void construct(net.minecraftforge.event.entity.EntityEvent.EntityConstructing event){
            if(event.entity instanceof net.minecraft.entity.passive.EntityVillager)
                com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.initializeWatcher((net.minecraft.entity.passive.EntityVillager)event.entity);
        }
    }
    public static class AuditWorld extends VillageFauna114Fixture.W {
        NBTTagCompound publishedTrades;
        public boolean spawnEntityInWorld(net.minecraft.entity.Entity entity){
            if(!reject&&entity instanceof net.minecraft.entity.passive.EntityVillager)
                publishedTrades=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerConversion114.capture((net.minecraft.entity.passive.EntityVillager)entity);
            return super.spawnEntityInWorld(entity);
        }
        public void playAuxSFXAtEntity(net.minecraft.entity.player.EntityPlayer player,int id,int x,int y,int z,int data){}
        public int getFullBlockLightValue(int x,int y,int z){return 15;}
        public float getLightBrightness(int x,int y,int z){return 1;}
    }
}
