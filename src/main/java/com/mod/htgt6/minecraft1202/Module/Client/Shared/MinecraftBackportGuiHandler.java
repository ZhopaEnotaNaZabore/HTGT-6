package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.client.GUI.GuiVillageWorkstation120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ContainerVillageWorkstation120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageWorkstationType120;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** GUI routes belonging exclusively to the Minecraft_1.20.2 mod container. */
public final class MinecraftBackportGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if(id==com.mod.htgt6.minecraft1202.Module.Network.Frostburn.StructureNetwork120.GUI_ID) {
            net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
            if(!(tile instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120))return null;
            com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn.ContainerStructure120 container=new com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn.ContainerStructure120((com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120)tile);
            return container.canInteractWith(player)?container:null;
        }
        if(id==111) {
            net.minecraft.entity.Entity entity=world.getEntityByID(x);
            if(!(entity instanceof com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112))return null;
            com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112 llama=(com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112)entity;
            com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120 container=new com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120(player,llama,llama.getStorageColumns());
            return container.canInteractWith(player)?container:null;
        }
        if(id==36){net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
            return tile instanceof com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageFurnace120
                ?new net.minecraft.inventory.ContainerFurnace(player.inventory,(net.minecraft.tileentity.TileEntityFurnace)tile):null;}
        VillageWorkstationType120 workstation = VillageWorkstationType120.byGuiId(id);
        return workstation == null ? null
                : new ContainerVillageWorkstation120(player.inventory, workstation, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if(id==com.mod.htgt6.minecraft1202.Module.Network.Frostburn.StructureNetwork120.GUI_ID) {
            net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
            return tile instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120
                ? new com.mod.htgt6.minecraft1202.Module.Client.Frostburn.GuiStructure120(new com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn.ContainerStructure120((com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120)tile)):null;
        }
        if(id==111) {
            net.minecraft.entity.Entity entity=world.getEntityByID(x);
            if(!(entity instanceof com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112))return null;
            com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112 llama=(com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112)entity;
            com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120 container=new com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120(player,llama,y);
            return new com.mod.htgt6.minecraft1202.Module.Client.Exploration.GuiLlama120(container);
        }
        if(id==36){net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
            return tile instanceof com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageFurnace120
                ?new com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.GuiVillageFurnace120(player.inventory,(com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageFurnace120)tile):null;}
        VillageWorkstationType120 workstation = VillageWorkstationType120.byGuiId(id);
        return workstation == null ? null : new GuiVillageWorkstation120(
                new ContainerVillageWorkstation120(player.inventory, workstation, world, x, y, z));
    }
}
