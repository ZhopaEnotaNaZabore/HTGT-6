package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.material.Material;
public final class SwimmingInput120 {
    public static void update(EntityPlayerSP p){
        if(p.isInWater()&&!p.capabilities.isFlying&&p.movementInput.sneak){p.movementInput.moveForward/=.3F;p.movementInput.moveStrafe/=.3F;}
    }
    public static boolean sprintSurface(boolean ground,EntityPlayer p){return ground||p.isInsideOfMaterial(Material.water);}
}
