package com.mod.htgt6.common.handler.recipes;
import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.item.htgt6.fluids.dustsDatabase;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.*;
import gregapi.oredict.*;
import gregapi.recipes.Recipe;
import gregapi.util.OM;
import net.minecraft.item.*;
import net.minecraftforge.fluids.*;
import java.util.*;
public final class PlatinumGroupFixture {
 static class Part extends Item implements IOreDictItemDataOverrideItem {
  final OreDictPrefix prefix;
  Part(OreDictPrefix p){prefix=p;setHasSubtypes(true);}
  public OreDictItemData getOreDictItemData(ItemStack s){return new OreDictItemData(prefix,OreDictMaterial.MATERIAL_ARRAY[s.getItemDamage()]);}
 }
 static ItemStack stack(OreDictPrefix p,OreDictMaterial m,int n){return p.mat(m,n);}
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static Recipe recipe(ItemStack in,ItemStack out){return new Recipe(false,false,false,new ItemStack[]{in},new ItemStack[]{out},null,new long[]{2300},new FluidStack[0],new FluidStack[0],120,480,0);}
 public static void main(String[] args)throws Exception{
  CrusherOreImportFixture.initializeGT6();
  java.io.InputStream source=Recipe.class.getResourceAsStream("Recipe.class");
  java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();
  byte[] chunk=new byte[8192];int read;while((read=source.read(chunk))!=-1)buffer.write(chunk,0,read);source.close();
  Class<?> asm=Class.forName("com.mod.htgt6.asm.PlatinumGroupRecipeASM");
  java.lang.reflect.Method transform=asm.getDeclaredMethod("transform",byte[].class);transform.setAccessible(true);
  byte[] patched=(byte[])transform.invoke(null,(Object)buffer.toByteArray());
  require(java.util.Arrays.equals(patched,(byte[])transform.invoke(null,(Object)patched)),"ASM idempotence");
  org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();
  new org.objectweb.asm.ClassReader(patched).accept(node,0);
  int hooks=0;
  for(org.objectweb.asm.tree.MethodNode method:node.methods)if(method.name.equals("<init>")){
   new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
   for(org.objectweb.asm.tree.AbstractInsnNode i:method.instructions.toArray())if(i instanceof org.objectweb.asm.tree.MethodInsnNode && ((org.objectweb.asm.tree.MethodInsnNode)i).name.equals("normalize"))hooks++;
  }
  require(hooks>0,"late recipe constructor hooks verified");
  java.lang.reflect.Method direct=asm.getDeclaredMethod("transformDirectProcessing",byte[].class,boolean.class);direct.setAccessible(true);
  for(String type:new String[]{"gregapi/oredict/listeners/OreDictListenerItem_Washing","gregtech/tileentity/multiblocks/MultiTileEntityCrucible"}){
   source=Recipe.class.getClassLoader().getResourceAsStream(type+".class");buffer.reset();while((read=source.read(chunk))!=-1)buffer.write(chunk,0,read);source.close();
   boolean washing=type.contains("Washing");patched=(byte[])direct.invoke(null,buffer.toByteArray(),washing);
   require(java.util.Arrays.equals(patched,(byte[])direct.invoke(null,patched,washing)),"direct hook idempotence");
   node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(patched).accept(node,0);
   for(org.objectweb.asm.tree.MethodNode method:node.methods)if((method.access & (org.objectweb.asm.Opcodes.ACC_ABSTRACT|org.objectweb.asm.Opcodes.ACC_NATIVE))==0)
    new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
  }
  Loader.instance().activeModContainer().getMetadata().modId="gregtech";
  OreDictMaterial[] metals={MT.Pt,MT.Pd,MT.Ir,MT.Os,MT.Ru,MT.Rh,MT.Ke};
  for(OreDictPrefix prefix:new OreDictPrefix[]{OP.dust,OP.dustTiny,OP.dustSmall,OP.ingot,OP.plate,OP.oreBasalt,OP.crushed,OP.crushedCentrifugedTiny}){
   Item item=new Part(prefix);GameRegistry.registerItem(item,"gt.meta."+prefix.mNameInternal);
   for(OreDictMaterial mat:new OreDictMaterial[]{MT.Pt,MT.Pd,MT.Ir,MT.Os,MT.Ru,MT.Rh,MT.Ke,MT.PlatinumGroupSludge,MT.KOH,MT.K2S2O7,MT.Al,MT.Na,MT.NaCl,MT.Na2SO4,MT.Al2O3})
    OreDictManager.INSTANCE.setTarget(prefix,mat,new ItemStack(item,1,mat.mID),true,true);
  }
  Loader.instance().activeModContainer().getMetadata().modId="HTGT6";
  dustsDatabase.InitFluidSubItems();
  for(String name:new String[]{"aquaregia","platiunum_sollution","water","hydrogen","hydrochloricacid","potassium_disuplhate","ruthenium_salt","acidic_osmium_sollution","trinium_fluiride"})
   if(FluidRegistry.getFluid(name)==null)FluidRegistry.registerFluid(new Fluid(name));
  // Initialize maps, then isolate the fixture from GT6's unrelated recipes.
  Object init=RM.HeatMixer;
  for(Recipe.RecipeMap map:Recipe.RecipeMap.RECIPE_MAPS.values())map.mRecipeList.clear();
  PlatinumGroupChemistryRecipes.register();
  require(RM.HeatMixer.mRecipeList.size()+RM.Centrifuge.mRecipeList.size()==11,"eight chemistry stages and three equivalent sludge inputs");
  net.minecraft.item.crafting.FurnaceRecipes.smelting().func_151394_a(stack(OP.oreBasalt,MT.Pt,1),stack(OP.ingot,MT.Pt,1),0);
  PlatinumGroupSludgeRecipes.install();
  require(OM.anyassociation(net.minecraft.item.crafting.FurnaceRecipes.smelting().getSmeltingResult(stack(OP.oreBasalt,MT.Pt,1))).mMaterial.mMaterial==MT.PlatinumGroupSludge,"furnace bypass");
  Fluid melt=new Fluid("pgm_fixture_molten");FluidRegistry.registerFluid(melt);MT.Pt.liquid(new FluidStack(melt,144));
  Recipe liquid=new Recipe(false,false,false,new ItemStack[]{stack(OP.oreBasalt,MT.Pt,1)},new ItemStack[0],null,new long[0],new FluidStack[0],new FluidStack[]{new FluidStack(melt,288)},120,480,0);
  require(PlatinumGroupSludgeRecipes.normalize(liquid),"raw molten output blocked");
  require(liquid.mFluidOutputs.length==0 && liquid.mOutputs.length==1 && liquid.mOutputs[0].stackSize==2,"molten amount converted to solid sludge");
  PlatinumGroupSludgeRecipes.enforceOutputCapacity(liquid,0);
  require(!liquid.mEnabled,"fluid-only machine cannot consume inputs without a sludge output slot");
  for(OreDictMaterial metal:metals){
   Recipe r=recipe(stack(OP.oreBasalt,metal,1),stack(OP.dustTiny,metal,3));
   require(PlatinumGroupSludgeRecipes.normalize(r),"ore output redirected "+metal);
   require(OM.anyassociation(r.mOutputs[0]).mMaterial.mMaterial==MT.PlatinumGroupSludge,"sludge output");
   require(r.mOutputs[0].stackSize==3 && r.mChances[0]==2300 && r.mEUt==480 && r.mDuration==120,"amount/chance/cost retained");
   require(!PlatinumGroupSludgeRecipes.normalize(r),"idempotence");
   require(PlatinumGroupSludgeRecipes.washingMaterial(metal)==MT.PlatinumGroupSludge,"cauldron washing");
   OreDictItemData raw=new OreDictItemData(OP.oreBasalt,metal);
   require(PlatinumGroupSludgeRecipes.crucibleData(raw).mMaterial.mMaterial==MT.PlatinumGroupSludge,"physical crucible ore");
   require(raw.mMaterial.mMaterial==metal,"crucible leaves shared material data untouched");
   OreDictItemData clean=new OreDictItemData(OP.ingot,metal);
   require(PlatinumGroupSludgeRecipes.crucibleData(clean)==clean,"clean ingot melting");
   r=recipe(stack(OP.plate,metal,1),stack(OP.dust,metal,1));
   require(PlatinumGroupSludgeRecipes.normalize(r),"parts recycling requires purification");
   r=recipe(stack(OP.dust,metal,1),stack(OP.ingot,metal,1));
   require(!PlatinumGroupSludgeRecipes.normalize(r),"clean metal forming preserved");
   r=recipe(stack(OP.dust,MT.Pt,1),stack(OP.dust,metal==MT.Pt?MT.Ir:metal,1));
   require(PlatinumGroupSludgeRecipes.normalize(r),"unrelated PGM input cannot exempt output");
  }
  int pure=0;
  for(Recipe.RecipeMap map:new Recipe.RecipeMap[]{RM.HeatMixer,RM.Centrifuge})for(Recipe r:map.mRecipeList){
   require(r.mEUt==480,"all new reactions HV");
   for(ItemStack output:r.mOutputs){OreDictItemData a=OM.anyassociation(output);if(a!=null&&a.hasValidPrefixMaterialData()&&PlatinumGroupSludgeRecipes.isGroup(a.mMaterial.mMaterial))pure++;}
  }
  require(pure==4,"four purification outputs retained");
  System.out.println("PGM PASS: seven materials, ore/recycling/catalyst bypass, quantities/chances, idempotence, clean forming, eight HV stages, three sludge inputs/four purified metals");
 }
}
