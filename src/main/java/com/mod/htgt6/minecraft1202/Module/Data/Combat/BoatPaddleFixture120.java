package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ModelCombatBoat120;
import net.minecraft.client.model.ModelBox;

/** Vanilla controlBoat truth table and independently synchronized rowing phases. */
public final class BoatPaddleFixture120 {
    public static void main(String[] args) {
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        int[] expected = {0,2,1,0,3,3,3,3};
        for (int input=0;input<8;input++) {
            if (EntityCombatBoat.paddleMask((input&1)!=0,(input&2)!=0,(input&4)!=0)!=expected[input])
                throw new AssertionError("input mask " + input);
        }
        EntityCombatBoat boat = new EntityCombatBoat(null);
        boat.setVariant(0);if(boat.getVariant()!=0)throw new AssertionError("oak variant must retain its registered vanilla item");
        boat.getDataWatcher().updateObject(21, Byte.valueOf((byte)1));
        boat.tickPaddles();
        close(boat.getRowingTime(0,0),0);close(boat.getRowingTime(0,.5F),.19634955F);
        close(boat.getRowingTime(1,1),0);
        boat.tickPaddles();close(boat.getRowingTime(0,1),.7853982F);
        boat.getDataWatcher().updateObject(21, Byte.valueOf((byte)0));
        close(boat.getRowingTime(0,.5F),0);
        boat.tickPaddles();
        boat.getDataWatcher().updateObject(21, Byte.valueOf((byte)3));
        boat.tickPaddles();
        close(boat.getRowingTime(0,1),.3926991F);close(boat.getRowingTime(1,1),.3926991F);
        ModelCombatBoat120 model=new ModelCombatBoat120();
        close(model.leftPaddle.rotationPointX,3);close(model.rightPaddle.rotationPointX,3);
        close(model.leftPaddle.rotationPointZ,9);close(model.rightPaddle.rotationPointZ,-9);
        close(model.leftPaddle.rotateAngleZ,.19634955F);close(model.rightPaddle.rotateAngleZ,.19634955F);
        ModelBox left=(ModelBox)model.leftPaddle.cubeList.get(1);
        ModelBox right=(ModelBox)model.rightPaddle.cubeList.get(1);
        close(left.posX1,-1.001F);close(right.posX1,.001F);
        close(left.posX2-left.posX1,1);close(left.posY2-left.posY1,6);close(left.posZ2-left.posZ1,7);
        ModelBox bottom=(ModelBox)model.hull[0].cubeList.get(0);
        close(bottom.posX2-bottom.posX1,28);close(bottom.posY2-bottom.posY1,16);
        close(model.hull[0].rotationPointY,3);close(model.hull[0].rotateAngleX,(float)Math.PI/2);
        close(model.hull[1].rotationPointX,-15);close(model.hull[2].rotationPointX,15);
        close(model.hull[3].rotationPointZ,-9);close(model.hull[4].rotationPointZ,9);
        System.out.println("Boat paddles OK: input combinations, independent sides, stop, restart, interpolation");
        System.out.println("Boat geometry OK: five hull parts, opposing paddles, reference blade dimensions and pivots");
    }
    private static void close(float actual,float expected) {
        if(Math.abs(actual-expected)>0.000001F)throw new AssertionError(actual+" != "+expected);
    }
}
