import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.registry.GameRegistry;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120;

/** Executes the production registrations, recipes, item methods and NBT serializers. */
public final class B11601Fixture {
    private static int checks;
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        Field info=net.minecraft.world.World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));world.provider.worldObj=world;
        for(String name:new String[]{"worldAccesses","playerEntities"}){Field list=net.minecraft.world.World.class.getDeclaredField(name);list.setAccessible(true);list.set(world,new ArrayList());}
        Field profiler=net.minecraft.world.World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(world,new net.minecraft.profiler.Profiler());
        Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.SERVER);
        Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        Field owner=cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");owner.setAccessible(true);
        cpw.mods.fml.common.ModMetadata metadata=new cpw.mods.fml.common.ModMetadata();metadata.modId="htgt6";metadata.name="B116 fixture";
        owner.set(controller.get(cpw.mods.fml.common.Loader.instance()),new cpw.mods.fml.common.DummyModContainer(metadata));
        GameRegistry.registerBlock(EtFVanillaGapContent120.ancientDebris,"backport_1_20_ancient_debris");
        GameRegistry.registerBlock(EtFVanillaGapContent120.netheriteBlock,"backport_1_20_netherite_block");
        GameRegistry.registerItem(EtFVanillaGapContent120.netheriteScrap,"backport_1_20_netherite_scrap");
        GameRegistry.registerItem(EtFVanillaGapContent120.netheriteIngot,"backport_1_20_netherite_ingot");
        SmithingContent120.register();
        EtFVanillaGapContent120.registerNetheriteRecipes();
        ItemStack scrap=new ItemStack(EtFVanillaGapContent120.netheriteScrap), ingot=new ItemStack(EtFVanillaGapContent120.netheriteIngot);
        ItemStack smelt=FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(EtFVanillaGapContent120.ancientDebris));
        check(smelt!=null&&smelt.getItem()==scrap.getItem()&&smelt.stackSize==1,"1 debris -> 1 scrap");
        ItemStack[] mix={scrap,scrap,scrap,scrap,new ItemStack(Items.gold_ingot),new ItemStack(Items.gold_ingot),new ItemStack(Items.gold_ingot),new ItemStack(Items.gold_ingot),null};
        verifyRecipe(mix,ingot,world);
        ItemStack[] packed=new ItemStack[9];Arrays.fill(packed,ingot);
        verifyRecipe(packed,new ItemStack(EtFVanillaGapContent120.netheriteBlock),world);
        ItemStack[] unpacked=new ItemStack[9];unpacked[4]=new ItemStack(EtFVanillaGapContent120.netheriteBlock);
        verifyRecipe(unpacked,new ItemStack(EtFVanillaGapContent120.netheriteIngot,9),world);
        int[] durability={2031,2031,2031,2031,2031,407,592,555,481};
        double[] damage={7,5,9,5.5,0}, speed={-2.4,-2.8,-3, -3,0};
        int[] protection={3,8,6,3}; int i=0;
        for(Map.Entry<Item,Item> entry:SmithingContent120.upgrades.entrySet()) {
            Item item=entry.getValue();ItemStack stack=new ItemStack(item,1,113);
            check(item.getMaxDamage()==durability[i],"durability "+i);
            check(item.getIsRepairable(stack,ingot),"ingot repair "+i);
            check(!item.getIsRepairable(stack,new ItemStack(Items.diamond)),"reject diamond repair "+i);
            check(item.getItemEnchantability()==15,"enchantability "+i);
            check(stack.getMaxStackSize()==1,"equipment stack size "+i);
            check(Item.itemRegistry.getNameForObject(item).toString().equals("htgt6:"+item.getUnlocalizedName().substring(5)),"existing registry name "+i);
            stack.setStackDisplayName("B116 retained");stack.addEnchantment(net.minecraft.enchantment.Enchantment.unbreaking,3);
            stack.getTagCompound().setString("UserData","retained");
            ItemStack loaded=ItemStack.loadItemStackFromNBT(stack.writeToNBT(new NBTTagCompound()));
            check(ItemStack.areItemStacksEqual(stack,loaded),"save round trip "+i);
            if(i<5) {
                close(modifier(stack,"generic.attackDamage"),damage[i],"damage modifier "+i);
                close(modifier(stack,"generic.attackSpeed"),speed[i],"speed modifier "+i);
            } else check(((ItemArmor)item).damageReduceAmount==protection[i-5],"armor defense "+i);
            i++;
        }
        check(i==9,"all nine equipment items");
        net.minecraft.entity.passive.EntityPig wearer=new net.minecraft.entity.passive.EntityPig(world);
        Item[] armor={Items.diamond_boots,Items.diamond_leggings,Items.diamond_chestplate,Items.diamond_helmet};
        for(int slot=1;slot<=4;slot++)wearer.setCurrentItemOrArmor(slot,new ItemStack(SmithingContent120.upgrades.get(armor[slot-1])));
        close(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatDamage120.toughness(wearer),12,"CombatUpdate toughness 3 per piece");
        close(wearer.getTotalArmorValue(),20,"armor total");
        Item hoe=SmithingContent120.upgrades.get(Items.diamond_hoe);
        for(net.minecraft.enchantment.Enchantment enchantment:new net.minecraft.enchantment.Enchantment[]{net.minecraft.enchantment.Enchantment.efficiency,net.minecraft.enchantment.Enchantment.silkTouch,net.minecraft.enchantment.Enchantment.fortune,net.minecraft.enchantment.Enchantment.unbreaking})
            check(enchantment.canApplyAtEnchantingTable(new ItemStack(hoe)),"native table hoe enchantment "+enchantment.effectId);
        Map candidates=net.minecraft.enchantment.EnchantmentHelper.mapEnchantmentData(30,new ItemStack(hoe));
        for(net.minecraft.enchantment.Enchantment enchantment:new net.minecraft.enchantment.Enchantment[]{net.minecraft.enchantment.Enchantment.efficiency,net.minecraft.enchantment.Enchantment.silkTouch,net.minecraft.enchantment.Enchantment.fortune}) {
            check(candidates!=null&&candidates.containsKey(enchantment.effectId),"real table candidate "+enchantment.effectId);
            check(enchantment.canApply(new ItemStack(hoe)),"native anvil eligibility "+enchantment.effectId);
            check(!enchantment.canApply(new ItemStack(Items.stick)),"ordinary item unchanged "+enchantment.effectId);
            check(!enchantment.canApply(new ItemStack(Items.diamond_hoe)),"legacy hoe unchanged "+enchantment.effectId);
        }
        check(!net.minecraft.enchantment.Enchantment.sharpness.canApply(new ItemStack(hoe)),"hoe does not gain weapon enchantments");
        check(!net.minecraft.enchantment.Enchantment.protection.canApply(new ItemStack(hoe)),"hoe does not gain armor enchantments");
        check(!net.minecraft.enchantment.Enchantment.fortune.canApplyTogether(net.minecraft.enchantment.Enchantment.silkTouch),"fortune and silk remain incompatible");
        close(hoe.getDigSpeed(new ItemStack(hoe),Blocks.hay_block,0),9,"hoe effective speed");
        close(hoe.getDigSpeed(new ItemStack(hoe),Blocks.stone,0),1,"hoe ineffective speed");
        for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{EtFVanillaGapContent120.ancientDebris,EtFVanillaGapContent120.netheriteBlock}) {
            check("pickaxe".equals(block.getHarvestTool(0))&&block.getHarvestLevel(0)==3,"diamond-tier mining");
            close(block.getExplosionResistance(null),1200,"blast resistance");
        }
        close(EtFVanillaGapContent120.ancientDebris.getBlockHardness(null,0,0,0),30,"debris hardness");
        close(EtFVanillaGapContent120.netheriteBlock.getBlockHardness(null,0,0,0),50,"block hardness");
        for(boolean creative:new boolean[]{false,true}) {
            EntityPlayer player=new EntityPlayer(world,new com.mojang.authlib.GameProfile(new UUID(116,creative?1:2),"B116")) {
                public void addChatMessage(net.minecraft.util.IChatComponent c){}
                public boolean canCommandSenderUseCommand(int level,String command){return false;}
                public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,80,0);}
            };
            player.capabilities.isCreativeMode=creative;
            for(net.minecraft.enchantment.Enchantment enchantment:new net.minecraft.enchantment.Enchantment[]{net.minecraft.enchantment.Enchantment.efficiency,net.minecraft.enchantment.Enchantment.silkTouch,net.minecraft.enchantment.Enchantment.fortune}) {
                ContainerRepair anvil=new ContainerRepair(player.inventory,world,0,80,0,player);
                ItemStack book=new ItemStack(Items.enchanted_book);
                Items.enchanted_book.addEnchantment(book,new net.minecraft.enchantment.EnchantmentData(enchantment,1));
                anvil.getSlot(0).putStack(new ItemStack(hoe));anvil.getSlot(1).putStack(book);
                ItemStack result=anvil.getSlot(2).getStack();
                check(result!=null&&net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(enchantment.effectId,result)==1,"actual anvil book application "+enchantment.effectId);
            }
            ItemStack tool=new ItemStack(hoe);hoe.onBlockDestroyed(tool,world,Blocks.hay_block,0,80,0,player);
            check(tool.getItemDamage()==(creative?0:1),"survival/creative hoe wear");
        }
        System.out.println("PASS B116-01: "+checks+" behavioral checks");
    }
    static double modifier(ItemStack s,String key){Collection values=s.getAttributeModifiers().get(key);check(values.size()==1,"one "+key);AttributeModifier m=(AttributeModifier)values.iterator().next();check(m.getOperation()==0,"additive "+key);return m.getAmount();}
    static InventoryCrafting grid(ItemStack[] values){InventoryCrafting g=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);for(int i=0;i<9;i++)g.setInventorySlotContents(i,values[i]==null?null:values[i].copy());return g;}
    static void verifyRecipe(ItemStack[] values,ItemStack expected,net.minecraft.world.World world){
        IRecipe found=null;for(Object raw:CraftingManager.getInstance().getRecipeList()){IRecipe r=(IRecipe)raw;if(r.matches(grid(values),world)){check(found==null,"unique matching recipe");found=r;}}
        check(found!=null,"runtime recipe exists");check(ItemStack.areItemStacksEqual(found.getCraftingResult(grid(values)),expected),"recipe exact output");
        for(int i=0;i<9;i++)if(values[i]!=null){ItemStack[] missing=values.clone();missing[i]=null;check(!found.matches(grid(missing),world),"cannot omit input "+i);}
        InventoryCrafting a=grid(values);ItemStack first=found.getCraftingResult(a);first.stackSize=0;check(found.getCraftingResult(a).stackSize==expected.stackSize,"no shared output stack");
    }
    static void close(double a,double b,String m){check(Math.abs(a-b)<0.0001,m+": "+a+" != "+b);}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
