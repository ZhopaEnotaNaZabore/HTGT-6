package com.mod.htgt6.orehelper.client;

import com.mod.htgt6.orehelper.*;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.ClientCommandHandler;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class OreHelperClient extends OreHelperProxy {
    private final ConcurrentLinkedQueue<NBTTagCompound> incoming = new ConcurrentLinkedQueue<NBTTagCompound>();
    public void init() {
        if (cpw.mods.fml.common.Loader.isModLoaded("NotEnoughItems")) {
            NBTTagCompound info = new NBTTagCompound();
            info.setString("handler", "hitech6.ore.worldgen");
            info.setString("modName", "HiTech6 Ore Helper");
            info.setString("modId", OreHelperMod.ID);
            info.setString("itemName", "minecraft:gold_ore");
            info.setInteger("maxRecipesPerPage", 1);
            info.setInteger("height", 90);
            cpw.mods.fml.common.event.FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", info);
        }
        FMLCommonHandler.instance().bus().register(this);
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            public String getCommandName() { return "orehelper"; }
            public String getCommandUsage(ICommandSender sender) { return "/orehelper [GT6 material ID]"; }
            public int getRequiredPermissionLevel() { return 0; }
            public void processCommand(ICommandSender sender, String[] args) {
                int id = args.length > 0 ? parseIntBounded(sender, args[0], 1, 32767) :
                        OreCatalog.material(Minecraft.getMinecraft().thePlayer.getHeldItem());
                if (id <= 0) throw new WrongUsageException(getCommandUsage(sender));
                Minecraft.getMinecraft().displayGuiScreen(new OreHelperScreen(null, id));
            }
        });
    }
    public void receive(NBTTagCompound data) { incoming.add(data); }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        NBTTagCompound n;
        while ((n = incoming.poll()) != null) {
            if (Minecraft.getMinecraft().currentScreen instanceof OreHelperScreen)
                ((OreHelperScreen) Minecraft.getMinecraft().currentScreen).receive(n);
        }
    }
}
