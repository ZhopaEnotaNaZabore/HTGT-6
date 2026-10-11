package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.relauncher.*;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.ForgeDirection;

/** Modern wall geometry is derived from neighbours; only source water uses metadata bit 0. */
public class BlockBlackstoneWall116 extends BlockWall implements IWaterloggedBlock150 {
    private static final int[] DX={0,1,0,-1}, DZ={-1,0,1,0};
    private static final ForgeDirection[] FACE={ForgeDirection.SOUTH,ForgeDirection.WEST,ForgeDirection.NORTH,ForgeDirection.EAST};
    private final Block source;
    public BlockBlackstoneWall116(Block source,String name) {
        super(source);this.source=source;
        setBlockName("backport_1_20."+name);setHarvestLevel("pickaxe",0);
        setStepSound(BuildingSounds116.STONE);setLightOpacity(0);useNeighborBrightness=true;
    }
    public static final class State {
        public final int[] side; // N,E,S,W: none=0, low=1, tall=2
        public final boolean up;
        State(int[] side,boolean up){this.side=side;this.up=up;}
        public String variant(){String[] v={"none","low","tall"};return "east="+v[side[1]]+",north="+v[side[0]]+",south="+v[side[2]]+",up="+up+",west="+v[side[3]];}
        public List<AxisAlignedBB> boxes(boolean collision) {
            List<AxisAlignedBB> out=new ArrayList<AxisAlignedBB>();
            if(up)out.add(box(4,0,4,12,collision?24:16,12));
            for(int i=0;i<4;i++)if(side[i]!=0) {
                int h=collision?24:side[i]==2?16:14;
                out.add(i==0?box(5,0,0,11,h,11):i==1?box(5,0,5,16,h,11):i==2?box(5,0,5,11,h,16):box(0,0,5,11,h,11));
            }
            return out;
        }
    }
    private static AxisAlignedBB box(double x,double y,double z,double xx,double yy,double zz){return AxisAlignedBB.getBoundingBox(x/16,y/16,z/16,xx/16,yy/16,zz/16);}
    public boolean connects(IBlockAccess w,int x,int y,int z,int direction) {
        int nx=x+DX[direction],nz=z+DZ[direction];Block b=w.getBlock(nx,y,nz);
        if(b instanceof BlockWall||b instanceof BlockPane)return true;
        if(b instanceof BlockFenceGate)return (w.getBlockMetadata(nx,y,nz)&1)==(direction%2==0?1:0);
        if(b instanceof BlockLeavesBase||b.getMaterial()==net.minecraft.block.material.Material.gourd
                ||b instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBarrier
                ||b instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112)return false;
        if(b instanceof BlockFarmland)return false;
        if(b instanceof BlockStairs) {
            List<AxisAlignedBB> face=new ArrayList<AxisAlignedBB>();
            for(AxisAlignedBB part:parts(w,nx,y,nz,b)) {
                boolean touches=direction==0?part.maxZ>=1:direction==1?part.minX<=0:direction==2?part.minZ<=0:part.maxX>=1;
                if(touches)face.add(AxisAlignedBB.getBoundingBox(direction%2==0?part.minX:part.minZ,0,part.minY,direction%2==0?part.maxX:part.maxZ,1,part.maxY));
            }
            return covers(face,0,0,16,16);
        }
        if(b instanceof BlockBreakable)return true;
        return b.isSideSolid(w,nx,y,nz,FACE[direction]);
    }
    private static boolean modern(Block block){return block==Blocks.cobblestone_wall||block instanceof BlockBlackstoneWall116;}
    public State state(IBlockAccess w,int x,int y,int z) {
        int top=y;while(top<w.getHeight()-1&&modern(w.getBlock(x,top+1,z)))top++;
        State above=null;for(int yy=top;yy>=y;yy--)above=stateUnder(w,x,yy,z,above);return above;
    }
    private State stateUnder(IBlockAccess w,int x,int y,int z,State wallAbove) {
        int[] sides=new int[4];
        Block above=y>=w.getHeight()-1?Blocks.air:w.getBlock(x,y+1,z);
        List<AxisAlignedBB> bottom=wallAbove!=null?wallAbove.boxes(false):parts(w,x,y+1,z,above);
        for(int i=0;i<4;i++)if(connects(w,x,y,z,i))sides[i]=covers(bottom,i==3?0:7,i==0?0:7,i==1?16:9,i==2?16:9)?2:1;
        boolean n=sides[0]==0,e=sides[1]==0,s=sides[2]==0,west=sides[3]==0;
        boolean up;
        if(wallAbove!=null&&wallAbove.up||wallAbove==null&&legacyPost(w,x,y+1,z,above))up=true;
        else if(n&&e&&s&&west||n!=s||e!=west)up=true;
        else if(sides[0]==2&&sides[2]==2||sides[1]==2&&sides[3]==2)up=false;
        else up=(postOverride(above)&&(!(above instanceof BlockTorch)||w.getBlockMetadata(x,y+1,z)==5))||covers(bottom,7,7,9,9);
        return new State(sides,up);
    }
    private static boolean legacyPost(IBlockAccess w,int x,int y,int z,Block b) {
        if(!(b instanceof BlockWall))return false;
        BlockWall wall=(BlockWall)b;
        boolean n=wall.canConnectWallTo(w,x,y,z-1),e=wall.canConnectWallTo(w,x+1,y,z),s=wall.canConnectWallTo(w,x,y,z+1),west=wall.canConnectWallTo(w,x-1,y,z);
        return !(n&&s&&!e&&!west||e&&west&&!n&&!s)||!w.isAirBlock(x,y+1,z);
    }
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block changed) {
        // State is derived, so a changed upper post must also invalidate lower meshes.
        // Walk down once rather than emitting recursive neighbour notifications.
        for(int yy=y;yy>=0&&w.getBlock(x,yy,z) instanceof BlockWall;yy--)w.markBlockForUpdate(x,yy,z);
    }
    private static boolean postOverride(Block b) {
        return b instanceof BlockTorch||b instanceof BlockSign||b instanceof BlockBasePressurePlate||b==Blocks.tripwire
            ||b instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulBanner;
    }
    /** Collect component boxes without a World-only adapter; also works with client ChunkCache. */
    private static List<AxisAlignedBB> parts(IBlockAccess w,int x,int y,int z,Block b) {
        List<AxisAlignedBB> out=new ArrayList<AxisAlignedBB>();
        if(b instanceof BlockSnow) {
            int layers=w.getBlockMetadata(x,y,z)&7;
            if(layers>0)out.add(box(0,0,0,16,layers*2,16));
            return out;
        }
        if(!b.getMaterial().blocksMovement()||postOverride(b))return out;
        synchronized(b) {
            double a=b.getBlockBoundsMinX(),c=b.getBlockBoundsMinY(),d=b.getBlockBoundsMinZ(),e=b.getBlockBoundsMaxX(),f=b.getBlockBoundsMaxY(),g=b.getBlockBoundsMaxZ();
            try {
                if(b instanceof BlockFence||b instanceof BlockPane||b instanceof BlockWall) {
                    int half=b instanceof BlockPane?1:b instanceof BlockFence?2:4;
                    boolean[] connected=new boolean[4];boolean any=false;
                    for(int i=0;i<4;i++) {
                        int nx=x+DX[i],nz=z+DZ[i];
                        connected[i]=b instanceof BlockPane?((BlockPane)b).canPaneConnectTo(w,nx,y,nz,FACE[i].getOpposite()):b instanceof BlockFence?((BlockFence)b).canConnectFenceTo(w,nx,y,nz):((BlockWall)b).canConnectWallTo(w,nx,y,nz);
                        any|=connected[i];
                    }
                    out.add(box(8-half,0,8-half,8+half,16,8+half));
                    for(int i=0;i<4;i++)if(connected[i]||b instanceof BlockPane&&!any)
                        out.add(i==0?box(8-half,0,0,8+half,16,8+half):i==1?box(8-half,0,8-half,16,16,8+half):i==2?box(8-half,0,8-half,8+half,16,16):box(0,0,8-half,8+half,16,8+half));
                } else if(b instanceof BlockStairs) {
                    BlockStairs stairs=(BlockStairs)b;
                    stairs.func_150147_e(w,x,y,z);addPart(out,b);
                    boolean inner=stairs.func_150145_f(w,x,y,z);addPart(out,b);
                    if(inner&&stairs.func_150144_g(w,x,y,z))addPart(out,b);
                } else if(b instanceof BlockFenceGate&&(w.getBlockMetadata(x,y,z)&4)!=0) {
                    return out;
                } else {
                    b.setBlockBoundsBasedOnState(w,x,y,z);addPart(out,b);
                }
            } finally {b.setBlockBounds((float)a,(float)c,(float)d,(float)e,(float)f,(float)g);}
        }
        return out;
    }
    private static void addPart(List<AxisAlignedBB> out,Block b) {
        if(b.getBlockBoundsMaxY()>b.getBlockBoundsMinY())out.add(AxisAlignedBB.getBoundingBox(b.getBlockBoundsMinX(),b.getBlockBoundsMinY(),b.getBlockBoundsMinZ(),b.getBlockBoundsMaxX(),b.getBlockBoundsMaxY(),b.getBlockBoundsMaxZ()));
    }
    /** Exact rectangle-union coverage, including fractional mod block bounds. */
    public static boolean covers(List<AxisAlignedBB> boxes,int x0,int z0,int x1,int z1) {
        TreeSet<Double> xs=new TreeSet<Double>(),zs=new TreeSet<Double>();
        double ax=x0/16D,az=z0/16D,bx=x1/16D,bz=z1/16D;
        xs.add(ax);xs.add(bx);zs.add(az);zs.add(bz);
        for(AxisAlignedBB b:boxes){if(b.minX>ax&&b.minX<bx)xs.add(b.minX);if(b.maxX>ax&&b.maxX<bx)xs.add(b.maxX);if(b.minZ>az&&b.minZ<bz)zs.add(b.minZ);if(b.maxZ>az&&b.maxZ<bz)zs.add(b.maxZ);}
        Double[] xx=xs.toArray(new Double[0]),zz=zs.toArray(new Double[0]);
        for(int i=1;i<xx.length;i++)for(int j=1;j<zz.length;j++) {
            double x=(xx[i-1]+xx[i])/2,z=(zz[j-1]+zz[j])/2;boolean found=false;
            for(AxisAlignedBB b:boxes)if(b.minY<=0&&b.maxY>0&&x>=b.minX&&x<=b.maxX&&z>=b.minZ&&z<=b.maxZ){found=true;break;}
            if(!found)return false;
        }
        return true;
    }
    @Override public void addCollisionBoxesToList(World w,int x,int y,int z,AxisAlignedBB mask,List out,Entity entity) {
        for(AxisAlignedBB local:state(w,x,y,z).boxes(true)){AxisAlignedBB b=local.getOffsetBoundingBox(x,y,z);if(mask.intersectsWith(b))out.add(b);}
    }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){return envelope(state(w,x,y,z).boxes(true)).getOffsetBoundingBox(x,y,z);}
    private static AxisAlignedBB envelope(List<AxisAlignedBB> boxes){AxisAlignedBB out=boxes.get(0).copy();for(int i=1;i<boxes.size();i++)out=out.func_111270_a(boxes.get(i));return out;}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){AxisAlignedBB b=envelope(state(w,x,y,z).boxes(false));setBlockBounds((float)b.minX,(float)b.minY,(float)b.minZ,(float)b.maxX,(float)b.maxY,(float)b.maxZ);}
    @Override public MovingObjectPosition collisionRayTrace(World w,int x,int y,int z,Vec3 start,Vec3 end) {
        MovingObjectPosition nearest=null;double distance=Double.POSITIVE_INFINITY;
        for(AxisAlignedBB box:state(w,x,y,z).boxes(false)) {
            MovingObjectPosition hit=box.getOffsetBoundingBox(x,y,z).calculateIntercept(start,end);
            if(hit!=null&&start.squareDistanceTo(hit.hitVec)<distance){distance=start.squareDistanceTo(hit.hitVec);nearest=new MovingObjectPosition(x,y,z,hit.sideHit,hit.hitVec);}
        }
        return nearest;
    }
    @Override public void getSubBlocks(net.minecraft.item.Item item,net.minecraft.creativetab.CreativeTabs tab,List out){out.add(new net.minecraft.item.ItemStack(item));}
    @Override public int damageDropped(int meta){return 0;}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side,int meta){return source.getIcon(side,0);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override @SideOnly(Side.CLIENT) public int getRenderBlockPass(){return 1;}
    @Override @SideOnly(Side.CLIENT) public boolean canRenderInPass(int pass){return com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.pass(pass,false);}
    @Override public int getLightOpacity(IBlockAccess w,int x,int y,int z){return isWaterlogged(w,x,y,z)?1:0;}
    public boolean isWaterlogged(IBlockAccess w,int x,int y,int z){return (w.getBlockMetadata(x,y,z)&1)!=0;}
    public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){return ShapeWater116.set(w,x,y,z,this,1,wet);}
    @Override public int onBlockPlaced(World w,int x,int y,int z,int side,float hx,float hy,float hz,int meta){return ShapeWater116.present(w,x,y,z)?1:0;}
    @Override public float getExplosionResistance(Entity entity,World w,int x,int y,int z,double ex,double ey,double ez){float dry=super.getExplosionResistance(entity,w,x,y,z,ex,ey,ez);return isWaterlogged(w,x,y,z)?Math.max(100F,dry):dry;}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){return ShapeWater116.bucket(this,w,x,y,z,p,side);}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){return ShapeWater116.remove(w,x,y,z,isWaterlogged(w,x,y,z));}
    @Override public void onBlockExploded(World w,int x,int y,int z,Explosion explosion){boolean wet=isWaterlogged(w,x,y,z);super.onBlockExploded(w,x,y,z,explosion);if(wet&&w.isAirBlock(x,y,z))w.setBlock(x,y,z,Blocks.water,0,3);}
}
