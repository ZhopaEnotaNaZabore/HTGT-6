package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Map;
import java.lang.ref.WeakReference;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.LodestoneContent116;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneBinding116;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneMath116;

/** Each live stack and render holder has independent wobble; no shared mutable target. */
@SideOnly(Side.CLIENT)
public final class LodestoneCompassClient116 {
    private static final Map<ItemStack,Map<Entity,State>> states=new WeakHashMap<ItemStack,Map<Entity,State>>();
    private static final class State {
        final WeakReference<World> world;final LodestoneMath116.Wobble wobble=new LodestoneMath116.Wobble();
        final double phase;State(World world,ItemStack stack){this.world=new WeakReference<World>(world);phase=(System.identityHashCode(stack)&65535)/65536D;}
    }
    public static void register(){MinecraftForge.EVENT_BUS.register(new LodestoneCompassClient116());}
    @SubscribeEvent public void frame(RenderItemInFrameEvent event){
        // DataWatcher deserialization does not reattach the client ItemStack's frame.
        // Restore only our stack's contextual pointer, not its saved target or NBT.
        if(event.item!=null&&event.item.getItem()==LodestoneContent116.compass)
            event.item.setItemFrame(event.entityItemFrame);
    }
    public static float angle(ItemStack stack,Object... context){
        if(stack==null)return 0F;
        Entity holder=stack.isOnItemFrame()?stack.getItemFrame():null;
        if(holder==null&&context!=null)for(Object value:context)if(value instanceof Entity){holder=(Entity)value;break;}
        if(holder==null)holder=Minecraft.getMinecraft().thePlayer;
        if(holder==null||holder.worldObj==null)return 0F;
        // Frame rendering in legacy creates a temporary EntityItem at (0,0,0).
        // The ItemStack frame reference, above, deliberately takes precedence.
        World world=holder.worldObj;double yaw=holder.rotationYaw;
        if(holder instanceof EntityItemFrame){
            EntityItemFrame frame=(EntityItemFrame)holder;
            // Native frame renderer rotates the item -90 degrees per step.
            yaw=180D+frame.hangingDirection*90D-frame.getRotation()*90D;
        }else if(holder instanceof EntityItem){
            EntityItem item=(EntityItem)holder;
            yaw=(item.age/20D+item.hoverStart)*180D/Math.PI;
        }
        Map<Entity,State> byHolder=states.get(stack);
        if(byHolder==null){byHolder=new WeakHashMap<Entity,State>();states.put(stack,byHolder);}
        State state=byHolder.get(holder);
        if(state==null||state.world.get()!=world){state=new State(world,stack);byHolder.put(holder,state);}
        double target;
        if(LodestoneBinding116.hasTarget(stack)&&stack.getTagCompound().getInteger(LodestoneBinding116.DIMENSION)==world.provider.dimensionId){
            NBTTagCompound pos=stack.getTagCompound().getCompoundTag(LodestoneBinding116.POS);
            target=LodestoneMath116.bearing(holder.posX,holder.posZ,pos.getInteger("X")+.5D,pos.getInteger("Z")+.5D,yaw);
            // Fixed/dropped displays use their own position and orientation immediately.
            if(holder instanceof EntityItemFrame||holder instanceof EntityItem)return (float)target;
        }else target=LodestoneMath116.wrap(world.getTotalWorldTime()*.071D+state.phase);
        return (float)state.wobble.update(world.getTotalWorldTime(),target);
    }
    private LodestoneCompassClient116(){}
}
