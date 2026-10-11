package com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** Two separate actions: load on completed release, then fire the stored projectile on click. */
public final class ItemCrossbow extends Item {
    public static final int CHARGE_TICKS = 25;
    private IIcon[] pulling;
    private IIcon loadedArrow, loadedFirework;

    public ItemCrossbow() {
        setUnlocalizedName("village_pillage_crossbow");
        setTextureName("htgt6:village_pillage/crossbow_standby");
        setCreativeTab(CreativeTabs.tabCombat);
        setMaxStackSize(1);
        setMaxDamage(465);
        setFull3D();
    }

    @Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.none; }
    @Override public int getItemEnchantability() { return 1; }

    public static int quickChargeLevel(ItemStack stack) {
        return stack == null ? 0 : Math.max(0, net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
                com.mod.htgt6.asm.HTGT6Config.QUICK_CHARGE_ENCHANTMENT_ID, stack));
    }

    public static int chargeDuration(ItemStack stack) {
        // Normal levels 0..3: 25,20,15,10. Keep command-created levels safe for legacy divisions.
        return Math.max(1, CHARGE_TICKS - 5 * Math.min(5, quickChargeLevel(stack)));
    }

    public static String chargeStartSound(ItemStack stack) {
        int level = quickChargeLevel(stack);
        return level == 0 ? "htgt6:item.crossbow.loading_start"
                : "htgt6:item.crossbow.quick_charge_" + Math.min(3, level);
    }

    public static boolean isCharged(ItemStack stack) {
        return stack != null && stack.hasTagCompound() && stack.getTagCompound().getBoolean("Charged");
    }

    public static ItemStack loadedProjectile(ItemStack stack) {
        if (!isCharged(stack)) return null;
        NBTTagList list = stack.getTagCompound().getTagList("ChargedProjectiles", 10);
        if (list.tagCount() < 1 || list.tagCount() > 3) return null;
        ItemStack projectile = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(0));
        if (!isProjectile(projectile)) return null;
        projectile.stackSize = 1;
        return projectile;
    }

    public static boolean isArrow(ItemStack stack) {
        return stack != null && stack.stackSize > 0 && (stack.getItem() == Items.arrow
                || stack.getItem() == CombatContent.tippedArrow || stack.getItem() == CombatContent.spectralArrow);
    }

    public static boolean isProjectile(ItemStack stack) {
        return isArrow(stack) || stack != null && stack.stackSize > 0 && stack.getItem() == Items.fireworks;
    }

    public static boolean hasAmmunition(EntityPlayer player) {
        return player.capabilities.isCreativeMode || findArrow(player) != null;
    }

    private static ItemStack findArrow(EntityPlayer player) {
        ItemStack offhand = OffhandHooks.getOffhand(player);
        if (isProjectile(offhand)) return offhand;
        ItemStack main = player.getHeldItem();
        if (isProjectile(main)) return main;
        for (ItemStack stack : player.inventory.mainInventory) if (isArrow(stack)) return stack;
        return null;
    }

    private static void storeProjectile(ItemStack crossbow, ItemStack ammunition) {
        if (!crossbow.hasTagCompound()) crossbow.setTagCompound(new NBTTagCompound());
        ItemStack single = ammunition.copy(); single.stackSize = 1;
        NBTTagList list = new NBTTagList();
        int count = net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
                com.mod.htgt6.asm.HTGT6Config.MULTISHOT_ENCHANTMENT_ID, crossbow) > 0 ? 3 : 1;
        for (int slot = 0; slot < count; slot++) {
            NBTTagCompound entry = single.copy().writeToNBT(new NBTTagCompound());
            // Preserve the original direction/pickup identity if a plugin rejects part of the volley.
            entry.setInteger("HTGT6VolleySlot", slot);
            list.appendTag(entry);
        }
        crossbow.getTagCompound().setTag("ChargedProjectiles", list);
        crossbow.getTagCompound().setBoolean("Charged", true);
    }

    public static boolean load(EntityPlayer player, ItemStack crossbow) {
        if (player.worldObj.isRemote || isCharged(crossbow) || crossbow.stackSize <= 0) return false;
        ItemStack ammunition = findArrow(player);
        if (ammunition == null && !player.capabilities.isCreativeMode) return false;
        storeProjectile(crossbow, ammunition == null ? new ItemStack(Items.arrow) : ammunition);
        if (!player.capabilities.isCreativeMode) {
            --ammunition.stackSize;
            if (ammunition.stackSize == 0) {
                if (OffhandHooks.getOffhand(player) == ammunition) OffhandHooks.setOffhand(player, null);
                else for (int slot = 0; slot < player.inventory.mainInventory.length; ++slot)
                    if (player.inventory.mainInventory[slot] == ammunition) player.inventory.mainInventory[slot] = null;
            }
        }
        player.inventory.markDirty();
        return true;
    }

    /** Mobs supply their own ammunition, just as the modern ranged weapon goal does. */
    public static boolean loadMob(EntityLivingBase mob, ItemStack crossbow) {
        if (mob.worldObj.isRemote || crossbow == null || isCharged(crossbow)) return false;
        storeProjectile(crossbow, new ItemStack(Items.arrow));
        return true;
    }

    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (isCharged(stack)) {
            if (!world.isRemote) fire(stack, world, player, null);
        } else if (findArrow(player) != null || player.capabilities.isCreativeMode) {
            player.setItemInUse(stack, getMaxItemUseDuration(stack));
        }
        return stack;
    }

    @Override public void onUsingTick(ItemStack stack, EntityPlayer player, int remaining) {
        if (player.worldObj.isRemote) return;
        int elapsed = getMaxItemUseDuration(stack) - remaining;
        int duration = chargeDuration(stack);
        if (elapsed == (int)Math.ceil(duration * .2D))
            player.worldObj.playSoundAtEntity(player, chargeStartSound(stack), .5F, 1F);
        if (quickChargeLevel(stack) == 0 && elapsed == (int)Math.ceil(duration * .5D))
            player.worldObj.playSoundAtEntity(player, "htgt6:item.crossbow.loading_middle", .5F, 1F);
    }

    @Override public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
        if (getMaxItemUseDuration(stack) - remaining >= chargeDuration(stack) && load(player, stack))
            world.playSoundAtEntity(player, "htgt6:item.crossbow.loading_end", 1F, 1F);
    }

    public static boolean fire(ItemStack stack, World world, EntityLivingBase shooter, EntityLivingBase target) {
        if (world.isRemote || stack == null || stack.stackSize <= 0 || !isCharged(stack)) return false;
        NBTTagList loaded = stack.getTagCompound().getTagList("ChargedProjectiles", 10);
        if (loaded.tagCount() < 1 || loaded.tagCount() > 3) return false;
        boolean[] seen = new boolean[3];
        // Validate the entire volley before committing any spawn.
        for (int i = 0; i < loaded.tagCount(); i++) {
            NBTTagCompound entry = loaded.getCompoundTagAt(i);
            int slot = entry.hasKey("HTGT6VolleySlot", 99) ? entry.getInteger("HTGT6VolleySlot") : i;
            if (slot < 0 || slot > 2 || seen[slot] || !isProjectile(ItemStack.loadItemStackFromNBT(entry))) return false;
            seen[slot] = true;
        }
        NBTTagList remaining = new NBTTagList();
        int spawned = 0;
        for (int i = 0; i < loaded.tagCount(); i++) {
            NBTTagCompound entry = loaded.getCompoundTagAt(i);
            int slot = entry.hasKey("HTGT6VolleySlot", 99) ? entry.getInteger("HTGT6VolleySlot") : i;
            ItemStack ammunition = ItemStack.loadItemStackFromNBT(entry);
            boolean rocket = ammunition.getItem() == Items.fireworks;
            net.minecraft.entity.Entity projectile = rocket ? createRocket(world, shooter, target, ammunition, slot)
                    : createArrow(world, shooter, target, ammunition, slot, stack);
            if (world.spawnEntityInWorld(projectile)) {
                spawned++;
                stack.damageItem(rocket ? 3 : 1, shooter);
                world.playSoundAtEntity(shooter, "htgt6:item.crossbow.shoot", 1F, 1F);
            } else {
                NBTTagCompound pending = (NBTTagCompound)entry.copy();
                pending.setInteger("HTGT6VolleySlot", slot);
                remaining.appendTag(pending);
            }
        }
        if (remaining.tagCount() == 0 || stack.stackSize <= 0) {
            stack.getTagCompound().setBoolean("Charged", false);
            stack.getTagCompound().removeTag("ChargedProjectiles");
        } else stack.getTagCompound().setTag("ChargedProjectiles", remaining);
        if (shooter instanceof EntityPlayer) ((EntityPlayer)shooter).inventory.markDirty();
        return spawned > 0;
    }

    private static net.minecraft.entity.Entity createRocket(World world, EntityLivingBase shooter,
            EntityLivingBase target, ItemStack ammunition, int slot) {
        com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143 rocket =
                new com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143(world, shooter, ammunition);
        Vec3 direction = shooter.getLookVec();
        if (target != null) {
            double dx=target.posX-rocket.posX,dz=target.posZ-rocket.posZ;
            direction=Vec3.createVectorHelper(dx,target.boundingBox.minY+target.height/3F-rocket.posY
                    +Math.sqrt(dx*dx+dz*dz)*.2D,dz);
        }
        direction=volleyDirection(direction,shooter.rotationYaw,slot==1?-10:slot==2?10:0);
        rocket.setThrowableHeading(direction.xCoord,direction.yCoord,direction.zCoord,1.6F,
                target==null?1F:14F-world.difficultySetting.getDifficultyId()*4F);
        return rocket;
    }

    private static EntityCombatArrow createArrow(World world, EntityLivingBase shooter,
            EntityLivingBase target, ItemStack ammunition, int slot, ItemStack stackForShot) {
        int type = ammunition.getItem() == CombatContent.tippedArrow ? EntityCombatArrow.TYPE_TIPPED
                : ammunition.getItem() == CombatContent.spectralArrow ? EntityCombatArrow.TYPE_SPECTRAL : EntityCombatArrow.TYPE_NORMAL;
        EntityCombatArrow arrow = new EntityCombatArrow(world, shooter, 1F, type, ammunition.getItemDamage());
        arrow.setPotionTag(ammunition.getTagCompound());
        arrow.setPierceLevel(net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
                com.mod.htgt6.asm.HTGT6Config.PIERCING_ENCHANTMENT_ID, stackForShot));
        arrow.setPosition(shooter.posX, shooter.posY + shooter.getEyeHeight() - .1D, shooter.posZ);
        Vec3 direction;
        if (target == null) direction = shooter.getLookVec();
        else {
            double dx = target.posX - arrow.posX, dz = target.posZ - arrow.posZ;
            double dy = target.boundingBox.minY + target.height / 3F - arrow.posY;
            direction = Vec3.createVectorHelper(dx, dy + Math.sqrt(dx * dx + dz * dz) * .2D, dz);
        }
        direction = volleyDirection(direction, shooter.rotationYaw, slot == 1 ? -10 : slot == 2 ? 10 : 0);
        arrow.setThrowableHeading(direction.xCoord, direction.yCoord, direction.zCoord,
                target == null ? 3.15F : 1.6F, target == null ? 1F : 14F - world.difficultySetting.getDifficultyId() * 4F);
        boolean player = shooter instanceof EntityPlayer;
        arrow.setIsCritical(player);
        arrow.canBePickedUp = slot != 0 ? 2 : player ? (((EntityPlayer)shooter).capabilities.isCreativeMode ? 2 : 1) : 0;
        return arrow;
    }

    /** Rotate around the local up axis; yaw-only rotation collapses the spread when looking vertically. */
    public static Vec3 volleyDirection(Vec3 forward, float yaw, float degrees) {
        Vec3 direction = forward.normalize();
        double horizontal = Math.sqrt(direction.xCoord * direction.xCoord + direction.zCoord * direction.zCoord);
        double rx = horizontal > 1.0E-7 ? direction.zCoord / horizontal : Math.cos(Math.toRadians(yaw));
        double rz = horizontal > 1.0E-7 ? -direction.xCoord / horizontal : Math.sin(Math.toRadians(yaw));
        Vec3 up = direction.crossProduct(Vec3.createVectorHelper(rx, 0, rz)).normalize();
        Vec3 cross = up.crossProduct(direction);
        double cos = Math.cos(Math.toRadians(degrees)), sin = Math.sin(Math.toRadians(degrees));
        return Vec3.createVectorHelper(direction.xCoord * cos + cross.xCoord * sin,
                direction.yCoord * cos + cross.yCoord * sin, direction.zCoord * cos + cross.zCoord * sin);
    }

    @Override @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon(getIconString());
        pulling = new IIcon[3];
        for (int i = 0; i < 3; i++) pulling[i] = register.registerIcon("htgt6:village_pillage/crossbow_pulling_" + i);
        loadedArrow = register.registerIcon("htgt6:village_pillage/crossbow_arrow");
        loadedFirework = register.registerIcon("htgt6:village_pillage/crossbow_firework");
    }

    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(ItemStack stack, int pass) { return getIconIndex(stack); }

    @Override @SideOnly(Side.CLIENT) public IIcon getIconIndex(ItemStack stack) {
        EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft() == null ? null
                : net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        return inventoryIcon(stack, player);
    }

    @SideOnly(Side.CLIENT) public IIcon inventoryIcon(ItemStack stack, EntityPlayer player) {
        // Equal NBT is insufficient here: another identical crossbow in the bag is not in use.
        return player != null && (player.getItemInUse() == stack || player.getHeldItem() == stack
                || OffhandHooks.getOffhand(player) == stack) ? getLivingIcon(stack, player) : chargeIcon(stack, -1);
    }

    @SideOnly(Side.CLIENT) public IIcon chargeIcon(ItemStack stack, int elapsed) {
        if (!isCharged(stack) && elapsed >= 0 && pulling != null) {
            float charge = elapsed / (float) chargeDuration(stack);
            return pulling[charge >= 1F ? 2 : charge >= .58F ? 1 : 0];
        }
        if (!isCharged(stack)) return itemIcon;
        ItemStack ammunition=loadedProjectile(stack);
        return ammunition!=null&&ammunition.getItem()==Items.fireworks ? loadedFirework : loadedArrow;
    }

    @SideOnly(Side.CLIENT) public IIcon getLivingIcon(ItemStack stack, EntityLivingBase holder) {
        int elapsed = -1;
        if (holder instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) holder;
            elapsed = player.getItemInUse() == stack ? player.getItemInUseDuration()
                    : com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.renderUseTicks(player, stack);
        } else if (holder instanceof com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager
                && holder.getHeldItem() == stack) {
            int ticks = ((com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager) holder).getCrossbowChargeTicks();
            if (ticks > 0) elapsed = ticks;
        }
        if(holder instanceof com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116 && holder.getHeldItem()==stack){
            int ticks=((com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116)holder).getCrossbowChargeTicks();if(ticks>0)elapsed=ticks;
        }
        return chargeIcon(stack, elapsed);
    }

    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(ItemStack stack, int pass, EntityPlayer player, ItemStack using, int remaining) {
        int elapsed = using == stack ? getMaxItemUseDuration(stack) - remaining
                : com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.renderUseTicks(player, stack);
        return chargeIcon(stack, elapsed);
    }
}
