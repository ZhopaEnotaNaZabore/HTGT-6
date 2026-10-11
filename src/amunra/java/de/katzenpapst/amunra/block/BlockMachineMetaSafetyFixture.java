package de.katzenpapst.amunra.block;

/** Regression for orphaned machine metadata encountered while loading old chunks. */
public final class BlockMachineMetaSafetyFixture {
    public static void main(String[] ignored) {
        SubBlockMachine[] registered = new SubBlockMachine[4];
        if (BlockMachineMeta.hasTileEntity(registered, 2))
            throw new IllegalStateException("Unregistered machine metadata advertised a tile entity");
        if (BlockMachineMeta.machineForMetadata(registered, 2) != null)
            throw new IllegalStateException("Unregistered machine metadata resolved a machine");
        System.out.println("Amun-Ra orphaned machine metadata fixture OK");
    }

    private BlockMachineMetaSafetyFixture() { }
}
