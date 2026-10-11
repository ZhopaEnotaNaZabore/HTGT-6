package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Repairs the legacy village component/chunk spawn edge case in tall Futura terrain. */
public final class FuturaVillagePopulation120 {
    private static final Logger LOGGER=LogManager.getLogger("HTGT6-FuturaVillage120");
    private static final int MINIMUM_RESIDENTS=2;
    private static final int VILLAGE_ENTITY_RADIUS=176;

    static void ensureResidents(World world,int chunkX,int chunkZ,boolean villageGenerated) {
        if(!shouldRepairForFixture(villageGenerated,world.isRemote,true))return;
        int minX=chunkX<<4,minZ=chunkZ<<4;
        List<int[]> anchors=findAnchors(world,minX,minZ);
        if(!shouldRepairForFixture(villageGenerated,world.isRemote,!anchors.isEmpty()))return;
        AxisAlignedBB area=AxisAlignedBB.getBoundingBox(minX-VILLAGE_ENTITY_RADIUS,1,minZ-VILLAGE_ENTITY_RADIUS,
                minX+16+VILLAGE_ENTITY_RADIUS,world.getActualHeight(),minZ+16+VILLAGE_ENTITY_RADIUS);
        List villagers=world.getEntitiesWithinAABB(EntityVillager.class,area);
        Random random=new Random(world.getSeed()^(long)chunkX*341873128712L^(long)chunkZ*132897987541L);
        int spawned=0;
        for(int i=0;i<anchors.size()&&villagers.size()+spawned<MINIMUM_RESIDENTS;i++){
            int[] anchor=anchors.get(i),position=safeAdjacent(world,anchor[0],anchor[1],anchor[2],false);
            if(position==null)continue;
            EntityVillager villager=new EntityVillager(world,random.nextInt(5));
            villager.setLocationAndAngles(position[0]+.5D,position[1],position[2]+.5D,random.nextFloat()*360F,0F);
            if(!canSpawn(world,villager))continue;
            villager.func_110163_bv();
            if(world.spawnEntityInWorld(villager))spawned++;
        }
        if(spawned>0)LOGGER.info("Restored {} missing village resident(s) in Futura chunk {},{}",spawned,chunkX,chunkZ);
        if(villagers.size()+spawned>=MINIMUM_RESIDENTS
                && world.getEntitiesWithinAABB(EntityIronGolem.class,area).isEmpty()){
            for(int[] anchor:anchors){int[] position=safeAdjacent(world,anchor[0],anchor[1],anchor[2],true);
                if(position==null)continue;
                EntityIronGolem golem=new EntityIronGolem(world);
                golem.setPlayerCreated(false);
                golem.setLocationAndAngles(position[0]+.5D,position[1],position[2]+.5D,random.nextFloat()*360F,0F);
                if(!canSpawn(world,golem))continue;
                golem.func_110163_bv();
                if(world.spawnEntityInWorld(golem))LOGGER.info(
                        "Restored missing village iron golem in Futura chunk {},{}",chunkX,chunkZ);
                break;
            }
        }
    }

    private static List<int[]> findAnchors(World world,int minX,int minZ){
        List<int[]> result=new ArrayList<int[]>();
        for(int x=minX;x<minX+16;x++)for(int z=minZ;z<minZ+16;z++){
            int surface=world.getHeightValue(x,z);
            int minY=Math.max(2,surface-32),maxY=Math.min(world.getActualHeight()-4,surface+16);
            for(int y=minY;y<=maxY;y++){
                Block block=world.getBlock(x,y,z);
                String poi=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagePoiType120.forBlock(block);
                boolean lowerDoor=block==Blocks.wooden_door&&(world.getBlockMetadata(x,y,z)&8)==0;
                if(poi==null&&!lowerDoor)continue;
                if("home".equals(poi)&&!com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagePoiType120.isHomeHead(world,x,y,z))continue;
                result.add(new int[]{x,y,z});
            }
        }
        return result;
    }

    private static int[] safeAdjacent(World world,int x,int y,int z,boolean golem){
        int radius=golem?3:2;
        for(int distance=1;distance<=radius;distance++)for(int dx=-distance;dx<=distance;dx++)
            for(int dz=-distance;dz<=distance;dz++){
                if(Math.max(Math.abs(dx),Math.abs(dz))!=distance)continue;
                int px=x+dx,pz=z+dz;
                for(int py=Math.min(world.getActualHeight()-4,y+2);py>=Math.max(2,y-4);py--){
                    Block floor=world.getBlock(px,py-1,pz);
                    if(world.isAirBlock(px,py,pz)&&world.isAirBlock(px,py+1,pz)
                            &&(!golem||world.isAirBlock(px,py+2,pz))&&floor.getMaterial().isSolid())
                        return new int[]{px,py,pz};
                }
            }
        return null;
    }

    private static boolean canSpawn(World world,EntityLiving entity){
        return world.checkNoEntityCollision(entity.boundingBox)
                &&world.getCollidingBoundingBoxes(entity,entity.boundingBox).isEmpty()
                &&!world.isAnyLiquid(entity.boundingBox);
    }

    public static boolean shouldRepairForFixture(boolean generated,boolean remote,boolean hasPoiAnchor){
        return generated&&!remote&&hasPoiAnchor;
    }

    private FuturaVillagePopulation120(){}
}
