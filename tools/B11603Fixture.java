import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;

/** Native container click entry points, with real ItemStacks, inventories and NBT. */
public final class B11603Fixture {
    public static final class TossCancellation{
        @cpw.mods.fml.common.eventhandler.SubscribeEvent public void toss(net.minecraftforge.event.entity.item.ItemTossEvent event){event.setCanceled(true);}
    }
    static int checks;
    static BountifulMechanicsFixture120.Grid world;
    static final class Player extends EntityPlayer {
        final List<ItemStack> drops=new ArrayList<ItemStack>();
        Player(World w){super(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"SmithingFixture"));setPosition(.5,80,.5);}
        public void addChatMessage(net.minecraft.util.IChatComponent c){}
        public boolean canCommandSenderUseCommand(int level,String command){return false;}
        public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,80,0);}
        @Override public EntityItem dropPlayerItemWithRandomChoice(ItemStack stack,boolean random){
            if(stack==null)return null;drops.add(stack.copy());return new EntityItem(worldObj,posX,posY,posZ,stack);
        }
    }
    public static void main(String[] args)throws Exception {
        world=BountifulMechanicsFixture120.initialize();world.provider.worldObj=world;
        Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        for(String name:new String[]{"worldAccesses","playerEntities"}){Field list=net.minecraft.world.World.class.getDeclaredField(name);list.setAccessible(true);list.set(world,new ArrayList());}
        Field profiler=net.minecraft.world.World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(world,new net.minecraft.profiler.Profiler());
        Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.SERVER);
        Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        Field owner=cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");owner.setAccessible(true);
        cpw.mods.fml.common.ModMetadata metadata=new cpw.mods.fml.common.ModMetadata();metadata.modId="htgt6";metadata.name="B116 fixture";
        owner.set(controller.get(cpw.mods.fml.common.Loader.instance()),new cpw.mods.fml.common.DummyModContainer(metadata));
        GameRegistry.registerItem(EtFVanillaGapContent120.netheriteIngot,"backport_1_20_netherite_ingot");SmithingContent120.register();
        world.setBlock(0,80,0,VillagePillageContent.smithingTable,0,2);
        for(Map.Entry<Item,Item> pair:SmithingContent120.upgrades.entrySet())for(boolean creative:new boolean[]{false,true})for(int mode:new int[]{0,1,2,4}) {
            Player player=new Player(world);player.capabilities.isCreativeMode=creative;
            ContainerVillageWorkstation120 menu=menu(player);ItemStack base=base(pair.getKey());fill(menu,base);
            check(base.getItem()==pair.getKey()&&base.getItemDamage()==113,"preview leaves input unchanged");
            check(menu.getSlot(3).getHasStack(),"preview produced");menu.slotClick(3,0,mode,player);
            ItemStack result=mode==0?player.inventory.getItemStack():mode==4?((EntityItem)world.lastSpawn).getEntityItem():find(player,pair.getValue());
            check(result!=null&&result.stackSize==1&&result.getItem()==pair.getValue(),"one result mode "+mode);
            check(result.getItemDamage()==113&&result.hasDisplayName()&&result.isItemEnchanted(),"item state survives");
            check(result.getTagCompound().getCompoundTag("UserData").getInteger("answer")==42,"custom NBT preserved");
            check(!result.getTagCompound().hasKey("TemplateOnly")&&!result.getTagCompound().hasKey("AdditionOnly"),"input service NBT does not leak");
            check(ItemStack.areItemStacksEqual(result,ItemStack.loadItemStackFromNBT(result.writeToNBT(new NBTTagCompound()))),"result save round trip");
            for(int i=0;i<3;i++)check(menu.getInput(i)==null,"all three inputs consumed once");
            if(mode==4)player.drops.add(result.copy());
            menu.slotClick(3,0,mode,player);check(menu.getSlot(3).getStack()==null,"repeated packet cannot create result");
            check(count(player,pair.getValue())==1,"no repeated-click duplication");
        }
        Player full=new Player(world);ContainerVillageWorkstation120 blocked=menu(full);fill(blocked,base(Items.diamond_sword));
        for(int i=0;i<36;i++)full.inventory.mainInventory[i]=new ItemStack(Blocks.cobblestone,64);
        check(blocked.transferStackInSlot(full,3)==null,"full inventory rejects shift-click");check(blocked.getInput(0).stackSize==1&&blocked.getInput(2).stackSize==1,"failure retains ingredients");
        full.inventory.setItemStack(new ItemStack(Items.stick));blocked.slotClick(3,0,0,full);check(blocked.getInput(1)!=null,"occupied cursor retains base");
        full.inventory.setItemStack(null);blocked.slotClick(3,0,2,full);check(blocked.getInput(1)!=null,"hotbar swap with no displacement room is rejected");
        full.inventory.mainInventory[20]=null;blocked.slotClick(3,0,2,full);
        check(full.inventory.mainInventory[20]!=null&&full.inventory.mainInventory[20].stackSize==64,"hotbar displaced stack preserved");check(blocked.getInput(1)==null,"hotbar consumes once");
        Player denied=new Player(world);ContainerVillageWorkstation120 deniedMenu=menu(denied);fill(deniedMenu,base(Items.diamond_sword));
        world.rejectSpawns=true;deniedMenu.slotClick(3,0,4,denied);world.rejectSpawns=false;
        check(deniedMenu.getInput(1)!=null&&deniedMenu.getSlot(3).getHasStack(),"spawn denial preserves all inputs and preview");
        Object cancellation=new TossCancellation();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(cancellation);
        try{deniedMenu.slotClick(3,0,4,denied);check(deniedMenu.getInput(0)!=null&&deniedMenu.getInput(2)!=null,"cancelled toss retains ingredients");}
        finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(cancellation);}
        Player creativeFull=new Player(world);creativeFull.capabilities.isCreativeMode=true;
        for(int i=0;i<36;i++)creativeFull.inventory.mainInventory[i]=new ItemStack(Blocks.cobblestone,64);
        ContainerVillageWorkstation120 closeFull=menu(creativeFull);fill(closeFull,base(Items.diamond_sword));closeFull.onContainerClosed(creativeFull);
        check(creativeFull.drops.size()==3,"creative full close never silently discards owned inputs");
        Player stalePlayer=new Player(world);ContainerVillageWorkstation120 stale=menu(stalePlayer);fill(stale,base(Items.diamond_axe));
        stale.getInput(1).stackSize=0;stale.slotClick(3,0,0,stalePlayer);check(stalePlayer.inventory.getItemStack()==null&&stale.getInput(2)!=null,"zero-count stale preview rejected");
        for(Item pattern:SmithingContent120.patterns.keySet()) {
            Player player=new Player(world);ContainerVillageWorkstation120 menu=menu(player);
            menu.getSlot(0).putStack(new ItemStack(pattern));menu.getSlot(1).putStack(base(Items.diamond_chestplate));menu.getSlot(2).putStack(new ItemStack(Items.gold_ingot));
            menu.slotClick(3,1,0,player);ItemStack trimmed=player.inventory.getItemStack();
            check(trimmed!=null&&trimmed.getTagCompound().hasKey("Trim",10),"trim applied");
            player.inventory.setItemStack(null);menu.getSlot(0).putStack(new ItemStack(pattern));menu.getSlot(1).putStack(trimmed);menu.getSlot(2).putStack(new ItemStack(Items.gold_ingot));
            check(!menu.getSlot(3).getHasStack(),"same trim is not consumed again");
        }
        for(boolean death:new boolean[]{false,true}) {
            Player player=new Player(world);ContainerVillageWorkstation120 menu=menu(player);fill(menu,base(Items.diamond_pickaxe));player.isDead=death;
            menu.onContainerClosed(player);menu.onContainerClosed(player);
            check(count(player,SmithingContent120.upgradeTemplate)==1&&count(player,Items.diamond_pickaxe)==1&&count(player,EtFVanillaGapContent120.netheriteIngot)==1,"close/death returns inputs exactly once");
            check(!menu.getSlot(3).getHasStack(),"close clears non-owned preview");menu.slotClick(3,0,0,player);check(find(player,SmithingContent120.upgrades.get(Items.diamond_pickaxe))==null,"closed container cannot be reused");
        }
        System.out.println("PASS B116-03: "+checks+" native-container checks");
    }
    static ContainerVillageWorkstation120 menu(Player p){return new ContainerVillageWorkstation120(p.inventory,VillageWorkstationType120.SMITHING,world,0,80,0);}
    static ItemStack base(Item item){ItemStack b=new ItemStack(item,1,113);b.setStackDisplayName("Preserved name");b.addEnchantment(net.minecraft.enchantment.Enchantment.unbreaking,3);NBTTagCompound user=new NBTTagCompound();user.setInteger("answer",42);b.getTagCompound().setTag("UserData",user);return b;}
    static void fill(ContainerVillageWorkstation120 menu,ItemStack base){ItemStack template=new ItemStack(SmithingContent120.upgradeTemplate);template.setTagCompound(new NBTTagCompound());template.getTagCompound().setBoolean("TemplateOnly",true);ItemStack ingot=new ItemStack(EtFVanillaGapContent120.netheriteIngot);ingot.setTagCompound(new NBTTagCompound());ingot.getTagCompound().setBoolean("AdditionOnly",true);menu.getSlot(0).putStack(template);menu.getSlot(1).putStack(base);menu.getSlot(2).putStack(ingot);}
    static ItemStack find(Player p,Item item){for(ItemStack s:p.inventory.mainInventory)if(s!=null&&s.getItem()==item)return s;return null;}
    static int count(Player p,Item item){int n=0;for(ItemStack s:p.inventory.mainInventory)if(s!=null&&s.getItem()==item)n+=s.stackSize;for(ItemStack s:p.drops)if(s.getItem()==item)n+=s.stackSize;ItemStack c=p.inventory.getItemStack();return n+(c!=null&&c.getItem()==item?c.stackSize:0);}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
