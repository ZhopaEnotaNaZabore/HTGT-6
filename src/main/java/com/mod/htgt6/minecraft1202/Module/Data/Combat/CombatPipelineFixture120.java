package com.mod.htgt6.minecraft1202.Module.Data.Combat;
import java.util.*;import java.lang.reflect.*;import java.net.*;import java.io.*;
import net.minecraft.block.Block;import net.minecraft.entity.*;import net.minecraft.entity.player.*;
import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.enchantment.*;import net.minecraft.potion.*;
import net.minecraft.util.*;import net.minecraft.world.*;import net.minecraft.world.chunk.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120SculkShrieker;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
/** Executes actual transformed Minecraft classes, not just the extracted formulas. */
public final class CombatPipelineFixture120 {
 public static void main(String[] args)throws Exception{
  if(args.length==0){
   String[] entries=System.getProperty("java.class.path").split(java.io.File.pathSeparator);URL[] urls=new URL[entries.length];for(int i=0;i<urls.length;i++)urls[i]=new File(entries[i]).toURI().toURL();
   try(URLClassLoader loader=new URLClassLoader(urls,CombatPipelineFixture120.class.getClassLoader()){
    @Override protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{
     if(!(name.startsWith("net.minecraft.")||name.startsWith("net.minecraftforge.")||name.startsWith("cpw.mods.fml.")||name.startsWith("com.mod.htgt6.")))return super.loadClass(name,resolve);
     Class<?> type=findLoadedClass(name);if(type==null)type=findClass(name);if(resolve)resolveClass(type);return type;
    }
    @Override protected Class<?> findClass(String name)throws ClassNotFoundException{
     URL url=findResource(name.replace('.','/')+".class");if(url==null)throw new ClassNotFoundException(name);
     try(InputStream in=url.openStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);byte[] bytes=out.toByteArray();
      int kind=name.equals("net.minecraft.entity.player.EntityPlayer")?0:name.equals("net.minecraft.entity.EntityLivingBase")?1:name.equals("net.minecraft.enchantment.EnchantmentHelper")?2:name.equals("net.minecraft.enchantment.EnchantmentDamage")?3:name.equals("net.minecraft.item.ItemSword")?4:-1;
      if(name.equals("net.minecraftforge.event.entity.living.LivingAttackEvent")){
       // FML's event transformer materializes @Cancelable as this override at launch.
       org.objectweb.asm.tree.ClassNode eventNode=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(bytes).accept(eventNode,0);
       org.objectweb.asm.tree.MethodNode cancel=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC,"isCancelable","()Z",null,null);cancel.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ICONST_1));cancel.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.IRETURN));eventNode.methods.add(cancel);
       org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);eventNode.accept(writer);bytes=writer.toByteArray();
      }
      if(name.equals("net.minecraft.entity.item.EntityItem"))bytes=com.mod.htgt6.asm.NetheriteItemASM120.transform(bytes);
      if(kind>=0)bytes=com.mod.htgt6.asm.CombatPipelineASM120.transform(bytes,kind);
      return defineClass(name,bytes,0,bytes.length);
     }catch(IOException e){throw new ClassNotFoundException(name,e);}
    }
   }){loader.loadClass(CombatPipelineFixture120.class.getName()).getMethod("main",String[].class).invoke(null,(Object)new String[]{"child"});}return;
  }
  Class<?> unsafe=Class.forName("sun.misc.Unsafe");Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
  Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);loader.set(null,unsafe.getMethod("allocateInstance",Class.class).invoke(singleton.get(null),cpw.mods.fml.common.Loader.class));
  Bootstrap.func_151354_b();
  com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
  Grid world=new Grid();world.difficultySetting=EnumDifficulty.NORMAL;
  Player p=new Player(world,1);p.setPosition(0,80,0);p.inventory.mainInventory[0]=new ItemStack(Items.iron_sword);p.getAttributeMap().applyAttributeModifiers(p.getHeldItem().getAttributeModifiers());
  net.minecraft.entity.passive.EntityPig target=new net.minecraft.entity.passive.EntityPig(world);target.setPosition(0,80,1);target.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100);target.setHealth(100);
  p.getHeldItem().addEnchantment(Enchantment.sharpness,5);p.addPotionEffect(new PotionEffect(Potion.damageBoost.id,100,0));
  close(CombatAttack120.baseDamage(p),9,"Strength preserves weapon damage");
  close(EnchantmentHelper.getEnchantmentModifierLiving(p,target),3,"modern Sharpness V");
  p.attackTargetEntityWithCurrentItem(target);close(target.getHealth(),88,"real attack includes strength and sharpness before hurt pipeline");
  p.attackTargetEntityWithCurrentItem(target);close(target.getHealth(),88,"weak repeated attack rejected by hurt immunity");
  p.inventory.mainInventory[0]=new ItemStack(Items.diamond_axe);close(CombatAttack120.strength(p,0),0,"weapon swap reset");
  world.time=100;CombatAttack120.tick(p);p.swingItem();world.time++;CombatAttack120.tick(p);require(CombatAttack120.strength(p,0)<.1F,"air swing reset");
  p.getEntityAttribute(CombatAttack120.ATTACK_SPEED).applyModifier(new net.minecraft.entity.ai.attributes.AttributeModifier(UUID.randomUUID(),"test",1,0));close(CombatAttack120.speed(p),2,"additive speed modifier");
  ItemStack diamond=new ItemStack(Items.diamond_chestplate);diamond.addEnchantment(Enchantment.protection,4);
  for(int i=0;i<20;i++)require(EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{diamond},DamageSource.generic)==4,"protection must be deterministic");
  Player armored=new Player(world,2);armored.inventory.armorInventory=new ItemStack[]{new ItemStack(Items.diamond_boots),new ItemStack(Items.diamond_leggings),new ItemStack(Items.diamond_chestplate),new ItemStack(Items.diamond_helmet)};
  close(CombatDamage120.ApplyArmor(armored,armored.inventory.armorInventory,DamageSource.causeMobDamage(target),20),8,"real player armour adapter");
  for(ItemStack piece:armored.inventory.armorInventory)require(piece.getItemDamage()==5,"armour durability uses original incoming damage");
  EntityWarden120 warden=new EntityWarden120(world);warden.setPosition(0,80,0);warden.beginEmerging();
  require(!warden.attackEntityFrom(DamageSource.causePlayerDamage(p),10)&&warden.getHealth()==500,"emerging immunity");
  Method state=EntityWarden120.class.getDeclaredMethod("setState",int.class,int.class);state.setAccessible(true);state.invoke(warden,EntityWarden120.IDLE,0);
  Method sonic=EntityWarden120.class.getDeclaredMethod("sonicBoom",EntityLivingBase.class);sonic.setAccessible(true);
  for(EnumDifficulty difficulty:new EnumDifficulty[]{EnumDifficulty.EASY,EnumDifficulty.NORMAL,EnumDifficulty.HARD}){
   world.difficultySetting=difficulty;Player victim=new Player(world,10+difficulty.ordinal());victim.setPosition(0,80,5);victim.inventory.armorInventory[2]=diamond.copy();
   sonic.invoke(warden,victim);close(20-victim.getHealth(),difficulty==EnumDifficulty.EASY?6:difficulty==EnumDifficulty.NORMAL?10:15,"sonic difficulty and protection bypass");
  }
  world.difficultySetting=EnumDifficulty.NORMAL;target.setHealth(100);target.hurtResistantTime=0;target.setPosition(0,80,40);sonic.invoke(warden,target);close(target.getHealth(),100,"sonic range recheck");
  target.setPosition(0,98,5);target.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1);target.motionX=target.motionY=target.motionZ=0;sonic.invoke(warden,target);
  close(target.getHealth(),90,"sonic vertical cylinder");close((float)(Math.abs(target.motionX)+Math.abs(target.motionY)+Math.abs(target.motionZ)),0,"sonic knockback resistance");
  armored.hurtResistantTime=0;armored.attackEntityFrom(DamageSource.causeMobDamage(target),20);close(armored.getHealth(),12,"transformed player damage armour path");
  target.setHealth(100);target.hurtResistantTime=0;target.setCurrentItemOrArmor(3,new ItemStack(Items.diamond_chestplate));
  target.attackEntityFrom(DamageSource.causeMobDamage(warden),20);close(100-target.getHealth(),CombatRules120.damageAfterAbsorb(20,8,2),"transformed mob armour path");
  discardNetwork(OffhandSystem.class);discardNetwork(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatUpdate.class);
  final com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler handler=new com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler();
  Field eventErrors=cpw.mods.fml.common.eventhandler.EventBus.class.getDeclaredField("exceptionHandler");eventErrors.setAccessible(true);eventErrors.set(net.minecraftforge.common.MinecraftForge.EVENT_BUS,(cpw.mods.fml.common.eventhandler.IEventExceptionHandler)(bus,event,listeners,index,error)->{throw new AssertionError("event dispatch",error);});
  Field busId=cpw.mods.fml.common.eventhandler.EventBus.class.getDeclaredField("busID");busId.setAccessible(true);
  new net.minecraftforge.event.entity.living.LivingAttackEvent(p,DamageSource.generic,1).getListenerList().register(busId.getInt(net.minecraftforge.common.MinecraftForge.EVENT_BUS),cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST,event->{if(event instanceof net.minecraftforge.event.entity.living.LivingAttackEvent)handler.onLivingAttack((net.minecraftforge.event.entity.living.LivingAttackEvent)event);});
  Player attacker=new Player(world,30),defender=new Player(world,31);attacker.setPosition(0,80,0);defender.setPosition(0,80,1);defender.rotationYaw=180;
  attacker.inventory.mainInventory[0]=new ItemStack(Items.iron_sword);attacker.getAttributeMap().applyAttributeModifiers(attacker.getHeldItem().getAttributeModifiers());
  ItemStack shield=new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.shield);defender.inventory.mainInventory[0]=shield;
  Field count=EntityPlayer.class.getDeclaredField("itemInUseCount");count.setAccessible(true);defender.setItemInUse(shield,72000);count.setInt(defender,71995);
  attacker.attackTargetEntityWithCurrentItem(defender);close(defender.getHealth(),20,"shield cancels full hit");require(shield.getItemDamage()==7,"shield full modern damage durability");
  world.time+=3;attacker.attackTargetEntityWithCurrentItem(defender);require(shield.getItemDamage()==7,"weak hit under 3 does not wear shield");
  warden.setPosition(0,80,0);warden.attackEntityAsMob(defender);require(OffhandSystem.getShieldCooldownTicks(defender)==100,"Warden disables shield");
  require(warden.getWardenState()==EntityWarden120.MELEE,"blocked Warden swing has cooldown/animation");
  Player sweeping=new Player(world,40);sweeping.setPosition(0,80,0);sweeping.onGround=true;sweeping.setAIMoveSpeed(.1F);sweeping.inventory.mainInventory[0]=new ItemStack(Items.iron_sword);sweeping.getAttributeMap().applyAttributeModifiers(sweeping.getHeldItem().getAttributeModifiers());sweeping.getHeldItem().addEnchantment(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.sweepingEdge,3);
  net.minecraft.entity.passive.EntityPig primary=new net.minecraft.entity.passive.EntityPig(world),secondary=new net.minecraft.entity.passive.EntityPig(world);primary.setPosition(0,80,1);secondary.setPosition(.5,80,1);world.spawned.add(primary);world.spawned.add(secondary);
  sweeping.attackTargetEntityWithCurrentItem(primary);close(secondary.getHealth(),4.5F,"Sweeping Edge III uses primary damage");
  Player resistant=new Player(world,41);resistant.setPosition(0,80,5);resistant.addPotionEffect(new PotionEffect(Potion.resistance.id,100,0));sonic.invoke(warden,resistant);close(resistant.getHealth(),12,"sonic keeps Resistance reduction");
  world.hidden=true;primary.setHealth(10);warden.setPosition(0,80,0);
  Method anger=EntityWarden120.class.getDeclaredMethod("increaseAnger",EntityLivingBase.class,int.class);anger.setAccessible(true);anger.invoke(warden,primary,120);state.invoke(warden,EntityWarden120.IDLE,0);
  warden.onLivingUpdate();close(primary.getHealth(),10,"Warden melee requires visibility");world.hidden=false;
  EntityWarden120 listening=new EntityWarden120(world);listening.setPosition(0,80,0);listening.hearVibration(primary,0,80,1,1);int heard=listening.getAnger();listening.hearVibration(primary,0,80,1,1);require(listening.getAnger()==heard,"vibration cooldown");
  SpecialArmor special=new SpecialArmor();ItemStack[] mixed={new ItemStack(special),new ItemStack(Items.iron_helmet),null,null};
  Player specialWearer=new Player(world,50);specialWearer.inventory.armorInventory=mixed;
  verifyServerArmorHooks(specialWearer,mixed,DamageSource.causeMobDamage(target),special);
  close(CombatDamage120.ApplyArmor(specialWearer,mixed,DamageSource.causeMobDamage(target),20),9.84F,"mixed special armour priority and modern ordinary armour");require(special.wear>0,"special armour callback preserved");
  ItemStack sword=new ItemStack(Items.iron_sword);p.clearItemInUse();sword.getItem().onItemRightClick(sword,world,p);require(PlayerItemUse120.item(p)==null,"right-click sword cannot start blocking");
  Player custom=new Player(world,51);custom.inventory.mainInventory[0]=new ItemStack(Items.iron_sword);custom.getAttributeMap().applyAttributeModifiers(custom.getHeldItem().getAttributeModifiers());custom.getEntityAttribute(SharedMonsterAttributes.attackDamage).applyModifier(new net.minecraft.entity.ai.attributes.AttributeModifier(UUID.randomUUID(),"external",2,0));close(CombatAttack120.baseDamage(custom),8,"external damage modifier retained");custom.addPotionEffect(new PotionEffect(Potion.weakness.id,100,0));close(CombatAttack120.baseDamage(custom),4,"modern weakness");
  target.motionX=target.motionY=target.motionZ=0;target.onGround=true;target.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(.5);target.knockBack(p,99,0,-1);close((float)target.motionZ,.2F,"fractional knockback resistance");
  net.minecraft.entity.projectile.EntityArrow distraction=new net.minecraft.entity.projectile.EntityArrow(world);distraction.shootingEntity=p;
  EntityWarden120 distracted=new EntityWarden120(world);distracted.setPosition(0,80,0);distracted.hearVibration(distraction,2,80,0,1);require(distracted.getAnger()==10,"first projectile anger");
  Field vibration=EntityWarden120.class.getDeclaredField("vibrationCooldown");vibration.setAccessible(true);vibration.setInt(distracted,0);distracted.hearVibration(distraction,3,80,0,1);require(distracted.getAnger()==45,"repeated projectile anger");
  Field location=EntityWarden120.class.getDeclaredField("disturbanceX");location.setAccessible(true);close((float)location.getDouble(distracted),(float)p.posX,"repeated projectile finds shooter");
  com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.prepare();
  Player smith=new Player(world,75);
  ItemStack netheriteSword=new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgrades.get(Items.diamond_sword));
  smith.inventory.mainInventory[0]=netheriteSword;smith.getAttributeMap().applyAttributeModifiers(netheriteSword.getAttributeModifiers());
  close(CombatAttack120.baseDamage(smith),8,"netherite sword total damage");close(CombatAttack120.speed(smith),1.6F,"netherite sword speed");
  net.minecraft.entity.item.EntityItem drop=new net.minecraft.entity.item.EntityItem(world,0,80,0,netheriteSword.copy());
  require(!drop.attackEntityFrom(DamageSource.lava,100)&&!drop.isDead,"netherite destroyed by lava");
  drop.attackEntityFrom(DamageSource.outOfWorld,100);require(drop.isDead,"netherite incorrectly immune to void");
  int armorSlot=1;for(Item source:new Item[]{Items.diamond_boots,Items.diamond_leggings,Items.diamond_chestplate,Items.diamond_helmet})
    smith.setCurrentItemOrArmor(armorSlot++,new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgrades.get(source)));
  com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.NetheriteEquipment120 equipment=new com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.NetheriteEquipment120();
  equipment.update(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(smith));
  close((float)smith.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue(),.4F,"netherite knockback resistance");
  for(int slot=1;slot<=4;slot++)smith.setCurrentItemOrArmor(slot,null);
  equipment.update(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(smith));
  close((float)smith.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue(),0,"removed netherite retained resistance");
  require(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidWavePlan145.Type.WITCH.create(world) instanceof com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRaidWitch151,"raid witch factory bypasses support AI");
  System.out.println("Combat pipeline OK: transformed attack, modifiers, cooldown/swap/air swing, enchantments, armour durability, sonic difficulty/range/resistance, emerging immunity, shield wear/disable, Sweeping Edge, visibility, special armour, projectile anger");
 }
 private static void verifyServerArmorHooks(Player wearer,ItemStack[] armor,DamageSource source,SpecialArmor special)throws Exception{
  // Execute transformed calls with both Forge and hybrid-server descriptors.
  // Merely verifying the operand stack does not catch a missing target overload.
  final String prefix="(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;D";
  for(int kind=0;kind<=1;kind++){
   org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
   writer.visit(52,org.objectweb.asm.Opcodes.ACC_PUBLIC,"ArmorServerBridge"+kind,null,"java/lang/Object",null);
   for(String suffix:new String[]{")F","Z)F","I)F"}){
    org.objectweb.asm.MethodVisitor m=writer.visitMethod(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"apply",prefix+suffix,null,null);
    m.visitCode();m.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD,0);m.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD,1);m.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD,2);m.visitVarInsn(org.objectweb.asm.Opcodes.DLOAD,3);
    if(!suffix.equals(")F"))m.visitVarInsn(org.objectweb.asm.Opcodes.ILOAD,5);
    m.visitMethodInsn(org.objectweb.asm.Opcodes.INVOKESTATIC,"net/minecraftforge/common/ISpecialArmor$ArmorProperties","ApplyArmor",prefix+suffix,false);
    m.visitInsn(org.objectweb.asm.Opcodes.FRETURN);m.visitMaxs(0,0);m.visitEnd();
   }
   writer.visitEnd();byte[] transformed=com.mod.htgt6.asm.CombatPipelineASM120.transform(writer.toByteArray(),kind);
   org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(transformed).accept(node,0);
   int redirected=0,untouched=0;
   for(org.objectweb.asm.tree.MethodNode method:node.methods)for(org.objectweb.asm.tree.AbstractInsnNode raw:method.instructions.toArray())if(raw instanceof org.objectweb.asm.tree.MethodInsnNode){
    org.objectweb.asm.tree.MethodInsnNode call=(org.objectweb.asm.tree.MethodInsnNode)raw;
    if(call.desc.equals(prefix+"I)F")){require(call.owner.equals("net/minecraftforge/common/ISpecialArmor$ArmorProperties"),"unknown armour signature must remain with original owner");untouched++;}
    else {require(call.owner.equals("com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/CombatDamage120"),"armour overload redirected");redirected++;}
   }
   require(redirected==2&&untouched==1,"both armour signatures covered");
   class BridgeLoader extends ClassLoader {BridgeLoader(){super(CombatPipelineFixture120.class.getClassLoader());}Class<?> define(byte[] bytes){return defineClass(null,bytes,0,bytes.length);}}
   Class<?> bridge=new BridgeLoader().define(transformed);
   Method hybrid=bridge.getMethod("apply",EntityLivingBase.class,ItemStack[].class,DamageSource.class,double.class,boolean.class);
   int specialBefore=special.wear,normalBefore=armor[1].getItemDamage();
   close((Float)hybrid.invoke(null,wearer,armor,source,20D,false),9.84F,"server armour preview result");
   require(special.wear==specialBefore&&armor[1].getItemDamage()==normalBefore,"preview must not damage either armour type");
   close((Float)hybrid.invoke(null,wearer,armor,source,20D,true),9.84F,"server armour applied result");
   int specialDelta=special.wear-specialBefore,normalDelta=armor[1].getItemDamage()-normalBefore;
   require(specialDelta>0&&normalDelta>0,"server apply damages both armour types");
   specialBefore=special.wear;normalBefore=armor[1].getItemDamage();
   Method forge=bridge.getMethod("apply",EntityLivingBase.class,ItemStack[].class,DamageSource.class,double.class);
   close((Float)forge.invoke(null,wearer,armor,source,20D),9.84F,"Forge armour result");
   require(special.wear-specialBefore==specialDelta&&armor[1].getItemDamage()-normalBefore==normalDelta,"server apply wears armour exactly once like Forge");
  }
 }
 private static final class SpecialArmor extends Item implements net.minecraftforge.common.ISpecialArmor {
  int wear;
  public net.minecraftforge.common.ISpecialArmor.ArmorProperties getProperties(EntityLivingBase e,ItemStack s,DamageSource d,double amount,int slot){return new net.minecraftforge.common.ISpecialArmor.ArmorProperties(1,.5,Integer.MAX_VALUE);}
  public int getArmorDisplay(EntityPlayer p,ItemStack s,int slot){return 0;}
  public void damageArmor(EntityLivingBase e,ItemStack s,DamageSource d,int amount,int slot){wear+=amount;}
 }
 private static void discardNetwork(Class<?> owner)throws Exception{
  Field field=owner.getDeclaredField("NETWORK");field.setAccessible(true);Field modifiers=Field.class.getDeclaredField("modifiers");modifiers.setAccessible(true);modifiers.setInt(field,field.getModifiers()&~Modifier.FINAL);
  field.set(null,new cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper("fixture"+owner.getSimpleName()){
   @Override public void sendToAllAround(cpw.mods.fml.common.network.simpleimpl.IMessage message,cpw.mods.fml.common.network.NetworkRegistry.TargetPoint point){}
  });
 }
 private static void close(float value,float expected,String why){if(Math.abs(value-expected)>.001)throw new AssertionError(why+": "+value+" != "+expected);}
 private static void require(boolean condition,String why){if(!condition)throw new AssertionError(why);}
    private static final class Player extends net.minecraft.entity.player.EntityPlayer {
        Player(World world,int id){super(world,new com.mojang.authlib.GameProfile(new UUID(1202,id),"WardenTest"+id));}
        @Override public void addChatMessage(IChatComponent message){}
        @Override public boolean canCommandSenderUseCommand(int level,String command){return false;}
        @Override public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,80,0);}
    }
    private static final class Grid extends World {
        AxisAlignedBB wall;boolean hidden;int floor=-1;long time;Block floorBlock=Blocks.stone,placed;int meta,delay;TileEntityBackport120SculkShrieker tile;
        @Override public net.minecraft.tileentity.TileEntity getTileEntity(int x,int y,int z){return tile;}
        @Override public boolean setBlockMetadataWithNotify(int x,int y,int z,int value,int flags){meta=value;return true;}
        @Override public boolean setBlock(int x,int y,int z,Block block,int value,int flags){placed=block;meta=value;return true;}
        @Override public void scheduleBlockUpdate(int x,int y,int z,Block block,int ticks){delay=ticks;}
        @Override public void playSoundEffect(double x,double y,double z,String sound,float volume,float pitch){}
        @Override public long getTotalWorldTime(){return time;}
        @Override public boolean checkNoEntityCollision(AxisAlignedBB box){return true;}
        @Override public void playSoundAtEntity(Entity entity,String sound,float volume,float pitch){}
        final List<Entity> spawned=new ArrayList<Entity>();
        Grid(){super(null,"phantom fixture",new WorldProviderSurface(),new WorldSettings(1202,WorldSettings.GameType.SURVIVAL,true,false,WorldType.DEFAULT),new net.minecraft.profiler.Profiler());provider.registerWorld(this);chunkProvider=createChunkProvider();}
        @Override public void setEntityState(Entity entity,byte state){}
        @Override public Block getBlock(int x,int y,int z){return y==floor?floorBlock:Blocks.air;}
        @Override public int getBlockMetadata(int x,int y,int z){return meta;}
        @Override public boolean checkChunksExist(int x,int y,int z,int xx,int yy,int zz){return true;}
        @Override public boolean blockExists(int x,int y,int z){return true;}
        @Override public List getCollidingBoundingBoxes(Entity entity,AxisAlignedBB box){
            List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();
            if(wall!=null&&wall.intersectsWith(box))boxes.add(wall);
            return boxes;
        }
        @Override public MovingObjectPosition rayTraceBlocks(Vec3 from,Vec3 to){return hidden?new MovingObjectPosition(0,80,0,0,from):null;}
        @Override public boolean isAnyLiquid(AxisAlignedBB box){return false;}
        @Override public boolean canBlockSeeTheSky(int x,int y,int z){return true;}
        @Override public boolean spawnEntityInWorld(Entity entity){spawned.add(entity);return true;}
        @Override public List getEntitiesWithinAABB(Class type,AxisAlignedBB box){
            List<Entity> result=new ArrayList<Entity>();
            for(Entity entity:spawned)if(type.isInstance(entity)&&entity.boundingBox.intersectsWith(box))result.add(entity);
            return result;
        }
        @Override protected int func_152379_p(){return 0;}
        @Override public Entity getEntityByID(int id){return null;}
        @Override protected IChunkProvider createChunkProvider(){
            return (IChunkProvider)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{IChunkProvider.class},(proxy,method,args)->{
                if(method.getName().equals("chunkExists"))return true;
                if(method.getName().equals("provideChunk"))return new Chunk(this,(Integer)args[0],(Integer)args[1]);
                if(method.getReturnType()==boolean.class)return false;
                if(method.getReturnType()==int.class)return 0;
                return null;
            });
        }
    }
}
