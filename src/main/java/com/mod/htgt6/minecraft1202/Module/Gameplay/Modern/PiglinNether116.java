package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

/** Spawn list extension works in existing chunks and with any Nether terrain provider. */
public final class PiglinNether116 {
    private final java.util.Map<EntityPlayer,net.minecraft.inventory.Container> pendingOpens=new java.util.WeakHashMap<EntityPlayer,net.minecraft.inventory.Container>();
    @SubscribeEvent public void potentialSpawns(WorldEvent.PotentialSpawns e){
        if(e.world.provider.dimensionId!=-1||e.type!=EnumCreatureType.monster)return;
        for(Object entry:e.list)if(((BiomeGenBase.SpawnListEntry)entry).entityClass==EntityPiglin116.class)return;
        e.list.add(new BiomeGenBase.SpawnListEntry(EntityPiglin116.class,15,2,4));
    }
    private static boolean container(Block b){return b==Blocks.chest||b==Blocks.trapped_chest||b==Blocks.ender_chest||b instanceof com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillageBarrel120||b instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112;}
    public static boolean guarded(Block b){return b==Blocks.gold_block||b==Blocks.gold_ore||b==EtFVanillaGapContent120.netherGoldOre||b==EtFVanillaGapContent120.gildedBlackstone||b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.rawGoldBlock||container(b);}
    public static void alert(EntityPlayer player,boolean requireSight){
        if(player==null||player.worldObj.isRemote||player.capabilities.isCreativeMode)return;
        for(Object o:player.worldObj.getEntitiesWithinAABB(EntityPiglin116.class,player.boundingBox.expand(16,16,16))){EntityPiglin116 p=(EntityPiglin116)o;if(!p.isAdmiring()&&p.getAttackTarget()==null&&(!requireSight||p.getEntitySenses().canSee(player)))p.angerAt(player,false);}
    }
    @SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.LOWEST) public void open(PlayerInteractEvent e){
        if(e.world.isRemote||e.useBlock==cpw.mods.fml.common.eventhandler.Event.Result.DENY||e.entityPlayer.capabilities.isCreativeMode)return;
        if(e.action==PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK&&container(e.world.getBlock(e.x,e.y,e.z)))pendingOpens.put(e.entityPlayer,e.entityPlayer.openContainer);
    }
    /** A click on a blocked/denied chest must not count as opening its inventory. */
    @SubscribeEvent public void playerTick(cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent e){
        if(e.phase!=cpw.mods.fml.common.gameevent.TickEvent.Phase.END||e.player.worldObj.isRemote)return;
        finishOpen(e.player);
    }
    public void finishOpen(EntityPlayer player){
        if(!pendingOpens.containsKey(player))return;
        net.minecraft.inventory.Container before=pendingOpens.remove(player);
        if(player.openContainer!=null&&player.openContainer!=before&&player.openContainer!=player.inventoryContainer)alert(player,true);
    }
    @SubscribeEvent public void logout(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent e){pendingOpens.remove(e.player);}
    @SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.LOWEST) public void breakBlock(BlockEvent.BreakEvent e){if(guarded(e.block))alert(e.getPlayer(),false);}
}
