package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityEndGateway;
import com.mod.htgt6.minecraft1202.Module.Data.Combat.EndFightData;
import com.mod.htgt6.minecraft1202.Module.World.Combat.EndSpikeGenerator;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityDragonBreathFireball;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class EndMechanics {
    @SubscribeEvent public void onDragonDeath(LivingDeathEvent event) {
        if (!(event.entityLiving instanceof EntityDragon) || event.entityLiving.worldObj.isRemote) return;
        if(event.entityLiving.worldObj.provider.dimensionId==1)generateGateway(event.entityLiving.worldObj);
    }

    public static void generateGateway(World world) {
        int index = EndFightData.get(world).nextGateway(world.getSeed());
        if(index<0)return;
        double angle = 2.0D * (-Math.PI + Math.PI / 20.0D * index);
        int x = (int) Math.floor(Math.cos(angle) * 96.0D);
        int z = (int) Math.floor(Math.sin(angle) * 96.0D);
        int y = 75;
        generateGateway(world,x,y,z,0,-1,0,false);
    }

    /** Shape and persistent destination used by WorldGenEndGateway in 1.12. */
    public static void generateGateway(World world,int x,int y,int z,int exitX,int exitY,int exitZ,boolean exact) {
        for (int dx = -1; dx <= 1; ++dx) for (int dy = -2; dy <= 2; ++dy) for (int dz = -1; dz <= 1; ++dz) {
            boolean frame = gatewayFrame(dx,dy,dz);
            if (frame) world.setBlock(x + dx, y + dy, z + dz, net.minecraft.init.Blocks.bedrock);
            else if (dx == 0 && dy == 0 && dz == 0) world.setBlock(x, y, z, CombatContent.endGateway);
            else world.setBlockToAir(x + dx, y + dy, z + dz);
        }
        TileEntityEndGateway tile=(TileEntityEndGateway)world.getTileEntity(x,y,z);
        if(tile!=null&&exitY>=0)tile.setExit(exitX,exitY,exitZ,exact);
    }

    public static boolean gatewayFrame(int dx,int dy,int dz) {
        return Math.abs(dy)==2 ? dx==0&&dz==0 : dy!=0&&(dx==0||dz==0);
    }

    public static void tryRespawnDragon(World world) {
        if (world.isRemote || world.provider.dimensionId != 1) return;
        List dragons = world.getEntitiesWithinAABB(EntityDragon.class, AxisAlignedBB.getBoundingBox(-256, 0, -256, 256, 256, 256));
        if (!dragons.isEmpty()) return;
        int portalY = findPortalY(world);
        if(EndFightData.get(world).isRespawning())return;
        if(findRespawnCrystals(world,portalY).size()!=4)return;
        EndFightData.get(world).beginRespawn();
    }

    @SubscribeEvent public void onWorldTick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||event.world.isRemote||event.world.provider.dimensionId!=1)return;
        tickDragonBreath(event.world);
        EndFightData data=EndFightData.get(event.world);if(!data.isRespawning())return;
        int portalY=findPortalY(event.world),tick=data.tickRespawn();
        if(tick%20==0&&findRespawnCrystals(event.world,portalY).size()!=4){data.setRespawnState(0);return;}
        if(data.getRespawnState()==1){if(tick%20==0)event.world.playAuxSFX(2003,0,portalY+1,0,0);if(tick>=100)data.setRespawnState(2);}
        else if(data.getRespawnState()==2){if(tick%40==0)event.world.createExplosion(null,0,128,0,0F,false);if(tick%40==39)EndSpikeGenerator.regenerate(event.world,tick/40);if(tick>=400)data.setRespawnState(3);}
        else if(data.getRespawnState()==3){if(tick<80&&tick%10==0)event.world.playAuxSFX(2003,0,portalY+1,0,0);if(tick>=100)finishRespawn(event.world,portalY,data);}
    }

    private static void tickDragonBreath(World world) {
        if (world.getTotalWorldTime() % 100L != 0L) return;
        @SuppressWarnings("unchecked") List<EntityDragon> dragons = world.getEntitiesWithinAABB(EntityDragon.class,
                AxisAlignedBB.getBoundingBox(-256, 0, -256, 256, 256, 256));
        for (EntityDragon dragon : dragons) {
            EntityPlayer target = world.getClosestVulnerablePlayerToEntity(dragon, 64.0D);
            if (target == null) continue;
            double x = target.posX - dragon.posX;
            double y = target.posY + target.getEyeHeight() - (dragon.posY + dragon.height * 0.5D);
            double z = target.posZ - dragon.posZ;
            EntityDragonBreathFireball fireball = new EntityDragonBreathFireball(world, dragon, x, y, z);
            fireball.setPosition(dragon.posX, dragon.posY + dragon.height * 0.5D, dragon.posZ);
            world.spawnEntityInWorld(fireball);
            world.playSoundAtEntity(dragon, "mob.ghast.fireball", 2.0F,
                    0.8F + dragon.getRNG().nextFloat() * 0.2F);
        }
    }

    private static void finishRespawn(World world,int portalY,EndFightData data){
        for(EntityEnderCrystal crystal:findRespawnCrystals(world,portalY))crystal.setDead();
        EntityDragon dragon=new EntityDragon(world);dragon.setLocationAndAngles(0,128,0,world.rand.nextFloat()*360F,0);
        if(world.spawnEntityInWorld(dragon))for(Object object:world.playerEntities){
            net.minecraft.entity.player.EntityPlayer player=(net.minecraft.entity.player.EntityPlayer)object;
            if(player.isEntityAlive()&&player.getDistanceSq(0,128,0)<=192D*192D)
                com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(player,"respawn_dragon");
        }
        world.playAuxSFX(2003,0,portalY+1,0,0);data.setRespawnState(0);
    }

    private static List<EntityEnderCrystal> findRespawnCrystals(World world,int y){
        int[][] pads={{0,-3},{0,3},{-3,0},{3,0}};List<EntityEnderCrystal> found=new ArrayList<EntityEnderCrystal>();
        for(int[] p:pads){List list=world.getEntitiesWithinAABB(EntityEnderCrystal.class,AxisAlignedBB.getBoundingBox(p[0]-1,y-1,p[1]-1,p[0]+2,y+5,p[1]+2));if(list.size()!=1)return new ArrayList<EntityEnderCrystal>();found.add((EntityEnderCrystal)list.get(0));}return found;
    }

    private static int findPortalY(World world) {
        for (int y = 96; y >= 40; --y) if (world.getBlock(0, y, 0) == net.minecraft.init.Blocks.bedrock) return y;
        return 64;
    }
}
