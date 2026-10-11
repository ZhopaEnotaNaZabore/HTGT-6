package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SculkVein;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** SculkPatchFeature bridge; charge propagation is adapted to the legacy world API. */
public final class FuturaDeepDarkRuntime120 {
    public static final int PLACEMENT_COUNT=256;
    public static final int SCULK_VEIN_MIN_COUNT=204;
    public static final int SCULK_VEIN_MAX_COUNT=250;
    public static final int SCULK_VEIN_SEARCH_RANGE=20;
    public static final PatchConfig DEEP_DARK=new PatchConfig(10,32,64,0,1,0,0,.5F);
    public static final PatchConfig ANCIENT_CITY=new PatchConfig(10,32,64,0,1,1,3,.5F);
    private static final int[][] DIRECTIONS={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    private final FuturaMountainDensityRouter120 terrain;
    private final MutableDensityContext120 densitySample=new MutableDensityContext120();

    public FuturaDeepDarkRuntime120(FuturaMountainDensityRouter120 terrain){
        if(terrain==null)throw new NullPointerException("terrain");
        this.terrain=terrain;
    }

    public void populate(World world,Random random,int chunkX,int chunkZ,int generationHeight){
        populateFeature(world,random,chunkX,chunkZ,generationHeight,null);
    }
    public void populateFeature(World world,Random random,int chunkX,int chunkZ,int generationHeight,String selected){
        if(!(world.getWorldChunkManager() instanceof FuturaWorldChunkManager120))return;
        FuturaWorldChunkManager120 manager=(FuturaWorldChunkManager120)world.getWorldChunkManager();
        int originX=chunkX<<4,originZ=chunkZ<<4;
        int maxY=Math.min(generationHeight-1,FuturaMountainDensityRouter120.modernToFuturaY(256));
        random=FuturaPlacementData120.INSTANCE.random(world.getSeed(),chunkX,chunkZ,7,"sculk_vein");
        int veinAttempts=SCULK_VEIN_MIN_COUNT+random.nextInt(
                SCULK_VEIN_MAX_COUNT-SCULK_VEIN_MIN_COUNT+1);
        if(selected==null||selected.equals("minecraft:sculk_vein"))for(int attempt=0;attempt<veinAttempts;attempt++){
            int x=originX+random.nextInt(16),z=originZ+random.nextInt(16);
            int y=random.nextInt(Math.max(1,maxY+1));
            if(isDeepDark(manager,x,y,z))generateVein(world,random,x,y,z);
        }
        random=FuturaPlacementData120.INSTANCE.random(world.getSeed(),chunkX,chunkZ,7,"sculk_patch_deep_dark");
        if(selected==null||selected.equals("minecraft:sculk_patch_deep_dark"))for(int attempt=0;attempt<PLACEMENT_COUNT;attempt++){
            int x=originX+random.nextInt(16),z=originZ+random.nextInt(16);
            int y=random.nextInt(Math.max(1,maxY+1));
            if(!isDeepDark(manager,x,y,z)||!validOrigin(world,x,y,z))continue;
            generate(world,random,x,y,z,DEEP_DARK);
        }
    }

    private boolean isDeepDark(FuturaWorldChunkManager120 manager,int x,int y,int z){
        return manager.getUndergroundBiomeAt(x,y,z,terrain,densitySample).biomeID
                ==FuturaBiomeIds120.DEEP_DARK;
    }

    /** Called by the official Ancient City feature_pool_element, once in its owning chunk. */
    public static boolean generateAncientCity(World world,int x,int y,int z){
        Random random=new Random(mix(world.getSeed()^(long)x*341873128712L^(long)z*132897987541L^y));
        return generate(world,random,x,y,z,ANCIENT_CITY);
    }

    static boolean generate(World world,Random random,int x,int y,int z,PatchConfig config){
        if(!validOrigin(world,x,y,z))return false;
        List<Cursor> cursors=new ArrayList<Cursor>(config.chargeCount);
        for(int i=0;i<config.chargeCount;i++)cursors.add(new Cursor(x,y,z,config.amountPerCharge));
        int rounds=config.spreadRounds+config.growthRounds;
        for(int round=0;round<rounds;round++){
            boolean spread=round<config.spreadRounds;
            for(int attempt=0;attempt<config.spreadAttempts;attempt++)
                for(Cursor cursor:cursors)updateCursor(world,random,cursor,spread,true);
        }
        if(random.nextFloat()<=config.catalystChance&&isSturdyUp(world,x,y-1,z))
            world.setBlock(x,y,z,Backport120Content.sculkCatalyst,0,2);
        int rare=config.extraMin+(config.extraMax==config.extraMin?0:random.nextInt(config.extraMax-config.extraMin+1));
        for(int i=0;i<rare;i++)placeRareGrowth(world,random,x,y,z);
        return true;
    }

    /** Converts death experience into the same charge cursor used by generated sculk patches. */
    public static int spreadCharge(World world,Random random,int x,int y,int z,int charge){
        Cursor cursor=new Cursor(x,y,z,Math.max(0,Math.min(1000,charge)));int before=cursor.charge;
        for(int attempt=0;attempt<Math.min(512,64+before*4)&&cursor.charge>0;attempt++)updateCursor(world,random,cursor,true,false);
        return before-cursor.charge;
    }

    private static void updateCursor(World world,Random random,Cursor cursor,boolean spread,boolean worldGeneration){
        if(cursor.charge<=0)return;
        if(world instanceof FuturaGenerationRegion120&&!((FuturaGenerationRegion120)world).writable(cursor.x,cursor.z)){
            cursor.charge=0;return;
        }
        int start=random.nextInt(DIRECTIONS.length),supportX=0,supportY=0,supportZ=0,supportSide=-1;
        boolean found=false;
        for(int i=0;i<DIRECTIONS.length;i++){
            int[] d=DIRECTIONS[(start+i)%DIRECTIONS.length];
            int sx=cursor.x+d[0],sy=cursor.y+d[1],sz=cursor.z+d[2];
            if(canReplace(world.getBlock(sx,sy,sz))){supportX=sx;supportY=sy;supportZ=sz;supportSide=(start+i)%DIRECTIONS.length;found=true;break;}
        }
        if(found&&spread){
            world.setBlock(supportX,supportY,supportZ,Backport120Content.sculk,0,2);
            cursor.charge--;
            if(random.nextInt(18)==0)placeGrowth(world,random,supportX,supportY,supportZ,worldGeneration);
            else if(random.nextInt(4)==0&&world.isAirBlock(cursor.x,cursor.y,cursor.z)){
                world.setBlock(cursor.x,cursor.y,cursor.z,Backport120Content.sculkVein,0,2);
                BlockBackport120SculkVein.setFace(world,cursor.x,cursor.y,cursor.z,ForgeDirection.getOrientation(supportSide));
            }
        }
        moveCursor(world,random,cursor);
    }

    private static void moveCursor(World world,Random random,Cursor cursor){
        int start=random.nextInt(DIRECTIONS.length);
        for(int i=0;i<DIRECTIONS.length;i++){
            int[] d=DIRECTIONS[(start+i)%DIRECTIONS.length];
            int nx=cursor.x+d[0],ny=cursor.y+d[1],nz=cursor.z+d[2];
            if(ny<=0||ny>=FuturaMountainDensityRouter120.MAX_Y)continue;
            Block cell=world.getBlock(nx,ny,nz);
            if(cell!=Blocks.air&&cell!=Backport120Content.sculkVein)continue;
            for(int[] side:DIRECTIONS)if(canReplace(world.getBlock(nx+side[0],ny+side[1],nz+side[2]))
                    ||world.getBlock(nx+side[0],ny+side[1],nz+side[2])==Backport120Content.sculk){
                cursor.x=nx;cursor.y=ny;cursor.z=nz;return;
            }
        }
    }

    private static void placeGrowth(World world,Random random,int x,int y,int z,boolean canSummon){
        int[][] horizontal={{0,0},{0,-1},{0,1},{-1,0},{1,0}};
        int start=random.nextInt(horizontal.length);
        for(int i=0;i<horizontal.length;i++){
            int[] d=horizontal[(start+i)%horizontal.length];
            int px=x+d[0],pz=z+d[1],py=y+1;
            if(world.getBlock(px,py-1,pz)!=Backport120Content.sculk||!world.isAirBlock(px,py,pz))continue;
            int nearbyGrowth=0;
            for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)for(int dy=0;dy<=2;dy++){
                Block nearby=world.getBlock(px+dx,py-1+dy,pz+dz);
                if(nearby==Backport120Content.sculkSensor||nearby==Backport120Content.sculkShrieker)nearbyGrowth++;
            }
            if(nearbyGrowth>2)continue;
            if(random.nextInt(11)==0)world.setBlock(px,py,pz,Backport120Content.sculkShrieker,canSummon?1:0,2);
            else world.setBlock(px,py,pz,Backport120Content.sculkSensor,0,2);
            return;
        }
    }

    private static void placeRareGrowth(World world,Random random,int x,int y,int z){
        for(int attempt=0;attempt<8;attempt++){
            int px=x+random.nextInt(5)-2,py=y,pz=z+random.nextInt(5)-2;
            if(world.isAirBlock(px,py,pz)&&isSturdyUp(world,px,py-1,pz)){
                world.setBlock(px,py,pz,Backport120Content.sculkShrieker,1,2);return;
            }
        }
    }

    private static boolean validOrigin(World world,int x,int y,int z){
        Block origin=world.getBlock(x,y,z);
        if(isSculk(origin))return true;
        if(origin!=Blocks.air&&origin!=Blocks.water&&origin!=Blocks.flowing_water)return false;
        for(int side=0;side<DIRECTIONS.length;side++){
            int[] d=DIRECTIONS[side];int nx=x+d[0],ny=y+d[1],nz=z+d[2];
            if(world.getBlock(nx,ny,nz).isSideSolid(world,nx,ny,nz,
                    ForgeDirection.getOrientation(side).getOpposite()))return true;
        }
        return false;
    }

    /** Direct port of the separate 1.20 sculk_vein multiface-growth feature. */
    static boolean generateVein(World world,Random random,int x,int y,int z){
        if(!canContainVein(world.getBlock(x,y,z)))return false;
        int start=random.nextInt(DIRECTIONS.length);
        if(attachVein(world,x,y,z,start))return true;
        for(int d=0;d<DIRECTIONS.length;d++){
            int[] travel=DIRECTIONS[(start+d)%DIRECTIONS.length];
            int px=x,py=y,pz=z;
            for(int distance=1;distance<=SCULK_VEIN_SEARCH_RANGE;distance++){
                px+=travel[0];py+=travel[1];pz+=travel[2];
                if(py<=0||py>=FuturaMountainDensityRouter120.MAX_Y
                        ||!canContainVein(world.getBlock(px,py,pz)))break;
                if(attachVein(world,px,py,pz,random.nextInt(DIRECTIONS.length)))return true;
            }
        }
        return false;
    }

    private static boolean attachVein(World world,int x,int y,int z,int start){
        for(int i=0;i<DIRECTIONS.length;i++){
            int side=(start+i)%DIRECTIONS.length;int[] d=DIRECTIONS[side];
            if(isVeinSupport(world.getBlock(x+d[0],y+d[1],z+d[2]))){
                world.setBlock(x,y,z,Backport120Content.sculkVein,0,2);
                BlockBackport120SculkVein.setFace(world,x,y,z,ForgeDirection.getOrientation(side));return true;
            }
        }
        return false;
    }

    private static boolean canContainVein(Block block){
        return block==Blocks.air||block==Blocks.water||block==Blocks.flowing_water
                ||block==Backport120Content.sculkVein;
    }

    public static boolean isVeinSupport(Block block){
        return block==Blocks.stone||block==Backport120Content.deepslate
                ||block==Backport120Content.tuff||block==Backport120Content.calcite
                ||block==Backport120Content.dripstoneBlock;
    }

    private static boolean isSturdyUp(World world,int x,int y,int z){
        return world.getBlock(x,y,z).isSideSolid(world,x,y,z,ForgeDirection.UP);
    }

    private static boolean isSculk(Block block){
        return block==Backport120Content.sculk||block==Backport120Content.sculkVein
                ||block==Backport120Content.sculkCatalyst||block==Backport120Content.sculkSensor
                ||block==Backport120Content.sculkShrieker;
    }

    public static boolean canReplace(Block block){
        return block==Blocks.stone||block==Blocks.dirt||block==Blocks.grass||block==Blocks.mycelium
                ||block==Backport120Content.rootedDirt||block==Backport120Content.mossBlock
                ||block==Backport120Content.mud||block==Backport120Content.muddyMangroveRoots
                ||block==Blocks.hardened_clay||block==Blocks.stained_hardened_clay
                ||block==Blocks.netherrack||block==Blocks.sand||block==Blocks.gravel
                ||block==Blocks.soul_sand||block==Blocks.clay||block==Blocks.end_stone
                ||block==Blocks.sandstone||block==Backport120Content.deepslate
                ||block==Backport120Content.cobbledDeepslate||block==Backport120Content.polishedDeepslate
                ||block==Backport120Content.deepslateBricks||block==Backport120Content.crackedDeepslateBricks
                ||block==Backport120Content.deepslateTiles||block==Backport120Content.crackedDeepslateTiles
                ||block==Backport120Content.tuff||block==Backport120Content.calcite
                ||block==Backport120Content.smoothBasalt||block==Backport120Content.dripstoneBlock;
    }

    private static long mix(long x){x^=x>>>33;x*=0xff51afd7ed558ccdL;x^=x>>>33;
        x*=0xc4ceb9fe1a85ec53L;return x^(x>>>33);}

    public static final class PatchConfig{
        public final int chargeCount,amountPerCharge,spreadAttempts,growthRounds,spreadRounds,extraMin,extraMax;
        public final float catalystChance;
        public PatchConfig(int chargeCount,int amountPerCharge,int spreadAttempts,int growthRounds,int spreadRounds,
                    int extraMin,int extraMax,float catalystChance){this.chargeCount=chargeCount;
            this.amountPerCharge=amountPerCharge;this.spreadAttempts=spreadAttempts;this.growthRounds=growthRounds;
            this.spreadRounds=spreadRounds;this.extraMin=extraMin;this.extraMax=extraMax;
            this.catalystChance=catalystChance;}
    }
    private static final class Cursor{int x,y,z,charge;
        Cursor(int x,int y,int z,int charge){this.x=x;this.y=y;this.z=z;this.charge=charge;}}
}
