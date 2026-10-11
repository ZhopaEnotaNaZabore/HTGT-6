import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneBinding116;
import com.mod.htgt6.minecraft1202.Module.Events.Modern.LodestoneEvents116;

public final class B11605Fixture{
    static int checks;
    static final class OffhandInventory extends net.minecraft.entity.player.InventoryPlayer implements com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.InventoryPlayer112{
        ItemStack[] offhand=new ItemStack[1];java.util.List<ItemStack[]> all;
        OffhandInventory(EntityPlayer p){super(p);all=java.util.Arrays.asList(mainInventory,armorInventory,offhand);}
        public ItemStack[] htgt6$getOffHandInventory(){return offhand;}
        public java.util.List<ItemStack[]> htgt6$getAllInventories(){return all;}
        public void htgt6$setOffHandInventory(ItemStack[] value){offhand=value;}
        public void htgt6$setAllInventories(java.util.List<ItemStack[]> value){all=value;}
    }
    static final class Player extends EntityPlayer{
        Player(World w){super(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"LodestoneFixture"));setPosition(.5,80,-1);}
        public void addChatMessage(IChatComponent message){}
        public boolean canCommandSenderUseCommand(int level,String command){return false;}
        public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,80,-1);}
    }
    public static void main(String[] args)throws Exception{
        BountifulMechanicsFixture120.Grid world=initialize();
        cpw.mods.fml.common.registry.GameRegistry.registerItem(EtFVanillaGapContent120.netheriteIngot,"backport_1_20_netherite_ingot");
        LodestoneContent116.register();LodestoneContent116.register();
        check("htgt6:backport_1_20_lodestone_compass".equals(Item.itemRegistry.getNameForObject(LodestoneContent116.compass).toString()),"own stable registry identity");
        world.setBlock(0,80,0,LodestoneContent116.lodestone,0,2);world.setBlock(2,80,0,LodestoneContent116.lodestone,0,2);
        Player eventPlayer=new Player(world);eventPlayer.inventory.mainInventory[0]=new ItemStack(Items.compass);
        net.minecraftforge.event.entity.player.PlayerInteractEvent interaction=new net.minecraftforge.event.entity.player.PlayerInteractEvent(
                eventPlayer,net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,0,80,0,2,world);
        check(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(interaction),"registered server listener handles native interaction");
        check(LodestoneBinding116.pointsTo(eventPlayer.inventory.mainInventory[0],0,0,80,0),"event delivery binds the compass");
        Player deniedEventPlayer=new Player(world);deniedEventPlayer.inventory.mainInventory[0]=new ItemStack(Items.compass);
        net.minecraftforge.event.entity.player.PlayerInteractEvent deniedInteraction=new net.minecraftforge.event.entity.player.PlayerInteractEvent(
                deniedEventPlayer,net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,0,80,0,2,world);
        deniedInteraction.useItem=cpw.mods.fml.common.eventhandler.Event.Result.DENY;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(deniedInteraction);
        check(deniedEventPlayer.inventory.mainInventory[0].getItem()==Items.compass,"denied interaction does not bind");
        for(boolean creative:new boolean[]{false,true})for(boolean full:new boolean[]{false,true})for(int size:new int[]{1,2,64}){
            world.time+=10;world.lastSpawn=null;Player player=new Player(world);player.capabilities.isCreativeMode=creative;
            if(full)for(int i=0;i<36;i++)player.inventory.mainInventory[i]=new ItemStack(Blocks.cobblestone,64);
            ItemStack source=new ItemStack(Items.compass,size);source.setStackDisplayName("User name");source.getTagCompound().setString("UserData","keep");player.inventory.mainInventory[0]=source;
            check(LodestoneBinding116.bind(player,world,0,80,0,2),"native binding accepted");
            check(count(player,Items.compass)+(world.lastSpawn instanceof EntityItem&&((EntityItem)world.lastSpawn).getEntityItem().getItem()==Items.compass?((EntityItem)world.lastSpawn).getEntityItem().stackSize:0)==(creative?size:size-1),"source count "+creative+"/"+full+"/"+size);
            ItemStack result=find(player,LodestoneContent116.compass);
            if(result==null&&world.lastSpawn instanceof EntityItem)result=((EntityItem)world.lastSpawn).getEntityItem();
            check(result!=null&&result.stackSize==1&&LodestoneBinding116.pointsTo(result,0,0,80,0),"one bound result");
            check(result.hasDisplayName()&&"keep".equals(result.getTagCompound().getString("UserData")),"source item NBT copied");
            ItemStack saved=ItemStack.loadItemStackFromNBT(result.writeToNBT(new NBTTagCompound()));
            check(ItemStack.areItemStacksEqual(result,saved),"survives item save/reconnect format");
            int countBefore=count(player,LodestoneContent116.compass);net.minecraft.entity.Entity drop=world.lastSpawn;
            LodestoneBinding116.bind(player,world,0,80,0,2);
            check(count(player,LodestoneContent116.compass)==countBefore&&world.lastSpawn==drop,"duplicate native interaction coalesced");
        }
        Player off=new Player(world);OffhandInventory offInventory=new OffhandInventory(off);off.inventory=offInventory;
        offInventory.offhand[0]=new ItemStack(Items.compass);
        check(LodestoneBinding116.bind(off,world,0,80,0,2)&&offInventory.offhand[0].getItem()==LodestoneContent116.compass,"offhand native interaction uses slot 40 without changing selected slot");
        check(off.inventory.currentItem==0&&off.inventory.mainInventory[0]==null,"offhand binding never swaps the main hand");
        Player blockedOff=new Player(world);OffhandInventory blockedInventory=new OffhandInventory(blockedOff);blockedOff.inventory=blockedInventory;
        blockedInventory.mainInventory[0]=new ItemStack(Blocks.stone);blockedInventory.offhand[0]=new ItemStack(Items.compass);
        check(!LodestoneBinding116.bind(blockedOff,world,0,80,0,2)&&blockedInventory.offhand[0].getItem()==Items.compass,"consuming main-hand placement takes priority");
        Player denied=new Player(world);for(int i=0;i<36;i++)denied.inventory.mainInventory[i]=new ItemStack(Blocks.cobblestone,64);
        denied.inventory.mainInventory[0]=new ItemStack(Items.compass,2);world.rejectSpawns=true;
        LodestoneBinding116.bind(denied,world,0,80,0,2);world.rejectSpawns=false;
        check(denied.inventory.mainInventory[0].stackSize==2,"denied drop does not consume source");
        Player far=new Player(world);far.setPosition(100,80,0);far.inventory.mainInventory[0]=new ItemStack(Items.compass);
        LodestoneBinding116.bind(far,world,0,80,0,2);check(far.inventory.mainInventory[0].getItem()==Items.compass,"server rejects out-of-reach request");
        ItemStack a=LodestoneBinding116.boundCopy(new ItemStack(Items.compass),0,0,80,0),b=LodestoneBinding116.boundCopy(new ItemStack(Items.compass),0,2,80,0);
        a.getTagCompound().getCompoundTag(LodestoneBinding116.POS).setInteger("X",-12);
        check(LodestoneBinding116.pointsTo(b,0,2,80,0),"independent targets and nested NBT");
        world.loaded=false;check(!LodestoneBinding116.validate(a,world)&&LodestoneBinding116.hasTarget(a),"unloaded chunk is unknown, not destroyed");
        // Grid has no chunk provider: accidental remote chunk access fails this fixture.
        world.loaded=true;world.provider.dimensionId=-1;check(!LodestoneBinding116.validate(a,world)&&LodestoneBinding116.hasTarget(a),"wrong dimension preserves target");
        world.provider.dimensionId=0;check(LodestoneBinding116.validate(a,world)&&!LodestoneBinding116.hasTarget(a),"loaded destroyed target clears position");
        check(a.getTagCompound().hasKey(LodestoneBinding116.DIMENSION,3),"broken remains bound/spinning, never a spawn compass");
        ItemStack untracked=LodestoneBinding116.boundCopy(new ItemStack(Items.compass),0,9,80,0);untracked.getTagCompound().setBoolean(LodestoneBinding116.TRACKED,false);
        check(!LodestoneBinding116.validate(untracked,world)&&LodestoneBinding116.hasTarget(untracked),"untracked contract");
        ItemStack bad=LodestoneBinding116.boundCopy(new ItemStack(Items.compass),0,0,80,0);bad.getTagCompound().setString(LodestoneBinding116.DIMENSION,"overworld");
        check(!LodestoneBinding116.hasTarget(bad),"malformed NBT does not read wrong dimension type");
        world.setBlock(2,80,0,Blocks.air,0,2);EntityItem dropped=new EntityItem(world,0,80,0,b.copy());
        check(!LodestoneContent116.compass.onEntityItemUpdate(dropped)&&!LodestoneBinding116.hasTarget(dropped.getEntityItem()),"EntityItem validation keeps native lifecycle");
        EntityItemFrame frame=new EntityItemFrame(world);frame.setDisplayedItem(b.copy());world.loadedEntityList.add(frame);world.time=200;
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().post(new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(cpw.mods.fml.relauncher.Side.SERVER,cpw.mods.fml.common.gameevent.TickEvent.Phase.END,world));
        check(!LodestoneBinding116.hasTarget(frame.getDisplayedItem()),"frame watcher validates and synchronizes changed NBT");
        net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
        for(int i=0;i<9;i++)grid.setInventorySlotContents(i,i==4?new ItemStack(EtFVanillaGapContent120.netheriteIngot):new ItemStack(Blocks.stonebrick,1,3));
        ItemStack craft=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world);
        check(craft!=null&&craft.stackSize==1&&craft.getItem()==Item.getItemFromBlock(LodestoneContent116.lodestone),"survival lodestone recipe");
        grid.setInventorySlotContents(0,new ItemStack(Blocks.stonebrick,1,0));check(net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world)==null,"ordinary stone bricks rejected");
        Player client=new Player(world);client.inventory.mainInventory[0]=new ItemStack(Items.compass,4);field(World.class,world,"isRemote",true);
        net.minecraftforge.event.entity.player.PlayerInteractEvent clientInteraction=new net.minecraftforge.event.entity.player.PlayerInteractEvent(
                client,net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,0,80,0,2,world);
        check(!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(clientInteraction),"client listener preserves outgoing interaction packet");
        LodestoneBinding116.bind(client,world,0,80,0,2);check(client.inventory.mainInventory[0].getItem()==Items.compass&&client.inventory.mainInventory[0].stackSize==4,"client prediction never consumes");
        System.out.println("PASS B116-05: "+checks+" registry/recipe/binding/NBT/loaded-world checks");
    }
    static ItemStack find(Player p,Item item){for(ItemStack stack:p.inventory.mainInventory)if(stack!=null&&stack.getItem()==item)return stack;return null;}
    static int count(Player p,Item item){int count=0;for(ItemStack stack:p.inventory.mainInventory)if(stack!=null&&stack.getItem()==item)count+=stack.stackSize;return count;}
    static com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid initialize()throws Exception{
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid world=com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
        world.provider.worldObj=world;
        field(net.minecraft.world.World.class,world,"worldScoreboard",new net.minecraft.scoreboard.Scoreboard());
        field(net.minecraft.world.World.class,world,"worldInfo",new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
        field(net.minecraft.world.World.class,world,"theProfiler",new net.minecraft.profiler.Profiler());
        field(net.minecraft.world.World.class,world,"worldAccesses",new java.util.ArrayList());
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
