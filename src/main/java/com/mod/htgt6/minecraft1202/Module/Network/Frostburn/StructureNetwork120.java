package com.mod.htgt6.minecraft1202.Module.Network.Frostburn;

import java.util.concurrent.ConcurrentHashMap;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn.ContainerStructure120;

/** One pending edit per connection; all world access happens on the server tick. */
public final class StructureNetwork120 {
    public static final int GUI_ID=110;
    public static final SimpleNetworkWrapper NETWORK=NetworkRegistry.INSTANCE.newSimpleChannel("HTGT6Structure");
    private static final ConcurrentHashMap<EntityPlayerMP,Edit> PENDING=new ConcurrentHashMap<EntityPlayerMP,Edit>();
    public static void init() {
        NETWORK.registerMessage(Handler.class,Edit.class,0,Side.SERVER);
        FMLCommonHandler.instance().bus().register(new StructureNetwork120());
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        for(EntityPlayerMP player:PENDING.keySet()) {
            Edit edit=PENDING.remove(player);
            if(edit==null||!(player.openContainer instanceof ContainerStructure120))continue;
            ContainerStructure120 container=(ContainerStructure120)player.openContainer;
            if(!container.canInteractWith(player)||player.dimension!=edit.dimension)continue;
            TileEntityStructure120 tile=container.tile;
            if(tile.xCoord!=edit.x||tile.yCoord!=edit.y||tile.zCoord!=edit.z||!edit.valid())continue;
            tile.name=edit.name;tile.offsetX=edit.ox;tile.offsetY=edit.oy;tile.offsetZ=edit.oz;
            tile.sizeX=edit.sx;tile.sizeY=edit.sy;tile.sizeZ=edit.sz;
            tile.showBoundingBox=edit.showBoundingBox;tile.rotation=edit.rotation;tile.mirror=edit.mirror;
            tile.integrity=edit.integrity;tile.seed=edit.seed;tile.metadata=edit.metadata;
            tile.getWorldObj().setBlockMetadataWithNotify(tile.xCoord,tile.yCoord,tile.zCoord,edit.mode,3);
            try {
                if(edit.action==1&&edit.mode==0)tile.save();
                else if(edit.action==2&&edit.mode==1) {
                    if(!tile.load(true)) {
                        player.addChatMessage(new ChatComponentTranslation("htgt6.structure.prepared"));continue;
                    }
                }
                else if(edit.action==3&&edit.mode==0) { if(!tile.detectCorners())throw new java.io.IOException("No valid corners"); }
                else if(edit.action!=0)throw new java.io.IOException("Wrong structure mode");
                player.addChatMessage(new ChatComponentTranslation("htgt6.structure.success"));
            } catch(java.io.IOException error) {
                player.addChatMessage(new ChatComponentTranslation("htgt6.structure.error",error.getMessage()));
            }
            tile.changed();
        }
    }
    public static final class Edit implements IMessage {
        public int dimension,x,y,z,mode,action,ox,oy,oz,sx,sy,sz;
        public String name="";
        public String metadata="";
        public float integrity=1F;
        public long seed;
        public int rotation,mirror;
        public boolean showBoundingBox=true;
        public Edit() { }
        @Override public void fromBytes(ByteBuf b) {
            dimension=b.readInt();x=b.readInt();y=b.readInt();z=b.readInt();mode=b.readUnsignedByte();action=b.readUnsignedByte();
            ox=b.readInt();oy=b.readInt();oz=b.readInt();sx=b.readInt();sy=b.readInt();sz=b.readInt();
            integrity=b.readFloat();seed=b.readLong();rotation=b.readUnsignedByte();mirror=b.readUnsignedByte();showBoundingBox=b.readBoolean();
            if(b.readableBytes()>1040)throw new IllegalArgumentException("Oversized structure edit");
            name=ByteBufUtils.readUTF8String(b);
            metadata=ByteBufUtils.readUTF8String(b);
        }
        @Override public void toBytes(ByteBuf b) {
            b.writeInt(dimension);b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeByte(mode);b.writeByte(action);
            b.writeInt(ox);b.writeInt(oy);b.writeInt(oz);b.writeInt(sx);b.writeInt(sy);b.writeInt(sz);
            b.writeFloat(integrity);b.writeLong(seed);b.writeByte(rotation);b.writeByte(mirror);b.writeBoolean(showBoundingBox);ByteBufUtils.writeUTF8String(b,name);
            ByteBufUtils.writeUTF8String(b,metadata);
        }
        public boolean valid() {
            return rotation>=0&&rotation<=3&&mirror>=0&&mirror<=2&&validIntegrity()&&metadata.length()<=128&&name.length()<=128&&(name.isEmpty()||TileEntityStructure120.validName(name))
                    &&mode>=0&&mode<=3&&action>=0&&action<=3
                    &&ox>=-48&&ox<=48&&oy>=-48&&oy<=48&&oz>=-48&&oz<=48
                    &&sx>=0&&sx<=48&&sy>=0&&sy<=48&&sz>=0&&sz<=48;
        }
        private boolean validIntegrity() { return !Float.isNaN(integrity)&&integrity>=0F&&integrity<=1F; }
    }
    public static final class Handler implements IMessageHandler<Edit,IMessage> {
        @Override public IMessage onMessage(Edit edit,MessageContext context) {
            if(edit.valid())PENDING.put(context.getServerHandler().playerEntity,edit);
            return null;
        }
    }
}
