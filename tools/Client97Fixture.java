import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.client.inventory112.TutorialWood112;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.world.World;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.oredict.OreDictionary;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.lang.reflect.*;

public final class Client97Fixture {
 public static void main(String[] args)throws Exception {
  World w=BountifulMechanicsFixture120.initialize();
  Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.CLIENT);
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field provider=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");provider.setAccessible(true);provider.set(w.provider,w);
  Player player=new Player(w);player.inventory=new Inventory112Audit86Fixture.Bridge(player);OffhandHooks.initializeCompartments(player.inventory);
  for(String name:new String[]{"logWood","woodLog","LogAny","logAny","logAnyWood"}) {
   Item item=new Item();cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"test97_"+name);
   OreDictionary.registerOre(name,new ItemStack(item,1,OreDictionary.WILDCARD_VALUE));
   check(TutorialWood112.matches(new ItemStack(item,1,7),false),name+" wildcard");
   check(!TutorialWood112.matches(new ItemStack(item,1,7),true),"logs not planks");
  }
  Item tagged=new Item();cpw.mods.fml.common.registry.GameRegistry.registerItem(tagged,"test97_specific");
  OreDictionary.registerOre("plankAnyWood",new ItemStack(tagged,1,3));
  check(TutorialWood112.matches(new ItemStack(tagged,1,3),true)&&!TutorialWood112.matches(new ItemStack(tagged,1,4),true),"metadata specificity");
  check(TutorialWood112.matches(new ItemStack(Blocks.log),false)&&TutorialWood112.matches(new ItemStack(Blocks.planks),true),"vanilla wood");
  check(!TutorialWood112.matches(new ItemStack(Items.stick),false)&&!TutorialWood112.matches(null,false),"nonwood/null");
  Target modEntity=new Target(w);
  check(AttackIndicator112.isReadyTarget(1,modEntity),"mod entity outside LivingBase");
  check(AttackIndicator112.isReadyTarget(1,new net.minecraft.entity.item.EntityItem(w)),"custom selected nonliving target");
  check(AttackIndicator112.isReadyTarget(1,new net.minecraft.entity.passive.EntityPig(w)),"living target");
  check(!AttackIndicator112.isReadyTarget(.5F,modEntity)&&!AttackIndicator112.isReadyTarget(1,null),"charging/no target");
  modEntity.isDead=true;check(!AttackIndicator112.isReadyTarget(1,modEntity),"dead target");
  Class<?> asm=Class.forName("com.mod.htgt6.asm.OffhandLightASM");Method transform=asm.getDeclaredMethod("transform",byte[].class);transform.setAccessible(true);
  // Verify the actual installed Angelica class, without loading its optional renderer dependencies.
  try(java.util.zip.ZipFile zip=new java.util.zip.ZipFile(args[0])) {
   java.io.InputStream in=zip.getInputStream(zip.getEntry("com/gtnewhorizons/angelica/dynamiclights/DynamicLights.class"));
   ClassReader reader=new ClassReader(in);in.close();ClassWriter copy=new ClassWriter(0);reader.accept(copy,0);
   byte[] transformed=(byte[])transform.invoke(null,(Object)copy.toByteArray());
   ClassNode node=new ClassNode();new ClassReader(transformed).accept(node,0);
   for(MethodNode method:node.methods)new Analyzer(new BasicVerifier()).analyze(node.name,method);
   check(java.util.Arrays.equals(transformed,(byte[])transform.invoke(null,(Object)transformed)),"idempotent injection");
  }
  // Execute the injected bridge in the JVM with a minimal luminance provider.
  ClassWriter stub=new ClassWriter(ClassWriter.COMPUTE_MAXS);stub.visit(52,Opcodes.ACC_PUBLIC,"Light97",null,"java/lang/Object",null);
  MethodVisitor m=stub.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"getLuminanceFromEntity","(Lnet/minecraft/entity/Entity;)I",null,null);
  m.visitCode();m.visitInsn(Opcodes.ICONST_5);m.visitInsn(Opcodes.IRETURN);m.visitMaxs(0,0);m.visitEnd();
  m=stub.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"getLuminanceFromItemStack","(Lnet/minecraft/item/ItemStack;Z)I",null,null);
  m.visitCode();m.visitVarInsn(Opcodes.ALOAD,0);m.visitVarInsn(Opcodes.ILOAD,1);m.visitMethodInsn(Opcodes.INVOKESTATIC,"Client97Fixture","itemLight","(Lnet/minecraft/item/ItemStack;Z)I",false);m.visitInsn(Opcodes.IRETURN);m.visitMaxs(0,0);m.visitEnd();stub.visitEnd();
  byte[] code=(byte[])transform.invoke(null,(Object)stub.toByteArray());
  Method light=new Loader().define(code).getMethod("getLuminanceFromEntity",Entity.class);
  check((Integer)light.invoke(null,player)==5,"empty hand keeps original light");
  OffhandHooks.setOffhand(player,new ItemStack(Blocks.torch));check((Integer)light.invoke(null,player)==14,"offhand torch contributes");
  OffhandHooks.setOffhand(player,new ItemStack(Blocks.stone));check((Integer)light.invoke(null,player)==5,"dim hand does not lower light");
  OffhandHooks.setOffhand(player,new ItemStack(Blocks.torch));player.wet=true;check((Integer)light.invoke(null,player)==5,"provider underwater rule");
  check((Integer)light.invoke(null,modEntity)==5,"nonplayer unaffected");
  System.out.println("PASS: ore aliases/wildcards/metadata, indicator targets, actual Angelica ASM analysis/idempotence, JVM bridge empty/bright/dim/water/nonplayer");
 }
 public static int itemLight(ItemStack stack,boolean water){return water?0:stack.getItem()==Item.getItemFromBlock(Blocks.torch)?14:0;}
 static class Loader extends ClassLoader {Class<?> define(byte[] bytes){return defineClass("Light97",bytes,0,bytes.length);}}
 static class Player extends EntityPlayer {
  boolean wet;
  Player(World w){super(w,new com.mojang.authlib.GameProfile(new java.util.UUID(97,97),"Fixture97"));}
  public boolean isInsideOfMaterial(net.minecraft.block.material.Material material){return wet;}
  public void addChatMessage(IChatComponent c){}public boolean canCommandSenderUseCommand(int i,String s){return false;}public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,0,0);}
 }
 static class Target extends Entity {
  Target(World w){super(w);}protected void entityInit(){}protected void readEntityFromNBT(NBTTagCompound n){}protected void writeEntityToNBT(NBTTagCompound n){}public boolean canBeCollidedWith(){return true;}
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
