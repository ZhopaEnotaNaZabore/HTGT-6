package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemChorusFruit;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;

/** Exercise real position/bounding-box updates with a controlled legacy world. */
public final class ChorusTeleportFixture120 {
    public static void main(String[] args)throws Exception {
        Grid w=BountifulMechanicsFixture120.initialize();
        EntityPig pig=new EntityPig(w);pig.setPosition(0,20,0);pig.fallDistance=12;
        w.setBlock(5,10,5,Blocks.stone,0,2);
        require(ItemChorusFruit.tryTeleport(pig,5.5,18,5.5),"valid destination");
        require(pig.posY==11&&pig.fallDistance==0,"descend to solid ground and reset fall distance");
        pig.setPosition(0,20,0);w.liquid=true;
        require(!ItemChorusFruit.tryTeleport(pig,5.5,18,5.5),"reject liquid");unchanged(pig);
        w.liquid=false;w.collision=true;
        require(!ItemChorusFruit.tryTeleport(pig,5.5,18,5.5),"reject obstruction");unchanged(pig);
        w.collision=false;w.loaded=false;
        require(!ItemChorusFruit.tryTeleport(pig,5.5,18,5.5),"do not load destination chunks");unchanged(pig);
        w.loaded=true;
        require(!ItemChorusFruit.tryTeleport(pig,8.5,18,8.5),"reject void without support");unchanged(pig);
        w.isRemote=true;
        require(!ItemChorusFruit.tryTeleport(pig,5.5,18,5.5),"server-only teleport");unchanged(pig);
        System.out.println("Chorus teleport OK: ground, liquid, collision, unloaded chunks, void, rollback, side");
    }
    private static void unchanged(EntityPig p){require(p.posX==0&&p.posY==20&&p.posZ==0&&p.boundingBox.minY==20,"failed attempt restores position and box");}
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
