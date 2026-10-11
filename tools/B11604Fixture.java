import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Target;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143;

/** Native onUpdate -> actual resolved-impact bytecode -> target metadata/timer. */
public final class B11604Fixture {
    static int checks;
    static final class Player extends EntityPlayer{
        Player(net.minecraft.world.World w){super(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"TargetFixture"));setPosition(.5,80,-2);}
        public void addChatMessage(IChatComponent message){}
        public boolean canCommandSenderUseCommand(int level,String command){return false;}
        public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,80,-2);}
    }
    public static void main(String[] args)throws Exception{
        if(args.length==0){
            String[] paths=System.getProperty("java.class.path").split(java.io.File.pathSeparator);java.net.URL[] urls=new java.net.URL[paths.length];
            for(int i=0;i<paths.length;i++)urls[i]=new java.io.File(paths[i]).toURI().toURL();
            try(java.net.URLClassLoader loader=new java.net.URLClassLoader(urls,B11604Fixture.class.getClassLoader()){
                protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{
                    if(!(name.startsWith("net.minecraft.")||name.startsWith("net.minecraftforge.")||name.startsWith("cpw.mods.fml.")||name.startsWith("com.mod.htgt6.")||name.startsWith("B11604Fixture")))return super.loadClass(name,resolve);
                    Class<?> c=findLoadedClass(name);if(c==null)c=findClass(name);if(resolve)resolveClass(c);return c;
                }
                protected Class<?> findClass(String name)throws ClassNotFoundException{
                    java.net.URL url=findResource(name.replace('.','/')+".class");if(url==null)throw new ClassNotFoundException(name);
                    try(java.io.InputStream in=url.openStream()){
                        java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
                        while((n=in.read(buffer))!=-1)out.write(buffer,0,n);byte[] bytes=out.toByteArray();
                        if(name.equals("net.minecraft.entity.projectile.EntityArrow")||name.equals("net.minecraft.entity.projectile.EntityThrowable")||name.equals("net.minecraft.entity.projectile.EntityFireball")||name.equals("net.minecraft.entity.projectile.EntityFishHook")){
                            byte[] once=com.mod.htgt6.asm.TargetImpactASM116.transform(bytes);bytes=com.mod.htgt6.asm.TargetImpactASM116.transform(once);
                            if(!java.util.Arrays.equals(once,bytes))throw new AssertionError("MCP target idempotence: "+name);
                        }
                        return defineClass(name,bytes,0,bytes.length);
                    }catch(java.io.IOException e){throw new ClassNotFoundException(name,e);}
                }
            }){loader.loadClass("B11604Fixture").getMethod("main",String[].class).invoke(null,(Object)new String[]{"child"});}return;
        }
        BountifulMechanicsFixture120.Grid world=initialize();
        BlockBackport120Target target=(BlockBackport120Target)EtFVanillaGapContent120.target;
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(target,"backport_1_20_target");
        EtFVanillaGapContent120.registerTargetRecipe116();
        Player player=new Player(world);player.inventory.mainInventory[0]=new ItemStack(Items.fishing_rod);
        Entity[] projectiles={new EntityArrow(world),new EntityCombatArrow(world),new EntityThrownTrident(world),
                new EntitySnowball(world),new EntityEgg(world),new EntityExpBottle(world),
                new EntityPotion(world,0,80,0,0),new EntitySmallFireball(world),new EntityFishHook(world,player),new EntityCrossbowRocket143(world)};
        for(int index=0;index<projectiles.length;index++){
            world.setBlock(0,80,0,target,0,2);world.scheduled=0;
            world.rayHit=new MovingObjectPosition(0,80,0,2,Vec3.createVectorHelper(.5,80.5,0));
            Entity projectile=projectiles[index];projectile.setPosition(.5,80.5,-1);projectile.motionZ=1.1D;
            projectile.onUpdate();
            check(world.getBlockMetadata(0,80,0)==15,"resolved impact "+projectile.getClass().getSimpleName());
            check(world.scheduled==1&&world.lastDelay==(index<3?20:8),"one pulse and native projectile duration "+index);
            target.hit(world,0,80,0,1,8);check(world.scheduled==1&&world.getBlockMetadata(0,80,0)==15,"second hit neither replaces nor extends pulse");
            target.updateTick(world,0,80,0,world.rand);check(world.getBlockMetadata(0,80,0)==0,"scheduled reset "+index);
            if(projectile instanceof EntityArrow||projectile instanceof EntityThrownTrident||projectile instanceof EntityFishHook){
                for(int t=0;t<24;t++){world.time++;projectile.onUpdate();}
                check(world.getBlockMetadata(0,80,0)==0&&world.scheduled==1,"embedded projectile cannot retrigger "+index);
                NBTTagCompound saved=new NBTTagCompound();projectile.writeToNBT(saved);
                Entity restored=(Entity)projectile.getClass().getConstructor(net.minecraft.world.World.class).newInstance(world);
                restored.readFromNBT(saved);
                if(restored instanceof EntityFishHook)((EntityFishHook)restored).field_146042_b=player;
                restored.onUpdate();check(world.scheduled==1,"NBT reload retains embedding "+index);
            }
        }
        world.setBlock(0,80,0,target,0,2);
        world.rayHit=new MovingObjectPosition(0,80,0,2,Vec3.createVectorHelper(0,80,0));
        EntitySnowball edge=new EntitySnowball(world);edge.setPosition(.5,80.5,-1);edge.motionZ=1.1;edge.onUpdate();
        check(world.getBlockMetadata(0,80,0)==1,"edge uses hit vector, not entity position");
        for(int face=0;face<6;face++){check(target.isProvidingWeakPower(world,0,80,0,face)==1,"weak output face "+face);check(target.isProvidingStrongPower(world,0,80,0,face)==0,"no strong output");}
        check(target.canConnectRedstone(world,0,80,0,0)&&!target.canConnectRedstone(world,0,80,0,-1),"redstone connectivity");
        target.onBlockAdded(world,0,80,0);check(world.getBlockMetadata(0,80,0)==0,"copied powered placement resets");
        check(target.getItemDropped(0,world.rand,3)==net.minecraft.item.Item.getItemFromBlock(target)&&target.quantityDropped(world.rand)==1,"stable own drop");
        net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
        for(int i:new int[]{1,3,5,7})grid.setInventorySlotContents(i,new ItemStack(Items.redstone));grid.setInventorySlotContents(4,new ItemStack(Blocks.hay_block));
        ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world);
        check(crafted!=null&&crafted.stackSize==1&&crafted.getItem()==net.minecraft.item.Item.getItemFromBlock(target),"survival recipe registered");
        field(net.minecraft.world.World.class,world,"isRemote",true);world.scheduled=0;target.hit(world,0,80,0,15,20);check(world.scheduled==0,"client never schedules authoritative pulse");
        System.out.println("PASS B116-04: "+checks+" real onUpdate/impact/persistence checks");
    }
    static com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid initialize()throws Exception{
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid world=com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
        world.provider.worldObj=world;
        field(net.minecraft.world.World.class,world,"worldInfo",new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
        field(net.minecraft.world.World.class,world,"theProfiler",new net.minecraft.profiler.Profiler());
        field(net.minecraft.world.World.class,world,"worldAccesses",new java.util.ArrayList());
        field(net.minecraft.world.World.class,world,"collidingBoundingBoxes",new java.util.ArrayList());
        field(net.minecraft.world.World.class,world,"playerEntities",new java.util.ArrayList());
        field(cpw.mods.fml.relauncher.FMLRelaunchLog.class,null,"side",cpw.mods.fml.relauncher.Side.SERVER);
        java.lang.reflect.Field f=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");f.setAccessible(true);
        cpw.mods.fml.common.ModMetadata meta=new cpw.mods.fml.common.ModMetadata();meta.modId="htgt6";meta.name="B116 fixture";
        field(cpw.mods.fml.common.LoadController.class,f.get(cpw.mods.fml.common.Loader.instance()),"activeContainer",new cpw.mods.fml.common.DummyModContainer(meta));
        return world;
    }
    static void field(Class<?> type,Object instance,String name,Object value)throws Exception{
        java.lang.reflect.Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(instance,value);
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
