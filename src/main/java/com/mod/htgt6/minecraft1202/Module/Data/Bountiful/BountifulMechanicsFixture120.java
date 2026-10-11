package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.asm.HTGT6Transformer;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.*;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

/** Behavioral regressions for the Bountiful fixes; not a full parity assertion. */
public final class BountifulMechanicsFixture120 {
    public static void main(String[] args)throws Exception {
        Grid w=initialize();
        daylight(); gates(w); sponge(w); landing(w); water(w); rabbits(w); furnace();
        System.out.println("Bountiful mechanics OK: daylight, gates, sponge, landing, Depth Strider, rabbit NBT, furnace");
    }
    public static Grid initialize()throws Exception {
        Class<?> u=Class.forName("sun.misc.Unsafe");Field f=u.getDeclaredField("theUnsafe");f.setAccessible(true);
        Object unsafe=f.get(null);Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,u.getMethod("allocateInstance",Class.class).invoke(unsafe,cpw.mods.fml.common.Loader.class));
        Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        controller.set(loader.get(null),new cpw.mods.fml.common.LoadController(cpw.mods.fml.common.Loader.instance()));
        net.minecraft.init.Bootstrap.func_151354_b();
        Grid w=(Grid)u.getMethod("allocateInstance",Class.class).invoke(unsafe,Grid.class);w.cells=new HashMap<String,Object[]>();
        Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(w,new net.minecraft.world.WorldProviderSurface());
        Field random=World.class.getDeclaredField("rand");random.setAccessible(true);random.set(w,new Random(1));
        w.loaded=true;
        w.tiles=new HashMap<String,net.minecraft.tileentity.TileEntity>();w.rules=new net.minecraft.world.GameRules();
        Field entities=World.class.getDeclaredField("loadedEntityList");entities.setAccessible(true);entities.set(w,new java.util.ArrayList<net.minecraft.entity.Entity>());
        w.difficultySetting=net.minecraft.world.EnumDifficulty.NORMAL;
        return w;
    }
    static void daylight(){
        for(int sky=0;sky<=15;sky++)for(int dark=0;dark<=15;dark++)
            require(BlockInvertedDaylightDetector.invertedPower(sky,dark)==Math.min(15,15-sky+dark),"raw inverse sky power");
    }
    static void gates(Grid w){
        BlockBountifulFenceGate gate=(BlockBountifulFenceGate)BountifulContent.woodFenceGates[0];
        w.setBlock(0,10,0,gate,0,2);w.powered=true;gate.onNeighborBlockChange(w,0,10,0,Blocks.redstone_block);
        require(w.getBlockMetadata(0,10,0)==12,"rising power opens gate");
        w.setBlockMetadataWithNotify(0,10,0,8,2);gate.onNeighborBlockChange(w,0,10,0,Blocks.stone);
        require(w.getBlockMetadata(0,10,0)==8,"constant power preserves manually closed gate");
        near(gate.getCollisionBoundingBoxFromPool(w,0,10,0).minZ,.375,"powered closed gate axis");
        w.powered=false;gate.onNeighborBlockChange(w,0,10,0,Blocks.air);
        require(w.getBlockMetadata(0,10,0)==0,"falling power clears powered state");
    }
    static void sponge(Grid w){
        w.cells.clear();w.setBlock(0,10,0,BountifulContent.sponge,0,2);
        for(int x=-5;x<=5;x++)for(int y=5;y<=15;y++)for(int z=-5;z<=5;z++)
            if(x!=0||y!=10||z!=0)w.setBlock(x,y,z,Blocks.water,0,2);
        w.removed=0;w.requireWet=true;BountifulContent.sponge.onNeighborBlockChange(w,0,10,0,Blocks.water);
        require(w.removed==64&&w.getBlockMetadata(0,10,0)==1,"sponge removes at most 64 water blocks");
        BountifulContent.sponge.onNeighborBlockChange(w,0,10,0,Blocks.water);require(w.removed==64,"wet sponge cannot absorb again");
        w.requireWet=false;w.cells.clear();w.setBlock(0,10,0,BountifulContent.sponge,0,2);
        for(int x=1;x<=7;x++)w.setBlock(x,10,0,Blocks.water,0,2);
        BountifulContent.sponge.onNeighborBlockChange(w,0,10,0,Blocks.water);
        require(w.getBlock(6,10,0)==Blocks.air&&w.getBlock(7,10,0)==Blocks.water,"six-step absorption depth");
    }
    static void landing(Grid w){
        w.cells.clear();w.setBlock(0,9,0,BountifulContent.slimeBlock,0,2);
        EntityPig pig=new EntityPig(w);pig.setPosition(.5,10,.5);pig.onGround=true;pig.motionY=-1.25;
        near(HTGT6Transformer.slimeLanding120(pig),1.25,"living collision-time bounce");
        pig.setSneaking(true);near(HTGT6Transformer.slimeLanding120(pig),0,"sneaking suppresses bounce");
        EntityItem item=new EntityItem(w);item.setPosition(.5,10+item.yOffset,.5);item.onGround=true;item.motionY=-1.25;
        near(HTGT6Transformer.slimeLanding120(item),1,"nonliving bounce factor");
        w.setBlock(0,9,0,Blocks.stone,0,2);near(HTGT6Transformer.slimeLanding120(item),0,"ordinary floor stops vertical motion");
    }
    static void water(Grid w){
        EntityPig pig=new EntityPig(w);pig.onGround=true;pig.setAIMoveSpeed(.1F);
        near(HTGT6Transformer.depthDrag120(.8,pig),.8,"unenchanted drag unchanged");
        ItemStack boots=new ItemStack(Items.diamond_boots);boots.addEnchantment(BountifulContent.depthStrider,3);pig.setCurrentItemOrArmor(1,boots);
        near(HTGT6Transformer.depthDrag120(.8,pig),.54600006,"level III grounded drag");
        near(HTGT6Transformer.depthAcceleration120(.02F,pig),.1,"level III ground acceleration");
        pig.onGround=false;near(HTGT6Transformer.depthAcceleration120(.02F,pig),.06,"airborne effectiveness halved");
    }
    static void rabbits(Grid w){
        near(EntityBountifulRabbit.jumpImpulse(.6,true),.2,"slow rabbit jump");
        near(EntityBountifulRabbit.jumpImpulse(1,true),.5,"obstacle jump");
        near(EntityBountifulRabbit.jumpImpulse(1,false),.3,"ordinary jump");
        EntityBountifulRabbit rabbit=new EntityBountifulRabbit(w);NBTTagCompound n=new NBTTagCompound();rabbit.writeEntityToNBT(n);
        n.setInteger("MoreCarrotTicks",37);rabbit.readEntityFromNBT(n);NBTTagCompound saved=new NBTTagCompound();rabbit.writeEntityToNBT(saved);
        require(saved.getInteger("MoreCarrotTicks")==37,"carrot cooldown survives reload");
        Random half=new Random(){public float nextFloat(){return .5F;}};
        require(EntityBountifulGuardian.lootingBonus(half,3)==2,"Looting uses rounded float");
    }
    static void furnace(){
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(BountifulContent.sponge,"fixture_sponge");
        TileEntityFurnace f=new TileEntityFurnace();f.setInventorySlotContents(0,new ItemStack(BountifulContent.sponge,1,1));
        f.setInventorySlotContents(1,new ItemStack(Items.bucket));HTGT6Transformer.spongeBucket120(f);
        require(f.getStackInSlot(1).getItem()==Items.water_bucket,"wet sponge fills fuel bucket");
        f.setInventorySlotContents(0,new ItemStack(BountifulContent.sponge,1,0));f.setInventorySlotContents(1,new ItemStack(Items.bucket));
        HTGT6Transformer.spongeBucket120(f);require(f.getStackInSlot(1).getItem()==Items.bucket,"dry sponge does not fill bucket");
    }
    static void near(double actual,double expected,String message){require(Math.abs(actual-expected)<.000001,message+": "+actual);}
    static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    public static final class Grid extends World {
        public int skyFromY;
        public boolean loaded,liquid,collision;
        public int light=15,scheduled,lastDelay;
        public net.minecraft.world.biome.BiomeGenBase biome=net.minecraft.world.biome.BiomeGenBase.frozenOcean;
        public boolean sky=true,rejectSpawns,rejectWrites;
        public net.minecraft.entity.Entity lastSpawn;
        public boolean spawnEntityInWorld(net.minecraft.entity.Entity entity){lastSpawn=entity;return !rejectSpawns;}
        public net.minecraft.world.biome.BiomeGenBase getBiomeGenForCoords(int x,int z){return biome;}
        public boolean canBlockSeeTheSky(int x,int y,int z){return sky&&y>=skyFromY;}
        public int getFullBlockLightValue(int x,int y,int z){return light;}
        public int entityStates,particles,lastEntityState;
        public double particleRed,particleGreen,particleBlue;
        public void setEntityState(net.minecraft.entity.Entity entity,byte state){entityStates++;lastEntityState=state;}
        public void spawnParticle(String name,double x,double y,double z,double red,double green,double blue){particles++;particleRed=red;particleGreen=green;particleBlue=blue;}
        public long time;
        public net.minecraft.world.GameRules rules;
        public HashMap<String,net.minecraft.tileentity.TileEntity> tiles;
        HashMap<String,Object[]> cells;boolean powered,requireWet;int removed;
        private Grid(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
        public Block getBlock(int x,int y,int z){Object[] c=cells.get(x+","+y+","+z);return c==null?Blocks.air:(Block)c[0];}
        public int getBlockMetadata(int x,int y,int z){Object[] c=cells.get(x+","+y+","+z);return c==null?0:(Integer)c[1];}
        public boolean setBlock(int x,int y,int z,Block b,int m,int flags){if(rejectWrites)return false;if(b==Blocks.air&&getBlock(x,y,z)==Blocks.water)removed++;cells.put(x+","+y+","+z,new Object[]{b,m});return true;}
        public boolean setBlockMetadataWithNotify(int x,int y,int z,int m,int flags){return setBlock(x,y,z,getBlock(x,y,z),m,flags);}
        public boolean isBlockIndirectlyGettingPowered(int x,int y,int z){return powered;}
        public boolean blockExists(int x,int y,int z){return loaded;}
        public boolean checkChunksExist(int x,int y,int z,int a,int b,int c){return loaded;}
        public java.util.List getCollidingBoundingBoxes(net.minecraft.entity.Entity e,net.minecraft.util.AxisAlignedBB box){return collision?java.util.Collections.singletonList(box):java.util.Collections.emptyList();}
        public boolean isAnyLiquid(net.minecraft.util.AxisAlignedBB box){return liquid;}
        public int getBlockLightValue(int x,int y,int z){return light;}
        public void scheduleBlockUpdate(int x,int y,int z,Block block,int delay){scheduled++;lastDelay=delay;}
        public net.minecraft.tileentity.TileEntity getTileEntity(int x,int y,int z){return tiles.get(x+","+y+","+z);}
        public long getTotalWorldTime(){return time;}
        public net.minecraft.world.GameRules getGameRules(){return rules;}
        public int blockUpdates;
        public void markBlockForUpdate(int x,int y,int z){blockUpdates++;}
        public void markTileEntityChunkModified(int x,int y,int z,net.minecraft.tileentity.TileEntity tile){}
        public void func_147453_f(int x,int y,int z,Block block){}
        public java.util.List getEntitiesWithinAABB(Class type,net.minecraft.util.AxisAlignedBB box){
            java.util.List result=new java.util.ArrayList();for(Object value:loadedEntityList){net.minecraft.entity.Entity e=(net.minecraft.entity.Entity)value;if(type.isInstance(e)&&!e.isDead&&e.boundingBox.intersectsWith(box))result.add(e);}return result;
        }
        public java.util.List getEntitiesWithinAABBExcludingEntity(net.minecraft.entity.Entity excluded,net.minecraft.util.AxisAlignedBB box){
            java.util.List result=getEntitiesWithinAABB(net.minecraft.entity.Entity.class,box);result.remove(excluded);return result;
        }
        public java.util.List selectEntitiesWithinAABB(Class type,net.minecraft.util.AxisAlignedBB box,net.minecraft.command.IEntitySelector selector){
            java.util.List result=getEntitiesWithinAABB(type,box);
            if(selector!=null)for(java.util.Iterator iterator=result.iterator();iterator.hasNext();)
                if(!selector.isEntityApplicable((net.minecraft.entity.Entity)iterator.next()))iterator.remove();
            return result;
        }
        public net.minecraft.util.MovingObjectPosition rayHit;
        public net.minecraft.util.MovingObjectPosition rayTraceBlocks(net.minecraft.util.Vec3 start,net.minecraft.util.Vec3 end){return rayHit;}
        public net.minecraft.util.MovingObjectPosition func_147447_a(net.minecraft.util.Vec3 start,net.minecraft.util.Vec3 end,boolean liquids,boolean ignore,boolean last){return rayHit;}
        public void notifyBlocksOfNeighborChange(int x,int y,int z,Block b){if(requireWet)require(getBlockMetadata(0,10,0)==1,"sponge must be wet before notifications");}
        public void playAuxSFXAtEntity(net.minecraft.entity.player.EntityPlayer p,int id,int x,int y,int z,int data){}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
}
