package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerSwimming120;
import com.mod.htgt6.asm.HTGT6Transformer;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.block.material.Material;
import net.minecraft.util.*;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.lang.reflect.Field;

public final class Swimming115Fixture {
    static int checks;
    public static void main(String[] args)throws Exception {
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));world.provider.worldObj=world;
        Player p=new Player(world);p.setPosition(.5,10,.5);p.setSprinting(true);p.wet=true;p.eyes=false;
        PlayerSwimming120.beforeTravel(p,false);check(!PlayerSwimming120.isSwimming(p),"cannot start with eyes above water");
        p.eyes=true;PlayerSwimming120.beforeTravel(p,false);check(PlayerSwimming120.isSwimming(p),"submerged sprint starts swimming");
        p.eyes=false;PlayerSwimming120.beforeTravel(p,false);check(PlayerSwimming120.isSwimming(p),"surface retains swimming while body in water");
        for(int reason=0;reason<5;reason++) {
            p.wet=true;p.eyes=true;p.setSprinting(true);p.setHealth(20);p.ridingEntity=null;p.capabilities.isFlying=false;
            PlayerSwimming120.beforeTravel(p,false);
            if(reason==0)p.wet=false;if(reason==1)p.setSprinting(false);if(reason==2)p.capabilities.isFlying=true;if(reason==3)p.ridingEntity=new EntityPig(world);if(reason==4)p.setHealth(0);
            PlayerSwimming120.beforeTravel(p,false);check(!PlayerSwimming120.isSwimming(p),"exit reason "+reason);
        }
        p.setHealth(20);p.wet=true;p.eyes=true;p.setSprinting(true);p.capabilities.isFlying=false;p.ridingEntity=null;
        double[] directions={-1,-.21,-.2,0,.5,1};
        for(double y:directions)for(boolean jumping:new boolean[]{false,true})for(boolean above:new boolean[]{false,true}) {
            world.cells.clear();if(above)world.setBlock(0,MathHelper.floor_double(p.boundingBox.minY+.9D),0,Blocks.water,0,2);p.lookY=y;p.motionY=.1;
            PlayerSwimming120.beforeTravel(p,jumping);
            double expected=y<=0||jumping||above?.1+(y-.1)*(y<-.2?.085:.06):.1;
            check(Math.abs(p.motionY-expected)<1e-10,"steering y="+y+" jump="+jumping+" water="+above);
        }
        Player other=new Player(world);other.wet=true;other.eyes=false;other.setSprinting(true);PlayerSwimming120.beforeTravel(other,false);
        check(!PlayerSwimming120.isSwimming(other)&&PlayerSwimming120.isSwimming(p),"states belong to separate entity objects");
        EntityPig pig=new EntityPig(world);pig.motionY=.12;PlayerSwimming120.beforeTravel(pig,true);check(pig.motionY==.12,"mob movement unaffected");
        for(int level=0;level<=3;level++)for(boolean ground:new boolean[]{false,true})for(boolean sprint:new boolean[]{false,true}) {
            ItemStack boots=new ItemStack(Items.diamond_boots);if(level>0)boots.addEnchantment(BountifulContent.depthStrider,level);p.inventory.armorInventory[0]=boots;
            p.onGround=ground;p.setSprinting(sprint);float base=sprint?.9F:.8F,depth=level*(ground?1F:.5F);
            double expected=depth==0?(sprint?(double).9F:.800000011920929D):base+(.54600006F-base)*depth/3;
            check(Math.abs(HTGT6Transformer.depthDrag120(.800000011920929D,p)-expected)<1e-8,"Depth Strider composition "+level+"/"+ground+"/"+sprint);
        }
        p.capabilities.isFlying=true;p.setSprinting(true);check(PlayerSwimming120.waterDrag(p,.8F)==.8F,"flight does not get swim drag");
        System.out.println("PASS swimming115: "+checks+" state, steering, entity isolation and Depth Strider checks");
    }
    static final class Player extends EntityPlayer {
        boolean wet,eyes;double lookY;
        Player(World w){super(w,new GameProfile(UUID.randomUUID(),"SwimFixture"));}
        @Override public boolean isInWater(){return wet;}
        @Override public boolean isInsideOfMaterial(Material m){return m==Material.water&&eyes;}
        @Override public Vec3 getLookVec(){return Vec3.createVectorHelper(0,lookY,1);}
        @Override public void addChatMessage(IChatComponent m){}
        @Override public boolean canCommandSenderUseCommand(int l,String c){return false;}
        @Override public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,0,0);}
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}

