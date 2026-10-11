import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import cpw.mods.fml.common.registry.GameRegistry;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.NetheriteEquipment120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;

/** Real EntityItem methods in a child loader with the production transformer installed. */
public final class B11602Fixture {
    static int checks;
    public static void main(String[] args)throws Exception {
        if(args.length==0){
            String[] paths=System.getProperty("java.class.path").split(File.pathSeparator);URL[] urls=new URL[paths.length];for(int i=0;i<paths.length;i++)urls[i]=new File(paths[i]).toURI().toURL();
            try(URLClassLoader loader=new URLClassLoader(urls,B11602Fixture.class.getClassLoader()){
                protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{
                    if(!(name.startsWith("net.minecraft.")||name.startsWith("net.minecraftforge.")||name.startsWith("cpw.mods.fml.")||name.startsWith("com.mod.htgt6.")||name.startsWith("B11602Fixture")))return super.loadClass(name,resolve);
                    Class<?> c=findLoadedClass(name);if(c==null)c=findClass(name);if(resolve)resolveClass(c);return c;
                }
                protected Class<?> findClass(String name)throws ClassNotFoundException{
                    URL resource=findResource(name.replace('.','/')+".class");if(resource==null)throw new ClassNotFoundException(name);
                    try(InputStream in=resource.openStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);byte[] bytes=out.toByteArray();
                        if(name.equals("net.minecraft.entity.item.EntityItem")){
                            byte[] once=com.mod.htgt6.asm.NetheriteItemASM120.transform(bytes);bytes=com.mod.htgt6.asm.NetheriteItemASM120.transform(once);
                            if(!Arrays.equals(once,bytes))throw new AssertionError("MCP idempotence");
                        }
                        return defineClass(name,bytes,0,bytes.length);
                    }catch(IOException e){throw new ClassNotFoundException(name,e);}
                }
            }){loader.loadClass("B11602Fixture").getMethod("main",String[].class).invoke(null,(Object)new String[]{"child"});}return;
        }
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        Field info=net.minecraft.world.World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));world.provider.worldObj=world;
        for(String name:new String[]{"worldAccesses","playerEntities","collidingBoundingBoxes"}){Field list=net.minecraft.world.World.class.getDeclaredField(name);list.setAccessible(true);list.set(world,new ArrayList());}
        Field profiler=net.minecraft.world.World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(world,new net.minecraft.profiler.Profiler());
        Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.SERVER);
        Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        Field owner=cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");owner.setAccessible(true);
        cpw.mods.fml.common.ModMetadata metadata=new cpw.mods.fml.common.ModMetadata();metadata.modId="htgt6";metadata.name="B116 fixture";
        owner.set(controller.get(cpw.mods.fml.common.Loader.instance()),new cpw.mods.fml.common.DummyModContainer(metadata));
        GameRegistry.registerBlock(EtFVanillaGapContent120.ancientDebris,"backport_1_20_ancient_debris");
        GameRegistry.registerBlock(EtFVanillaGapContent120.netheriteBlock,"backport_1_20_netherite_block");
        GameRegistry.registerItem(EtFVanillaGapContent120.netheriteScrap,"backport_1_20_netherite_scrap");
        GameRegistry.registerItem(EtFVanillaGapContent120.netheriteIngot,"backport_1_20_netherite_ingot");SmithingContent120.register();
        List<Item> items=new ArrayList<Item>(SmithingContent120.upgrades.values());
        items.add(EtFVanillaGapContent120.netheriteScrap);items.add(EtFVanillaGapContent120.netheriteIngot);
        items.add(Item.getItemFromBlock(EtFVanillaGapContent120.ancientDebris));items.add(Item.getItemFromBlock(EtFVanillaGapContent120.netheriteBlock));items.add(Items.diamond);items.add(Items.stick);
        DamageSource[] sources={DamageSource.inFire,DamageSource.onFire,DamageSource.lava,DamageSource.generic,DamageSource.outOfWorld,DamageSource.cactus,new DamageSource("fixtureExplosion").setExplosion()};
        for(int index=0;index<items.size();index++)for(DamageSource source:sources){
            EntityItem entity=new EntityItem(world,0,80,0,new ItemStack(items.get(index)));
            boolean fireproof=index<13;boolean protectedHit=fireproof&&source.isFireDamage();
            boolean accepted=entity.attackEntityFrom(source,100);
            check(protectedHit?!accepted&&!entity.isDead:entity.isDead,"damage matrix "+index+"/"+source.getDamageType());
        }
        for(int index=0;index<13;index++){
            ItemStack stack=new ItemStack(items.get(index));stack.setStackDisplayName("B116 NBT");
            EntityItem first=new EntityItem(world,0,80,0,stack);first.age=123;first.delayBeforeCanPickup=37;first.lifespan=9000;
            NBTTagCompound nbt=new NBTTagCompound();first.writeEntityToNBT(nbt);EntityItem loaded=new EntityItem(world);loaded.readEntityFromNBT(nbt);
            check(loaded.age==123&&loaded.lifespan==9000&&loaded.getEntityItem().hasDisplayName(),"EntityItem NBT "+index);
            check(!loaded.attackEntityFrom(DamageSource.lava,100)&&!loaded.isDead,"reload immunity "+index);
            first.setFire(10);NetheriteEquipment120.afterItemTick(first);check(!first.isBurning(),"burning visual cleared "+index);
            check(first.age==123&&first.lifespan==9000,"extinguish must not reset despawn "+index);
        }
        for(int index=0;index<items.size();index++){
            EntityItem expiring=new EntityItem(world,0,80,0,new ItemStack(items.get(index)));expiring.age=5999;expiring.lifespan=6000;
            expiring.onUpdate();check(expiring.isDead,"native despawn remains active "+index);
        }
        EntityItem a=new EntityItem(world,0,80,0,new ItemStack(EtFVanillaGapContent120.netheriteIngot,11));
        EntityItem b=new EntityItem(world,0,80,0,new ItemStack(EtFVanillaGapContent120.netheriteIngot,13));a.age=10;b.age=80;
        check(a.combineItems(b),"merge accepted");EntityItem survivor=a.isDead?b:a;
        check(survivor.getEntityItem().stackSize==24&&survivor.age==10,"merge quantity and age");
        check(!survivor.attackEntityFrom(DamageSource.inFire,100),"merged stack remains fireproof");
        EntityItem named=new EntityItem(world,0,80,0,new ItemStack(EtFVanillaGapContent120.netheriteIngot));named.getEntityItem().setStackDisplayName("different");
        check(!survivor.combineItems(named),"NBT prevents merge");
        check(!NetheriteEquipment120.isNetherite(new ItemStack(EtFVanillaGapContent120.netheriteIngot)),"material not equipment");
        check(!NetheriteEquipment120.isNetheriteArmor(new ItemStack(SmithingContent120.upgrades.get(Items.diamond_sword))),"weapon not armor");
        net.minecraft.entity.passive.EntityPig wearer=new net.minecraft.entity.passive.EntityPig(world);
        NetheriteEquipment120 handler=new NetheriteEquipment120();
        for(int slot=1;slot<=4;slot++)wearer.setCurrentItemOrArmor(slot,new ItemStack(EtFVanillaGapContent120.netheriteIngot));
        handler.update(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(wearer));
        check(wearer.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.knockbackResistance).getAttributeValue()==0,"materials confer no armor bonus");
        Item[] armor={Items.diamond_boots,Items.diamond_leggings,Items.diamond_chestplate,Items.diamond_helmet};
        for(int slot=1;slot<=4;slot++)wearer.setCurrentItemOrArmor(slot,new ItemStack(SmithingContent120.upgrades.get(armor[slot-1])));
        handler.update(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(wearer));
        check(Math.abs(wearer.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.knockbackResistance).getAttributeValue()-.4)<1E-8,"four correct armor slots");
        System.out.println("PASS B116-02: "+checks+" checks; actual EntityItem damage, merge, serialization, armor and MCP idempotence");
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
