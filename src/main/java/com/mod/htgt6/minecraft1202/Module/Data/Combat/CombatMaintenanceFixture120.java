package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockFrostedIce;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockCombatCommand;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityCombatCommandBlock;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.MendingRepair120;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class CombatMaintenanceFixture120 {
    public static void main(String[] args)throws Exception {
        Grid world=BountifulMechanicsFixture120.initialize();mending();frost(world);commands(world);
        System.out.println("Combat maintenance OK: Mending, frost lifecycle, conditional commands, inactive chains, loops, limits, execution NBT");
    }
    private static ItemStack damaged(int damage){ItemStack stack=new ItemStack(Items.diamond_pickaxe);stack.setItemDamage(damage);return stack;}
    private static final Random FIRST=new Random(){public int nextInt(int bound){return 0;}};
    private static int repair(int xp,ItemStack... stacks){return MendingRepair120.repair(new ArrayList<ItemStack>(Arrays.asList(stacks)),xp,FIRST);}
    private static void mending(){
        ItemStack a=damaged(5),b=damaged(7);require(repair(10,a,b)==5&&a.getItemDamage()==0&&b.getItemDamage()==0,"one orb repairs several items and returns XP remainder");
        a=damaged(4);b=damaged(10);require(repair(3,a,b)==0&&a.getItemDamage()==0&&b.getItemDamage()==4,"1.20.2 original orb capacity on subsequent repairs");
        a=damaged(1);b=damaged(1);require(repair(1,a,b)==1&&a.getItemDamage()==0&&b.getItemDamage()==0,"odd repair rounds XP cost down and terminates");
        a=damaged(20);require(repair(0,a)==0&&a.getItemDamage()==20,"zero XP cannot repair");
        require(repair(9)==9,"no damaged candidates awards all XP");
    }
    private static void frost(Grid w){
        BlockFrostedIce ice=new BlockFrostedIce();w.light=15;
        w.setBlock(0,10,0,ice,0,2);ice.updateTick(w,0,10,0,FIRST);
        require(w.getBlockMetadata(0,10,0)==1&&w.lastDelay==20,"age and lower schedule boundary");
        Random maximum=new Random(){public int nextInt(int bound){return bound-1;}};
        w.light=0;ice.updateTick(w,0,10,0,maximum);require(w.getBlockMetadata(0,10,0)==1&&w.lastDelay==40,"darkness and inclusive upper schedule boundary");
        w.light=15;w.setBlock(0,10,0,ice,3,2);w.setBlock(1,10,0,ice,1,2);
        ice.updateTick(w,0,10,0,FIRST);
        require(w.getBlock(0,10,0)==Blocks.water&&w.getBlockMetadata(1,10,0)==2,"melt ages adjacent ice immediately");
        ice.onNeighborBlockChange(w,1,10,0,Blocks.stone);require(w.getBlock(1,10,0)==ice,"ordinary neighbor change does not cause sparse melt");
        ice.onNeighborBlockChange(w,1,10,0,ice);require(w.getBlock(1,10,0)==Blocks.water,"removed ice neighbor melts isolated ice");
        w.provider.isHellWorld=true;w.setBlock(0,10,0,ice,3,2);ice.updateTick(w,0,10,0,FIRST);
        require(w.getBlock(0,10,0)==Blocks.air,"Nether melting does not create water");
        w.isRemote=true;w.setBlock(0,10,0,ice,3,2);ice.updateTick(w,0,10,0,FIRST);ice.onNeighborBlockChange(w,0,10,0,ice);
        require(w.getBlock(0,10,0)==ice,"client does not mutate ice");
    }
    private static TileEntityCombatCommandBlock tile(Grid w,int x,BlockCombatCommand block,int metadata,boolean automatic){
        w.setBlock(x,20,0,block,metadata,2);TileEntityCombatCommandBlock tile=new TileEntityCombatCommandBlock();
        tile.xCoord=x;tile.yCoord=20;tile.setAutomatic(automatic);w.tiles.put(x+",20,0",tile);return tile;
    }
    private static void commands(Grid w){
        w.isRemote=false;w.time=10;
        BlockCombatCommand repeating=new BlockCombatCommand(BlockCombatCommand.REPEATING),chain=new BlockCombatCommand(BlockCombatCommand.CHAIN);
        TileEntityCombatCommandBlock root=tile(w,0,repeating,13,true),middle=tile(w,1,chain,5,false),last=tile(w,2,chain,4,true);
        net.minecraft.nbt.NBTTagCompound logic=new net.minecraft.nbt.NBTTagCompound();logic.setString("Command","say keep");logic.setInteger("SuccessCount",7);
        root.func_145993_a().func_145759_b(logic);repeating.updateTick(w,0,20,0,FIRST);
        require(root.getLastExecution()==-1&&last.getLastExecution()==-1&&root.func_145993_a().func_145760_g()==0,"failed repeating condition resets count and does not execute chain");
        require("say keep".equals(root.func_145993_a().func_145753_i()),"reset preserves command");
        w.setBlockMetadataWithNotify(0,20,0,5,2);repeating.updateTick(w,0,20,0,FIRST);
        require(root.getLastExecution()==10&&middle.getLastExecution()==-1&&last.getLastExecution()==10,"inactive chain continues to active successor");
        w.time=11;middle.setAutomatic(true);repeating.updateTick(w,0,20,0,FIRST);
        require(middle.getLastExecution()==11&&last.getLastExecution()==11&&!middle.claimExecution(11),"loop executes each block once per tick");
        w.rules.setOrCreateGameRule("maxCommandChainLength","0");w.time=12;repeating.updateTick(w,0,20,0,FIRST);
        require(root.getLastExecution()==12&&middle.getLastExecution()==11,"zero chain gamerule stops successors");
        cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(TileEntityCombatCommandBlock.class,"fixture_command52");
        net.minecraft.nbt.NBTTagCompound saved=new net.minecraft.nbt.NBTTagCompound();root.writeToNBT(saved);
        TileEntityCombatCommandBlock loaded=new TileEntityCombatCommandBlock();loaded.readFromNBT(saved);
        require(!loaded.claimExecution(12)&&loaded.claimExecution(13),"execution tick survives NBT and next tick is allowed");
        saved.setBoolean("UpdateLastExecution",false);loaded.readFromNBT(saved);
        require(loaded.claimExecution(12)&&loaded.claimExecution(12),"disabled timestamp permits repeated execution");
        loaded.setWorldObj(w);loaded.xCoord=0;loaded.yCoord=20;
        int schedules=w.scheduled;loaded.updateEntity();loaded.updateEntity();
        require(w.scheduled==schedules+1,"automatic repeating block schedules once after reload");
        for(int m=0;m<16;m++)require(BlockCombatCommand.facing(m)<6,"metadata cannot index past six faces");
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
