package com.mod.htgt6.common.crusher;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialCrusherRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import java.util.List;

/** 7x6x16 crusher built on the shared HTGT6 processing/UI and gateway core. */
public final class MultiTileIndustrialCrusher extends MultiTileChemicalPlant {
    /** 0 = both maps, 1 = Crusher, 2 = Shredder. Native recipes work in every mode. */
    private int recipeMode;
    final CrusherConveyor conveyor=new CrusherConveyor(this);
    public boolean isConveyorRunning(){return conveyor.isRunning();}
    public ItemStack getMobOutputForRender(){return conveyor.outputForRender();}
    public float getMobOutputTicksForRender(float partial){return conveyor.outputTicksForRender(partial);}
    boolean canDeliverMobDrops(){return !hasPreparedRecipe();}
    @Override public void updateEntity(){
        super.updateEntity();
        if(worldObj==null)return;
        if(worldObj.isRemote){conveyor.clientTick();return;}
        if(!enabled||!structureFormed){
            if(conveyor.tick(false))markControllerDirtyAndSync();
        }
    }
    @Override protected void processMachineLogic(){
        super.processMachineLogic();
        if(!getStructureDefinition().areChunksLoaded(this)){conveyor.release();return;}
        boolean changed=conveyor.tick(energy>=CrusherConveyor.POWER);
        if(conveyor.consumedPower()){
            energy-=CrusherConveyor.POWER;addAuxiliaryEnergyUsage(CrusherConveyor.POWER);
            markDirty();
        }
        if(changed)markControllerDirtyAndSync();
    }
    @Override public void invalidate(){conveyor.release();super.invalidate();}
    @Override public void onChunkUnload(){conveyor.release();super.onChunkUnload();}
    public void dropConveyorContents(){
        conveyor.dropContents();
        if(worldObj==null||worldObj.isRemote)return;
        for(int i=0;i<inventory.length;i++)if(inventory[i]!=null){
            worldObj.spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(worldObj,xCoord+.5,yCoord+.5,zCoord+.5,inventory[i].copy()));
            inventory[i]=null;
        }
    }

    @Override protected boolean hasRecipeFluidInputs(ChemicalPlantRecipe recipe, int multiplier) {
        return IndustrialCrusherRecipeHandler.acceptsMode(recipeMode, recipe)
                && super.hasRecipeFluidInputs(recipe, multiplier);
    }

    @Override protected ChemicalPlantRecipe findRecipe() {
        if (worldObj != null && !worldObj.isRemote) {
            for (int i = INPUT_SLOT_START; i < OUTPUT_SLOT_START; i++)
                IndustrialCrusherRecipeHandler.INSTANCE.importGT6RecipesForInput(inventory[i]);
        }
        return super.findRecipe();
    }

    @Override public com.cleanroommc.modularui.screen.ModularPanel buildUI(
            com.cleanroommc.modularui.manager.GuiCreationContext context,
            com.cleanroommc.modularui.value.sync.GuiSyncManager sync, boolean client) {
        com.cleanroommc.modularui.screen.ModularPanel panel = super.buildUI(context, sync, client);
        sync.syncValue("crusher_mode", com.cleanroommc.modularui.value.sync.SyncHandlers.intNumber(
                () -> recipeMode, value -> recipeMode = Math.max(0, Math.min(2, value))));
        sync.syncValue("crusher_mode_action", new com.cleanroommc.modularui.value.sync.InteractionSyncHandler()
                .setOnMouseTapped(data -> {
                    if (worldObj != null && !worldObj.isRemote && progress == 0
                            && getCurrentRecipeItemInputCount() == 0) {
                        recipeMode = (recipeMode + 1) % 3;
                        markDirty();
                        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                    }
                }));
        com.cleanroommc.modularui.widgets.ButtonWidget<?> button = new com.cleanroommc.modularui.widgets.ButtonWidget();
        button.pos(10, 106).size(36, 22).background(com.cleanroommc.modularui.drawable.GuiTextures.BUTTON)
                .overlay(com.cleanroommc.modularui.api.drawable.IKey.dynamic(() -> recipeMode == 0 ? "A" : recipeMode == 1 ? "C" : "S"))
                .syncHandler("crusher_mode_action");
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocal("ui.htgt6.industrial_crusher.mode_" + recipeMode));
            t.addLine(StatCollector.translateToLocal("ui.htgt6.industrial_crusher.mode_hint"));
        });
        panel.child(button);
        return panel;
    }

    @Override public void readFromNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        recipeMode = Math.max(0, Math.min(2, nbt.getInteger("CrusherRecipeMode")));
        if ((worldObj == null || !worldObj.isRemote) && nbt.hasKey("CrusherRecipeKey")) {
            net.minecraft.nbt.NBTTagList inputs=nbt.getTagList("CrusherRecipeInputs",10);
            for(int i=0;i<inputs.tagCount();i++)
                IndustrialCrusherRecipeHandler.INSTANCE.importGT6RecipesForInput(
                        ItemStack.loadItemStackFromNBT(inputs.getCompoundTagAt(i)));
            String key=nbt.getString("CrusherRecipeKey");
            nbt=(net.minecraft.nbt.NBTTagCompound)nbt.copy();
            nbt.setInteger("ChemicalRecipeIndex",-1);
            List<ChemicalPlantRecipe> recipes=getMachineRecipes();
            for(int i=0;i<recipes.size();i++)if(key.equals(IndustrialCrusherRecipeHandler.recipeKey(recipes.get(i)))){
                nbt.setInteger("ChemicalRecipeIndex",i);break;
            }
        }
        super.readFromNBT(nbt);
        conveyor.read(nbt);
    }

    @Override public void writeToNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("CrusherRecipeMode", recipeMode);
        int index=nbt.getInteger("ChemicalRecipeIndex");
        if(index>=0&&index<getMachineRecipes().size()){
            ChemicalPlantRecipe recipe=getMachineRecipes().get(index);
            nbt.setString("CrusherRecipeKey",IndustrialCrusherRecipeHandler.recipeKey(recipe));
            net.minecraft.nbt.NBTTagList inputs=new net.minecraft.nbt.NBTTagList();
            for(ItemStack input:recipe.inputs)if(input!=null){
                net.minecraft.nbt.NBTTagCompound item=new net.minecraft.nbt.NBTTagCompound();
                input.writeToNBT(item);inputs.appendTag(item);
            }
            nbt.setTag("CrusherRecipeInputs",inputs);
        }else{
            nbt.removeTag("CrusherRecipeKey");nbt.removeTag("CrusherRecipeInputs");
        }
        conveyor.write(nbt);
    }
    public static final int WIDTH = IndustrialCrusherGeometry.WIDTH;
    public static final int HEIGHT = IndustrialCrusherGeometry.HEIGHT;
    public static final int DEPTH = IndustrialCrusherGeometry.DEPTH;

    public static final int ELEMENT_AIR = 0;
    public static final int ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_FLEXIBLE_BASE = 2;
    public static final int ELEMENT_CASING = 3;
    public static final int ELEMENT_OUTPUT = 4;
    public static final int ELEMENT_RIB = 5;

    @Override public String getInventoryName() {
        return StatCollector.translateToLocal("tile.industrial_crusher.name");
    }

    @Override protected List<ChemicalPlantRecipe> getMachineRecipes() {
        return IndustrialCrusherRecipeHandler.INSTANCE.getRecipeList();
    }

    @Override protected String getPanelId() { return "industrial_crusher"; }
    @Override protected String getTitleKey() { return "tile.industrial_crusher.name"; }
    @Override protected ItemStack getControllerDisplayStack() {
        return new ItemStack(IndustrialCrusherContent.controller, 1, 0);
    }
    @Override protected String getStructureTooltipKey() {
        return "ui.htgt6.industrial_crusher.nei_structure";
    }
    @Override protected String getRecipeOverlayId() {
        return com.mod.htgt6.client.nei.IndustrialCrusherNEIHandler.OVERLAY_ID;
    }
    @Override protected String getRecipeTooltipKey() {
        return "ui.htgt6.industrial_crusher.nei_recipes";
    }
    @Override protected String getUiKey(String suffix) {
        return "ui.htgt6.industrial_crusher." + suffix;
    }
    @Override protected Block resolveGatewayCasingBlock() { return IndustrialCrusherContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-3, 3, 0, 5, 0, 15,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        IMultiblockGateway gateway = context.getGateway();
                        switch (element) {
                            case ELEMENT_CONTROLLER:
                                return result(context.block == IndustrialCrusherContent.controller, "controller");
                            case ELEMENT_FLEXIBLE_BASE:
                                return result(context.block == IndustrialCrusherContent.casing || gateway != null,
                                        gateway == null ? "crusher_casing"
                                                : "gateway." + gateway.getGatewayType().serializedName);
                            case ELEMENT_CASING:
                                return result(context.block == IndustrialCrusherContent.casing, "crusher_casing");
                            case ELEMENT_RIB:
                                return result(context.block == IndustrialCrusherContent.rib, "crusher_rib");
                            case ELEMENT_OUTPUT:
                                return result(gateway != null && gateway.getGatewayType() == GatewayType.ITEM_OUTPUT, "rear_output");
                            default:
                                return result(context.isAir() || context.tileEntity instanceof TileCrusherCollision
                                        && ((TileCrusherCollision) context.tileEntity).belongsTo(MultiTileIndustrialCrusher.this), "air");
                        }
                    }
                })
                .gateway(GatewayType.FLUID_INPUT, 0, 10)
                .gateway(GatewayType.FLUID_OUTPUT, 0, 10)
                .gateway(GatewayType.ITEM_INPUT, 1, 10)
                .gateway(GatewayType.ITEM_OUTPUT, 1, 10)
                .gateway(GatewayType.ENERGY_INPUT, 1, 1)
                .optionalGateway(GatewayType.ENERGY_OUTPUT)
                .build();
    }

    /** Shared by world validation, hologram construction and the NEI layer renderer. */
    public static int getRequiredElement(int u, int y, int v) {
        return IndustrialCrusherGeometry.element(u, y, v);
    }

    private static MultiblockStructureDefinition.Match result(boolean valid, String key) {
        return valid ? MultiblockStructureDefinition.Match.valid(key)
                : MultiblockStructureDefinition.Match.invalid();
    }

    @Override protected void onStructureFormed(boolean newlyFormed, MultiblockStructureDefinition.Result result) {
        updateMechanicalCollision(true);
    }
    @Override protected void onStructureInvalidated(boolean wasFormed, MultiblockStructureDefinition.Result result) {
        updateMechanicalCollision(false);
    }

    /** Only owns its helper cells; never removes casing, gateways or player blocks. */
    public void updateMechanicalCollision(boolean install) {
        if (worldObj == null || worldObj.isRemote) return;
        int facing = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        for (int u = -3; u <= 3; u++) for (int y = 1; y < HEIGHT; y++) for (int v = 0; v < DEPTH; v++) {
            if (getRequiredElement(u, y, v) != ELEMENT_AIR) continue;
            int[] p = localToWorld(u, y, v);
            if (!worldObj.blockExists(p[0], p[1], p[2])) continue;
            net.minecraft.tileentity.TileEntity tile = worldObj.getTileEntity(p[0], p[1], p[2]);
            if (tile instanceof TileCrusherCollision && ((TileCrusherCollision) tile).belongsTo(this)) {
                if (!install) worldObj.setBlockToAir(p[0], p[1], p[2]);
                continue;
            }
            if (!install || tile != null || !worldObj.isAirBlock(p[0], p[1], p[2])
                    || IndustrialCrusherCollision.cell(u, y, v, facing).isEmpty()) continue;
            if (worldObj.setBlock(p[0], p[1], p[2], IndustrialCrusherContent.collision, 0, 2)) {
                tile = worldObj.getTileEntity(p[0], p[1], p[2]);
                if (tile instanceof TileCrusherCollision) ((TileCrusherCollision) tile).bind(this, u, y, v);
            }
        }
    }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        int[] a = localToWorld(-3, 0, 0), b = localToWorld(3, 5, 15);
        return AxisAlignedBB.getBoundingBox(Math.min(a[0], b[0]), yCoord, Math.min(a[2], b[2]),
                Math.max(a[0], b[0]) + 1, yCoord + HEIGHT, Math.max(a[2], b[2]) + 1);
    }

    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }
}
