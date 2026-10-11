package buildcraft.api.tools;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Compile-only shape of the legacy BuildCraft wrench API.
 * The class is intentionally not packaged; FML removes this optional interface
 * from ItemNanotool when BuildCraft is absent.
 */
public interface IToolWrench {
    boolean canWrench(EntityPlayer player, int x, int y, int z);

    void wrenchUsed(EntityPlayer player, int x, int y, int z);
}
