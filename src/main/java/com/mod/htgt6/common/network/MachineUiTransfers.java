package com.mod.htgt6.common.network;

import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidHandler;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;

/** Serialises tank clicks with machine ticks and binds requests to the open UI. */
public final class MachineUiTransfers {
    public static final MachineUiTransfers INSTANCE = new MachineUiTransfers();
    private static final Map<Container, TileEntity> OPEN = Collections.synchronizedMap(new WeakHashMap<Container, TileEntity>());
    private final ArrayBlockingQueue<Request> requests = new ArrayBlockingQueue<Request>(256);

    public static void bind(GuiSyncManager manager, TileEntity tile, boolean client) {
        if (client) return;
        manager.addOpenListener(player -> OPEN.put(manager.getContainer(), tile));
        manager.addCloseListener(player -> OPEN.remove(manager.getContainer()));
    }

    public static void enqueue(EntityPlayerMP player, int x, int y, int z) {
        if (player != null) INSTANCE.requests.offer(new Request(player, x, y, z));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (int i = 0; i < 64; i++) {
            Request request = requests.poll();
            if (request == null) break;
            EntityPlayerMP player = request.player;
            TileEntity tile = OPEN.get(request.container);
            if (player.isDead || player.openContainer != request.container || tile == null || tile.isInvalid()
                    || player.worldObj != tile.getWorldObj() || !(tile instanceof IFluidHandler)
                    || tile.xCoord != request.x || tile.yCoord != request.y || tile.zCoord != request.z
                    || player.getDistanceSq(request.x + .5, request.y + .5, request.z + .5) > 64
                    || player.worldObj.getTileEntity(request.x, request.y, request.z) != tile) continue;
            if (StackedFluidContainerTransfer.transfer(player, player.inventory.getItemStack(),
                    (IFluidHandler) tile, true, true)) {
                tile.markDirty();
                player.inventory.markDirty();
            }
            // Cursor is not an ordinary container slot; update it explicitly, even on rejection.
            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, player.inventory.getItemStack()));
            request.container.detectAndSendChanges();
        }
    }

    private static final class Request {
        final EntityPlayerMP player;
        final Container container;
        final int x, y, z;
        Request(EntityPlayerMP player, int x, int y, int z) {
            this.player = player; this.container = player.openContainer;
            this.x = x; this.y = y; this.z = z;
        }
    }
}
