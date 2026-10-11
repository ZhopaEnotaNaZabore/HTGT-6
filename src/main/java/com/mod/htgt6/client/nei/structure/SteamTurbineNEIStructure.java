package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.common.turbine.*;
import com.mod.htgt6.common.mining.MiningUnitContent;
import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.multiblock.glass.ObservationGlassContent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import java.util.*;

public final class SteamTurbineNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks=new StructureBlock[7][7][13];
    private final List<Component> components=new ArrayList<Component>();
    public SteamTurbineNEIStructure(){
        Map<Block,Integer> counts=new LinkedHashMap<Block,Integer>();
        for(int x=0;x<7;x++)for(int y=0;y<7;y++)for(int z=0;z<13;z++){
            int element=SteamTurbineGeometry.element(x-3,y-3,z);
            Block block=element==1?SteamTurbineContent.controller:element==2||element==3?SteamTurbineContent.casing:element==4?SteamTurbineContent.rib:element==5?ObservationGlassContent.glass:null;
            if(block!=null)blocks[x][y][z]=new StructureBlock(block,element==1?2:0);
        }
        blocks[0][0][2]=new StructureBlock(MiningUnitContent.port,0);
        blocks[0][0][10]=new StructureBlock(MiningUnitContent.port,1);
        blocks[3][0][12]=new StructureBlock(MiningUnitContent.port,9);
        for(int x=0;x<7;x++)for(int y=0;y<7;y++)for(int z=0;z<13;z++){
            StructureBlock cell=blocks[x][y][z];if(cell==null||cell.block==MiningUnitContent.port||cell.block==SteamTurbineContent.controller)continue;
            counts.put(cell.block,counts.containsKey(cell.block)?counts.get(cell.block)+1:1);
        }
        for(Map.Entry<Block,Integer> entry:counts.entrySet())components.add(new Component(new ItemStack(entry.getKey()),entry.getValue(),entry.getKey().getUnlocalizedName()+".name"));
        components.add(new Component(new ItemStack(MiningUnitContent.port,1,0),1,"tile.mining_unit_port.fluid_input.name"));
        components.add(new Component(new ItemStack(MiningUnitContent.port,1,1),1,"tile.mining_unit_port.fluid_output.name"));
        components.add(new Component(new ItemStack(MiningUnitContent.port,1,9),1,"tile.mining_unit_port.energy_output.name"));
    }
    @Override public String getName(){return net.minecraft.util.StatCollector.translateToLocal("tile.steam_turbine.name");}
    @Override public ItemStack getController(){return new ItemStack(SteamTurbineContent.controller);}
    @Override public int getWidth(){return 7;}
    @Override public int getHeight(){return 7;}
    @Override public int getDepth(){return 13;}
    @Override public int getControllerX(){return 3;}
    @Override public int getControllerY(){return 3;}
    @Override public int getControllerZ(){return 0;}
    @Override public StructureBlock getBlock(int x,int y,int z){return blocks[x][y][z];}
    @Override public List<Component> getComponents(){return Collections.unmodifiableList(components);}
}
