package com.mod.htgt6.common.turbine;

import com.mod.htgt6.api.hitech6.multiblock.*;
import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import com.mod.htgt6.common.GTHooks.TurbineRotorHooks.*;
import com.mod.htgt6.common.multiblock.glass.ObservationGlassContent;
import com.mod.htgt6.common.ui.MachineUiTheme;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import gregapi.data.FL;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import java.util.*;

/** Steam -> rotor work -> inherited-tier energy output. No recipe-consumer controller inheritance. */
public final class MultiTileSteamTurbine extends MultiTileGeneratorBase implements IGuiHolder {
    private final Map<String,FluidTank> tanks=new LinkedHashMap<String,FluidTank>();
    private long reserve;
    private int condensateRemainder,wearTicks;
    private boolean active,validated;
    private long production,steamFlow;
    private String status="structure";
    private final IItemHandlerModifiable slots=new RotorInventory();
    public MultiTileSteamTurbine(){super(3);}
    @Override public int getModuleSlot(){return 1;}
    @Override public int getTransformerSlot(){return 2;}
    @Override public int getInventoryStackLimit(){return 1;}
    @Override public String getInventoryName(){return StatCollector.translateToLocal("tile.steam_turbine.name");}
    public boolean isActive(){return active;}
    public TurbineRotorStats rotor(){return GT6TurbineRotorHooks.stats(inventory[0]);}
    @Override protected Block resolveGatewayCasingBlock(){return SteamTurbineContent.casing;}
    @Override protected ic2.api.energy.tile.IEnergySink resolveEnergyGateway(GatewayType type){return null;}

    @Override public void updateEntity(){
        if(worldObj==null)return;
        if(worldObj.isRemote){super.updateEntity();return;}
        if(!validated){checkStructure();validated=true;}
        boolean wasActive=active;
        active=false;production=steamFlow=0;
        status=enabled?"structure":"disabled";
        super.updateEntity();
        if(wasActive!=active || worldObj.getTotalWorldTime()%20==0)markControllerDirtyAndSync();
    }

    @Override protected void processMachineLogic(){
        if(!structureFormed || !getStructureDefinition().areChunksLoaded(this))return;
        TurbineRotorStats rotor=rotor();
        if(rotor==null || GT6TurbineRotorHooks.ITEM.getRemainingDurability(inventory[0])<=0){status="rotor";return;}
        long budget=Math.min(getMaxInputVoltage(),Math.max(0,maxEnergy-energy));
        if(budget<=0){status="buffer";return;}
        List<FluidTank> input=connectedTanks(GatewayType.FLUID_INPUT),output=connectedTanks(GatewayType.FLUID_OUTPUT);
        FluidStack water=FL.DistW.make(1);
        if(water==null){status="water_missing";return;}
        long available=0,room=0;
        for(FluidTank tank:input)if(isSteam(tank.getFluid()))available+=tank.getFluidAmount();
        for(FluidTank tank:output)if(tank.getFluid()==null || tank.getFluid().isFluidEqual(water))
            room+=Math.max(0,tank.getCapacity()-tank.getFluidAmount());
        long waterLimited=Math.max(0,room*SteamTurbineBalance.STEAM_PER_WATER+199-condensateRemainder);
        long microEu=Math.max(1,Math.round(rotor.baseEfficiency*500000D));
        SteamTurbineBalance.Step step=SteamTurbineBalance.plan(reserve,
                (int)Math.min(Integer.MAX_VALUE,Math.min(available,waterLimited)),
                rotor.optimalFlow(TurbineRotorStats.Fuel.STEAM),microEu,budget);
        int remaining=step.steam;
        for(FluidTank tank:input){
            if(remaining==0)break;
            if(!isSteam(tank.getFluid()))continue;
            FluidStack drained=tank.drain(remaining,true);
            if(drained!=null)remaining-=drained.amount;
        }
        long used=(long)condensateRemainder+step.steam;
        int waterAmount=(int)(used/SteamTurbineBalance.STEAM_PER_WATER);
        condensateRemainder=(int)(used%SteamTurbineBalance.STEAM_PER_WATER);
        for(FluidTank tank:output){
            if(waterAmount==0)break;
            if(tank.getFluidAmount()>=tank.getCapacity())continue;
            water.amount=waterAmount;waterAmount-=tank.fill(water,true);
        }
        reserve=step.reserve;energy+=step.generated;production=step.generated;steamFlow=step.steam;
        active=step.generated>0;
        status=active?"running":waterLimited<=0?"condensate":available<=0?"steam":"waiting";
        if(active && ++wearTicks>=20){
            wearTicks=0;
            if(GT6TurbineRotorHooks.damage(inventory[0],1)){inventory[0]=null;active=false;markControllerDirtyAndSync();}
        }
        if(step.steam>0 || step.generated>0)markDirty();
    }

    public static boolean isSteam(FluidStack fluid){return fluid!=null && (FL.Steam.is(fluid)||FL.Steam_IC2.is(fluid));}
    private static String tankKey(GatewayType type,int x,int y,int z,int tank){return type.name()+":"+x+":"+y+":"+z+":"+tank;}
    private FluidTank tank(GatewayType type,int x,int y,int z,int slot){
        String key=tankKey(type,x,y,z,slot);
        FluidTank tank=tanks.get(key);
        int capacity=(int)Math.min(Integer.MAX_VALUE,getGatewayFluidCapacity(type));
        if(tank==null){tank=new FluidTank(capacity);tanks.put(key,tank);}
        tank.setCapacity(capacity); // Preserve contents when a controller is downgraded; fills wait for space.
        return tank;
    }
    private List<FluidTank> connectedTanks(GatewayType type){
        List<FluidTank> result=new ArrayList<FluidTank>();
        for(IMultiblockGateway gateway:getStructureGateways()) if(gateway.getGatewayType()==type && gateway instanceof TileEntity){
            TileEntity tile=(TileEntity)gateway;
            for(int i=0;i<gateway.getFluidTankCount();i++)result.add(tank(type,tile.xCoord,tile.yCoord,tile.zCoord,i));
        }
        return result;
    }
    @Override protected IFluidHandler resolveFluidGateway(GatewayType type,int x,int y,int z){
        if(type!=GatewayType.FLUID_INPUT && type!=GatewayType.FLUID_OUTPUT)return null;
        int count=getStructureFluidTankRange(type,x,y,z)[1];
        return count<=0?null:new Port(type,x,y,z,0,count);
    }
    @Override public IFluidHandler getGatewayFluidTankHandler(GatewayType type,int x,int y,int z,int slot){
        if((type!=GatewayType.FLUID_INPUT && type!=GatewayType.FLUID_OUTPUT)||slot<0||slot>=getStructureFluidTankRange(type,x,y,z)[1])return null;
        return new Port(type,x,y,z,slot,1);
    }
    private final class Port implements IFluidHandler {
        private final GatewayType type;private final int x,y,z,start,count;
        Port(GatewayType type,int x,int y,int z,int start,int count){this.type=type;this.x=x;this.y=y;this.z=z;this.start=start;this.count=count;}
        @Override public int fill(ForgeDirection side,FluidStack fluid,boolean commit){
            if(type!=GatewayType.FLUID_INPUT||!isSteam(fluid)||fluid.amount<=0)return 0;
            int remaining=fluid.amount;
            for(int i=0;i<count && remaining>0;i++){
                FluidStack part=fluid.copy();part.amount=remaining;
                FluidTank target=tank(type,x,y,z,start+i);
                if(target.getFluidAmount()>=target.getCapacity())continue;
                remaining-=target.fill(part,commit);
            }
            if(commit && remaining<fluid.amount)markDirty();return fluid.amount-remaining;
        }
        @Override public FluidStack drain(ForgeDirection side,FluidStack request,boolean commit){
            if(request==null||request.amount<=0)return null;
            FluidStack result=null;int remaining=request.amount;
            for(int i=0;i<count && remaining>0;i++){
                FluidTank tank=tank(type,x,y,z,start+i);
                if(!request.isFluidEqual(tank.getFluid()))continue;
                FluidStack part=tank.drain(remaining,commit);
                if(part==null)continue;
                remaining-=part.amount;if(result==null)result=part;else result.amount+=part.amount;
            }
            if(commit && result!=null)markDirty();return result;
        }
        @Override public FluidStack drain(ForgeDirection side,int amount,boolean commit){
            if(amount<=0)return null;
            for(int i=0;i<count;i++){
                FluidStack fluid=tank(type,x,y,z,start+i).getFluid();
                if(fluid!=null){FluidStack request=fluid.copy();request.amount=amount;return drain(side,request,commit);}
            }
            return null;
        }
        @Override public boolean canFill(ForgeDirection side,Fluid fluid){return type==GatewayType.FLUID_INPUT && fluid!=null && isSteam(new FluidStack(fluid,1));}
        @Override public boolean canDrain(ForgeDirection side,Fluid fluid){return true;}
        @Override public FluidTankInfo[] getTankInfo(ForgeDirection side){
            FluidTankInfo[] info=new FluidTankInfo[count];for(int i=0;i<count;i++)info[i]=tank(type,x,y,z,start+i).getInfo();return info;
        }
    }

    @Override protected MultiblockStructureDefinition createStructureDefinition(){
        return MultiblockStructureDefinition.builder(-3,3,-3,3,0,12,context->{
            int element=SteamTurbineGeometry.element(context.u,context.y,context.v);
            IMultiblockGateway gateway=context.getGateway();
            boolean valid;
            switch(element){
                case SteamTurbineGeometry.CONTROLLER:valid=context.block==SteamTurbineContent.controller;break;
                case SteamTurbineGeometry.BASE:valid=context.block==SteamTurbineContent.casing || gateway!=null;break;
                case SteamTurbineGeometry.CASING:valid=context.block==SteamTurbineContent.casing;break;
                case SteamTurbineGeometry.RIB:valid=context.block==SteamTurbineContent.rib;break;
                case SteamTurbineGeometry.GLASS:valid=context.block==ObservationGlassContent.glass;break;
                default:valid=context.isAir();
            }
            return valid?MultiblockStructureDefinition.Match.valid("steam_turbine."+element):MultiblockStructureDefinition.Match.invalid();
        }).gateway(GatewayType.FLUID_INPUT,1,10).gateway(GatewayType.FLUID_OUTPUT,1,10)
                .gateway(GatewayType.ENERGY_OUTPUT,1,1).gateway(GatewayType.ENERGY_INPUT,0,0)
                .gateway(GatewayType.ITEM_INPUT,0,0).gateway(GatewayType.ITEM_OUTPUT,0,0).build();
    }
    public void detachGateways(){for(IMultiblockGateway gateway:getStructureGateways())gateway.unlink();structureFormed=false;active=false;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){super.setInventorySlotContents(slot,stack);markControllerDirtyAndSync();}
    @Override public boolean isItemValidForSlot(int slot,ItemStack stack){
        if(stack==null)return false;
        return slot==0?GT6TurbineRotorHooks.stats(stack)!=null && GT6TurbineRotorHooks.ITEM.getRemainingDurability(stack)>0
                :slot==1?MachineTierSystem.moduleTier(stack.getItem())>0:slot==2&&MachineTierSystem.transformerMaxTier(stack)>0;
    }
    @Override public AxisAlignedBB getRenderBoundingBox(){return AxisAlignedBB.getBoundingBox(xCoord-14,yCoord-4,zCoord-14,xCoord+15,yCoord+5,zCoord+15);}
    @Override public double getMaxRenderDistanceSquared(){return 65536;}
    @Override public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);reserve=Math.max(0,Math.min(100000000000000000L,nbt.getLong("SteamReserve")));
        condensateRemainder=Math.max(0,Math.min(199,nbt.getInteger("SteamRemainder")));
        wearTicks=Math.max(0,Math.min(19,nbt.getInteger("RotorWearTicks")));
        active=nbt.getBoolean("SteamActive");production=nbt.getLong("SteamProduction");steamFlow=nbt.getLong("SteamFlow");status=nbt.getString("SteamStatus");
        tanks.clear();NBTTagList list=nbt.getTagList("SteamTanks",10);
        for(int i=0;i<list.tagCount();i++){NBTTagCompound entry=list.getCompoundTagAt(i);FluidTank tank=new FluidTank(Integer.MAX_VALUE);tank.readFromNBT(entry);tanks.put(entry.getString("Key"),tank);}
        if(worldObj==null||!worldObj.isRemote){validated=false;structureFormed=false;active=false;}
    }
    @Override public void writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt);nbt.setLong("SteamReserve",reserve);nbt.setInteger("SteamRemainder",condensateRemainder);nbt.setInteger("RotorWearTicks",wearTicks);
        nbt.setBoolean("SteamActive",active);nbt.setLong("SteamProduction",production);nbt.setLong("SteamFlow",steamFlow);nbt.setString("SteamStatus",status);
        NBTTagList list=new NBTTagList();for(Map.Entry<String,FluidTank> entry:tanks.entrySet()){
            if(entry.getValue().getFluidAmount()<=0)continue;
            NBTTagCompound tag=new NBTTagCompound();entry.getValue().writeToNBT(tag);tag.setString("Key",entry.getKey());list.appendTag(tag);
        }nbt.setTag("SteamTanks",list);
    }

    @Override public ModularScreen createScreen(GuiCreationContext context,ModularPanel panel){return new ModularScreen(panel);}
    @Override public ModularPanel buildUI(GuiCreationContext context,GuiSyncManager sync,boolean client){
        sync.registerSlotGroup("turbine",3);
        sync.syncValue("status",SyncHandlers.string(()->status,v->status=v));
        sync.syncValue("formed",SyncHandlers.bool(()->structureFormed,v->structureFormed=v));
        sync.syncValue("enabled",SyncHandlers.bool(()->enabled,v->enabled=v));
        sync.syncValue("tier",SyncHandlers.intNumber(()->machineTier,v->machineTier=v));
        sync.syncValue("energy",SyncHandlers.longNumber(()->energy,v->energy=v));
        sync.syncValue("production",SyncHandlers.longNumber(()->production,v->production=v));
        sync.syncValue("flow",SyncHandlers.longNumber(()->steamFlow,v->steamFlow=v));
        sync.syncValue("power",new InteractionSyncHandler().setOnMouseTapped(data->{enabled=!enabled;active=false;markControllerDirtyAndSync();}));
        sync.syncValue("check",new InteractionSyncHandler().setOnMouseTapped(data->{checkStructure();markControllerDirtyAndSync();}));
        MachineUiTheme theme=MachineUiTheme.getActive();
        ModularPanel panel=ModularPanel.defaultPanel("steam_turbine",260,240).background(new Rectangle().setColor(theme.getFrameColor()));
        Widget body=new Widget().background(new Rectangle().setColor(theme.getPanelColor()));
        body.pos(2,2).size(256,236);panel.child(body);
        panel.child(new TextWidget(IKey.lang("tile.steam_turbine.name")).pos(10,8).color(theme.getTitleColor()));
        Widget terminal=new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        terminal.pos(8,26).size(244,72);panel.child(terminal);
        panel.child(new TextWidget(IKey.dynamic(()->StatCollector.translateToLocal(structureFormed?"ui.htgt6.structure.complete":"ui.htgt6.structure.incomplete"))).pos(14,32).size(232,12).color(theme.getPrimaryColor()));
        panel.child(new TextWidget(IKey.dynamic(()->StatCollector.translateToLocal("ui.htgt6.steam_turbine."+status))).pos(14,50).size(232,12).color(theme.getLabelColor()));
        panel.child(new TextWidget(IKey.dynamic(()->production+" EU/t | "+steamFlow+" L/t")).pos(14,73).color(theme.getEnergyColor()));
        for(int i=0;i<3;i++)panel.child(new ItemSlot().slot(SyncHandlers.itemSlot(slots,i).slotGroup("turbine")).pos(18+i*28,108));
        panel.child(new TextWidget(IKey.lang("ui.htgt6.steam_turbine.rotor_slot")).pos(13,130).color(theme.getLabelColor()));
        final UITexture powerOn=UITexture.fullImage("htgt6","textures/gui/overlays/gt5_power_switch_on.png");
        final UITexture powerOff=UITexture.fullImage("htgt6","textures/gui/overlays/gt5_power_switch_off.png");
        panel.child(new ButtonWidget<>().size(22,22).pos(108,106).background(GuiTextures.BUTTON)
                .overlay(new com.cleanroommc.modularui.api.drawable.IDrawable(){
                    @Override public void draw(com.cleanroommc.modularui.screen.viewport.GuiContext context,int x,int y,int w,int h){
                        (enabled?powerOn:powerOff).draw(context,x,y,w,h);
                    }
                }).syncHandler("power"));
        panel.child(new ButtonWidget<>().size(22,22).pos(134,106).background(GuiTextures.BUTTON)
                .overlay(UITexture.fullImage("htgt6","textures/gui/overlays/gt5_structure_update.png")).syncHandler("check"));
        final ParentWidget<?> energyPanel=new ParentWidget<>();
        energyPanel.pos(260,26).size(186,92).background(new Rectangle().setColor(theme.getTerminalColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(()->tierName()+" | "+getMaxInputVoltage()+" EU/t")).pos(7,8).color(theme.getEnergyColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(()->"EU: "+energy)).pos(7,25).color(theme.getLabelColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(()->rotor()==null?"-":String.format(Locale.US,"Rotor: %.1f%%",rotor().baseEfficiency*100))).pos(7,43).color(theme.getLabelColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(()->rotor()==null?"-":"Max flow: "+rotor().optimalFlow(TurbineRotorStats.Fuel.STEAM)+" L/t")).pos(7,61).color(theme.getLabelColor()));
        energyPanel.setEnabled(false);panel.child(energyPanel);
        panel.child(new ButtonWidget<>().size(22,22).pos(160,106).background(GuiTextures.BUTTON)
                .overlay(UITexture.fullImage("htgt6","textures/gui/overlays/gt5_power_panel.png"))
                .onMouseTapped(mouse->{energyPanel.setEnabled(!energyPanel.isEnabled());return true;}));
        if(client)panel.child(com.mod.htgt6.client.GUI.MiningUnitModularUiButtons.createStructureButton(new ItemStack(SteamTurbineContent.controller),"ui.htgt6.steam_turbine.structure_button").pos(210,7));
        panel.child(SlotGroupWidget.playerInventory().pos(44,155));return panel;
    }
    private String tierName(){String[] names={"LV","MV","HV","EV","IV","LuV","ZPM","UV","PUV","UX","OLV","OMV","OHV","OEV","OIV","MAX"};return names[Math.max(0,Math.min(15,machineTier-1))];}
    private final class RotorInventory implements IItemHandlerModifiable {
        @Override public int getSlots(){return 3;}
        @Override public ItemStack getStackInSlot(int slot){return inventory[slot];}
        @Override public int getSlotLimit(int slot){return 1;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return isItemValidForSlot(slot,stack);}
        @Override public void setStackInSlot(int slot,ItemStack stack){setInventorySlotContents(slot,stack);}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
            if(stack==null||!isItemValid(slot,stack)||inventory[slot]!=null)return stack;
            ItemStack one=stack.copy();one.stackSize=1;if(!simulate)setInventorySlotContents(slot,one);
            if(stack.stackSize==1)return null;ItemStack rest=stack.copy();rest.stackSize--;return rest;
        }
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){
            if(amount<=0||inventory[slot]==null)return null;
            ItemStack result=inventory[slot].copy();if(!simulate)setInventorySlotContents(slot,null);return result;
        }
    }
}
