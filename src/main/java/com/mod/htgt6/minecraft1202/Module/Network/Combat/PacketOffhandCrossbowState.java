package com.mod.htgt6.minecraft1202.Module.Network.Combat;
import java.util.UUID;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;

/** Watcher snapshot; server-to-client only, with full player identity and connection validation. */
public final class PacketOffhandCrossbowState implements IMessage {
    private int dimension,entityId,elapsed,mainElapsed=-1;private UUID uuid;private ItemStack stack,mainStack;
    public PacketOffhandCrossbowState(){}
    public PacketOffhandCrossbowState(EntityPlayer player,int elapsed){
        dimension=player.dimension;entityId=player.getEntityId();uuid=player.getUniqueID();this.elapsed=elapsed;
        stack=ItemStack.copyItemStack(OffhandHooks.getOffhand(player));
        mainElapsed=OffhandCrossbowUse120.mainUseTicks(player);
        mainStack=mainElapsed<0?null:ItemStack.copyItemStack(player.getHeldItem());
    }
    public PacketOffhandCrossbowState stopped(){
        PacketOffhandCrossbowState p=new PacketOffhandCrossbowState();p.dimension=dimension;p.entityId=entityId;p.uuid=uuid;p.elapsed=-1;p.stack=ItemStack.copyItemStack(stack);return p;
    }
    public boolean samePresentation(PacketOffhandCrossbowState other){
        return other!=null&&dimension==other.dimension&&entityId==other.entityId&&uuid.equals(other.uuid)
            &&(elapsed>=0)==(other.elapsed>=0)&&ItemStack.areItemStacksEqual(stack,other.stack)
            &&(mainElapsed>=0)==(other.mainElapsed>=0)&&ItemStack.areItemStacksEqual(mainStack,other.mainStack);
    }
    @Override public void fromBytes(ByteBuf buffer){
        if(buffer.readableBytes()<36||buffer.readableBytes()>32768)throw new IllegalArgumentException("Invalid crossbow state size");
        dimension=buffer.readInt();entityId=buffer.readInt();uuid=new UUID(buffer.readLong(),buffer.readLong());elapsed=buffer.readInt();stack=ByteBufUtils.readItemStack(buffer);
        mainElapsed=buffer.readInt();mainStack=ByteBufUtils.readItemStack(buffer);
        if(buffer.isReadable()||invalid(elapsed,stack)||invalid(mainElapsed,mainStack))throw new IllegalArgumentException("Invalid crossbow state");
    }
    private static boolean invalid(int elapsed,ItemStack stack){return elapsed< -1||elapsed>72000||elapsed>=0&&(stack==null||stack.stackSize<=0||!(stack.getItem() instanceof ItemCrossbow));}
    @Override public void toBytes(ByteBuf buffer){buffer.writeInt(dimension);buffer.writeInt(entityId);buffer.writeLong(uuid.getMostSignificantBits());buffer.writeLong(uuid.getLeastSignificantBits());buffer.writeInt(elapsed);ByteBufUtils.writeItemStack(buffer,stack);buffer.writeInt(mainElapsed);ByteBufUtils.writeItemStack(buffer,mainStack);}
    public boolean applyTo(EntityPlayer player,boolean local){
        if(local||!player.worldObj.isRemote||player.dimension!=dimension||player.getEntityId()!=entityId||!player.getUniqueID().equals(uuid))return false;
        OffhandHooks.setOffhand(player,ItemStack.copyItemStack(stack));OffhandCrossbowUse120.setRemoteState(player,elapsed);OffhandCrossbowUse120.setRemoteMainState(player,mainElapsed,mainStack);return true;
    }
    public static final class Handler implements IMessageHandler<PacketOffhandCrossbowState,IMessage>{
        @Override public IMessage onMessage(final PacketOffhandCrossbowState packet,MessageContext context){
            final net.minecraft.client.network.NetHandlerPlayClient connection=context.getClientHandler();
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){@Override public void run(){
                net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();
                if(mc.getNetHandler()!=connection||mc.theWorld==null||mc.theWorld.provider.dimensionId!=packet.dimension)return;
                net.minecraft.entity.Entity entity=mc.theWorld.getEntityByID(packet.entityId);
                if(entity instanceof EntityPlayer)packet.applyTo((EntityPlayer)entity,entity==mc.thePlayer);
            }});return null;
        }
    }
}
