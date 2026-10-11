package com.mod.htgt6.minecraft1202.Module.World.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
/** Bounded loaded-chunk village/hut cat spawner and exact-class legacy mooshroom migration. */
public final class VillageFauna114 {
    @SubscribeEvent public void join(EntityJoinWorldEvent e){if(e.world.isRemote||e.entity.getClass()!=EntityMooshroom.class)return;NBTTagCompound n=new NBTTagCompound();e.entity.writeToNBT(n);EntityMooshroom114 modern=new EntityMooshroom114(e.world);modern.readFromNBT(n);if(e.world.spawnEntityInWorld(modern)){e.setCanceled(true);e.entity.setDead();}}
    @SubscribeEvent public void tick(TickEvent.WorldTickEvent e){World w=e.world;if(e.phase!=TickEvent.Phase.END||w.isRemote||w.provider.dimensionId!=0||w.getTotalWorldTime()%1200!=0||!w.getGameRules().getGameRuleBooleanValue("doMobSpawning")||w.playerEntities.isEmpty())return;
        EntityPlayer p=(EntityPlayer)w.playerEntities.get(w.rand.nextInt(w.playerEntities.size()));int x=MathHelper.floor_double(p.posX)+(8+w.rand.nextInt(24))*(w.rand.nextBoolean()?1:-1),z=MathHelper.floor_double(p.posZ)+(8+w.rand.nextInt(24))*(w.rand.nextBoolean()?1:-1);
        int center=MathHelper.floor_double(p.posY);for(int dy=2;dy>=-2;dy--){int y=center+dy;if(trySpawn(w,x,y,z))return;}
    }
    public static boolean trySpawn(World w,int x,int y,int z){if(w.isRemote||!w.getGameRules().getGameRuleBooleanValue("doMobSpawning")||y<1||y+1>=w.getActualHeight()||!w.checkChunksExist(x-2,y-1,z-2,x+2,y+2,z+2)||!w.getBlock(x,y-1,z).isSideSolid(w,x,y-1,z,net.minecraftforge.common.util.ForgeDirection.UP))return false;
        boolean hut=inHut(w,x,y,z);int homes=0;for(VillagePoiData120.Entry home:VillagePoiData120.get(w).occupiedNear(w,x,y,z,48))if("home".equals(home.type))homes++;
        if(!hut&&homes<5)return false;int radius=hut?16:48,limit=hut?1:5;
        if(w.getEntitiesWithinAABB(EntityCat114.class,AxisAlignedBB.getBoundingBox(x-radius,y-8,z-radius,x+radius,y+8,z+radius)).size()>=limit)return false;
        EntityCat114 cat=new EntityCat114(w);cat.setLocationAndAngles(x+.5,y,z+.5,w.rand.nextFloat()*360,0);if(!cat.getCanSpawnHere())return false;cat.onSpawnWithEgg(null);if(hut){cat.setVariant(10);cat.func_110163_bv();}return w.spawnEntityInWorld(cat);
    }
    private static boolean inHut(World w,int x,int y,int z){MapGenStructureData d=(MapGenStructureData)w.perWorldStorage.loadData(MapGenStructureData.class,"Temple");if(d==null)return false;NBTTagCompound root=new NBTTagCompound();d.writeToNBT(root);NBTTagCompound features=root.getCompoundTag("Features");for(Object key:features.func_150296_c()){NBTTagList children=features.getCompoundTag((String)key).getTagList("Children",10);for(int i=0;i<children.tagCount();i++){NBTTagCompound c=children.getCompoundTagAt(i);int[] b=c.getIntArray("BB");if("TeSH".equals(c.getString("id"))&&b.length==6&&x>=b[0]&&x<=b[3]&&y>=b[1]&&y<=b[4]&&z>=b[2]&&z<=b[5])return true;}}return false;}
}
