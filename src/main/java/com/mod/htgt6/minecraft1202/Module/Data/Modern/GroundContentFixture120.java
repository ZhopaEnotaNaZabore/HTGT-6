package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120GildedBlackstone;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MossBonemeal120;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.AxisAlignedBB;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoatSeat;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelPanda120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.RenderPanda120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.PandaTransform120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.TileEntityShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.ShulkerBoxShape112;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.PoiAttachment120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillagePoi120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBell120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageComposter120;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Behavioral tests against an in-memory world; no game window or render context is required. */
public final class GroundContentFixture120 {
    public static void main(String[] args) throws Exception {
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;
        com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;
        com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        // GameData needs only activeModContainer during vanilla registration. Avoid starting FML's GUI loader.
        Class<?> unsafeType=Class.forName("sun.misc.Unsafe");
        Field unsafeField=unsafeType.getDeclaredField("theUnsafe"); unsafeField.setAccessible(true);
        Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,unsafeType.getMethod("allocateInstance",Class.class).invoke(unsafeField.get(null),cpw.mods.fml.common.Loader.class));
        net.minecraft.init.Bootstrap.func_151354_b();
        gold(); roots(); moss(); carpet(); copper(); catalog(); doors(); doorTextures(); doorSounds();
        boatPassengers(); boatLifecycle36(); pandaInterpolation(); shulkerObstruction(); bellHits();
        bellRuntime(); stonecutterCollision(); composterMaturation();
        pandaSpecialPoses();
        pandaTransforms();
        directedShulkerBoxes(); shulkerContracts37(); armorStand38(); banners39(); standAndBanner40();
        treasureRuntime();
        System.out.println("GroundContentFixture120: OK; gold loot, bonemeal, moss patch, carpet support");
    }
    private static void gold() throws Exception {
        float[] chances={0.1F,0.14285715F,0.25F,1F};
        for(int fortune=0;fortune<4;fortune++) {
            require(BlockBackport120GildedBlackstone.nuggetCount(new Fixed(Math.nextAfter(chances[fortune],0),0),fortune)==2,"nuggets below chance threshold");
            if(fortune<3) require(BlockBackport120GildedBlackstone.nuggetCount(new Fixed(chances[fortune],0),fortune)==0,"block at chance threshold");
            require(BlockBackport120GildedBlackstone.nuggetCount(new Fixed(0,3),fortune)==5,"count never multiplied by Fortune");
        }
        require(BlockBackport120GildedBlackstone.nuggetCount(new Fixed(.99F,3),99)==5,"Fortune above three clamps");
        Grid world=grid();
        Field random=World.class.getDeclaredField("rand"); random.setAccessible(true); random.set(world,new Fixed(0,3));
        List<net.minecraft.item.ItemStack> drops=EtFVanillaGapContent120.gildedBlackstone.getDrops(world,0,10,0,0,3);
        require(drops.size()==1&&drops.get(0).getItem()==Items.gold_nugget&&drops.get(0).stackSize==5,"actual Forge getDrops produces one nugget stack");
        require(EtFVanillaGapContent120.gildedBlackstone.canSilkHarvest(world,null,0,10,0,0),"native Silk Touch branch enabled");
    }
    private static void catalog() {
        for(String wood:new String[]{"cherry","mangrove","bamboo"}) {
            com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120WoodFamily family=
                    new com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120WoodFamily(wood);
            for(Block block:new Block[]{family.doubleSlab,family.doorBlock,family.standingSign,family.wallSign,
                    family.hangingSignBlock,family.wallHangingSignBlock}) {
                net.minecraft.item.Item item=new com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical(block);
                require(com.mod.htgt6.client.nei.BackportItemVisibility.isTechnical(item),"hide technical "+wood);
                require(com.mod.htgt6.client.nei.BackportItemVisibility.publicVariants(item).isEmpty(),"no technical variants");
            }
            for(net.minecraft.item.Item item:new net.minecraft.item.Item[]{family.door,family.sign,family.hangingSign,
                    new net.minecraft.item.ItemBlock(family.planks),new net.minecraft.item.ItemBlock(family.stairs)})
                require(com.mod.htgt6.client.nei.BackportItemVisibility.publicVariants(item).size()==1,"one public item "+wood);
        }
        net.minecraft.item.Item concrete=new net.minecraft.item.ItemBlock(new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120ColoredConcrete());
        require(com.mod.htgt6.client.nei.BackportItemVisibility.publicVariants(concrete).size()==16,"all concrete colours retained");
        net.minecraft.item.Item copper=new net.minecraft.item.ItemBlock(EtFVanillaGapContent120.copperBlocks);
        require(com.mod.htgt6.client.nei.BackportItemVisibility.publicVariants(copper).size()==8,"all copper stages and wax retained");
        require(!com.mod.htgt6.client.nei.BackportItemVisibility.isBackport(Items.stick),"vanilla catalog not altered");
        System.out.println("Backport catalog OK: technical forms hidden, public wood items and metadata variants retained");
    }
    private static void doors() throws Exception {
        for(int facing=0;facing<4;facing++) {
            require(!com.mod.htgt6.minecraft1202.Module.Items.Modern.DoorPlacement120.rightHinge(facing,0,false,false,.5,.5),"center click chooses left");
            require(com.mod.htgt6.minecraft1202.Module.Items.Modern.DoorPlacement120.rightHinge(facing,1,false,false,.5,.5),"right obstruction");
            require(!com.mod.htgt6.minecraft1202.Module.Items.Modern.DoorPlacement120.rightHinge(facing,-1,false,false,.5,.5),"left obstruction");
            require(com.mod.htgt6.minecraft1202.Module.Items.Modern.DoorPlacement120.rightHinge(facing,0,true,false,.5,.5),"adjacent left lower door");
            require(!com.mod.htgt6.minecraft1202.Module.Items.Modern.DoorPlacement120.rightHinge(facing,0,false,true,.5,.5),"adjacent right lower door");
        }
        double[][] clicks={{.5,.75},{.25,.5},{.5,.25},{.75,.5}};
        for(int f=0;f<4;f++)require(com.mod.htgt6.minecraft1202.Module.Items.Modern.DoorPlacement120.rightHinge(f,0,false,false,clicks[f][0],clicks[f][1]),"click selects right hinge "+f);
        Class<?> type=Class.forName("sun.misc.Unsafe");Field uf=type.getDeclaredField("theUnsafe");uf.setAccessible(true);
        net.minecraft.entity.player.EntityPlayer player=(net.minecraft.entity.player.EntityPlayer)type.getMethod("allocateInstance",Class.class)
                .invoke(uf.get(null),net.minecraft.entity.player.EntityPlayerMP.class);
        Field caps=net.minecraft.entity.player.EntityPlayer.class.getDeclaredField("capabilities");caps.setAccessible(true);
        caps.set(player,new net.minecraft.entity.player.PlayerCapabilities());
        for(net.minecraft.item.Item item:new net.minecraft.item.Item[]{Backport120Content.cherryDoor,BountifulContent.woodDoorItems[0]}) {
            Grid world=grid();net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(item,3);
            world.setBlock(0,11,0,Blocks.stone,0,3);world.powered=true;
            require(item.onItemUse(stack,player,world,0,11,0,5,1,.5F,.25F),"side-face placement through real item callback");
            require(world.getBlock(1,11,0) instanceof net.minecraft.block.BlockDoor,"lower door placed");
            require((world.getBlockMetadata(1,11,0)&4)!=0,"initial powered door is open");
            require(world.getBlockMetadata(1,12,0)==15&&stack.stackSize==2,"upper half, right hinge, initialized power and one consumed item");
            net.minecraft.block.BlockDoor door=(net.minecraft.block.BlockDoor)world.getBlock(1,11,0);
            door.onBlockActivated(world,1,11,0,player,1,0,0,0);
            require((world.getBlockMetadata(1,11,0)&4)==0,"manual close under power");
            door.onNeighborBlockChange(world,1,12,0,Blocks.stone);
            require((world.getBlockMetadata(1,11,0)&4)==0,"steady power must not undo manual close");
            require((door.func_150012_g(world,1,11,0)&16)!=0,"extra upper bits preserve hinge decoding");
            Grid reloaded=grid();reloaded.cells.putAll(world.cells);reloaded.powered=true;
            door.onNeighborBlockChange(reloaded,1,11,0,Blocks.redstone_block);
            require((reloaded.getBlockMetadata(1,11,0)&4)==0,"stored power survives world reload");
            world.powered=false;door.onNeighborBlockChange(world,1,11,0,Blocks.redstone_block);
            require(world.getBlockMetadata(1,12,0)==13,"falling edge stores unpowered state and hinge");
            door.onBlockActivated(world,1,11,0,player,1,0,0,0);
            door.onNeighborBlockChange(world,1,11,0,Blocks.stone);
            require((world.getBlockMetadata(1,11,0)&4)!=0,"unpowered manual open survives unrelated update");
            world.powered=true;door.onNeighborBlockChange(world,1,12,0,Blocks.redstone_block);
            world.powered=false;door.onNeighborBlockChange(world,1,12,0,Blocks.redstone_block);
            require((world.getBlockMetadata(1,11,0)&4)==0,"power falling edge closes door through upper half");
            world.setBlockMetadataWithNotify(1,12,0,9,2);world.setBlockMetadataWithNotify(1,11,0,5,2);
            door.onNeighborBlockChange(world,1,11,0,Blocks.stone);
            require(world.getBlockMetadata(1,11,0)==5&&world.getBlockMetadata(1,12,0)==13,"old save initializes power without destroying manual open");
            world=grid();world.isRemote=true;
            require(item.onItemUse(stack,player,world,0,10,0,1,.25F,1,.5F),"client accepts valid placement");
            require(world.isAirBlock(0,11,0)&&stack.stackSize==2,"client does not place or consume");
            world=grid();world.setBlock(0,12,0,Blocks.stone,0,3);
            require(!item.onItemUse(stack,player,world,0,10,0,1,.25F,1,.5F)&&stack.stackSize==2,"blocked upper half rejects placement");
        }
        System.out.println("Door placement OK: hinge directions, obstruction, pairs, side clicks, power, client guard, upper half");
    }
    private static void doorTextures() throws Exception {
        final java.util.List<String> names=new ArrayList<String>();
        net.minecraft.client.renderer.texture.IIconRegister register=new net.minecraft.client.renderer.texture.IIconRegister(){
            public net.minecraft.util.IIcon registerIcon(final String name){
                names.add(name);
                return (net.minecraft.util.IIcon)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{net.minecraft.util.IIcon.class},new InvocationHandler(){
                    public Object invoke(Object proxy,Method method,Object[] args){
                        String m=method.getName();
                        if(m.equals("getIconName"))return name;
                        if(m.equals("getMinU")||m.equals("getMinV"))return 0F;
                        if(m.equals("getMaxU")||m.equals("getMaxV"))return 1F;
                        if(m.equals("getInterpolatedU")||m.equals("getInterpolatedV"))return ((Double)args[0]).floatValue()/16F;
                        return 16;
                    }
                });
            }
        };
        Block reference=Blocks.wooden_door;reference.registerBlockIcons(register);
        for(String wood:new String[]{"spruce","birch","jungle","acacia","big_oak"}) {
            Block door=new com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulWoodDoor(wood);
            door.registerBlockIcons(register);
            String texture="htgt6:bountiful/door_"+(wood.equals("big_oak")?"dark_oak":wood);
            require(names.contains(texture+"_upper")&&names.contains(texture+"_lower"),"same registered texture paths");
            Grid world=grid(),vanilla=grid();
            for(int facing=0;facing<4;facing++)for(int open=0;open<2;open++)for(int hinge=0;hinge<2;hinge++)for(int power=0;power<2;power++) {
                world.setBlock(0,11,0,door,facing|open*4,2);world.setBlock(0,12,0,door,12|hinge|power*2,2);
                vanilla.setBlock(0,11,0,reference,facing|open*4,2);vanilla.setBlock(0,12,0,reference,8|hinge,2);
                for(int half=0;half<2;half++)for(int face=0;face<6;face++) {
                    net.minecraft.util.IIcon actual=door.getIcon(world,0,11+half,0,face);
                    net.minecraft.util.IIcon expected=reference.getIcon(vanilla,0,11+half,0,face);
                    require(actual.getMinU()==expected.getMinU()&&actual.getMaxU()==expected.getMaxU(),"door face mirroring");
                    require(actual.getIconName().endsWith(half==1&&face>1?"_upper":"_lower"),"correct half texture");
                }
            }
        }
        System.out.println("Door textures OK: five woods, both halves, all facings/open/hinge/power states and six faces");
    }
    private static void doorSounds() throws Exception {
        for(String wood:new String[]{"cherry","bamboo","mangrove"}) {
            Grid world=grid();
            net.minecraft.block.BlockDoor door=new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Door(wood+"_door");
            world.setBlock(0,11,0,door,0,2);world.setBlock(0,12,0,door,12,2);
            door.onBlockActivated(world,0,12,0,null,1,0,0,0);
            String family=wood.equals("mangrove")?"wooden":wood+"_wood";
            require(world.soundCount==1&&world.lastSound.equals("htgt6:block."+family+"_door.open"),"family opening sound");
            require(world.soundPitch>=.9F&&world.soundPitch<1F,"reference pitch range");
            door.func_150014_a(world,0,11,0,false);
            require(world.soundCount==2&&world.lastSound.endsWith(".close"),"closing sound");
            door.func_150014_a(world,0,11,0,false);
            require(world.soundCount==2&&world.auxSoundCount==0,"no duplicate or legacy random door event");
            world.isRemote=true;door.onBlockActivated(world,0,11,0,null,1,0,0,0);
            require(world.soundCount==2&&world.getBlockMetadata(0,11,0)==0,"client does not duplicate state or sound");
        }
        System.out.println("Door sounds OK: family, open/close, pitch, no-op and client guards");
    }
    private static void boatPassengers() throws Exception {
        Grid w=grid(); EntityCombatBoat boat=new EntityCombatBoat(null);boat.worldObj=w;w.entities.add(boat);
        EntityCombatBoatSeat seat=new EntityCombatBoatSeat(null);seat.worldObj=w;seat.setBoat(boat);w.entities.add(seat);
        Entity first=new EntityBoat(null),second=new EntityBoat(null);first.worldObj=w;second.worldObj=w;
        first.mountEntity(boat);second.mountEntity(seat);boat.setPosition(2,14,3);seat.updateSeat();
        for(int yaw:new int[]{0,90,180,270}) {
            boat.rotationYaw=yaw;boat.updateRiderPosition();seat.updateRiderPosition();double angle=Math.toRadians(yaw);
            require(Math.abs(first.posX-(2-Math.sin(angle)*.2))<1E-6&&Math.abs(first.posZ-(3+Math.cos(angle)*.2))<1E-6,"driver longitudinal offset "+yaw);
            require(Math.abs(second.posX-(2+Math.sin(angle)*.6))<1E-6&&Math.abs(second.posZ-(3-Math.cos(angle)*.6))<1E-6,"second longitudinal offset "+yaw);
            require(Math.abs(second.posY-(boat.posY+boat.getMountedYOffset()+second.getYOffset()))<1E-6,"native rider height");
        }
        first.mountEntity(null);seat.updateSeat();
        require(boat.riddenByEntity==second&&second.ridingEntity==boat&&seat.isDead,"second rider promoted when driver leaves");
        boat.updateRiderPosition();require(second.posX==boat.posX&&second.posZ==boat.posZ,"solo rider centered");
        EntityCombatBoatSeat replacement=new EntityCombatBoatSeat(null);replacement.worldObj=w;replacement.setBoat(boat);w.entities.add(replacement);
        first.mountEntity(replacement);replacement.updateSeat();boat.setDead();
        require(replacement.isDead&&first.ridingEntity==null,"boat removal cleans seat and passenger");
        require(second.ridingEntity==null&&boat.riddenByEntity==null,"all removal paths release driver");
        for(boolean creative:new boolean[]{false,true})for(boolean drops:new boolean[]{false,true})
            require(EntityCombatBoat.shouldDropBoat(creative,drops)==(!creative&&drops),"creative/entity drop policy");
        net.minecraft.world.GameRules rules=new net.minecraft.world.GameRules();
        require(EntityCombatBoat.entityDropsEnabled(rules),"missing modern gamerule defaults true on 1.7");
        rules.addGameRule("doEntityDrops","false");require(!EntityCombatBoat.entityDropsEnabled(rules),"explicit false suppresses boat drop");
        rules.setOrCreateGameRule("doEntityDrops","true");require(EntityCombatBoat.entityDropsEnabled(rules),"explicit true restores boat drop");
        EntityCombatBoat another=new EntityCombatBoat(null);another.worldObj=w;w.entities.add(another);
        EntityCombatBoatSeat deadSeat=new EntityCombatBoatSeat(null);deadSeat.worldObj=w;deadSeat.setBoat(another);w.entities.add(deadSeat);
        Entity deadPassenger=new EntityBoat(null);deadPassenger.worldObj=w;deadPassenger.mountEntity(deadSeat);deadPassenger.isDead=true;deadSeat.updateSeat();
        require(another.riddenByEntity==null&&deadPassenger.ridingEntity==null&&deadSeat.riddenByEntity==null,"dead passenger cannot become driver");
        System.out.println("Boat passengers OK: cardinal positions, native height, promotion, removal");
    }
    private static void boatLifecycle36() throws Exception {
        Grid w=grid(); EntityCombatBoat boat=new EntityCombatBoat(null);boat.worldObj=w;w.entities.add(boat);
        Entity first=new EntityBoat(null),second=new EntityBoat(null),third=new EntityBoat(null);
        first.worldObj=w;second.worldObj=w;third.worldObj=w;
        require(boat.boardPassenger(first),"empty boat accepts driver");
        require(!boat.boardPassenger(first)&&first.ridingEntity==boat,"repeated boarding does not toggle dismount");
        w.rejectSpawn=true;
        require(!boat.boardPassenger(second)&&second.ridingEntity==null,"cancelled seat spawn cannot strand rider");
        w.rejectSpawn=false;
        require(boat.boardPassenger(second),"second passenger gets live registered seat");
        EntityCombatBoatSeat seat=(EntityCombatBoatSeat)second.ridingEntity;
        require(w.entities.contains(seat)&&seat.getBoat()==boat,"spawned seat belongs to boat");
        require(!boat.boardPassenger(second)&&second.ridingEntity==seat,"repeated second boarding preserves seat");
        require(!boat.boardPassenger(third)&&third.ridingEntity==null,"full boat rejects third passenger");
        require(boat.getCollisionBox(first)==null&&boat.getCollisionBox(second)==null&&boat.getCollisionBox(seat)==null,
                "own passengers and helper seat excluded from collision");
        require(boat.getCollisionBox(third)!=null,"unrelated entity retains collision");
        second.setPosition(0.5,0,0.5);boat.applyEntityCollision(second);
        require(boat.motionX==0&&boat.motionZ==0&&second.motionX==0&&second.motionZ==0,"own passenger cannot push boat");
        java.lang.reflect.Method fall=EntityBoat.class.getDeclaredMethod("updateFallState",double.class,boolean.class);fall.setAccessible(true);
        for(int variant=1;variant<=5;variant++)for(boolean drops:new boolean[]{true,false}) {
            Grid land=grid();land.rules.addGameRule("doEntityDrops",Boolean.toString(drops));
            EntityCombatBoat falling=new EntityCombatBoat(null);falling.worldObj=land;falling.setVariant(variant);land.entities.add(falling);
            require(falling.getPickedResult(null).getItem()==com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.boatItem(variant),
                    "pick block preserves wood variant "+variant);
            falling.fallDistance=4;fall.invoke(falling,-4D,true);
            require(falling.isDead,"legacy fall break still removes boat");
            int planks=0,sticks=0;
            for(Entity entity:land.entities)if(entity instanceof net.minecraft.entity.item.EntityItem) {
                net.minecraft.item.ItemStack stack=((net.minecraft.entity.item.EntityItem)entity).getEntityItem();
                if(stack.getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.planks)) {
                    require(stack.getItemDamage()==variant,"fall planks preserve variant "+variant);planks+=stack.stackSize;
                } else if(stack.getItem()==net.minecraft.init.Items.stick)sticks+=stack.stackSize;
            }
            require(planks==(drops?3:0)&&sticks==(drops?2:0),"fall drop count and gamerule "+variant+"/"+drops);
        }
        System.out.println("Boat lifecycle 36 OK: boarding, cancelled spawn, passenger collisions, pick variants, real fall drops");
    }
    private static void pandaInterpolation() throws Exception {
        EntityPanda120 panda=new EntityPanda120(null);ModelPanda120 model=new ModelPanda120();
        setFloat(panda,"sitAmountOld",0);setFloat(panda,"sitAmount",1);
        for(float partial:new float[]{0,.5F,1}) {
            model.setLivingAnimations(panda,0,0,partial);model.setRotationAngles(0,0,0,0,0,1,panda);
            float start=(float)Math.PI/2;
            require(Math.abs(model.body.rotateAngleX-(start+(1.7407963F-start)*partial))<1E-6,"panda partial tick "+partial);
        }
        setFloat(panda,"onBackAmountOld",1);setFloat(panda,"onBackAmount",0);
        require(!panda.isOnBack()&&RenderPanda120.backRotation(panda,.5F)==90,"back transition persists after flag clears");
        EntityPanda120 other=new EntityPanda120(null);model.setLivingAnimations(other,0,0,.5F);model.setRotationAngles(0,0,0,0,0,1,other);
        require(model.body.rotateAngleX==(float)Math.PI/2,"shared model resets between pandas");
        System.out.println("Panda interpolation OK: tick endpoints, midpoint, return transition, shared model reset");
    }
    private static void pandaSpecialPoses() throws Exception {
        EntityPanda120 panda=new EntityPanda120(null);ModelPanda120 model=new ModelPanda120();
        panda.setSneezing(true);
        int[] sneezeTicks={0,7,14,15,19,20};float[] sneezeAngles={0,-.3926991F,-.7853982F,-.7853982F,-.7853982F,0};
        for(int i=0;i<sneezeTicks.length;i++) {
            setPandaCounter(panda,"setSneezeCounter",sneezeTicks[i]);pose(model,panda,0,1);
            near(model.head.rotateAngleX,sneezeAngles[i],"sneeze phase "+sneezeTicks[i]);
        }
        panda.setSneezing(false);panda.setUnhappyCounter(10);pose(model,panda,(float)Math.PI/1.2F,1);
        near(model.head.rotateAngleY,.35F,"unhappy head yaw");near(model.head.rotateAngleZ,.35F,"unhappy head roll");
        require(model.rightFrontLeg.rotateAngleX<0&&model.leftFrontLeg.rotateAngleX>0,"unhappy opposite forelegs");
        panda.setUnhappyCounter(0);pose(model,panda,0,1);near(model.head.rotateAngleZ,0,"head reset");
        System.out.println("Panda head gestures OK: sneeze boundaries, unhappy motion, reset");

        setFloat(panda,"sitAmount",1);pose(model,panda,0,1);
        near(model.body.rotateAngleX,1.7407963F,"sitting body");near(model.head.rotateAngleX,1.5707964F,"sitting head");
        near(model.rightFrontLeg.rotateAngleZ,-.27079642F,"sitting right foreleg Z");near(model.leftFrontLeg.rotateAngleZ,.27079642F,"sitting left foreleg Z");
        near(model.rightHindLeg.rotateAngleZ,.5707964F,"sitting right hindleg Z");near(model.leftHindLeg.rotateAngleZ,-.5707964F,"sitting left hindleg Z");
        near(model.rightHindLeg.rotateAngleX,0,"sitting does not replace hindleg X");
        model.setLivingAnimations(panda,0,0,.5F);model.setRotationAngles(0,0,0,0,-180,1,panda);
        near(model.head.rotateAngleX,-3.9269908F,"shortest angular path across pi");
        System.out.println("Panda sitting OK: reference axes, angles and wrapped interpolation");

        setPandaCounter(panda,"setEatCounter",1);pose(model,panda,(float)Math.PI/1.2F,1);
        near(model.head.rotateAngleX,1.7707964F,"eating head");near(model.rightFrontLeg.rotateAngleX,-.6F,"eating right foreleg");near(model.leftFrontLeg.rotateAngleX,-.6F,"eating left foreleg");
        Grid w=grid();w.thundering=true;panda.worldObj=w;panda.setMainGene(EntityPanda120.Gene.WORRIED);panda.setHiddenGene(EntityPanda120.Gene.WORRIED);
        pose(model,panda,1,1);near(model.head.rotateAngleX,2.1707964F,"fear overrides eating");near(model.rightFrontLeg.rotateAngleX,-.9F,"fear covers face");
        w.thundering=false;pose(model,panda,0,1);near(model.head.rotateAngleX,1.5707964F,"fear clears with weather");
        setPandaCounter(panda,"setEatCounter",0);setFloat(panda,"sitAmount",0);pose(model,panda,0,1);
        near(model.rightFrontLeg.rotateAngleZ,0,"sitting Z resets");
        System.out.println("Panda eating/fear OK: synchronized forelegs, priority and weather reset");

        setFloat(panda,"onBackAmount",1);pose(model,panda,10,1);
        near(model.rightHindLeg.rotateAngleX,(float)(-.6*Math.sin(1.5)),"back hindleg frequency");
        near(model.rightFrontLeg.rotateAngleX,(float)(.3*Math.sin(2.5)),"back foreleg frequency");
        near(model.leftHindLeg.rotateAngleX,-model.rightHindLeg.rotateAngleX,"back hindleg opposition");near(model.leftFrontLeg.rotateAngleX,-model.rightFrontLeg.rotateAngleX,"back foreleg opposition");
        near(model.head.rotateAngleX,1.5707964F,"back head target");
        System.out.println("Panda on-back OK: two limb frequencies and head target");

        setFloat(panda,"onBackAmount",0);setFloat(panda,"rollAmount",1);pose(model,panda,(float)Math.PI,1);
        near(model.head.rotateAngleX,2.0561945F,"rolling head");near(model.rightHindLeg.rotateAngleX,-.5F,"rolling hindleg");near(model.rightFrontLeg.rotateAngleX,.5F,"rolling foreleg");
        panda.setGrowingAge(-100);pose(model,panda,(float)Math.PI,1);near(model.head.rotateAngleX,0,"baby excludes adult roll pose");near(model.rightHindLeg.rotateAngleX,0,"baby roll limbs unchanged");
        EntityPanda120 next=new EntityPanda120(null);pose(model,next,10,1);near(model.head.rotateAngleX,0,"shared model resets special head");near(model.rightFrontLeg.rotateAngleX,0,"shared model resets special limbs");
        System.out.println("Panda roll OK: adult pose, baby exclusion, shared-model reset");
    }
    private static void pandaTransforms() throws Exception {
        EntityPanda120 server=new EntityPanda120(null),client=new EntityPanda120(null);
        Method tick=EntityPanda120.class.getDeclaredMethod("tickRoll");tick.setAccessible(true);
        server.rotationPitch=12;server.setRolling(true);
        for(int i=1;i<=32;i++) {
            tick.invoke(server);require(server.getRollCounter()==i,"server roll tick "+i);
            client.getDataWatcher().updateWatchedObjectsFromList(server.getDataWatcher().getAllWatched());
            require(client.getRollCounter()==i,"roll counter watcher transfer "+i);
        }
        tick.invoke(server);require(server.getRollCounter()==0&&!server.isRolling(),"roll finishes/reset");near(server.rotationPitch,12,"roll does not corrupt look pitch");
        System.out.println("Panda roll protocol OK: 32 ticks, watcher transfer, reset, independent pitch");
        EntityPanda120 panda=new EntityPanda120(null);
        int[] boundaries={7,8,15,16,23,24,31};float[] angles={-90,-90,-180,-180,-270,-270,-360};
        float[] adultHeights={1,1,1.6F,1.6F,.8F,.8F,0},babyHeights={.5F,.5F,.6F,.6F,.3F,.3F,0};
        for(boolean baby:new boolean[]{false,true}) {
            panda.setGrowingAge(baby?-100:0);
            for(int i=0;i<boundaries.length;i++) {
                panda.getDataWatcher().updateObject(EntityPanda120.ROLL_COUNTER_WATCHER,Integer.valueOf(boundaries[i]));
                TransformTrace trace=new TransformTrace();PandaTransform120.body(panda,0,trace);
                require(trace.ops.size()==2&&trace.ops.get(0)[0]==0&&trace.ops.get(1)[0]==1,"roll translate precedes rotation");
                near(trace.ops.get(0)[2],baby?babyHeights[i]:adultHeights[i],"roll support height");near(trace.ops.get(1)[1],angles[i],"roll quadrant");
            }
        }
        panda.setGrowingAge(0);panda.getDataWatcher().updateObject(EntityPanda120.ROLL_COUNTER_WATCHER,Integer.valueOf(7));
        TransformTrace trace=new TransformTrace();PandaTransform120.body(panda,.9F,trace);near(trace.ops.get(1)[1],-90,"boundary interpolation clamp");
        panda.getDataWatcher().updateObject(EntityPanda120.ROLL_COUNTER_WATCHER,Integer.valueOf(1));trace=new TransformTrace();PandaTransform120.body(panda,.5F,trace);near(trace.ops.get(1)[1],-135F/7,"roll midpoint");
        panda.setRolling(false);trace=new TransformTrace();PandaTransform120.body(panda,1,trace);require(trace.ops.isEmpty(),"no stale roll transform");
        System.out.println("Panda roll transforms OK: four phases, adult/baby heights, interpolation boundary");

        setFloat(panda,"sitAmount",1);trace=new TransformTrace();PandaTransform120.body(panda,.5F,trace);
        require(trace.ops.size()==3,"sitting transform count");near(trace.ops.get(0)[2],.4F,"sit first translation");near(trace.ops.get(1)[1],45,"sit rotation");near(trace.ops.get(2)[2],-.5F,"sit pivot translation");
        Grid w=grid();w.thundering=true;panda.worldObj=w;panda.setMainGene(EntityPanda120.Gene.WORRIED);panda.setHiddenGene(EntityPanda120.Gene.WORRIED);panda.setGrowingAge(-100);
        trace=new TransformTrace();PandaTransform120.body(panda,1,trace);require(trace.ops.size()==5,"scared baby transforms");near(trace.ops.get(3)[3],1,"fear rotates around Y");near(trace.ops.get(4)[3],.55F,"scared baby Z offset");
        System.out.println("Panda sitting transforms OK: pivot order, partial tick, fear and baby adjustment");

        setFloat(panda,"sitAmount",0);setFloat(panda,"onBackAmount",1);w.thundering=false;
        for(boolean baby:new boolean[]{false,true}) {
            panda.setGrowingAge(baby?-100:0);trace=new TransformTrace();PandaTransform120.body(panda,.5F,trace);
            near(trace.ops.get(0)[2],baby?.25F:.65F,"back support height");near(trace.ops.get(1)[1],90,"back half angle");
        }
        System.out.println("Panda on-back transform OK: 180 degree target and adult/baby support");

        panda.setCurrentItemOrArmor(0,new net.minecraft.item.ItemStack(Items.apple));panda.setSitting(true);
        require(PandaTransform120.showsFood(panda),"food shown sitting");w.thundering=true;require(!PandaTransform120.showsFood(panda),"food hidden during fear");w.thundering=false;
        trace=new TransformTrace();PandaTransform120.food(panda,0,trace);near(trace.ops.get(0)[2],1.4F,"resting food Y");near(trace.ops.get(0)[3],-.6F,"resting food Z");
        setPandaCounter(panda,"setEatCounter",1);trace=new TransformTrace();PandaTransform120.food(panda,(float)Math.PI/1.2F,trace);near(trace.ops.get(0)[2],1.31F,"chewing food Y");near(trace.ops.get(0)[3],-1F,"chewing food Z");
        panda.setSitting(false);require(!PandaTransform120.showsFood(panda),"standing food hidden");
        System.out.println("Panda food anchor OK: rest, chewing and fear/standing visibility");
    }
    private static final class TransformTrace implements PandaTransform120.Sink {
        final List<float[]> ops=new ArrayList<float[]>();
        public void translate(float x,float y,float z){ops.add(new float[]{0,x,y,z});}
        public void rotate(float angle,float x,float y,float z){ops.add(new float[]{1,angle,x,y,z});}
    }
    private static void pose(ModelPanda120 model,EntityPanda120 panda,float age,float partial) {
        model.setLivingAnimations(panda,0,0,partial);model.setRotationAngles(0,0,age,0,0,1,panda);
    }
    private static void near(float actual,float expected,String message){require(Math.abs(actual-expected)<.0001F,message+": "+actual+" != "+expected);}
    private static void setPandaCounter(EntityPanda120 panda,String method,int value)throws Exception {
        Method setter=EntityPanda120.class.getDeclaredMethod(method,int.class);setter.setAccessible(true);setter.invoke(panda,value);
    }
    private static void setFloat(Object value,String name,float amount)throws Exception {
        Field field=value.getClass().getDeclaredField(name);field.setAccessible(true);field.setFloat(value,amount);
    }
    private static void shulkerObstruction() throws Exception {
        Grid w=grid();TileEntityShulkerBox112 box=new TileEntityShulkerBox112();
        require(BlockShulkerBox112.canOpen(w,0,11,0,box),"air above box");
        w.setBlock(0,12,0,Blocks.stone,0,2);require(!BlockShulkerBox112.canOpen(w,0,11,0,box),"full block obstructs lid");
        w.setBlock(0,12,0,Blocks.stone_slab,0,2);require(!BlockShulkerBox112.canOpen(w,0,11,0,box),"lower slab obstructs lid");
        w.setBlock(0,12,0,Blocks.stone_slab,8,2);require(BlockShulkerBox112.canOpen(w,0,11,0,box),"upper slab clears lid sweep");
        w.setBlock(0,12,0,Blocks.tallgrass,1,2);require(BlockShulkerBox112.canOpen(w,0,11,0,box),"noncolliding vegetation");
        EntityBoat obstacle=new EntityBoat(null);obstacle.setPosition(.5,12.2,.5);w.entities.add(obstacle);
        require(!BlockShulkerBox112.canOpen(w,0,11,0,box),"hard entity obstructs lid");w.entities.clear();
        box.openInventory();box.updateEntity();box.updateEntity();w.setBlock(0,12,0,Blocks.stone,0,2);
        require(BlockShulkerBox112.canOpen(w,0,11,0,box),"already opening box remains accessible");
        box.closeInventory();box.updateEntity();require(BlockShulkerBox112.canOpen(w,0,11,0,box),"closing box remains accessible");
        box.updateEntity();require(!BlockShulkerBox112.canOpen(w,0,11,0,box),"fully closed box checks obstruction again");
        System.out.println("Shulker obstruction OK: blocks, slabs, vegetation, hard entity, animation states");
    }
    private static void bellHits() {
        int[] allowed={12,48,12,48,60,60,60,60,48,12,48,12,48,12,48,12};
        for(int meta=0;meta<16;meta++)for(int side=0;side<6;side++) {
            boolean expected=(allowed[meta]&(1<<side))!=0;
            require(PoiAttachment120.canRing(meta,side,.5F)==expected,"bell face "+meta+":"+side);
            require(PoiAttachment120.canRing(meta,side,.8124F)==expected,"bell height boundary");
            require(!PoiAttachment120.canRing(meta,side,Math.nextUp(.8124F)),"bell rejects above waist");
        }
        System.out.println("Bell hit rules OK: 16 orientations, 6 faces, height boundary");
    }
    private static void bellRuntime() throws Exception {
        Grid w=grid();BlockVillagePoi120 bell=new BlockVillagePoi120("bell","meeting",net.minecraft.block.material.Material.iron,5);
        TileEntityVillageBell120 tile=new TileEntityVillageBell120();w.tile=tile;w.setBlock(0,11,0,bell,0,2);
        bell.onBlockActivated(w,0,11,0,null,2,.5F,.5F,.5F);
        require(w.soundCount==1&&"htgt6:block.bell.use".equals(w.lastSound)&&w.soundVolume==2&&w.soundPitch==1,"official bell sound");
        bell.onBlockActivated(w,0,11,0,null,1,.5F,.5F,.5F);require(w.soundCount==1,"invalid manual hit silent");
        w.powered=true;bell.onNeighborBlockChange(w,0,11,0,Blocks.stone);require(w.soundCount==2,"rising power rings");
        tile.updateEntity();bell.onNeighborBlockChange(w,0,11,0,Blocks.stone);require(w.soundCount==2&&tile.getRingTicks(0)==1,"steady power does not restart animation");
        w.powered=false;bell.onNeighborBlockChange(w,0,11,0,Blocks.stone);require(w.soundCount==2,"falling power silent");
        w.powered=true;bell.onNeighborBlockChange(w,0,11,0,Blocks.stone);require(w.soundCount==3,"second pulse rings");
        w.isRemote=true;bell.onBlockActivated(w,0,11,0,null,2,.5F,.5F,.5F);bell.onNeighborBlockChange(w,0,11,0,Blocks.stone);require(w.soundCount==3,"client silent");w.isRemote=false;
        Method mapping=TileEntity.class.getDeclaredMethod("addMapping",Class.class,String.class);mapping.setAccessible(true);mapping.invoke(null,TileEntityVillageBell120.class,"fixture_bell30");
        NBTTagCompound saved=new NBTTagCompound();tile.writeToNBT(saved);
        require(saved.getBoolean("Powered120")&&!saved.hasKey("Ringing120"),"persist power but not animation");
        saved.setBoolean("Ringing120",true);saved.setInteger("RingTicks120",23);
        TileEntityVillageBell120 loaded=new TileEntityVillageBell120();loaded.readFromNBT(saved);
        require(!loaded.isRinging()&&!loaded.updatePower(true),"reload ignores stale animation and retains power edge");
        loaded.onDataPacket(null,(net.minecraft.network.play.server.S35PacketUpdateTileEntity)tile.getDescriptionPacket());
        require(loaded.isRinging()&&loaded.getRingDirection()==tile.getRingDirection(),"animation network packet remains functional");
        for(int i=0;i<50;i++)loaded.updateEntity();require(!loaded.isRinging(),"ring ends at 50 ticks");
        w.tile=null;bell.onBlockActivated(w,0,11,0,null,2,.5F,.5F,.5F);require(w.soundCount==3,"missing tile silent");
        System.out.println("Bell runtime OK: sound, power edges, client guard, persistent power, transient animation and packet");
    }
    private static void stonecutterCollision() throws Exception {
        Grid w=grid();BlockVillagePoi120 cutter=new BlockVillagePoi120("stonecutter","stonecutter",net.minecraft.block.material.Material.rock,3);
        for(int facing=0;facing<4;facing++) {
            w.setBlock(0,11,0,cutter,facing,2);cutter.setBlockBounds(0,0,0,1,1,1);
            List<AxisAlignedBB> collisions=new ArrayList<AxisAlignedBB>();
            cutter.addCollisionBoxesToList(w,0,11,0,AxisAlignedBB.getBoundingBox(0,11,0,1,12,1),collisions,null);
            require(collisions.size()==1&&collisions.get(0).maxY==11+9D/16,"stonecutter native collision without render "+facing);
            collisions.clear();cutter.addCollisionBoxesToList(w,0,11,0,AxisAlignedBB.getBoundingBox(0,11.6,0,1,12,1),collisions,null);
            require(collisions.isEmpty(),"no invisible full cube above stonecutter");
        }
        System.out.println("Stonecutter collision OK: all facings, stale bounds, free space above 9/16");
    }
    private static void composterMaturation() throws Exception {
        BlockVillagePoi120 composter=new BlockVillagePoi120("composter","farmer",net.minecraft.block.material.Material.wood,1);
        for(int level=0;level<=8;level++) {
            Grid w=grid();w.setBlock(0,11,0,composter,level,2);
            TileEntityVillageComposter120 tile=new TileEntityVillageComposter120();tile.setWorldObj(w);tile.yCoord=11;
            tile.updateEntity();tile.updateEntity();require(w.scheduled==(level==7?1:0),"only loaded level 7 schedules once");
            if(level==7){require(w.delay==20,"reference maturation delay");composter.updateTick(w,0,11,0,new Random());require(w.getBlockMetadata(0,11,0)==8,"matures into extractable level");}
        }
        Grid client=grid();client.isRemote=true;client.setBlock(0,11,0,composter,7,2);
        TileEntityVillageComposter120 tile=new TileEntityVillageComposter120();tile.setWorldObj(client);tile.yCoord=11;tile.updateEntity();composter.updateTick(client,0,11,0,new Random());
        require(client.scheduled==0&&client.getBlockMetadata(0,11,0)==7,"client cannot mature compost");
        System.out.println("Composter recovery OK: levels 0..8, one 20-tick schedule, maturity and client guard");
    }
    private static void directedShulkerBoxes() throws Exception {
        BlockShulkerBox112 block=new BlockShulkerBox112("purple");
        require(ShulkerBoxShape112.direction(0)==ForgeDirection.UP,"old boxes remain upright");
        for(int side=0;side<6;side++) {
            Grid w=grid();int meta=block.onBlockPlaced(w,0,20,0,side,.5F,.5F,.5F,0);
            ForgeDirection d=ForgeDirection.getOrientation(side);
            require(ShulkerBoxShape112.direction(meta)==d,"clicked face sets opening direction");
            w.setBlock(0,20,0,block,meta,2);TileEntityShulkerBox112 tile=new TileEntityShulkerBox112();w.tile=tile;
            float[] rotations=RenderShulkerBox112.orientation(meta);
            double x=-Math.sin(Math.toRadians(rotations[1])),y=Math.cos(Math.toRadians(rotations[1]));
            double z=y*Math.sin(Math.toRadians(rotations[0]));y*=Math.cos(Math.toRadians(rotations[0]));
            require(Math.abs(x-d.offsetX)<1E-6&&Math.abs(y-d.offsetY)<1E-6&&Math.abs(z-d.offsetZ)<1E-6,"renderer lid vector matches placement "+side);
            require(BlockShulkerBox112.canOpen(w,0,20,0,tile),"free directed opening");
            w.setBlock(d.offsetX,20+d.offsetY,d.offsetZ,Blocks.stone,0,2);
            require(!BlockShulkerBox112.canOpen(w,0,20,0,tile),"obstacle along lid direction");
            w.setBlock(d.offsetX,20+d.offsetY,d.offsetZ,Blocks.air,0,2);
            w.setBlock(-d.offsetX,20-d.offsetY,-d.offsetZ,Blocks.stone,0,2);
            require(BlockShulkerBox112.canOpen(w,0,20,0,tile),"support behind box does not obstruct lid");
            AxisAlignedBB closed=block.getCollisionBoundingBoxFromPool(w,0,20,0);
            near((float)(closed.maxY-closed.minY),1,"closed cube");
            tile.receiveClientEvent(1,1);for(int i=0;i<10;i++)tile.updateEntity();
            AxisAlignedBB open=block.getCollisionBoundingBoxFromPool(w,0,20,0);
            near((float)(open.maxX-open.minX),d.offsetX!=0?1.5F:1,"open X extent");
            near((float)(open.maxY-open.minY),d.offsetY!=0?1.5F:1,"open Y extent");
            near((float)(open.maxZ-open.minZ),d.offsetZ!=0?1.5F:1,"open Z extent");
            block.setBlockBoundsBasedOnState(w,0,20,0);near((float)block.getBlockBoundsMinX(),(float)open.minX,"selection follows collision");
            tile.receiveClientEvent(1,0);for(int i=0;i<10;i++)tile.updateEntity();
            near((float)(block.getCollisionBoundingBoxFromPool(w,0,20,0).maxY-20),1,"closing restores cube");

            tile.setWorldObj(w);tile.yCoord=20;
            PushProbe probe=new PushProbe();probe.worldObj=w;
            double cx=.5+d.offsetX*.55,cy=20.5+d.offsetY*.55,cz=.5+d.offsetZ*.55;
            probe.boundingBox.setBounds(cx-.02,cy-.02,cz-.02,cx+.02,cy+.02,cz+.02);w.entities.add(probe);
            tile.receiveClientEvent(1,1);tile.updateEntity();
            require(probe.moves==1,"opening pushes intersecting entity "+side);
            near((float)probe.dx,d.offsetX*.11F,"push X");near((float)probe.dy,d.offsetY*.11F,"push Y");near((float)probe.dz,d.offsetZ*.11F,"push Z");
            tile.receiveClientEvent(1,0);tile.updateEntity();require(probe.moves==1,"closing never pulls entity");
            probe.noClip=true;tile.receiveClientEvent(1,1);tile.updateEntity();require(probe.moves==1,"noClip entity ignored");
            tile.receiveClientEvent(1,0);tile.updateEntity();probe.noClip=false;w.isRemote=true;tile.receiveClientEvent(1,1);tile.updateEntity();require(probe.moves==1,"server owns pushing");
        }
        System.out.println("Directed shulker boxes OK: six placements, render vectors, obstruction, dynamic bounds, push lifecycle");
    }
    private static void shulkerContracts37() throws Exception {
        Grid w=grid();BlockShulkerBox112 block=new BlockShulkerBox112("white");
        TileEntityShulkerBox112 tile=new TileEntityShulkerBox112();w.tile=tile;tile.setWorldObj(w);tile.yCoord=11;
        w.setBlock(0,11,0,block,0,2);
        net.minecraft.nbt.NBTTagCompound data=new net.minecraft.nbt.NBTTagCompound();
        data.setString("Lock","Key");data.setString("CustomName","Old name");tile.readInventory(data);
        net.minecraft.item.ItemStack key=new net.minecraft.item.ItemStack(net.minecraft.init.Items.stick);
        require(!tile.canUnlock(null)&&!tile.canUnlock(key),"lock denies missing and unnamed keys");
        key.setStackDisplayName("key");require(!tile.canUnlock(key),"lock comparison is case sensitive");
        key.setStackDisplayName("Key");require(tile.canUnlock(key),"matching custom key unlocks");
        key.stackSize=0;require(!tile.canUnlock(key),"empty key cannot unlock");
        net.minecraft.item.ItemStack placed=new net.minecraft.item.ItemStack(Blocks.stone);
        net.minecraft.nbt.NBTTagCompound root=new net.minecraft.nbt.NBTTagCompound();root.setTag("BlockEntityTag",data);placed.setTagCompound(root);
        placed.setStackDisplayName("New name");block.onBlockPlacedBy(w,0,11,0,null,placed);
        require("New name".equals(tile.getInventoryName()),"item display name wins over old block NBT");
        tile.setInventorySlotContents(0,new net.minecraft.item.ItemStack(net.minecraft.init.Items.diamond,3));
        net.minecraft.item.ItemStack picked=block.getPickBlock(null,w,0,11,0);
        require(picked.hasDisplayName()&&"New name".equals(picked.getTagCompound().getCompoundTag("display").getString("Name")),"pick retains visible custom name");
        TileEntityShulkerBox112 copy=new TileEntityShulkerBox112();copy.readInventory(picked.getTagCompound().getCompoundTag("BlockEntityTag"));
        require(copy.getStackInSlot(0).stackSize==3&&copy.getStackInSlot(0).getItem()==net.minecraft.init.Items.diamond,
                "pick retains actual inventory");
        require(!copy.canUnlock(null),"pick retains lock");
        copy.readInventory(new net.minecraft.nbt.NBTTagCompound());require(copy.canUnlock(null),"unlocked box accepts empty hand");
        for(int side=0;side<6;side++) {
            int meta=ShulkerBoxShape112.metadata(side);w.setBlockMetadataWithNotify(0,11,0,meta,2);
            tile.receiveClientEvent(1,0);for(int i=0;i<12;i++)tile.updateEntity();
            for(ForgeDirection face:ForgeDirection.VALID_DIRECTIONS)require(block.isSideSolid(w,0,11,0,face),"closed box supports all faces");
            tile.receiveClientEvent(1,1);tile.updateEntity();
            for(ForgeDirection face:ForgeDirection.VALID_DIRECTIONS)
                require(block.isSideSolid(w,0,11,0,face)==(face==ShulkerBoxShape112.direction(meta).getOpposite()),"open support face "+side+"/"+face);
            tile.receiveClientEvent(1,0);tile.updateEntity();
        }
        require(w.neighborUpdates>=12,"opening and closing notify support neighbors");
        require(block.getComparatorInputOverride(w,0,11,0,0)>0,"filled box supplies comparator signal");
        w.rules.setOrCreateGameRule("doTileDrops","false");
        int previousUpdates=w.comparatorUpdates;
        block.breakBlock(w,0,11,0,block,0);
        require(w.comparatorUpdates==previousUpdates+1&&w.tile==null,"removal updates comparator and removes tile");
        System.out.println("Shulker contracts 37 OK: lock, name precedence, portable pick NBT, six support directions, comparator removal");
    }
    private static void armorStand38() throws Exception {
        com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand stand=
                new com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand(null);
        stand.getDataWatcher().updateObject(20,Byte.valueOf((byte)1));
        require(stand.width==.25F&&stand.height==.9875F,"network small flag refreshes dimensions");
        stand.getDataWatcher().updateObject(20,Byte.valueOf((byte)16));
        require(stand.width==0&&stand.height==0&&!stand.canBeCollidedWith(),"network marker has no collision");
        stand.getDataWatcher().updateObject(20,Byte.valueOf((byte)0));
        require(stand.width==.5F&&stand.height==1.975F,"network normal flag restores dimensions");
        stand.setCurrentItemOrArmor(1,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_boots));
        stand.setCurrentItemOrArmor(2,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_leggings));
        stand.setCurrentItemOrArmor(3,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_chestplate));
        stand.setCurrentItemOrArmor(4,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_helmet));
        require(stand.clickedEquipmentSlot(.2)==1&&stand.clickedEquipmentSlot(.7)==2&&stand.clickedEquipmentSlot(1.3)==3&&stand.clickedEquipmentSlot(1.8)==4,"adult hit slots");
        stand.setSmall(true);
        require(stand.clickedEquipmentSlot(.2)==1&&stand.clickedEquipmentSlot(.5)==2&&stand.clickedEquipmentSlot(.7)==3&&stand.clickedEquipmentSlot(.96)==4,"small hit slots and reference priority");
        require(stand.equipmentSlot(new net.minecraft.item.ItemStack(net.minecraft.init.Items.skull))==4&&stand.equipmentSlot(new net.minecraft.item.ItemStack(Blocks.pumpkin))==4,"skulls and pumpkin use head slot");
        require(stand.equipmentSlot(new net.minecraft.item.ItemStack(net.minecraft.init.Items.stick))==0,"ordinary item remains hand slot");
        com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelBountifulArmorStand wood=new com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelBountifulArmorStand();
        require(((net.minecraft.client.model.ModelBox)wood.bipedRightArm.cubeList.get(0)).posX1==-2&&((net.minecraft.client.model.ModelBox)wood.bipedLeftArm.cubeList.get(0)).posX1==0,"reference arm geometry");
        require(wood.bipedLeftArm.mirror&&wood.bipedLeftLeg.mirror,"reference left UV mirroring");
        for(float inflation:new float[]{.5F,1F}) {
            com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelArmorStandArmor120 armor=new com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelArmorStandArmor120(inflation);
            stand.setPose(0,new float[]{30,20,10});stand.setPose(2,new float[]{45,15,-60});
            armor.setRotationAngles(3,1,100,90,90,.0625F,stand);
            require(Math.abs(armor.bipedLeftArm.rotateAngleX-Math.PI/4)<1E-6&&Math.abs(armor.bipedLeftArm.rotateAngleZ+Math.PI/3)<1E-6,"armor follows custom arm pose instead of walking");
            require(armor.bipedHeadwear.rotateAngleY==armor.bipedHead.rotateAngleY&&armor.bipedHead.rotationPointY==1&&armor.bipedLeftLeg.rotationPointY==11,"reference armor pivots and hat pose");
            stand.setPose(2,new float[]{0,0,0});armor.setRotationAngles(0,0,0,0,0,.0625F,stand);
            require(armor.bipedLeftArm.rotateAngleX==0&&armor.bipedLeftArm.rotateAngleZ==0,"armor model does not retain previous stand pose");
        }
        System.out.println("Armor stand 38 OK: network dimensions, adult/small hit slots, head items, mirrored geometry and posed armor layers");
    }
    private static void banners39() throws Exception {
        java.lang.reflect.Method mapping=TileEntity.class.getDeclaredMethod("addMapping",Class.class,String.class);mapping.setAccessible(true);
        mapping.invoke(null,com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner.class,"FixtureBanner39");
        net.minecraft.nbt.NBTTagList layers=new net.minecraft.nbt.NBTTagList();
        net.minecraft.nbt.NBTTagCompound unknown=new net.minecraft.nbt.NBTTagCompound();unknown.setString("Pattern","unknown");layers.appendTag(unknown);
        for(int i=0;i<20;i++) {
            net.minecraft.nbt.NBTTagCompound pattern=new net.minecraft.nbt.NBTTagCompound();
            pattern.setString("Pattern",i%2==0?"bs":"cr");pattern.setInteger("Color",i&15);layers.appendTag(pattern);
        }
        net.minecraft.nbt.NBTTagList visible=com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer.visiblePatterns(layers);
        require(visible.tagCount()==16,"16 pattern overlays after implicit base, not loom limit six");
        for(int i=0;i<16;i++)require(visible.getCompoundTagAt(i).getInteger("Color")==i&&visible.getCompoundTagAt(i).getString("Pattern").equals(i%2==0?"bs":"cr"),"pattern order/color "+i);
        visible.getCompoundTagAt(0).setInteger("Color",12);
        require(layers.getCompoundTagAt(1).getInteger("Color")==0,"render filtering cannot mutate stored patterns");
        net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.banner,1,15);
        net.minecraft.nbt.NBTTagCompound root=new net.minecraft.nbt.NBTTagCompound(),data=new net.minecraft.nbt.NBTTagCompound();
        data.setInteger("Base",1);data.setTag("Patterns",layers);root.setTag("BlockEntityTag",data);stack.setTagCompound(root);stack.setStackDisplayName("Banner test");
        com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner tile=new com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner();tile.readFromItem(stack);
        require(tile.getBaseColor()==com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer.itemBaseColor(stack),"item and placed base color agree on legacy Base");
        net.minecraft.nbt.NBTTagCompound saved=new net.minecraft.nbt.NBTTagCompound();tile.writeToNBT(saved);
        com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner copy=new com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner();copy.readFromNBT(saved);
        net.minecraft.item.ItemStack drop=copy.asItem();
        require(drop.getItemDamage()==1&&drop.getDisplayName().equals("Banner test")&&copy.getPatterns().equals(layers),"save/drop round trip preserves base, name and full ordered patterns");
        require(com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer.wallRotation(4)==-90&&com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer.wallRotation(5)==90,"wall west/east rotations follow support wall");
        float wave=com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer.waveAngle(0,0,0,0,0);
        require(Math.abs(wave-(-.0025*Math.PI))<1E-7,"wave includes reference PI factor");
        require(wave==com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer.waveAngle(10000000000L,0,0,0,0),"wave retains precision in old worlds");
        Class<?> modelType=Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer$BannerModel");
        java.lang.reflect.Constructor<?> constructor=modelType.getDeclaredConstructor();constructor.setAccessible(true);Object model=constructor.newInstance();
        Field clothField=modelType.getDeclaredField("cloth");clothField.setAccessible(true);net.minecraft.client.model.ModelRenderer cloth=(net.minecraft.client.model.ModelRenderer)clothField.get(model);
        require(cloth.rotationPointY==-32&&((net.minecraft.client.model.ModelBox)cloth.cubeList.get(0)).posZ1==-2,"reference cloth pivot and depth");
        System.out.println("Banners 39 OK: ordered 16 overlays, immutable filtering, base/name/pattern round trip, geometry and wave");
    }
    private static void standAndBanner40() throws Exception {
        Class<com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand> type=com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand.class;
        for(boolean fire:new boolean[]{false,true})for(boolean drops:new boolean[]{false,true}) {
            Grid w=grid();w.rules.setOrCreateGameRule("doTileDrops",Boolean.toString(drops));
            com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand stand=type.getConstructor(World.class).newInstance((World)null);stand.worldObj=w;
            stand.setCurrentItemOrArmor(4,new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_helmet));
            net.minecraft.util.DamageSource source=fire?net.minecraft.util.DamageSource.onFire:new net.minecraft.util.DamageSource("explosion").setExplosion();
            stand.attackEntityFrom(source,1);if(fire)stand.attackEntityFrom(source,1);
            require(stand.isDead&&stand.getEquipmentInSlot(4)==null,"fire/explosion removes stand and clears equipment");
            require(w.entities.size()==(drops?1:0),"environmental break drops only equipment and honors doTileDrops");
            if(drops)require(((net.minecraft.entity.item.EntityItem)w.entities.get(0)).getEntityItem().getItem()==net.minecraft.init.Items.iron_helmet,"no stand item after fire/explosion");
        }
        Grid w=grid();com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand stand=type.getConstructor(World.class).newInstance((World)null);stand.worldObj=w;
        stand.setMarker(true);w.isRemote=true;stand.attackEntityFrom(net.minecraft.util.DamageSource.outOfWorld,100);require(!stand.isDead,"client cannot remove marker");
        w.isRemote=false;stand.attackEntityFrom(net.minecraft.util.DamageSource.outOfWorld,100);require(stand.isDead,"void removes marker despite marker immunity");
        stand=type.getConstructor(World.class).newInstance((World)null);stand.worldObj=w;stand.setInvisible(true);
        stand.attackEntityFrom(new net.minecraft.util.DamageSource("explosion").setExplosion(),10);require(!stand.isDead&&stand.getHealth()==5,"invisible stand ignores ordinary damage");
        require(stand.getPickedResult(null).getItem()==com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.armorStand&&!stand.getPickedResult(null).hasTagCompound(),"pick returns plain armor stand item");
        stand.setCustomNameTag("Named stand");stand.setPose(0,new float[]{Float.NaN,Float.POSITIVE_INFINITY,725});
        float[] pose=stand.getPose(0);require(pose[0]==0&&pose[1]==0&&pose[2]==5,"pose normalization matches Rotations");
        stand.getDataWatcher().updateObject(21,"NaN,-Infinity,-725");require(stand.getPose(0)[0]==0&&stand.getPose(0)[2]==-5,"network pose values sanitized");
        java.lang.reflect.Method drop=type.getDeclaredMethod("dropContentsAndStand",boolean.class);drop.setAccessible(true);drop.invoke(stand,true);
        net.minecraft.item.ItemStack dropped=((net.minecraft.entity.item.EntityItem)w.entities.get(0)).getEntityItem();
        require(dropped.getDisplayName().equals("Named stand")&&!dropped.getTagCompound().hasKey("EntityTag"),"broken stand keeps name without command pose tags");
        Class<?> unsafeType=Class.forName("sun.misc.Unsafe");Field unsafeField=unsafeType.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        net.minecraft.entity.player.EntityPlayer player=(net.minecraft.entity.player.EntityPlayer)unsafeType.getMethod("allocateInstance",Class.class).invoke(unsafeField.get(null),net.minecraftforge.common.util.FakePlayer.class);
        Field caps=net.minecraft.entity.player.EntityPlayer.class.getDeclaredField("capabilities");caps.setAccessible(true);caps.set(player,new net.minecraft.entity.player.PlayerCapabilities());
        Grid placement=grid();placement.setBlock(0,11,0,Blocks.tallgrass,1,2);placement.rejectSpawn=true;
        net.minecraft.item.ItemStack item=new net.minecraft.item.ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.armorStand,2);item.setStackDisplayName("Placed name");
        require(!item.getItem().onItemUse(item,player,placement,0,11,0,1,.5F,.5F,.5F)&&item.stackSize==2&&placement.getBlock(0,11,0)==Blocks.tallgrass,"cancelled spawn preserves item and vegetation");
        placement.rejectSpawn=false;require(item.getItem().onItemUse(item,player,placement,0,11,0,1,.5F,.5F,.5F),"successful placement");
        require(((net.minecraft.entity.EntityLiving)placement.entities.get(0)).getCustomNameTag().equals("Placed name")&&item.stackSize==1,"placement preserves name and consumes one");
        Grid client=grid();client.isRemote=true;item.stackSize=2;require(item.getItem().onItemUse(item,player,client,0,10,0,1,.5F,.5F,.5F)&&item.stackSize==2&&client.entities.isEmpty(),"client placement does not spawn or consume");
        com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulBanner banner=new com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulBanner(true);
        Grid bannerWorld=grid();bannerWorld.rules.setOrCreateGameRule("doTileDrops","false");bannerWorld.setBlock(0,11,0,banner,15,2);bannerWorld.isRemote=true;
        banner.onNeighborBlockChange(bannerWorld,0,11,0,Blocks.air);require(bannerWorld.getBlock(0,11,0)==banner,"client does not remove unsupported banner");
        bannerWorld.isRemote=false;banner.onNeighborBlockChange(bannerWorld,0,11,0,Blocks.air);require(bannerWorld.isAirBlock(0,11,0),"invalid wall metadata removed without array exception");
        System.out.println("Cycle 40 OK: ten stand/banner fixes, environment drops, guards, names, placement and poses");
    }
    private static final class PushProbe extends EntityBoat {
        int moves;double dx,dy,dz;PushProbe(){super(null);}
        @Override public void moveEntity(double x,double y,double z){moves++;dx=x;dy=y;dz=z;}
    }
    private static void treasureRuntime() throws Exception {
        require(com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.waterOnMap(net.minecraft.world.biome.BiomeGenBase.ocean),"ocean preview");
        require(com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.waterOnMap(net.minecraft.world.biome.BiomeGenBase.river),"river preview");
        require(com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.waterOnMap(net.minecraft.world.biome.BiomeGenBase.swampland),"swamp preview");
        require(!com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.waterOnMap(net.minecraft.world.biome.BiomeGenBase.beach),"beach stays land");
        Grid w=grid();Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(w,new net.minecraft.world.WorldProviderSurface());
        w.residentChunk=new net.minecraft.world.chunk.Chunk(w,0,0) {
            @Override public Block getBlock(int x,int y,int z){return worldObj.getBlock(x,y,z);}
            @Override public int getHeightValue(int x,int z){return 14;}
        };
        Method burial=com.mod.htgt6.minecraft1202.Module.World.Aquatic.BuriedTreasureHandler.class.getDeclaredMethod("findBurialY",World.class,int.class,int.class);burial.setAccessible(true);
        require(((Integer)burial.invoke(null,w,9,9))==11,"bare foundation uses first position above surface");
        w.setBlock(9,11,9,Blocks.sand,0,2);w.setBlock(9,12,9,Blocks.sand,0,2);
        require(((Integer)burial.invoke(null,w,9,9))==11,"sand cover scanned to valid foundation");
        while(!com.mod.htgt6.common.util.StructurePlacement120.buriedTreasurePasses(w.seed,0,0))w.seed++;
        w.autoChest=true;
        Method generate=com.mod.htgt6.minecraft1202.Module.World.Aquatic.BuriedTreasureHandler.class.getDeclaredMethod("tryGenerate",World.class,int.class,int.class);generate.setAccessible(true);generate.invoke(null,w,0,0);
        require(w.getBlock(9,11,9)==Blocks.chest&&w.tile instanceof net.minecraft.tileentity.TileEntityChest,"candidate generates chest at local 9,9 and burial Y");
        int treasureHearts=0;net.minecraft.inventory.IInventory generated=(net.minecraft.inventory.IInventory)w.tile;
        for(int slot=0;slot<generated.getSizeInventory();slot++){net.minecraft.item.ItemStack item=generated.getStackInSlot(slot);if(item!=null&&item.getItem()==com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.heartOfTheSea)treasureHearts+=item.stackSize;}
        require(treasureHearts==1,"generated chest contains heart");
        NBTTagCompound savedIndex=new NBTTagCompound();w.savedData.get("HTGT6AquaticStructures").writeToNBT(savedIndex);
        com.mod.htgt6.minecraft1202.Module.Data.Aquatic.AquaticStructureData restored=new com.mod.htgt6.minecraft1202.Module.Data.Aquatic.AquaticStructureData("HTGT6AquaticStructures");restored.readFromNBT(savedIndex);w.savedData.put("HTGT6AquaticStructures",restored);
        w.setBlock(9,11,9,Blocks.air,0,2);w.tile=null;generate.invoke(null,w,0,0);
        require(w.getBlock(9,11,9)==Blocks.air,"removed treasure does not respawn after index reload");
        net.minecraft.item.ItemStack scroll=new net.minecraft.item.ItemStack(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.treasureMapScroll);
        NBTTagCompound tag=new NBTTagCompound();tag.setBoolean("HTGT6BuriedTreasureScroll",true);tag.setString("TargetKind","buried_treasure");tag.setInteger("TargetX",-311);tag.setInteger("TargetZ",249);scroll.setTagCompound(tag);
        require(com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.targetDimension(scroll)==0,"old scroll dimension defaults Overworld");
        w.isRemote=true;require(com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.open(scroll,w)==scroll,"client does not allocate map");w.isRemote=false;
        net.minecraft.item.ItemStack opened=com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.open(scroll,w);
        require(opened.getItem()==Items.filled_map&&com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.isTreasureMap(opened),"scroll opens to native map");
        net.minecraft.world.storage.MapData map=(net.minecraft.world.storage.MapData)w.savedData.get("map_"+opened.getItemDamage());
        require(map.dimension==0&&map.scale==1&&map.xCenter==-192&&map.zCenter==320,"dimension, scale, negative-coordinate center");
        require(map.playersVisibleOnMap.containsKey("htgt6:buried_treasure"),"red X registered on opened map");
        tag.setInteger("TargetDimension",987654);require(com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.open(scroll,w)==scroll,"unavailable target world preserves scroll");
        for(int seed=0;seed<100;seed++) {
            net.minecraft.inventory.InventoryBasic inventory=new net.minecraft.inventory.InventoryBasic("treasure",true,27);
            com.mod.htgt6.minecraft1202.Module.World.Aquatic.AquaticChestLoot120.fillBuriedTreasure(inventory,new Random(seed));
            int hearts=0;for(int slot=0;slot<27;slot++){net.minecraft.item.ItemStack item=inventory.getStackInSlot(slot);if(item!=null&&item.getItem()==com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.heartOfTheSea)hearts+=item.stackSize;}
            require(hearts==1,"one heart of sea per generated treasure");
        }
        System.out.println("Treasure runtime OK: burial surface, native map/center/marker, dimension safety, 100 loot seeds");
    }
    private static void roots() throws Exception {
        Grid world=grid(); world.setBlock(0,12,0,Backport120Content.rootedDirt,0,3);
        IGrowable roots=(IGrowable)Backport120Content.rootedDirt;
        require(roots.func_149851_a(world,0,12,0,false),"empty block under rooted dirt");
        roots.func_149853_b(world,new Random(1),0,12,0);
        require(world.getBlock(0,11,0)==Backport120Content.hangingRoots,"hanging roots placed below");
        require(!roots.func_149851_a(world,0,12,0,false),"occupied lower block rejects bonemeal");
        world.setBlock(1,12,0,Backport120Content.rootedDirt,0,3); world.isRemote=true;
        roots.func_149853_b(world,new Random(1),1,12,0);
        require(world.isAirBlock(1,11,0),"client does not create blocks");
    }
    private static void moss() throws Exception {
        Grid world=grid(); world.setBlock(0,10,0,Backport120Content.mossBlock,0,3);
        IGrowable moss=(IGrowable)Backport120Content.mossBlock;
        require(moss.func_149851_a(world,0,10,0,false),"moss accepts empty space above");
        world.setBlock(1,10,0,Blocks.cobblestone,0,3);
        world.setBlock(-1,10,0,Blocks.iron_ore,0,3);
        world.setBlock(0,10,1,BountifulContent.stone,1,3);
        moss.func_149853_b(world,new Fixed(0,0),0,10,0);
        require(world.getBlock(0,10,-1)==Backport120Content.mossBlock,"stone converted");
        require(world.getBlock(0,9,-1)==Blocks.stone,"one-block replacement depth");
        require(world.getBlock(2,10,2)==Blocks.stone,"corners excluded");
        require(world.getBlock(2,10,0)==Backport120Content.mossBlock,"radius includes outer edge");
        require(world.getBlock(1,10,0)==Blocks.cobblestone&&world.getBlock(-1,10,0)==Blocks.iron_ore
                &&world.getBlock(0,10,1)==BountifulContent.stone,"ores and polished/decorative blocks preserved");
        require(world.getBlock(0,11,-1)==Backport120Content.floweringAzalea,"weighted vegetation placed");
        require(!moss.func_149851_a(world,0,10,0,false),"vegetation above blocks repeated bonemeal");
        require(!MossBonemeal120.replaceable(Blocks.gravel,0)&&!MossBonemeal120.replaceable(Blocks.clay,0),"cave-worldgen replacement list is not reused");
        require(MossBonemeal120.replaceable(BountifulContent.stone,0)&&MossBonemeal120.replaceable(BountifulContent.stone,2)
                &&MossBonemeal120.replaceable(BountifulContent.stone,4),"all three natural stone variants");
        require(Backport120Content.mossBlock.canSustainPlant(world,0,10,0,ForgeDirection.UP,Blocks.tallgrass),"native grass can survive on moss");
        for(int roll:new int[]{4,11,36,86}) {
            Grid patch=grid(); MossBonemeal120.generate((MossBonemeal120.Access)patch,new Fixed(0,roll),0,11,0);
            Block expected=roll==4?Backport120Content.azalea:roll==11?Backport120Content.mossCarpet:roll==36?Blocks.tallgrass:Blocks.double_plant;
            require(patch.getBlock(0,11,0)==expected,"vegetation weight boundary "+roll);
            if(roll==86)require(patch.getBlock(0,12,0)==Blocks.double_plant&&patch.getBlockMetadata(0,12,0)==10,"tall grass upper half");
        }
        Grid slope=grid(); slope.setBlock(0,11,0,Blocks.dirt,0,3);
        MossBonemeal120.generate((MossBonemeal120.Access)slope,new Fixed(.7F,0),0,11,0);
        require(slope.getBlock(0,11,0)==Backport120Content.mossBlock,"scan climbs to adjacent surface");
    }
    private static void carpet() throws Exception {
        Grid world=grid(); world.setBlock(0,11,0,Blocks.torch,0,3);
        require(Backport120Content.mossCarpet.canPlaceBlockAt(world,0,12,0),"non-solid non-air carpet support");
        require(!Backport120Content.mossCarpet.canPlaceBlockAt(world,1,12,0),"carpet cannot float over air");
        require(Math.abs(Backport120Content.mossCarpet.getBlockBoundsMaxY()-1D/16)<0.00001,"reference carpet thickness");
    }
    private static Grid grid() throws Exception {
        Class<?> type=Class.forName("sun.misc.Unsafe"); Field field=type.getDeclaredField("theUnsafe");field.setAccessible(true);
        Grid grid=(Grid)type.getMethod("allocateInstance",Class.class).invoke(field.get(null),Grid.class);
        Field random=World.class.getDeclaredField("rand");random.setAccessible(true);random.set(grid,new Random(1));
        Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(grid,new net.minecraft.world.WorldProviderSurface());
        grid.rules=new net.minecraft.world.GameRules();grid.cells=new HashMap<String,Object[]>();grid.entities=new ArrayList<Entity>();grid.savedData=new HashMap<String,net.minecraft.world.WorldSavedData>(); return grid;
    }
    private static void copper() throws Exception {
        com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120CopperFamily copper=EtFVanillaGapContent120.copperBlocks;
        for(int meta=0;meta<8;meta++){
            Grid w=grid();w.setBlock(0,10,0,copper,meta,3);
            copper.updateTick(w,0,10,0,new Fixed(0,0));
            require(w.getBlockMetadata(0,10,0)==(meta<3?meta+1:meta),"oxidation stage/wax "+meta);
        }
        Grid w=grid();w.setBlock(0,10,0,copper,1,3);
        w.setBlock(4,10,0,EtFVanillaGapContent120.cutCopper,0,3);
        copper.updateTick(w,0,10,0,new Fixed(0,0));
        require(w.getBlockMetadata(0,10,0)==1,"younger copper at radius four prevents oxidation across families");
        w.setBlock(4,10,0,EtFVanillaGapContent120.cutCopper,4,3);
        w.setBlock(3,12,0,copper,0,3);
        copper.updateTick(w,0,10,0,new Fixed(0,0));
        require(w.getBlockMetadata(0,10,0)==2,"waxed neighbor and Manhattan distance five ignored");
        w.setBlock(0,10,0,copper,0,3);w.setBlock(1,10,0,copper,0,3);
        copper.updateTick(w,0,10,0,new Rolls(0,.1875F));
        require(w.getBlockMetadata(0,10,0)==0,"same-age neighbor: strict squared ratio times 0.75 threshold");
        copper.updateTick(w,0,10,0,new Rolls(0,.1874F));
        require(w.getBlockMetadata(0,10,0)==1,"same-age neighbor probability below threshold");
        w.setBlock(0,10,0,copper,0,3);
        copper.updateTick(w,0,10,0,new Rolls(.05688889F,0));
        require(w.getBlockMetadata(0,10,0)==0,"random tick gate is strict");
        w.isRemote=true;copper.updateTick(w,0,10,0,new Fixed(0,0));
        require(w.getBlockMetadata(0,10,0)==0,"client cannot oxidize copper");
        System.out.println("Copper oxidation: OK; stages, wax, neighbors, radius, probabilities, client guard");
    }
    private static final class Rolls extends Random {
        private final float[] values;private int index;
        Rolls(float... values){this.values=values;}
        @Override public float nextFloat(){return values[index++];}
    }
    private static final class Fixed extends Random {
        private final float f;private final int i;Fixed(float f,int i){this.f=f;this.i=i;}
        @Override public float nextFloat(){return f;}
        @Override public int nextInt(int bound){return i%bound;}
    }
    public static final class Grid extends World implements MossBonemeal120.Access {
        Map<String,Object[]> cells;
        List<Entity> entities;
        TileEntity tile;int scheduled,delay;
        net.minecraft.world.chunk.Chunk residentChunk;
        long seed;boolean autoChest;
        net.minecraft.world.GameRules rules;boolean rejectSpawn;
        @Override public net.minecraft.world.GameRules getGameRules(){return rules;}
        @Override public boolean spawnEntityInWorld(Entity entity){if(rejectSpawn)return false;entities.add(entity);return true;}
        @Override public long getSeed(){return seed;}
        @Override public net.minecraft.world.biome.BiomeGenBase getBiomeGenForCoords(int x,int z){return net.minecraft.world.biome.BiomeGenBase.beach;}
        Map<String,net.minecraft.world.WorldSavedData> savedData;int mapId;
        @Override public int getUniqueDataId(String name){return ++mapId;}
        @Override public void setItemData(String name,net.minecraft.world.WorldSavedData data){savedData.put(name,data);}
        @Override public net.minecraft.world.WorldSavedData loadItemData(Class type,String name){return savedData.get(name);}
        @Override public net.minecraft.world.biome.WorldChunkManager getWorldChunkManager(){return new net.minecraft.world.biome.WorldChunkManagerHell(net.minecraft.world.biome.BiomeGenBase.beach,.5F);}
        @Override public TileEntity getTileEntity(int x,int y,int z){return tile;}
        @Override public void scheduleBlockUpdate(int x,int y,int z,Block block,int ticks){scheduled++;delay=ticks;}
        @Override public List getEntitiesWithinAABB(Class type,AxisAlignedBB box) {
            List<Entity> found=new ArrayList<Entity>();for(Entity entity:entities)if(!entity.isDead&&type.isInstance(entity)&&entity.boundingBox.intersectsWith(box))found.add(entity);return found;
        }
        @Override public List getEntitiesWithinAABBExcludingEntity(Entity excluded,AxisAlignedBB box) {
            List<Entity> found=new ArrayList<Entity>();for(Entity entity:entities)if(entity!=excluded&&!entity.isDead&&entity.boundingBox.intersectsWith(box))found.add(entity);return found;
        }
        @Override public List func_147461_a(AxisAlignedBB box) {
            List<AxisAlignedBB> result=new ArrayList<AxisAlignedBB>();
            for(int x=(int)Math.floor(box.minX);x<=Math.floor(box.maxX);x++)
                for(int y=(int)Math.floor(box.minY);y<=Math.floor(box.maxY);y++)
                    for(int z=(int)Math.floor(box.minZ);z<=Math.floor(box.maxZ);z++)
                        getBlock(x,y,z).addCollisionBoxesToList(this,x,y,z,box,result,null);
            return result;
        }
        boolean powered;
        boolean thundering;
        @Override public boolean isThundering(){return thundering;}
        int soundCount,auxSoundCount;String lastSound;float soundPitch,soundVolume;
        @Override public void playSoundEffect(double x,double y,double z,String sound,float volume,float pitch){soundCount++;lastSound=sound;soundPitch=pitch;soundVolume=volume;}
        @Override public boolean isBlockIndirectlyGettingPowered(int x,int y,int z){return powered;}
        int neighborUpdates,comparatorUpdates;
        @Override public void notifyBlocksOfNeighborChange(int x,int y,int z,Block block){neighborUpdates++;}
        @Override public void func_147453_f(int x,int y,int z,Block block){comparatorUpdates++;}
        @Override public void removeTileEntity(int x,int y,int z){tile=null;}
        @Override public void markTileEntityChunkModified(int x,int y,int z,TileEntity tile){}
        @Override public void markBlockRangeForRenderUpdate(int x,int y,int z,int x2,int y2,int z2){}
        @Override public void playAuxSFXAtEntity(net.minecraft.entity.player.EntityPlayer player,int id,int x,int y,int z,int data){auxSoundCount++;}
        private Grid(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
        private String key(int x,int y,int z){return x+","+y+","+z;}
        @Override public Block getBlock(int x,int y,int z){Object[] c=cells.get(key(x,y,z));return c==null?(y<=10?Blocks.stone:Blocks.air):(Block)c[0];}
        @Override public int getBlockMetadata(int x,int y,int z){Object[] c=cells.get(key(x,y,z));return c==null?0:(Integer)c[1];}
        @Override public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
        @Override public boolean setBlock(int x,int y,int z,Block b,int m,int flags){cells.put(key(x,y,z),new Object[]{b,m});if(autoChest&&b==Blocks.chest)tile=new net.minecraft.tileentity.TileEntityChest();return true;}
        @Override public boolean setBlockMetadataWithNotify(int x,int y,int z,int m,int flags){return setBlock(x,y,z,getBlock(x,y,z),m,flags);}
        @Override public boolean isSideSolid(int x,int y,int z,ForgeDirection side){return getBlock(x,y,z).isOpaqueCube();}
        @Override public int getHeight(){return 256;}
        @Override protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        @Override protected int func_152379_p(){return 0;}
        @Override public net.minecraft.entity.Entity getEntityByID(int id){for(Entity entity:entities)if(entity.getEntityId()==id)return entity;return null;}
        @Override public net.minecraft.world.chunk.IChunkProvider getChunkProvider(){return (net.minecraft.world.chunk.IChunkProvider)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{net.minecraft.world.chunk.IChunkProvider.class},new InvocationHandler(){public Object invoke(Object p,Method m,Object[] a){return m.getName().equals("provideChunk")?residentChunk:Boolean.TRUE;}});}
        public boolean valid(int x,int y,int z){return y>=0&&y<256;}
        public boolean air(int x,int y,int z){return isAirBlock(x,y,z);}
        public boolean solidTop(int x,int y,int z){return isSideSolid(x,y,z,ForgeDirection.UP);}
        public Block block(int x,int y,int z){return getBlock(x,y,z);}
        public int metadata(int x,int y,int z){return getBlockMetadata(x,y,z);}
        public void set(int x,int y,int z,Block b,int m){setBlock(x,y,z,b,m,3);}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
