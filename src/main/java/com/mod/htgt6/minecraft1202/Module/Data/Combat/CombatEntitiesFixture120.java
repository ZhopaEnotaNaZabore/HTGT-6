package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;

public final class CombatEntitiesFixture120 {
    public static void main(String[] args)throws Exception {
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;
        com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;
        com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        Grid w=BountifulMechanicsFixture120.initialize();cloud(w);bullet(w);shell(w);brewing();arrows(w);gateway(w);levitation(w);plants(w);loot();
        System.out.println("Combat entities OK: cloud timing/radius/NBT, projectile steering/target/owner, six shell boxes and persisted orientation/color");
    }
    public static final class Victim extends EntityPig {
        float damage;
        public Victim(net.minecraft.world.World world){super(world);}
        @Override public boolean attackEntityFrom(net.minecraft.util.DamageSource source,float amount){damage+=amount;return true;}
    }
    private static void cloud(Grid w){
        Victim near=new Victim(w),corner=new Victim(w);near.setPosition(.5,10,0);corner.setPosition(2.5,10,2.5);
        w.loadedEntityList.add(near);w.loadedEntityList.add(corner);
        EntityLingeringCloud cloud=EntityLingeringCloud.dragonBreath(w,0,10,0);
        for(int i=0;i<19;i++)cloud.tickCloud();require(near.damage==0,"breath waits 20 ticks");
        cloud.tickCloud();require(near.damage==6&&corner.damage==0,"half-intensity instant damage and circular radius");
        for(int i=0;i<19;i++)cloud.tickCloud();require(near.damage==6,"reapplication delay");
        cloud.tickCloud();require(near.damage==12,"reapply at 20 ticks");require(cloud.getRadius()>3,"dragon cloud grows");
        cloud.setRadius(1.25F);NBTTagCompound n=new NBTTagCompound();cloud.writeEntityToNBT(n);
        EntityLingeringCloud loaded=new EntityLingeringCloud(w);loaded.readEntityFromNBT(n);near(loaded.getRadius(),1.25,"radius survives reload");
        NBTTagCompound saved=new NBTTagCompound();loaded.writeEntityToNBT(saved);require(saved.getInteger("Age")==40&&saved.getInteger("WaitTime")==20,"lifetime configuration survives reload");
        EntityLingeringCloud orphan=new EntityLingeringCloud(w,null,0);orphan.setPosition(0,10,0);orphan.tickCloud();
        w.loadedEntityList.clear();
    }
    private static void bullet(Grid w){
        EntityPig owner=new EntityPig(w),target=new EntityPig(w);owner.setPosition(0,10,0);target.setPosition(20,10,20);
        w.loadedEntityList.add(owner);w.loadedEntityList.add(target);
        EntityCombatShulkerBullet b=new EntityCombatShulkerBullet(w,owner,target);NBTTagCompound tag=new NBTTagCompound();b.writeEntityToNBT(tag);
        require(tag.getInteger("Dir")>=0&&tag.getInteger("Steps")>=10&&tag.getInteger("Steps")<=50,"axis route and bounded turn interval");
        int nonzero=(tag.getDouble("TXD")!=0?1:0)+(tag.getDouble("TYD")!=0?1:0)+(tag.getDouble("TZD")!=0?1:0);require(nonzero==1,"distant route follows one axis");
        EntityCombatShulkerBullet loaded=new EntityCombatShulkerBullet(w);loaded.readEntityFromNBT(tag);loaded.setPosition(.5,10.5,.5);
        require(loaded.getThrower()==owner,"non-player owner restored by UUID");loaded.tickFlight();require(loaded.motionX!=0||loaded.motionZ!=0,"target route survives reload");
        near(EntityCombatShulkerBullet.acceleratedTarget(.15),.15375,"target acceleration");near(EntityCombatShulkerBullet.steer(0,.15375),.03075,"velocity interpolation");
        near(EntityCombatShulkerBullet.acceleratedTarget(1),1,"target clamp");
        EntityCombatShulkerBullet untargeted=new EntityCombatShulkerBullet(w);untargeted.setPosition(100,100,100);w.loadedEntityList.clear();
        for(int i=0;i<201;i++)untargeted.tickFlight();require(!untargeted.isDead,"no invented 200-tick expiration");
    }
    private static void shell(Grid w){
        for(int face=0;face<6;face++){
            AxisAlignedBB closed=EntityCombatShulker.shellBox(.5,10,.5,face,0),open=EntityCombatShulker.shellBox(.5,10,.5,face,1);
            near((closed.maxX-closed.minX)*(closed.maxY-closed.minY)*(closed.maxZ-closed.minZ),1,"closed shell volume");
            near((open.maxX-open.minX)*(open.maxY-open.minY)*(open.maxZ-open.minZ),2,"open shell volume");
            EntityCombatShulker entity=new EntityCombatShulker(w);entity.setAttachFace(face);entity.setColor(face);
            NBTTagCompound tag=new NBTTagCompound();entity.writeEntityToNBT(tag);EntityCombatShulker restored=new EntityCombatShulker(w);restored.readEntityFromNBT(tag);
            require(restored.getAttachFace()==face&&restored.getColor()==face&&restored.getTotalArmorValue()==20,"face/color/closed armor survive reload");
        }
    }
    private static void brewing(){
        net.minecraft.tileentity.TileEntityBrewingStand stand=new net.minecraft.tileentity.TileEntityBrewingStand();
        for(int i=0;i<3;i++)stand.setInventorySlotContents(i,new net.minecraft.item.ItemStack(net.minecraft.init.Items.potionitem,1,i==0?16389:i==1?16384:0));
        NBTTagCompound tag=new NBTTagCompound();tag.setInteger("CustomPotionColor",0x123456);stand.getStackInSlot(0).setTagCompound(tag);
        stand.setInventorySlotContents(3,new net.minecraft.item.ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.dragonBreath));
        require(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatBrewing120.canBrew(stand),"instant and water splash recipes start");
        require(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatBrewing120.brew(stand),"brewing handled");
        require(stand.getStackInSlot(0).getItem()==com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.lingeringPotion
            &&stand.getStackInSlot(0).getTagCompound().getInteger("CustomPotionColor")==0x123456,"conversion preserves custom NBT");
        require(stand.getStackInSlot(1).getItem()==com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.lingeringPotion
            &&stand.getStackInSlot(2).getItem()==net.minecraft.init.Items.potionitem,"only splash bottles converted");
        require(stand.getStackInSlot(3).getItem()==net.minecraft.init.Items.glass_bottle,"breath bottle returned");
    }
    private static void arrows(Grid w){
        EntityCombatArrow arrow=new EntityCombatArrow(w);NBTTagCompound root=new NBTTagCompound();root.setInteger("CombatType",1);root.setInteger("Potion",5);arrow.readEntityFromNBT(root);
        NBTTagCompound potion=new NBTTagCompound();net.minecraft.nbt.NBTTagList list=new net.minecraft.nbt.NBTTagList();
        list.appendTag(new net.minecraft.potion.PotionEffect(Potion.moveSpeed.id,123,2).writeCustomPotionEffectToNBT(new NBTTagCompound()));
        potion.setTag("CustomPotionEffects",list);potion.setInteger("CustomPotionColor",0x123456);arrow.setPotionTag(potion);
        net.minecraft.item.ItemStack carrier=new net.minecraft.item.ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.lingeringPotion,1,5);carrier.setTagCompound(potion);
        net.minecraft.item.ItemStack crafted=com.mod.htgt6.minecraft1202.Module.Recipes.Combat.RecipeTippedArrow120.result(carrier);
        require(crafted.stackSize==8&&crafted.getTagCompound().getTagList("CustomPotionEffects",10).tagCount()==1&&!crafted.getTagCompound().hasKey("CustomPotionColor"),"arrow recipe copies effects but not display overrides, as in 1.20.2");
        java.util.List<net.minecraft.potion.PotionEffect> effects=arrow.hitEffects();
        require(effects.get(effects.size()-1).getDuration()==123,"custom arrow effects retain duration");
        NBTTagCompound saved=new NBTTagCompound();arrow.writeEntityToNBT(saved);EntityCombatArrow restored=new EntityCombatArrow(w);restored.readEntityFromNBT(saved);
        require(restored.pickupStack().getTagCompound().getInteger("CustomPotionColor")==0x123456,"pickup NBT survives entity reload");
        io.netty.buffer.ByteBuf bytes=io.netty.buffer.Unpooled.buffer();arrow.writeSpawnData(bytes);restored.readSpawnData(bytes);bytes.release();
        require(restored.getCombatType()==1&&restored.getPotionDamage()==5,"client receives arrow variant");
        require(restored.getPotionColor()==0x123456,"client receives custom arrow color");
        arrowExpiry(w,potion);
        EntityLingeringCloud cloud=new EntityLingeringCloud(w,null,0);cloud.setPotionTag(potion);cloud.setPosition(0,10,0);
        Victim victim=new Victim(w);victim.setPosition(.1,10,.1);w.loadedEntityList.add(victim);
        for(int i=0;i<10;i++)cloud.tickCloud();
        require(victim.getActivePotionEffect(Potion.moveSpeed).getDuration()==123,"custom cloud effects retain duration");w.loadedEntityList.clear();
    }
    private static void arrowExpiry(Grid w,NBTTagCompound potion){
        NBTTagCompound tag=new NBTTagCompound();tag.setInteger("CombatType",1);tag.setInteger("Potion",5);
        tag.setTag("PotionTag",potion.copy());tag.setByte("inGround",(byte)1);
        EntityCombatArrow arrow=new EntityCombatArrow(w);arrow.readEntityFromNBT(tag);
        int states=w.entityStates;
        for(int i=0;i<599;i++)arrow.tickPotionState();
        require(arrow.getCombatType()==1&&!arrow.hitEffects().isEmpty(),"custom arrow retains effects at 599 ground ticks");
        arrow.tickPotionState();
        require(arrow.getCombatType()==0&&arrow.hitEffects().isEmpty()&&arrow.getPotionColor()==-1,"custom arrow clears at 600 ground ticks");
        require(arrow.pickupStack().getItem()==net.minecraft.init.Items.arrow&&!arrow.pickupStack().hasTagCompound(),"expired arrow picks up as clean vanilla arrow");
        arrow.tickPotionState();require(w.entityStates==states+1&&w.lastEntityState==0,"expiry burst emitted once");
        NBTTagCompound saved=new NBTTagCompound();arrow.writeEntityToNBT(saved);
        EntityCombatArrow copy=new EntityCombatArrow(w);copy.readEntityFromNBT(saved);
        require(copy.getCombatType()==0&&copy.pickupStack().getItem()==net.minecraft.init.Items.arrow,"expired state survives reload");
        EntityCombatArrow client=new EntityCombatArrow(w);client.readEntityFromNBT(tag);
        client.getDataWatcher().updateWatchedObjectsFromList(arrow.getDataWatcher().getAllWatched());
        require(client.getCombatType()==0&&client.getPotionColor()==-1,"metadata clears already tracked client arrow");
        tag.removeTag("PotionTag");copy.readEntityFromNBT(tag);
        for(int i=0;i<600;i++)copy.tickPotionState();
        require(copy.getCombatType()==1&&copy.getPotionDamage()==5,"base-only potion arrow does not use custom-effect decay");
        tag.setInteger("CombatType",2);tag.setTag("PotionTag",potion.copy());copy.readEntityFromNBT(tag);
        for(int i=0;i<600;i++)copy.tickPotionState();
        require(copy.getCombatType()==2&&copy.pickupStack().getItem()==com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.spectralArrow,"spectral arrow never decays as tipped arrow");
        tag.setInteger("CombatType",1);tag.setByte("inGround",(byte)0);copy.readEntityFromNBT(tag);
        w.isRemote=true;int particles=w.particles;
        copy.tickPotionState();require(w.particles==particles+2,"flying arrow emits two effect particles");
        near(w.particleRed,0x12/255.0,"particle red");near(w.particleGreen,0x34/255.0,"particle green");near(w.particleBlue,0x56/255.0,"particle blue");
        copy.handleHealthUpdate((byte)0);require(w.particles==particles+22,"expiry status emits twenty particles");
        tag.setByte("inGround",(byte)1);copy.readEntityFromNBT(tag);particles=w.particles;
        for(int i=0;i<600;i++)copy.tickPotionState();
        require(copy.getCombatType()==1&&w.particles==particles+120,"client emits one particle every five ground ticks without deciding expiry");
        w.isRemote=false;
        copy.readEntityFromNBT(tag);for(int i=0;i<599;i++)copy.tickPotionState();
        try {
            java.lang.reflect.Field ground=cpw.mods.fml.relauncher.ReflectionHelper.findField(net.minecraft.entity.projectile.EntityArrow.class,"inGround","field_70254_i");
            ground.setBoolean(copy,false);copy.tickPotionState();ground.setBoolean(copy,true);copy.tickPotionState();
        } catch(IllegalAccessException failure){throw new AssertionError(failure);}
        require(copy.getCombatType()==1,"leaving ground resets continuous ground timer");
    }
    private static void gateway(Grid w){
        int frame=0;
        for(int x=-1;x<=1;x++)for(int y=-2;y<=2;y++)for(int z=-1;z<=1;z++){
            boolean bedrock=com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EndMechanics.gatewayFrame(x,y,z);
            if(bedrock)frame++;
            if(y==0)require(!bedrock,"gateway entrance plane stays open");
        }
        require(frame==12,"two bedrock crosses and two caps");
        com.mod.htgt6.minecraft1202.Module.Data.Combat.EndFightData data=new com.mod.htgt6.minecraft1202.Module.Data.Combat.EndFightData();
        java.util.Set<Integer> positions=new java.util.HashSet<Integer>();
        for(int i=0;i<20;i++)require(positions.add(data.nextGateway(1234)),"gateway positions do not repeat");
        require(data.nextGateway(1234)==-1,"gateway ring stops after twenty");
        com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityEndGateway tile=new com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityEndGateway();
        NBTTagCompound tag=new NBTTagCompound(),exit=new NBTTagCompound();exit.setInteger("X",12);exit.setInteger("Y",70);exit.setInteger("Z",34);
        tag.setTag("ExitPortal",exit);tag.setLong("Age",200);tile.readFromNBT(tag);
        require(!tile.isSpawning(),"gateway creation beam ends after two hundred ticks");
        require(tile.receiveClientEvent(1,0)&&tile.isCoolingDown(),"client cooldown event");
        for(int i=0;i<40;i++)tile.updateEntity();require(!tile.isCoolingDown(),"cooldown lasts forty ticks");
        w.setBlock(300,30,0,net.minecraft.init.Blocks.end_stone,0,2);w.setBlock(301,40,0,net.minecraft.init.Blocks.bedrock,0,2);
        int[] safe=com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityEndGateway.findExit(w,301,20,0,2,false);
        require(safe[0]==300&&safe[1]==30,"non-exact gateway exit excludes bedrock and center");
    }
    private static void levitation(Grid w){
        Victim e=new Victim(w);e.motionY=0;
        near(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionLevitation.gravityAdjustment(.08,e),.08,"normal gravity preserved");
        near(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionLevitation.gravityAdjustment(.012,e),.012,"planetary gravity preserved without levitation");
        near(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionLevitation.gravityAdjustment(0,e),0,"zero gravity preserved without levitation");
        e.addPotionEffect(new net.minecraft.potion.PotionEffect(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.levitation.id,200,0));
        near(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionLevitation.gravityAdjustment(.012,e),-.01,"levitation replaces planetary gravity");
        near(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionLevitation.gravityAdjustment(0,e),-.01,"levitation works in zero gravity");
        for(int i=0;i<100;i++)e.motionY=(e.motionY-com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionLevitation.gravityAdjustment(.08,e))*.9800000190734863D;
        require(e.motionY>.045&&e.motionY<.046,"levitation converges upward instead of sinking under legacy gravity");
    }
    private static void plants(Grid w){
        com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockChorusPlant plant=(com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockChorusPlant)com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.chorusPlant;
        w.setBlock(500,20,0,plant,0,2);w.setBlock(501,20,0,plant,0,2);
        require(!plant.canBlockStay(w,500,20,0),"unsupported side branch is not stable");
        w.setBlock(501,19,0,net.minecraft.init.Blocks.end_stone,0,2);require(plant.canBlockStay(w,500,20,0),"side branch with grounded neighbour survives");
        w.setBlock(501,19,0,net.minecraft.init.Blocks.air,0,2);int scheduled=w.scheduled;plant.onNeighborBlockChange(w,500,20,0,net.minecraft.init.Blocks.air);
        require(w.scheduled==scheduled+1&&w.getBlock(500,20,0)==plant,"support loss schedules instead of recursively destroying");
        for(int age=0;age<4;age++)require(com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockBeetrootCrop.age(com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockBeetrootCrop.metadata(age))==age,"beetroot metadata preserves four stages");
        com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockEndRod rod=new com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockEndRod();
        for(int side=0;side<6;side++){
            w.setBlock(600-net.minecraft.util.Facing.offsetsXForSide[side],20-net.minecraft.util.Facing.offsetsYForSide[side],-net.minecraft.util.Facing.offsetsZForSide[side],rod,side,2);
            require(rod.onBlockPlaced(w,600,20,0,side,0,0,0,0)==net.minecraft.util.Facing.oppositeSide[side],"end rods reverse on all six sides");
        }
    }
    private static void loot(){
        java.util.Set<net.minecraft.item.Item> seen=new java.util.HashSet<net.minecraft.item.Item>();boolean mending=false;
        for(int seed=0;seed<2000;seed++){
            net.minecraft.item.ItemStack stack=com.mod.htgt6.minecraft1202.Module.World.Combat.EndCityLoot120.roll(new java.util.Random(seed*7919L));seen.add(stack.getItem());
            require(stack.getItem()!=net.minecraft.init.Items.bread&&stack.getItem()!=net.minecraft.init.Items.rotten_flesh,"no dungeon chest substitutes");
            mending|=net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.mending.effectId,stack)>0;
        }
        require(seen.size()==23&&mending,"all twenty-three main-pool items and treasure Mending are reachable");
        net.minecraft.tileentity.TileEntityChest chest=new net.minecraft.tileentity.TileEntityChest();chest.setInventorySlotContents(0,new net.minecraft.item.ItemStack(net.minecraft.init.Items.apple,7));
        com.mod.htgt6.minecraft1202.Module.World.Combat.EndCityLoot120.fill(chest,new java.util.Random(3));
        require(chest.getStackInSlot(0).getItem()==net.minecraft.init.Items.apple&&chest.getStackInSlot(0).stackSize==7,"loot fill preserves occupied inventory slots");
    }
    private static void near(double a,double b,String message){require(Math.abs(a-b)<.00001,message);}
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
