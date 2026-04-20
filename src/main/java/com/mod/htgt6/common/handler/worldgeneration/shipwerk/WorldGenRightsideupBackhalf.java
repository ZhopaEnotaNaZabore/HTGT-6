package com.mod.htgt6.common.handler.worldgeneration.shipwerk;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import java.util.Random;
public class WorldGenRightsideupBackhalf extends WorldGenerator {

        @Override
        public boolean generate(World world, Random rand, int x, int y, int z) {
            // Ensure we are underwater or on floor
            if (world.getBlock(x, y, z) != Blocks.water) return false;

            // 1. BASE HULL (Dark Oak Planks - Meta 5)
            // Creating the curved stern shape
            for (int i = 0; i < 8; i++) { // Length
                for (int k = 0; k < 7; k++) { // Width
                    int distFromCenter = Math.abs(k - 3);

                    // Taper the hull at the back (i=0 is the very back)
                    if (i == 0 && distFromCenter > 1) continue;
                    if (i == 1 && distFromCenter > 2) continue;

                    // Floor of the hull
                    setBlock(world, x + i, y, z + k, Blocks.planks, 5);

                    // Walls
                    if (distFromCenter == 3 || (i == 0 && distFromCenter == 1)) {
                        for(int h = 1; h < 4; h++) {
                            setBlock(world, x + i, y + h, z + k, Blocks.planks, 5);
                        }
                    }
                }
            }

            // 2. INTERNAL DECKS (Jungle Planks - Meta 3)
            // Lower deck
            for (int i = 2; i < 7; i++) {
                for (int k = 1; k < 6; k++) {
                    setBlock(world, x + i, y + 1, z + k, Blocks.planks, 3);
                }
            }

            // Upper poop deck (raised section at the back)
            for (int i = 0; i < 4; i++) {
                for (int k = 1; k < 6; k++) {
                    setBlock(world, x + i, y + 3, z + k, Blocks.planks, 3);
                }
            }

            // 3. STAIRS AND BULWARKS (Dark Oak Stairs)
            // Back curve detailing
            setBlock(world, x, y + 4, z + 3, Blocks.dark_oak_stairs, 2); // Facing back

            // Steps up to the poop deck
            setBlock(world, x + 4, y + 2, z + 2, Blocks.jungle_stairs, 1);
            setBlock(world, x + 4, y + 2, z + 4, Blocks.jungle_stairs, 1);

            // 4. FENCES AND RAILS
            for (int i = 0; i < 5; i++) {
                // Dark Oak Fences on the very top edges
                setBlock(world, x + i, y + 4, z + 1, Blocks.fence, 0);
                setBlock(world, x + i, y + 4, z + 5, Blocks.fence, 0);
            }

            // 5. CABIN DETAILS (Door & Windows)
            setBlock(world, x + 3, y + 1, z + 3, Blocks.wooden_door, 0); // Bottom half
            setBlock(world, x + 3, y + 2, z + 3, Blocks.wooden_door, 8); // Top half

            // 6. STORAGE (Chests)
            setBlock(world, x + 1, y + 4, z + 2, Blocks.chest, 0);
            setBlock(world, x + 1, y + 4, z + 4, Blocks.chest, 0);

            // 7. MAST STUMP (Dark Oak Log)
            setBlock(world, x + 6, y + 1, z + 3, Blocks.log2, 1); // Dark Oak Log is log2 meta 1
            setBlock(world, x + 6, y + 2, z + 3, Blocks.log2, 1);

            return true;
        }

        // Helper to ensure we don't leave air bubbles underwater
        private void setBlock(World world, int x, int y, int z, Block block, int meta) {
            world.setBlock(x, y, z, block, meta, 2);
        }

    }

