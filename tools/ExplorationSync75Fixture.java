import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.*;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/** Serialize metadata across separate entities, rather than sharing watcher objects. */
public final class ExplorationSync75Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        EntityLlama112 server = new EntityLlama112(world), client = new EntityLlama112(world);
        NBTTagCompound saved = new NBTTagCompound();
        server.writeEntityToNBT(saved);
        saved.setInteger("Variant", 3);
        saved.setInteger("Strength", 5);
        saved.setBoolean("TraderLlama", true);
        server.readEntityFromNBT(saved);
        transfer(server, client, true);
        require(client.getVariant() == 3 && client.getStrength() == 5 && client.isTraderLlama(), "initial llama metadata");
        server.getDataWatcher().getChanged();
        saved.setInteger("Variant", 1);
        saved.setInteger("Strength", 2);
        server.readEntityFromNBT(saved);
        transfer(server, client, false);
        require(client.getVariant() == 1 && client.getStrength() == 2, "llama metadata delta");
        NBTTagCompound roundTrip = new NBTTagCompound();
        client.writeEntityToNBT(roundTrip);
        require(roundTrip.getInteger("Variant") == 1 && roundTrip.getInteger("Strength") == 2, "NBT compatibility");
        saved.setInteger("Strength", 99);
        server.readEntityFromNBT(saved);
        require(server.getStrength() == 5, "strength clamp");
        new EntityTraderLlama120(world); // inherited watcher indices must not collide

        EntityVex112 vex = new EntityVex112(world), remote = new EntityVex112(world);
        Method charging = EntityVex112.class.getDeclaredMethod("setCharging", boolean.class);
        charging.setAccessible(true);
        charging.invoke(vex, true);
        transfer(vex, remote, true);
        require(remote.isCharging(), "late tracking during charge");
        vex.getDataWatcher().getChanged();
        charging.invoke(vex, false);
        transfer(vex, remote, false);
        require(!remote.isCharging(), "charge reset delta");
        charging.invoke(vex, true);
        transfer(vex, remote, false);
        require(remote.isCharging(), "next charge delta");
        System.out.println("PASS: serialized initial/delta llama and vex metadata, legacy NBT and trader subclass");
    }

    private static void transfer(Entity from, Entity to, boolean full) throws Exception {
        List entries = full ? from.getDataWatcher().getAllWatched() : from.getDataWatcher().getChanged();
        require(entries != null, "missing metadata update");
        PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
        try {
            DataWatcher.writeWatchedListToPacketBuffer(entries, packet);
            to.getDataWatcher().updateWatchedObjectsFromList(DataWatcher.readWatchedListFromPacketBuffer(packet));
            require(packet.readableBytes() == 0, "unconsumed metadata bytes");
        } finally { packet.release(); }
    }

    private static void require(boolean pass, String message) {
        if (!pass) throw new AssertionError(message);
    }
}
