import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import java.lang.reflect.*;
public final class TridentAnchor92Fixture {
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  cpw.mods.fml.common.registry.GameRegistry.registerItem(OceanContent.trident,"fixture_trident92");
  EntityThrownTrident entity=new EntityThrownTrident(w);
  w.setBlock(1,10,2,Blocks.stone,0,3);
  Method embed=EntityThrownTrident.class.getDeclaredMethod("embedAt",MovingObjectPosition.class);embed.setAccessible(true);
  embed.invoke(entity,new MovingObjectPosition(1,10,2,1,Vec3.createVectorHelper(1.5,10.99,2.5)));
  Method support=EntityThrownTrident.class.getDeclaredMethod("hasEmbeddingSupport");support.setAccessible(true);
  check((Boolean)support.invoke(entity),"solid block supports projectile");
  NBTTagCompound saved=new NBTTagCompound();entity.writeEntityToNBT(saved);
  check(saved.getInteger("AnchorX")==1&&saved.getInteger("AnchorY")==10&&saved.getInteger("AnchorZ")==2,"anchor saved");
  EntityThrownTrident restored=new EntityThrownTrident(w);restored.readEntityFromNBT(saved);restored.setPosition(entity.posX,entity.posY,entity.posZ);
  check((Boolean)support.invoke(restored),"anchor restored");
  w.loaded=false;check((Boolean)support.invoke(restored),"unloaded anchor not removed");w.loaded=true;
  w.setBlockToAir(1,10,2);restored.onUpdate();
  NBTTagCompound released=new NBTTagCompound();restored.writeEntityToNBT(released);
  check(!released.getBoolean("Embedded")&&!restored.isDead&&!released.hasKey("AnchorX"),"removed block releases projectile");
  check(restored.getDataWatcher().getWatchableObjectByte(20)==0,"release synchronized as flying");
  System.out.println("PASS trident anchor: collision support, NBT round trip, unloaded chunk, support removal, network state");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
