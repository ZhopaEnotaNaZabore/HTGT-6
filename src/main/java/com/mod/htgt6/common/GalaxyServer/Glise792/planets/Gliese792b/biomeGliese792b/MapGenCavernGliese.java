package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.blocks.B_database;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class MapGenCavernGliese extends MapGenBaseMeta {
    public static final int BREAK_THROUGH_CHANCE = 25;

    public MapGenCavernGliese() {}

    @Override
    protected void recursiveGenerate(World par1World, int par2, int par3, int par4, int par5, Block[] blockIdArray, byte[] metaArray) {
        int var7 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(40) + 1) + 1);
        if (this.rand.nextInt(15) != 0) {
            var7 = 0;
        }

        for (int var8 = 0; var8 < var7; ++var8) {
            double var9 = (double) (par2 * 16 + this.rand.nextInt(16));
            double var11 = (double) this.rand.nextInt(this.rand.nextInt(120) + 8);
            double var13 = (double) (par3 * 16 + this.rand.nextInt(16));
            int var15 = 1;
            if (this.rand.nextInt(4) == 0) {
                this.generateLargeCaveNode(this.rand.nextLong(), par4, par5, blockIdArray, metaArray, var9, var11, var13);
                var15 += this.rand.nextInt(4);
            }

            for (int var16 = 0; var16 < var15; ++var16) {
                float var17 = this.rand.nextFloat() * (float) Math.PI * 2.0F;
                float var18 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float var19 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
                this.generateCaveNode(this.rand.nextLong(), par4, par5, blockIdArray, metaArray, var9, var11, var13, var19, var17, var18, 0, 0, 1.0D);
            }
        }
    }

    protected void generateLargeCaveNode(long par1, int par3, int par4, Block[] arrayOfIDs, byte[] arrayOfMeta, double par5, double par7, double par9) {
        this.generateCaveNode(par1, par3, par4, arrayOfIDs, arrayOfMeta, par5, par7, par9, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void generateCaveNode(long par1, int par3, int par4, Block[] blockIdArray, byte[] metaArray, double par6, double par8, double par10, float par12, float par13, float par14, int par15, int par16, double par17) {
        Random random = new Random(par1);
        double d4 = (double) (par3 * 16 + 8);
        double d5 = (double) (par4 * 16 + 8);
        float f3 = 0.0F;
        float f4 = 0.0F;

        if (par16 <= 0) {
            int j1 = this.range * 16 - 16;
            par16 = j1 - random.nextInt(j1 / 4);
        }

        boolean flag = false;

        if (par15 == -1) {
            par15 = par16 / 2;
            flag = true;
        }

        int k1 = random.nextInt(par16 / 2) + par16 / 4;

        for (boolean flag1 = random.nextInt(6) == 0; par15 < par16; ++par15) {
            double d6 = 1.5D + (double) (MathHelper.sin((float) par15 * (float) Math.PI / (float) par16) * par12 * 1.0F);
            double d7 = d6 * par17;
            float f5 = MathHelper.cos(par14);
            float f6 = MathHelper.sin(par14);
            par6 += (double) (MathHelper.cos(par13) * f5);
            par8 += (double) f6;
            par10 += (double) (MathHelper.sin(par13) * f5);

            if (flag1) {
                par14 *= 0.92F;
            } else {
                par14 *= 0.7F;
            }

            par14 += f4 * 0.1F;
            par13 += f3 * 0.1F;
            f4 *= 0.9F;
            f3 *= 0.75F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!flag && par15 == k1 && par12 > 1.0F && par16 > 0) {
                this.generateCaveNode(random.nextLong(), par3, par4, blockIdArray, metaArray, par6, par8, par10, random.nextFloat() * 0.5F + 0.5F, par13 - ((float) Math.PI / 2F), par14 / 3.0F, par15, par16, 1.0D);
                this.generateCaveNode(random.nextLong(), par3, par4, blockIdArray, metaArray, par6, par8, par10, random.nextFloat() * 0.5F + 0.5F, par13 + ((float) Math.PI / 2F), par14 / 3.0F, par15, par16, 1.0D);
                return;
            }

            if (flag || random.nextInt(4) != 0) {
                double d8 = par6 - d4;
                double d9 = par10 - d5;
                double d10 = (double) (par16 - par15);
                double d11 = (double) (par12 + 2.0F + 16.0F);

                if (d8 * d8 + d9 * d9 - d10 * d10 > d11 * d11) {
                    return;
                }

                if (par6 >= d4 - 16.0D - d6 * 2.0D && par10 >= d5 - 16.0D - d6 * 2.0D && par6 <= d4 + 16.0D + d6 * 2.0D && par10 <= d5 + 16.0D + d6 * 2.0D) {
                    int l1 = MathHelper.floor_double(par6 - d6) - par3 * 16 - 1;
                    int i2 = MathHelper.floor_double(par6 + d6) - par3 * 16 + 1;
                    int j2 = MathHelper.floor_double(par8 - d7) - 1;
                    int k2 = MathHelper.floor_double(par8 + d7) + 1;
                    int l2 = MathHelper.floor_double(par10 - d6) - par4 * 16 - 1;
                    int i3 = MathHelper.floor_double(par10 + d6) - par4 * 16 + 1;

                    if (l1 < 0) l1 = 0;
                    if (i2 > 16) i2 = 16;
                    if (j2 < 1) j2 = 1;
                    if (k2 > 248) k2 = 248;
                    if (l2 < 0) l2 = 0;
                    if (i3 > 16) i3 = 16;

                    for (int j3 = l1; j3 < i2; ++j3) {
                        for (int k3 = l2; k3 < i3; ++k3) {
                            for (int l3 = k2 - 1; l3 >= j2; --l3) {
                                int index = (j3 * 16 + k3) * 256 + l3;
                                double d12 = ((double) (j3 + par3 * 16) + 0.5D - par6) / d6;
                                double d13 = ((double) l3 + 0.5D - par8) / d7;
                                double d14 = ((double) (k3 + par4 * 16) + 0.5D - par10) / d6;

                                if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    // CRITICAL FIX: Ensure the cavern only carves through Gliese Stone
                                    if (blockIdArray[index] == B_database.Gliese792bSurface) {
                                        blockIdArray[index] = Blocks.air;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
