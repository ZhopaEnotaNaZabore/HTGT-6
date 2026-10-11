package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;

/** Server-only offhand use lifecycle, independent of vanilla's main-hand identity check. */
public final class OffhandCrossbowUse120 {
    private static final Map<EntityPlayer,RemoteSession> REMOTE=new WeakHashMap<EntityPlayer,RemoteSession>();
    private static final Map<EntityPlayer,MainSession> REMOTE_MAIN=new WeakHashMap<EntityPlayer,MainSession>();
    private static final Map<EntityPlayer,Boolean> DOWN=new WeakHashMap<EntityPlayer,Boolean>();
    private static final Map<EntityPlayer,Session> CLIENT=new WeakHashMap<EntityPlayer,Session>();
    private static final Map<EntityPlayer,Session> ACTIVE=new WeakHashMap<EntityPlayer,Session>();
    private OffhandCrossbowUse120(){}
    public static boolean available(EntityPlayer player){
        ItemStack stack=OffhandHooks.getOffhand(player);
        return player.isEntityAlive()&&stack!=null&&stack.stackSize>0&&stack.getItem() instanceof ItemCrossbow&&!OffhandSystem.mainHandConsumesUse(player);
    }
    public static boolean start(EntityPlayer player){
        if(player.worldObj.isRemote||!available(player)||DOWN.containsKey(player))return false;
        ItemStack stack=OffhandHooks.getOffhand(player);
        if(PlayerItemUse120.item(player)!=null)return false;
        DOWN.put(player,Boolean.TRUE);
        stack.getItem().onItemRightClick(stack,player.worldObj,player);
        if(PlayerItemUse120.item(player)==stack){
            player.clearItemInUse();ACTIVE.put(player,new Session(stack));
        }
        return true;
    }
    public static void tick(EntityPlayer player){
        if(player.worldObj.isRemote)return;
        Session session=ACTIVE.get(player);if(session==null)return;
        if(!valid(player,session)){cancel(player);return;}
        session.stack.getItem().onUsingTick(session.stack,player,session.stack.getMaxItemUseDuration()-session.elapsed);
        session.elapsed=Math.min(session.stack.getMaxItemUseDuration(),session.elapsed+1);
    }
    public static boolean release(EntityPlayer player){
        if(player.worldObj.isRemote)return false;
        DOWN.remove(player);
        Session session=ACTIVE.remove(player);if(session==null)return false;
        if(!valid(player,session))return false;
        session.stack.getItem().onPlayerStoppedUsing(session.stack,player.worldObj,player,session.stack.getMaxItemUseDuration()-session.elapsed);
        player.inventory.markDirty();return true;
    }
    private static boolean valid(EntityPlayer player,Session session){return available(player)&&OffhandHooks.getOffhand(player)==session.stack&&PlayerItemUse120.item(player)==null;}
    public static void cancel(EntityPlayer player){ACTIVE.remove(player);CLIENT.remove(player);DOWN.remove(player);REMOTE.remove(player);REMOTE_MAIN.remove(player);}
    public static int mainUseTicks(EntityPlayer player){
        if(player==null||!player.isEntityAlive())return -1;
        ItemStack held=player.getHeldItem();
        if(held==null||!(held.getItem() instanceof ItemCrossbow)||ItemCrossbow.isCharged(held))return -1;
        if(PlayerItemUse120.item(player)==held)return PlayerItemUse120.duration(player);
        MainSession session=player.worldObj.isRemote?REMOTE_MAIN.get(player):null;
        if(session==null||!ItemStack.areItemStacksEqual(held,session.stack))return -1;
        int age=player.ticksExisted-session.receivedTick;
        return age<0||age>60?-1:Math.min(72000,session.elapsed+age);
    }
    public static int renderUseTicks(EntityPlayer player,ItemStack rendered){
        if(player==null||rendered==null)return -1;
        if(ItemStack.areItemStacksEqual(rendered,player.getHeldItem())){
            int elapsed=mainUseTicks(player);if(elapsed>=0)return elapsed;
        }
        return useTicks(player,rendered);
    }
    public static void setRemoteMainState(EntityPlayer player,int elapsed,ItemStack stack){
        if(!player.worldObj.isRemote)return;
        if(elapsed<0||stack==null||!(stack.getItem() instanceof ItemCrossbow)){REMOTE_MAIN.remove(player);return;}
        REMOTE_MAIN.put(player,new MainSession(stack.copy(),elapsed,player.ticksExisted));
    }
    private static final class MainSession{
        final ItemStack stack;final int elapsed,receivedTick;
        MainSession(ItemStack stack,int elapsed,int tick){this.stack=stack;this.elapsed=elapsed;receivedTick=tick;}
    }
    /** Local visual prediction only; never consumes ammunition or writes charged NBT. */
    public static void clientTick(EntityPlayer player,boolean using){
        if(!player.worldObj.isRemote)return;
        ItemStack stack=OffhandHooks.getOffhand(player);
        if(!using||!available(player)||ItemCrossbow.isCharged(stack)){CLIENT.remove(player);return;}
        Session session=CLIENT.get(player);
        if(session!=null&&!ItemStack.areItemStacksEqual(session.stack,stack)){CLIENT.remove(player);session=null;}
        if(session==null){
            if(!ItemCrossbow.hasAmmunition(player))return;
            CLIENT.put(player,new Session(stack));
        }else session.elapsed=Math.min(stack.getMaxItemUseDuration(),session.elapsed+1);
    }
    /** -1 means inactive; accepts the renderer's copy of the equipped offhand stack. */
    public static int useTicks(EntityPlayer player,ItemStack rendered){
        if(player==null||rendered==null||!available(player))return -1;
        RemoteSession remote=player.worldObj.isRemote?REMOTE.get(player):null;
        if(remote!=null){
            int age=player.ticksExisted-remote.receivedTick;
            if(age<0||age>60||rendered.getItem()!=remote.item)return -1;
            return Math.min(72000,remote.elapsed+age);
        }
        Session session=(player.worldObj.isRemote?CLIENT:ACTIVE).get(player);
        if(session==null||rendered.getItem()!=session.stack.getItem())return -1;
        return session.elapsed;
    }
    /** Only called after packet identity checks, on the client main thread. */
    public static void setRemoteState(EntityPlayer player,int elapsed){
        if(!player.worldObj.isRemote)return;
        if(elapsed<0){REMOTE.remove(player);return;}
        ItemStack stack=OffhandHooks.getOffhand(player);
        if(stack!=null&&stack.getItem() instanceof ItemCrossbow)REMOTE.put(player,new RemoteSession(stack.getItem(),elapsed,player.ticksExisted));
    }
    private static final class RemoteSession {
        final net.minecraft.item.Item item;final int elapsed,receivedTick;
        RemoteSession(net.minecraft.item.Item item,int elapsed,int receivedTick){this.item=item;this.elapsed=elapsed;this.receivedTick=receivedTick;}
    }
    private static final class Session {final ItemStack stack;int elapsed;Session(ItemStack stack){this.stack=stack;}}
}
