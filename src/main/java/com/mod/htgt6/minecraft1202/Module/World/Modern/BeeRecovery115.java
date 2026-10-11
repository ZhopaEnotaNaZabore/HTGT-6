package com.mod.htgt6.minecraft1202.Module.World.Modern;
import net.minecraft.world.*;import net.minecraft.nbt.*;import java.util.*;import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115;
/** Persists bees rejected during destructive block removal until the world accepts them. */
public final class BeeRecovery115 extends WorldSavedData {
 private NBTTagList pending=new NBTTagList();public BeeRecovery115(){super("HTGT6BeeRecovery115");}public BeeRecovery115(String name){super(name);}
 private static BeeRecovery115 get(World w){BeeRecovery115 d=(BeeRecovery115)w.perWorldStorage.loadData(BeeRecovery115.class,"HTGT6BeeRecovery115");if(d==null){d=new BeeRecovery115();w.perWorldStorage.setData("HTGT6BeeRecovery115",d);}return d;}
 public static void save(World w,int x,int y,int z,NBTTagCompound hive){NBTTagList bees=hive.getTagList("Bees",10);if(bees.tagCount()==0)return;BeeRecovery115 data=get(w);NBTTagCompound record=new NBTTagCompound();record.setInteger("X",x);record.setInteger("Y",y);record.setInteger("Z",z);record.setTag("Bees",bees.copy());data.pending.appendTag(record);data.markDirty();}
 public static void tick(World w){BeeRecovery115 d=get(w);for(int i=d.pending.tagCount()-1;i>=0;i--){NBTTagCompound record=d.pending.getCompoundTagAt(i);int x=record.getInteger("X"),y=record.getInteger("Y"),z=record.getInteger("Z");if(!w.blockExists(x,y,z))continue;NBTTagList bees=record.getTagList("Bees",10);for(int j=bees.tagCount()-1;j>=0;j--){EntityBee115 b=new EntityBee115(w);b.readFromNBT(bees.getCompoundTagAt(j).getCompoundTag("EntityData"));b.setPosition(x+.5,y+.2,z+.5);b.setHome(0,-1,0);b.hiveCooldown=400;if(w.getCollidingBoundingBoxes(b,b.boundingBox).isEmpty()&&w.spawnEntityInWorld(b)){bees.removeTag(j);d.markDirty();}}if(bees.tagCount()==0){d.pending.removeTag(i);d.markDirty();}}}
 @Override public void readFromNBT(NBTTagCompound n){pending=n.getTagList("Pending",10);}@Override public void writeToNBT(NBTTagCompound n){n.setTag("Pending",pending);}
}
