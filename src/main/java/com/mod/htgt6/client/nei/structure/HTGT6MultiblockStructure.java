package com.mod.htgt6.client.nei.structure;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.List;

/** Client-side description consumed by the NEI StructureLib-style renderer. */
public interface HTGT6MultiblockStructure {
    String getName();
    ItemStack getController();
    int getWidth();
    int getHeight();
    int getDepth();
    /** Controller coordinates inside the model; used by hologram rotation and auto-build. */
    int getControllerX();
    int getControllerY();
    int getControllerZ();
    StructureBlock getBlock(int x, int y, int z);
    List<Component> getComponents();

    final class StructureBlock {
        public final Block block;
        public final int metadata;
        public final boolean acceptAnyMetadata;

        public StructureBlock(Block block, int metadata) {
            this(block, metadata, false);
        }

        public StructureBlock(Block block, int metadata, boolean acceptAnyMetadata) {
            this.block = block;
            this.metadata = metadata;
            this.acceptAnyMetadata = acceptAnyMetadata;
        }

        public boolean matches(Block actual, int actualMetadata) {
            return block == actual && (acceptAnyMetadata || metadata == actualMetadata
                    || block instanceof com.mod.htgt6.common.multiblock.glass.BlockObservationGlass);
        }
    }

    final class Component {
        public final ItemStack stack;
        public final int amount;
        public final String role;

        public Component(ItemStack stack, int amount, String role) {
            this.stack = stack;
            this.amount = amount;
            this.role = role;
        }
    }
}
