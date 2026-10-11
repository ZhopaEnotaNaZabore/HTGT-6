package com.mod.htgt6.client.inventory112;

import com.mod.htgt6.minecraft1202.Module.Network.Inventory.PacketTutorialStep112;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipeBookSystem112;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;

/** Client event adapter for the complete Minecraft 1.12 tutorial-step state machine. */
public final class TutorialSystem112 {
    private static final int MOVEMENT = 0, FIND_TREE = 1, PUNCH_TREE = 2;
    private static final int OPEN_INVENTORY = 3, CRAFT_PLANKS = 4, NONE = 5;
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private static int stage = -1;
    private static int moveTicks, lookTicks;
    private static float lastYaw, lastPitch, previousDamage;
    private static int breakingX, breakingY, breakingZ;
    private static boolean positionKnown, moveComplete, lookComplete;
    private static boolean movementShown, lookShown, stepShown;
    private static boolean baselineInitialized;
    private static int baselineLogs, baselinePlanks;

    static void setStage(int newStage) {
        stage = Math.max(MOVEMENT, Math.min(NONE, newStage));
        resetStep();
    }

    static void resetForSession() {
        stage = -1;
        resetStep();
    }

    static boolean isActive() { return stage >= MOVEMENT && stage < NONE; }

    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || MINECRAFT.isGamePaused()
                || MINECRAFT.thePlayer == null || MINECRAFT.theWorld == null || stage < 0) return;
        switch (stage) {
            case MOVEMENT: tickMovement(); break;
            case FIND_TREE: tickFindTree(); break;
            case PUNCH_TREE: tickPunchTree(); break;
            case OPEN_INVENTORY: tickOpenInventory(); break;
            case CRAFT_PLANKS: tickCraftPlanks(); break;
            default: break;
        }
    }

    private static void tickMovement() {
        GameSettings settings = MINECRAFT.gameSettings;
        if (!moveComplete) {
            if (!movementShown) {
                movementShown = true;
                RecipeToast112.showTutorial("movement", 0, 0,
                        tr("tutorial.move.title", key(settings.keyBindForward), key(settings.keyBindLeft),
                                key(settings.keyBindBack), key(settings.keyBindRight)),
                        tr("tutorial.move.description", key(settings.keyBindJump)), true);
            }
            if (down(settings.keyBindForward) || down(settings.keyBindBack)
                    || down(settings.keyBindLeft) || down(settings.keyBindRight) || down(settings.keyBindJump))
                moveTicks++;
            RecipeToast112.setTutorialProgress("movement", moveTicks / 40.0F);
            if (moveTicks > 40) {
                moveComplete = true;
                RecipeToast112.hideTutorial("movement");
                positionKnown = false;
            }
            return;
        }
        if (!lookShown) {
            lookShown = true;
            RecipeToast112.showTutorial("look", 1, 0, tr("tutorial.look.title"),
                    tr("tutorial.look.description"), true);
        }
        float yaw = MINECRAFT.thePlayer.rotationYaw;
        float pitch = MINECRAFT.thePlayer.rotationPitch;
        if (positionKnown && (Math.abs(yaw - lastYaw) > 0.01F || Math.abs(pitch - lastPitch) > 0.01F)) lookTicks++;
        lastYaw = yaw;
        lastPitch = pitch;
        positionKnown = true;
        RecipeToast112.setTutorialProgress("look", lookTicks / 40.0F);
        if (lookTicks > 40) {
            lookComplete = true;
            advance(isSurvival() ? FIND_TREE : NONE);
        }
    }

    private static void tickFindTree() {
        if (!isSurvival()) { advance(NONE); return; }
        if (!stepShown) {
            stepShown = true;
            RecipeToast112.showTutorial("find_tree", 2, 0, tr("tutorial.find_tree.title"),
                    tr("tutorial.find_tree.description"), false);
        }
        if (!baselineInitialized) {
            baselineLogs = countInventoryWood(false);
            baselineInitialized = true;
        }
        if (countInventoryWood(false) > baselineLogs) {
            advance(PUNCH_TREE);
            return;
        }
        if (targetIsTree(true)) {
            advance(PUNCH_TREE);
            return;
        }
    }

    private static void tickPunchTree() {
        if (!isSurvival()) { advance(NONE); return; }
        if (!stepShown) {
            stepShown = true;
            RecipeToast112.showTutorial("punch_tree", 2, 0, tr("tutorial.punch_tree.title"),
                    tr("tutorial.punch_tree.description", key(MINECRAFT.gameSettings.keyBindAttack)), true);
        }
        if (!baselineInitialized) {
            baselineLogs = countInventoryWood(false);
            baselineInitialized = true;
        }
        if (countInventoryWood(false) > baselineLogs) {
            advance(OPEN_INVENTORY);
            return;
        }
        if (positionKnown && previousDamage > 0.0F
                && MINECRAFT.theWorld.isAirBlock(breakingX, breakingY, breakingZ)) {
            advance(OPEN_INVENTORY);
            return;
        }
        boolean treeTarget = targetIsTree(false);
        boolean attackDown = down(MINECRAFT.gameSettings.keyBindAttack);
        float damage = treeTarget && attackDown ? currentBlockDamage() : 0.0F;
        if (treeTarget && damage > 0.0F && MINECRAFT.objectMouseOver != null) {
            breakingX = MINECRAFT.objectMouseOver.blockX;
            breakingY = MINECRAFT.objectMouseOver.blockY;
            breakingZ = MINECRAFT.objectMouseOver.blockZ;
            positionKnown = true;
        }
        previousDamage = damage;
        if (damage >= 0.99F) { advance(OPEN_INVENTORY); return; }
        if (stepShown) RecipeToast112.setTutorialProgress("punch_tree", damage);
    }

    private static void tickOpenInventory() {
        if (!isSurvival()) { advance(NONE); return; }
        if (!stepShown) {
            stepShown = true;
            RecipeToast112.showTutorial("open_inventory", 0, 1,
                    tr("tutorial.open_inventory.title"),
                    tr("tutorial.open_inventory.description", key(MINECRAFT.gameSettings.keyBindInventory)), false);
        }
        if (MINECRAFT.currentScreen instanceof GuiInventory) advance(CRAFT_PLANKS);
    }

    private static void tickCraftPlanks() {
        if (!isSurvival()) { advance(NONE); return; }
        if (!stepShown) {
            stepShown = true;
            RecipeToast112.showTutorial("craft_planks", 1, 1,
                    tr("tutorial.craft_planks.title"), tr("tutorial.craft_planks.description"), false);
        }
        if (!baselineInitialized) {
            baselinePlanks = countInventoryWood(true);
            baselineInitialized = true;
        }
        if (countInventoryWood(true) > baselinePlanks) advance(NONE);
    }

    private static void advance(int next) {
        if (next == stage) return;
        stage = next;
        RecipeBookSystem112.NETWORK.sendToServer(new PacketTutorialStep112(next));
        resetStep();
    }

    private static void resetStep() {
        RecipeToast112.clearTutorials();
        moveTicks = lookTicks = 0;
        previousDamage = 0.0F;
        positionKnown = moveComplete = lookComplete = false;
        movementShown = lookShown = stepShown = false;
        baselineInitialized = false;
        baselineLogs = baselinePlanks = 0;
    }

    private static boolean isSurvival() {
        return !MINECRAFT.thePlayer.capabilities.isCreativeMode && MINECRAFT.thePlayer.capabilities.allowEdit;
    }

    private static boolean down(KeyBinding binding) { return binding.getIsKeyPressed(); }

    private static boolean targetIsTree(boolean leaves) {
        MovingObjectPosition hit = MINECRAFT.objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return false;
        Block actual = MINECRAFT.theWorld.getBlock(hit.blockX, hit.blockY, hit.blockZ);
        if(actual.isWood(MINECRAFT.theWorld,hit.blockX,hit.blockY,hit.blockZ)
                ||leaves&&actual.isLeaves(MINECRAFT.theWorld,hit.blockX,hit.blockY,hit.blockZ))return true;
        Item item=Item.getItemFromBlock(actual);
        return item!=null&&TutorialWood112.matches(new ItemStack(item,1,
                actual.damageDropped(MINECRAFT.theWorld.getBlockMetadata(hit.blockX,hit.blockY,hit.blockZ))),false);
    }

    private static int countInventoryWood(boolean planks) {
        int count = 0;
        for (ItemStack stack : MINECRAFT.thePlayer.inventory.mainInventory) {
            if (stack == null) continue;
            if(TutorialWood112.matches(stack,planks))count+=stack.stackSize;
        }
        ItemStack offhand=com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks.getOffhand(MINECRAFT.thePlayer);
        if(TutorialWood112.matches(offhand,planks))count+=offhand.stackSize;
        return count;
    }

    private static float currentBlockDamage() {
        try {
            Float value = ReflectionHelper.getPrivateValue(PlayerControllerMP.class,
                    MINECRAFT.playerController, "curBlockDamageMP", "field_78770_f");
            return value == null ? 0.0F : value.floatValue();
        } catch (RuntimeException ignored) {
            return 0.0F;
        }
    }

    private static String key(KeyBinding binding) {
        return EnumChatFormatting.BOLD + GameSettings.getKeyDisplayString(binding.getKeyCode())
                + EnumChatFormatting.RESET;
    }

    private static String tr(String key, Object... arguments) {
        return StatCollector.translateToLocalFormatted(key, arguments);
    }

    public TutorialSystem112() { }
}
