package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import java.util.*;
import java.lang.reflect.*;
import com.google.gson.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import cpw.mods.fml.common.registry.GameRegistry;

public final class VillageContent114Fixture {
    static int checks;
    static void check(boolean v,String m){if(!v)throw new AssertionError(m);checks++;}
    public static void main(String[] args)throws Exception{
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.CLIENT);
        Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        Field owner=cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");owner.setAccessible(true);
        cpw.mods.fml.common.ModMetadata metadata=new cpw.mods.fml.common.ModMetadata();metadata.modId="fixture";metadata.name="fixture";
        owner.set(controller.get(cpw.mods.fml.common.Loader.instance()),new cpw.mods.fml.common.DummyModContainer(metadata));
        for(VillageBuildingContent114.Shape s:VillageBuildingContent114.SHAPES.values())if(Item.getItemFromBlock(s.source)==null)GameRegistry.registerBlock(s.source,"fixture_source_"+s.name);
        GameRegistry.registerBlock(EtFVanillaGapContent120.smoothStone,"fixture_smooth_stone");
        GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.redSandstoneSlab,"fixture_red_sandstone_slab");
        GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.redSandstoneStairs,"fixture_red_sandstone_stairs");
        // The shared stonecutter now also exposes registered 1.16 masonry outputs.
        Block[] masonrySources={EtFVanillaGapContent120.basalt,EtFVanillaGapContent120.polishedBlackstone,
                EtFVanillaGapContent120.quartzBricks,com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.polishedBasalt};
        for(int i=0;i<masonrySources.length;i++)if(Item.getItemFromBlock(masonrySources[i])==null)
            GameRegistry.registerBlock(masonrySources[i],"fixture_masonry_source_"+i);
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherMasonry116.register();
        VillageBuildingContent114.register();VillageSigns114.register();
        VillagePillageContent.registerColoredBeds();
        GameRegistry.registerBlock(VillagePillageContent.grindstone,"fixture_grindstone");
        VillagePillageContent.registerModernIngredientRecipes();
        Method silk=BlockBackport120Slab.class.getDeclaredMethod("createStackedBlock",int.class);silk.setAccessible(true);
        check(VillageBuildingContent114.SHAPES.size()==23,"all 23 missing shapes");
        for(Block smooth:new Block[]{EtFVanillaGapContent120.smoothSandstone,EtFVanillaGapContent120.smoothQuartz,EtFVanillaGapContent120.smoothSandstoneSlab,EtFVanillaGapContent120.smoothSandstoneStairs,VillageBuildingContent114.SHAPES.get("smooth_quartz_slab").block,VillageBuildingContent114.SHAPES.get("smooth_quartz_stairs").block}){
            check(smooth.getBlockHardness(null,0,0,0)==2F,"official smooth stone-family hardness");
            check(Math.abs(smooth.getExplosionResistance(null)-6F)<.0001F,"official smooth stone-family resistance");
        }
        for(VillageBuildingContent114.Shape s:VillageBuildingContent114.SHAPES.values()){
            check(Item.getItemFromBlock(s.block)!=null,"registered "+s.name);
            check(FuturaAncientCityGenerator120.mappedBlockForFixture(s.name)==s.block,"structure identity "+s.name);
            check(s.block.getBlockHardness(null,0,0,0)>0,"hardness "+s.name);
            if(s.doubleSlab!=null){
                check(s.doubleSlab.getCreativeTabToDisplayOn()==null,"technical slab hidden");
                check(s.doubleSlab.getItemDropped(8,new Random(1),0)==Item.getItemFromBlock(s.block),"double slab drop identity");
                check(((ItemStack)silk.invoke(s.doubleSlab,0)).stackSize==2,"silk quantity");
                for(String type:new String[]{"bottom","top","double"})check(FuturaAncientCityGenerator120.mappedStateBlockForFixture(s.name,Collections.singletonMap("type",type),0)==(type.equals("double")?s.doubleSlab:s.block),"NBT slab state");
            }
            if(s.kind.equals("stairs"))for(int r=0;r<4;r++)for(String dir:new String[]{"north","south","east","west"})for(String half:new String[]{"top","bottom"}){
                Map<String,String> props=new HashMap<>();props.put("facing",dir);props.put("half",half);
                check(FuturaAncientCityGenerator120.mappedStateBlockForFixture(s.name,props,r)==s.block,"rotated stair identity");
                check((FuturaAncientCityGenerator120.mappedStateMetaForFixture(s.name,props,r)&4)==(half.equals("top")?4:0),"rotated stair half");
            }
        }
        for(JsonElement e:VillageBuildingStonecutting114.recipes()){
            JsonObject recipe=e.getAsJsonObject();ItemStack in=VillageBuildingStonecutting114.ingredient(recipe.getAsJsonObject("ingredient").get("item").getAsString());
            String name=recipe.get("result").getAsString().substring(10);boolean found=false;
            for(int i=0;i<VillageWorkstationLogic120.choices(VillageWorkstationType120.STONECUTTER,new ItemStack[]{in});i++){
                ItemStack result=VillageWorkstationLogic120.result(VillageWorkstationType120.STONECUTTER,new ItemStack[]{in},i);
                check(result!=null&&result.getItem()!=null,"no null stonecutter output");
                if(result.getItem()==Item.getItemFromBlock(VillageBuildingContent114.SHAPES.get(name).block)&&result.stackSize==recipe.get("count").getAsInt())found=true;
            }
            check(found,"production stonecutter recipe "+recipe.get("id"));
        }
        ItemStack[] stone={new ItemStack(Blocks.stone)};
        for(int i=0;i<VillageWorkstationLogic120.choices(VillageWorkstationType120.STONECUTTER,stone);i++){
            ItemStack out=VillageWorkstationLogic120.result(VillageWorkstationType120.STONECUTTER,stone,i);
            check(!(out.getItem()==Item.getItemFromBlock(Blocks.stone_slab)&&out.getItemDamage()==0),"raw stone must not yield smooth stone slab");
        }
        Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));world.provider.worldObj=world;
        Player player=new Player(world);world.setBlock(0,64,0,Blocks.stone,0,2);
        net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(player.inventoryContainer,3,3);
        for(int color=0;color<16;color++){
            for(int slot=0;slot<9;slot++)grid.setInventorySlotContents(slot,slot<3?new ItemStack(Blocks.wool,1,color):slot<6?new ItemStack(Blocks.planks,1,color%6):null);
            ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world);
            check(crafted!=null&&crafted.getItem()==Item.getItemFromBlock(VillagePillageContent.coloredBeds[color]),"colored bed priority "+color);
        }
        for(int wood=0;wood<6;wood++){
            for(int slot=0;slot<9;slot++)grid.setInventorySlotContents(slot,slot<6?new ItemStack(Blocks.planks,1,wood):slot==7?new ItemStack(Items.stick):null);
            ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world);
            check(crafted!=null&&crafted.getItem()==(wood==0?Items.sign:VillageSigns114.ITEMS[wood-1])&&crafted.stackSize==3,"crafting priority for wood "+wood);
        }
        for(Block source:new Block[]{Blocks.stone,EtFVanillaGapContent120.smoothStone}){
            for(int slot=0;slot<9;slot++)grid.setInventorySlotContents(slot,slot<3?new ItemStack(source):null);
            ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world);
            check(crafted!=null&&crafted.stackSize==6&&crafted.getItem()==Item.getItemFromBlock(source==Blocks.stone?VillageBuildingContent114.SHAPES.get("stone_slab").block:Blocks.stone_slab),"distinct stone/smooth stone slab crafting");
        }
        for(int i=0;i<5;i++){
            String name=VillageSigns114.WOODS[i];
            check(FuturaAncientCityGenerator120.mappedBlockForFixture(name+"_sign")==VillageSigns114.STANDING[i],"sign NBT identity");
            check(FuturaAncientCityGenerator120.mappedBlockForFixture(name+"_wall_sign")==VillageSigns114.WALL[i],"wall sign NBT identity");
            check(VillageSigns114.STANDING[i].getItemDropped(0,new Random(),0)==VillageSigns114.ITEMS[i],"sign drop identity");
            world.setBlock(0,65,0,Blocks.air,0,2);world.rejectWrites=true;ItemStack held=new ItemStack(VillageSigns114.ITEMS[i],2);
            check(!held.getItem().onItemUse(held,player,world,0,64,0,1,0,0,0)&&held.stackSize==2,"rejected sign placement does not consume item");
            world.rejectWrites=false;player.capabilities.isCreativeMode=true;
            check(held.getItem().onItemUse(held,player,world,0,64,0,1,0,0,0)&&held.stackSize==2,"creative sign placement");
            world.setBlock(0,65,0,Blocks.air,0,2);player.capabilities.isCreativeMode=false;
            check(held.getItem().onItemUse(held,player,world,0,64,0,1,0,0,0)&&held.stackSize==1,"survival sign placement");
        }
        for(Block slab:new Block[]{VillageBuildingContent114.SHAPES.get("stone_slab").block,Blocks.stone_slab}){
            for(int slot=0;slot<9;slot++)grid.setInventorySlotContents(slot,slot==0||slot==2?new ItemStack(Items.stick):slot==1?new ItemStack(slab):slot==3||slot==5?new ItemStack(Blocks.planks):null);
            ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world);
            check(slab==Blocks.stone_slab?crafted==null:crafted!=null&&crafted.getItem()==Item.getItemFromBlock(VillagePillageContent.grindstone),"grindstone uses stone slab, not legacy smooth slab");
        }
        com.mod.htgt6.minecraft1202.Module.Data.Modern.WanderingTraderFixture120.registerTradeItems();
        GameRegistry.registerItem(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.suspiciousStew,"fixture_stew");
        GameRegistry.registerBlock(EtFVanillaGapContent120.witherRose,"fixture_wither_rose");
        VillageFoodRecipes114.register();
        ItemStack[] flowers={new ItemStack(Blocks.yellow_flower),new ItemStack(Blocks.red_flower,1,0),new ItemStack(Blocks.red_flower,1,1),new ItemStack(Blocks.red_flower,1,2),new ItemStack(Blocks.red_flower,1,3),new ItemStack(Blocks.red_flower,1,4),new ItemStack(Blocks.red_flower,1,5),new ItemStack(Blocks.red_flower,1,6),new ItemStack(Blocks.red_flower,1,7),new ItemStack(Blocks.red_flower,1,8),new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.cornflower),new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.lilyOfTheValley),new ItemStack(EtFVanillaGapContent120.witherRose)};
        int[] effects={23,16,23,12,15,18,18,18,18,10,8,19,20},durations={7,100,7,80,160,180,180,180,180,160,120,240,160};
        for(int f=0;f<flowers.length;f++){
            for(int slot=0;slot<9;slot++)grid.setInventorySlotContents(slot,slot==0?new ItemStack(Items.bowl):slot==4?new ItemStack(Blocks.red_mushroom):slot==8?new ItemStack(Blocks.brown_mushroom):slot==2?flowers[f]:null);
            ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world);
            check(crafted!=null&&crafted.getItem()==com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.suspiciousStew&&crafted.stackSize==1,"flower stew identity");
            check(crafted.getTagCompound().getInteger("EffectId")==effects[f]&&crafted.getTagCompound().getInteger("EffectDuration")==durations[f],"flower stew effect/duration "+f);
            grid.setInventorySlotContents(4,null);check(net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,world)==null,"stew requires both mushrooms");
        }
        check(WanderingTraderOffers114.definitions(1).size()==64&&WanderingTraderOffers114.definitions(2).size()==6,"full ordinary trader pools");
        for(int tier=1;tier<=2;tier++)for(JsonElement e:WanderingTraderOffers114.definitions(tier)){
            JsonObject d=e.getAsJsonObject();net.minecraft.village.MerchantRecipe offer=WanderingTraderOffers114.create(d);
            check(offer.getItemToSell().getItem()!=null&&offer.getItemToSell().stackSize==d.get("count").getAsInt(),"trader output/count");
            check(offer.getItemToBuy().stackSize==d.get("cost").getAsInt(),"trader emerald price");
            check(offer.writeToTags().getInteger("maxUses")==d.get("maxUses").getAsInt(),"trader stock limit");
            net.minecraft.village.MerchantRecipe restored=new net.minecraft.village.MerchantRecipe(offer.writeToTags());
            check(ItemStack.areItemStacksEqual(restored.getItemToSell(),offer.getItemToSell()),"trader saved identity");
        }
        for(int seed=0;seed<100;seed++)check(WanderingTraderOffers114.createOffers(new Random(seed)).size()==6,"five common plus one special");
        System.out.println("PASS Village content 1.14: "+checks+" checks; 23 shapes, 29 stonecutting recipes, 5 signs, 70 trader offers, 13 flower stews, 16 bed recipes, NBT, drops and placement transactions");
    }
    static final class Player extends EntityPlayer {
        Player(World w){super(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"VillageFixture"));}
        public void addChatMessage(IChatComponent c){}public boolean canCommandSenderUseCommand(int level,String command){return false;}
        public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,64,0);}
        public boolean canPlayerEdit(int x,int y,int z,int side,ItemStack stack){return true;}
    }
}
