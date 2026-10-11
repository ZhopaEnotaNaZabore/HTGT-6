package com.mod.htgt6.common.chemical;

import com.mod.htgt6.api.hitech6.multiblock.MachineAnimationProgress;
import com.mod.htgt6.common.TE.FluidSolidifierTE;
import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import com.mod.htgt6.common.crusher.MultiTileIndustrialCrusher;
import com.mod.htgt6.common.electronics.TEElectronicFactory;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import cpw.mods.fml.common.registry.GameRegistry;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;

/** Real controller packet round-trips with missing client recipes and delayed network snapshots. */
public final class MachineAnimationSyncFixture {
    public static final class Plant extends MultiTileChemicalPlant {
        List<ChemicalPlantRecipe> recipes;
        @Override protected List<ChemicalPlantRecipe> getMachineRecipes() { return recipes; }
    }

    public static void main(String[] args) throws Exception {
        verifyDedicatedServerPacketApi();
        Grid world = BountifulMechanicsFixture120.initialize();
        GameRegistry.registerTileEntity(Plant.class, "htgt6_animation_fixture");
        ChemicalPlantRecipe recipe = new ChemicalPlantRecipe("fixture",
                new ItemStack[] {new ItemStack(Items.iron_ingot)}, new ItemStack[] {new ItemStack(Items.gold_ingot)},
                null, null, null, 200, 8, 1, -1);
        Plant server = new Plant();
        server.recipes = Arrays.asList(recipe);
        server.setWorldObj(world);
        NBTTagCompound saved = new NBTTagCompound();
        saved.setBoolean("StructureFormed", true);
        saved.setBoolean("ChemicalProcessing", true);
        saved.setString("Diagnostic", "PROCESSING: fixture");
        saved.setInteger("ChemicalRecipeIndex", 0);
        saved.setInteger("Progress", 20);
        saved.setInteger("MaxProgress", 200);
        server.readFromNBT(saved);
        S35PacketUpdateTileEntity packet = (S35PacketUpdateTileEntity) server.getDescriptionPacket();
        require(packet.func_148857_g().getTagList("ChemicalRenderInputs", 10).tagCount() == 1,
                "Packet includes server recipe inputs");
        NBTTagCompound disk = new NBTTagCompound();
        server.writeToNBT(disk);
        require(!disk.hasKey("ChemicalRenderInputs"), "Visual data must not change saved recipe format");
        NBTTagCompound update = (NBTTagCompound) packet.func_148857_g().copy();
        update.setInteger("ChemicalRecipeIndex", Integer.MAX_VALUE);
        update.setInteger("SMCMultiblockVersion", 2);
        update.setInteger("SolidifierMultiblockVersion", 2);
        Field remote = World.class.getDeclaredField("isRemote");
        remote.setAccessible(true);
        remote.setBoolean(world, true);
        Plant mismatched = new Plant();
        mismatched.recipes = Arrays.asList(new ChemicalPlantRecipe("different client order",
                new ItemStack[]{new ItemStack(Items.diamond)},new ItemStack[]{new ItemStack(Items.coal)},
                null,null,null,200,8,1,-1));
        mismatched.setWorldObj(world);
        mismatched.readFromNBT(packet.func_148857_g());
        require(mismatched.getCurrentRecipeItemInputForRender(0).getItem()==Items.iron_ingot,
                "Valid but wrong local recipe index must never replace server host rock");
        NBTTagCompound legacy=(NBTTagCompound)packet.func_148857_g().copy();
        legacy.removeTag("ChemicalRenderInputs");legacy.removeTag("ChemicalRenderOutput");
        mismatched.readFromNBT(legacy);
        require(mismatched.getCurrentRecipeItemInputCount()==0,
                "Missing snapshot must not render a different local ore");
        for (MultiTileChemicalPlant client : new MultiTileChemicalPlant[] {
                new TEElectronicFactory(), new FluidSolidifierTE(), new TESuperMasicCompressor(),
                new MultiTileIndustrialCrusher()}) {
            client.setWorldObj(world);
            for (long age : new long[] {0, 1600000000L}) {
                world.time = age;
                client.onDataPacket(null, new S35PacketUpdateTileEntity(0, 0, 0, 1, update));
                require(client.isMachineActive() && client.progress == 20, "Client must keep server processing state");
                require(client.getCurrentRecipeItemInputForRender(0).getItem() == Items.iron_ingot,
                        "Client input independent of local recipe index");
                require(client.getCurrentRecipePrimaryOutputForRender().getItem() == Items.gold_ingot,
                        "Client output independent of local recipe index");
                ItemStack copy = client.getCurrentRecipeItemInputForRender(0);
                copy.stackSize = 0;
                require(client.getCurrentRecipeItemInputForRender(0).stackSize == 1, "Defensive render copy");
                for (int frame = 0; frame < 300; frame++) {
                    world.time = age + frame / 3;
                    float progress = client.getProcessProgressFraction((frame % 3) / 3F);
                    near(progress, (20F + frame / 3F) / 200F, "Smooth five-second packet gap");
                }
                require(client.progress == 20, "Visual prediction must not change recipe state");
                world.time = age + 1000;
                near(client.getProcessProgressFraction(.5F), 1F, "Do not invent the next recipe");
                NBTTagCompound paused = (NBTTagCompound) update.copy();
                paused.setString("Diagnostic", "WAITING: ENERGY");
                client.onDataPacket(null, new S35PacketUpdateTileEntity(0, 0, 0, 1, paused));
                world.time += 100;
                near(client.getProcessProgressFraction(.9F), .1F, "No motion without energy");
                paused.setBoolean("ChemicalProcessing", false);
                paused.setTag("ChemicalRenderInputs", new NBTTagList());
                paused.removeTag("ChemicalRenderOutput");
                client.onDataPacket(null, new S35PacketUpdateTileEntity(0, 0, 0, 1, paused));
                require(client.getCurrentRecipeItemInputCount() == 0
                        && client.getCurrentRecipePrimaryOutputForRender() == null, "Clear completed cycle visuals");
                near(client.getProcessProgressFraction(.5F), 0F, "Completed recipe reset");
            }
            if (client instanceof MultiTileIndustrialCrusher) {
                NBTTagCompound belt = (NBTTagCompound) update.copy();
                NBTTagList drops = new NBTTagList();
                NBTTagCompound drop = new NBTTagCompound();
                new ItemStack(Items.leather).writeToNBT(drop);
                drops.appendTag(drop);
                belt.setTag("CrusherMobDrops", drops);
                belt.setBoolean("CrusherBeltRunning", true);
                belt.setInteger("CrusherOutputTicks", 20);
                world.time = 1600000000L;
                client.onDataPacket(null, new S35PacketUpdateTileEntity(0, 0, 0, 1, belt));
                world.time += 50;
                near(((MultiTileIndustrialCrusher) client).getMobOutputTicksForRender(.5F), 70.5F,
                        "Actual crusher transport continues through delayed packets");
                belt.setBoolean("CrusherBeltRunning", false);
                client.onDataPacket(null, new S35PacketUpdateTileEntity(0, 0, 0, 1, belt));
                world.time += 50;
                near(((MultiTileIndustrialCrusher) client).getMobOutputTicksForRender(.5F), 20F,
                        "Actual crusher transport stops on pause packet");
            }
        }
        near(MachineAnimationProgress.ticks(20, 100, 50, .5F, true), 70.5F, "Delayed crusher belt snapshot");
        near(MachineAnimationProgress.ticks(20, 100, 50, .5F, false), 20F, "Paused belt");
        near(MachineAnimationProgress.ticks(20, 100, -30, 0F, true), 20F, "World time correction");
        remote.setBoolean(world, false);
        server.readFromNBT(update);
        require(!server.isMachineActive() && server.progress == 0, "Server still rejects missing saved recipes");
        System.out.println("Machine animation sync: four controllers, sparse packets, old worlds, pause, completion and recipe mismatch PASS");
    }

    private static void near(float value, float expected, String message) {
        require(Math.abs(value - expected) < .0001F, message + ": " + value + " != " + expected);
    }
    /** The merged dev jar exposes S35 client getters that dedicated servers strip. */
    private static void verifyDedicatedServerPacketApi() throws Exception {
        org.objectweb.asm.tree.ClassNode node = new org.objectweb.asm.tree.ClassNode();
        try (java.io.InputStream input = MultiTileChemicalPlant.class.getResourceAsStream(
                "/com/mod/htgt6/common/chemical/MultiTileChemicalPlant.class")) {
            new org.objectweb.asm.ClassReader(input).accept(node, 0);
        }
        boolean found = false;
        for (org.objectweb.asm.tree.MethodNode method : node.methods) {
            if (!"getDescriptionPacket".equals(method.name) && !"func_145844_m".equals(method.name)) continue;
            found = true;
            for (org.objectweb.asm.tree.AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof org.objectweb.asm.tree.MethodInsnNode)) continue;
                org.objectweb.asm.tree.MethodInsnNode call = (org.objectweb.asm.tree.MethodInsnNode) instruction;
                require(!("net/minecraft/network/play/server/S35PacketUpdateTileEntity".equals(call.owner)
                        && "func_148857_g".equals(call.name)),
                        "Outgoing server packet must not call S35's client-only NBT getter");
            }
        }
        require(found, "Server packet builder was checked");
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
