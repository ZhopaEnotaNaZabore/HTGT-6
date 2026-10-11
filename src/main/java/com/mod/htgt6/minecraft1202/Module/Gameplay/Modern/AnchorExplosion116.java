package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.block.material.Material;
import net.minecraft.world.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.WaterloggedFluid150;
import net.minecraftforge.event.world.ExplosionEvent;

/** A water-protected anchor has water resistance at the removed origin in modern Minecraft.
 * At power 5 this stops every block ray immediately. Forge's detonation event lets us
 * express that result without temporarily replacing blocks or suppressing entity damage. */
public final class AnchorExplosion116 {
    private static final ThreadLocal<Context> ACTIVE=new ThreadLocal<Context>();
    private static final class Context {final World world;final int x,y,z;final boolean water;Explosion explosion;Context(World w,int x,int y,int z){world=w;this.x=x;this.y=y;this.z=z;water=waterProtected(w,x,y,z);}}
    public static boolean waterProtected(World w,int x,int y,int z){
        if(w.getBlock(x,y+1,z).getMaterial()==Material.water||WaterloggedFluid150.wet(w,x,y+1,z))return true;
        for(int[] d:new int[][]{{0,-1},{-1,0},{0,1},{1,0}}){int px=x+d[0],pz=z+d[1];if(WaterloggedFluid150.wet(w,px,y,pz))return true;if(w.getBlock(px,y,pz).getMaterial()!=Material.water)continue;int meta=w.getBlockMetadata(px,y,pz);
            if(meta==0||meta<=6&&w.getBlock(px,y+1,pz).getMaterial()!=Material.water&&!WaterloggedFluid150.wet(w,px,y+1,pz))return true;
        }return false;
    }
    public static void explode(World w,int x,int y,int z){
        Context previous=ACTIVE.get(),context=new Context(w,x,y,z);ACTIVE.set(context);
        try{if(w.setBlockToAir(x,y,z))w.newExplosion(null,x+.5,y+.5,z+.5,5,true,true);}
        finally{if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
    }
    public static boolean damage(net.minecraft.entity.Entity target,net.minecraft.util.DamageSource source,float legacy,Explosion explosion){
        Context c=ACTIVE.get();
        if(c!=null&&c.explosion==explosion){
            double radius=explosion.explosionSize; // doExplosionA has already doubled the radius here.
            double distance=target.getDistance(explosion.explosionX,explosion.explosionY,explosion.explosionZ)/radius;
            double exposure=c.world.getBlockDensity(net.minecraft.util.Vec3.createVectorHelper(explosion.explosionX,explosion.explosionY,explosion.explosionZ),target.boundingBox);
            double impact=(1-distance)*exposure;
            legacy=(float)(int)((impact*impact+impact)/2D*7D*radius+1D);
        }
        return target.attackEntityFrom(source,legacy);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void start(ExplosionEvent.Start event){
        Context c=ACTIVE.get();if(c!=null&&c.explosion==null&&event.world==c.world&&event.explosion.explosionX==c.x+.5&&event.explosion.explosionY==c.y+.5&&event.explosion.explosionZ==c.z+.5)c.explosion=event.explosion;
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void detonate(ExplosionEvent.Detonate event){
        Context c=ACTIVE.get();if(c!=null&&c.water&&c.explosion==event.explosion)event.getAffectedBlocks().clear();
    }
}
