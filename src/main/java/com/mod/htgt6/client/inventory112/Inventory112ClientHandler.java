package com.mod.htgt6.client.inventory112;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketSwapOffhand;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketUseOffhand;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

/** 1.12 inventory screen selection and the vanilla F-key hand swap. */
public final class Inventory112ClientHandler {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final KeyBinding swap = new KeyBinding("key.htgt6.swap_offhand", Keyboard.KEY_F, "key.categories.inventory");
    private final KeyBinding saveHotbar = new KeyBinding("key.htgt6.save_hotbar", Keyboard.KEY_C, "key.categories.inventory");
    private final KeyBinding loadHotbar = new KeyBinding("key.htgt6.load_hotbar", Keyboard.KEY_X, "key.categories.inventory");
    private final boolean[] numberWasDown = new boolean[9];
    private boolean offhandUseWasDown;
    private boolean swapWasDown;
    private int swapSequence;
    private static boolean localShieldRaised;

    public Inventory112ClientHandler() {
        ClientRegistry.registerKeyBinding(swap);
        ClientRegistry.registerKeyBinding(saveHotbar);
        ClientRegistry.registerKeyBinding(loadHotbar);
    }

    @SubscribeEvent public void onConnect(cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent event) {
        RecipeBookClientState112.requestSessionReset();
    }

    @SubscribeEvent public void onDisconnect(cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        RecipeBookClientState112.requestSessionReset();
    }

    /**
     * The hand-swap binding owns its key while the player is in game.  Several
     * 1.7 mods allow duplicate bindings and then run both actions; GraviSuite
     * ships with its flight toggle on F, which raced the delayed inventory
     * transaction.  Leave the other binding configured, but consume its queued
     * press for this tick so it remains usable after the player remaps it.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void reserveSwapKey(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || minecraft.thePlayer == null
                || minecraft.currentScreen != null) return;
        int keyCode = swap.getKeyCode();
        if (keyCode == 0) return;
        for (KeyBinding binding : minecraft.gameSettings.keyBindings)
            if (binding != swap && binding.getKeyCode() == keyCode && binding.getIsKeyPressed())
                consumeBindingPress(binding);
    }

    private static void consumeBindingPress(KeyBinding binding) {
        ReflectionHelper.setPrivateValue(KeyBinding.class, binding, Boolean.FALSE,
                "pressed", "field_74513_e");
        ReflectionHelper.setPrivateValue(KeyBinding.class, binding, Integer.valueOf(0),
                "pressTime", "field_151474_i");
    }

    @SubscribeEvent public void onGuiOpen(GuiOpenEvent event) {
        if ((event.gui instanceof GuiInventory || event.gui instanceof GuiContainerCreative)
                && com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120.eligibleMount(minecraft.thePlayer)!=null) {
            com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationUpdate112.NETWORK.sendToServer(
                    new com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120());
            event.setCanceled(true);
            return;
        }
        // Defensive reset for third-party overlays which leave item tint state
        // behind when switching directly to a container screen.
        org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (event.gui != null && event.gui.getClass() == GuiInventory.class && minecraft.thePlayer != null)
            event.gui = new GuiInventory112(minecraft.thePlayer);
        else if (event.gui != null && event.gui.getClass() == GuiCrafting.class) {
            ContainerWorkbench container = workbenchContainer((GuiCrafting) event.gui);
            if (container != null) event.gui = new GuiCrafting112(container);
        }
    }

    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        RecipeBookClientState112.resetSessionIfNeeded();
        OffhandRenderer112.updateEquippedItem();
        if (minecraft.thePlayer == null) {
            OffhandRefillGuard.reset();
            localShieldRaised = false;
            offhandUseWasDown = false;
            swapWasDown = false;
            java.util.Arrays.fill(numberWasDown, false);
            return;
        }
        // 1.7 KeyBinding's global IntHashMap retains only one binding per key
        // code. After resetKeyBindingArrayAndHash a duplicate F binding may own
        // the queue, leaving swap.isPressed() permanently false. A physical
        // rising edge is deterministic for duplicate bindings and does not
        // repeat while the key is held.
        boolean swapDown=isPhysicalKeyDown(swap.getKeyCode());
        if (isSwapEdge(minecraft.currentScreen==null,swapDown,swapWasDown)) {
            int selectedSlot = minecraft.thePlayer.inventory.currentItem;
            if (selectedSlot >= 0 && selectedSlot < 9) {
                // Do not predict by mutating the client inventory. Inventory Tweaks,
                // Other inventory handlers and the vanilla container cache can observe that
                // half-finished transaction and restore/move one of its stacks before
                // the server reply arrives. PacketOffhandSync commits both slots from
                // the authoritative server state in one client-thread operation.
                OffhandRefillGuard.begin(selectedSlot);
                OffhandSystem.NETWORK.sendToServer(new PacketSwapOffhand(++swapSequence, selectedSlot));
            }
        }
        swapWasDown=swapDown;

        ItemStack offhand = OffhandHooks.getOffhand(minecraft.thePlayer);
        ItemStack main = minecraft.thePlayer.getHeldItem();
        boolean shieldAvailable = offhand != null && offhand.getItem() == CombatContent.shield
                && !OffhandSystem.isShieldDisabled(minecraft.thePlayer)
                && !OffhandSystem.mainHandConsumesUse(minecraft.thePlayer);
        boolean crossbowAvailable=com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.available(minecraft.thePlayer);
        boolean requestedUse = minecraft.currentScreen == null && (shieldAvailable||crossbowAvailable)
                && isPhysicalKeyDown(minecraft.gameSettings.keyBindUseItem.getKeyCode());
        localShieldRaised = requestedUse&&shieldAvailable;
        com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.clientTick(minecraft.thePlayer,requestedUse&&crossbowAvailable);
        if (requestedUse != offhandUseWasDown) {
            OffhandSystem.NETWORK.sendToServer(new PacketUseOffhand(requestedUse));
            offhandUseWasDown = requestedUse;
        }
        boolean creative = (minecraft.currentScreen == null || minecraft.currentScreen instanceof GuiContainerCreative)
                && minecraft.thePlayer.capabilities.isCreativeMode;
        for (int index = 0; index < 9; ++index) {
            boolean down = isPhysicalKeyDown(minecraft.gameSettings.keyBindsHotbar[index].getKeyCode());
            if (creative && down && !numberWasDown[index]) {
                if (isPhysicalKeyDown(saveHotbar.getKeyCode())) SavedHotbars112.save(index);
                else if (isPhysicalKeyDown(loadHotbar.getKeyCode())) SavedHotbars112.load(index);
            }
            numberWasDown[index] = down;
        }
    }

    public static boolean isLocalShieldRaised() { return localShieldRaised; }

    /** Reuse vanilla's 0.2 input multiplier and sprint checks, without double slowing main-hand use. */
    public static boolean isUsingItemForMovement(net.minecraft.entity.player.EntityPlayer player) {
        return player.isUsingItem() || localShieldRaised
                || com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.useTicks(
                        player,com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks.getOffhand(player))>=0;
    }

    /** Pure edge contract used by the headless swap regression fixture. */
    public static boolean isSwapEdge(boolean gameplay,boolean down,boolean wasDown){
        return gameplay&&down&&!wasDown;
    }

    private static ContainerWorkbench workbenchContainer(GuiCrafting gui) {
        try {
            Object container = ReflectionHelper.getPrivateValue(GuiContainer.class, gui,
                    "inventorySlots", "field_147002_h");
            return container instanceof ContainerWorkbench ? (ContainerWorkbench) container : null;
        } catch (RuntimeException ignored) {
            // Keep the vanilla screen if another coremod changes GuiContainer's field layout.
            return null;
        }
    }

    private static boolean isPhysicalKeyDown(int code) {
        if (code < 0) {
            int button = code + 100;
            return button >= 0 && Mouse.isButtonDown(button);
        }
        return code > 0 && Keyboard.isKeyDown(code);
    }

}
