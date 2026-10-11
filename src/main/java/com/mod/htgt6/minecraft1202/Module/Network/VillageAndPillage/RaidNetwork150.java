package com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** One nearest raid snapshot per player; no client authority over raid state. */
public final class RaidNetwork150 {
    private static SimpleNetworkWrapper network;
    public static synchronized void init() {
        if (network != null) return;
        network=NetworkRegistry.INSTANCE.newSimpleChannel("HTGT6Raid");
        network.registerMessage(Handler.class, Snapshot.class, 0, Side.CLIENT);
        network.registerMessage(MerchantHandler.class, MerchantProgress.class, 1, Side.CLIENT);
    }
    public static void send(EntityPlayerMP player, Snapshot snapshot) {
        if(network!=null) network.sendTo(snapshot,player);
    }
    public static final class Snapshot implements IMessage {
        public int dimension,status,remaining;
        public boolean visible;
        public float progress;
        public Snapshot() { }
        public Snapshot(int dimension,boolean visible,int status,int remaining,float progress) {
            this.dimension=dimension;this.visible=visible;this.status=status;this.remaining=remaining;this.progress=progress;
        }
        public void toBytes(ByteBuf b) {b.writeInt(dimension);b.writeBoolean(visible);b.writeByte(status);b.writeInt(remaining);b.writeFloat(progress);}
        public void fromBytes(ByteBuf b) {
            dimension=b.readInt();visible=b.readBoolean();status=Math.max(0,Math.min(2,b.readUnsignedByte()));
            remaining=Math.max(0,Math.min(1024,b.readInt()));float p=b.readFloat();progress=Float.isNaN(p)?0:Math.max(0,Math.min(1,p));
        }
    }
    public static final class Handler implements IMessageHandler<Snapshot,IMessage> {
        public IMessage onMessage(final Snapshot snapshot,MessageContext context) {
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){
                com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RaidHud150.accept(snapshot);
            }});
            return null;
        }
    }
    public static void sendMerchant(net.minecraft.entity.passive.EntityVillager villager,net.minecraft.entity.player.EntityPlayer player){
        if(network==null||!(player instanceof EntityPlayerMP)||villager.worldObj.isRemote)return;
        MerchantProgress packet=new MerchantProgress();packet.entityId=villager.getEntityId();
        packet.xp=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145.xp(villager);
        packet.rank=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145.rank(villager);
        net.minecraft.village.MerchantRecipeList offers=villager.getRecipes(player);
        packet.offerXp=new int[offers==null?0:Math.min(1024,offers.size())];
        for(int i=0;i<packet.offerXp.length;i++)if(offers.get(i) instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ManagedVillagerOffer145)
            packet.offerXp[i]=((com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ManagedVillagerOffer145)offers.get(i)).villagerXp;
        network.sendTo(packet,(EntityPlayerMP)player);
    }
    public static final class MerchantProgress implements IMessage {
        public int entityId,xp,rank;public int[] offerXp=new int[0];
        public void toBytes(ByteBuf b){b.writeInt(entityId);b.writeInt(xp);b.writeByte(rank);b.writeShort(offerXp.length);for(int value:offerXp)b.writeInt(value);}
        public void fromBytes(ByteBuf b){entityId=b.readInt();xp=Math.max(0,b.readInt());rank=Math.max(1,Math.min(5,b.readUnsignedByte()));
            int count=b.readUnsignedShort();if(count>1024)throw new IllegalArgumentException("Too many merchant offers");
            offerXp=new int[count];for(int i=0;i<count;i++)offerXp[i]=Math.max(0,b.readInt());}
    }
    public static final class MerchantHandler implements IMessageHandler<MerchantProgress,IMessage> {
        public IMessage onMessage(final MerchantProgress packet,MessageContext context){
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){
                net.minecraft.world.World world=net.minecraft.client.Minecraft.getMinecraft().theWorld;if(world==null)return;
                net.minecraft.entity.Entity entity=world.getEntityByID(packet.entityId);if(!(entity instanceof net.minecraft.entity.passive.EntityVillager))return;
                net.minecraft.nbt.NBTTagCompound tag=entity.getEntityData();tag.setInteger("MerchantXp120",packet.xp);
                tag.setInteger("MerchantRank120",packet.rank);tag.setIntArray("MerchantOfferXp120",packet.offerXp);
            }});return null;
        }
    }
    private RaidNetwork150() { }
}
