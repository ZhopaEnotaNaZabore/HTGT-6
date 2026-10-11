package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import net.minecraft.entity.*;
import net.minecraft.pathfinding.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.*;

/** Bounded surface pathfinding: legacy WalkNodeProcessor rejects lava unconditionally. */
public final class StriderNavigation116 extends PathNavigate {
    private static final int[] HEIGHT_OPTIONS={0,1,-1};
    private final EntityLiving owner;private final World world;
    private PathEntity route;private double speed;private int stuck;
    private double lastX,lastY,lastZ;
    public StriderNavigation116(EntityLiving owner,World world){super(owner,world);this.owner=owner;this.world=world;}
    private static final class Node {
        final int x,y,z;final Node parent;final double cost,score;
        Node(int x,int y,int z,Node p,double cost,int tx,int ty,int tz){this.x=x;this.y=y;this.z=z;parent=p;this.cost=cost;score=cost+Math.abs(x-tx)+Math.abs(z-tz)+Math.abs(y-ty);}
        long key(){return ((long)x&0x3FFFFFF)<<38|((long)z&0x3FFFFFF)<<12|y&4095;}
    }
    public boolean walkable(int x,int y,int z){
        if(y<1||y+Math.ceil(owner.height)>=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.height(world)||!world.blockExists(x,y,z)||!world.blockExists(x,y-1,z))return false;
        Block floor=world.getBlock(x,y-1,z);Material material=floor.getMaterial();
        if(material==Material.water)return false;
        if(material!=Material.lava&&floor.getCollisionBoundingBoxFromPool(world,x,y-1,z)==null)return false;
        double half=owner.width/2;AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x+.5-half,y,z+.5-half,x+.5+half,y+owner.height,z+.5+half);
        for(int dy=0;dy<(int)Math.ceil(owner.height);dy++){
            Block b=world.getBlock(x,y+dy,z);if(b.getMaterial().isLiquid())return false;
            AxisAlignedBB collision=b.getCollisionBoundingBoxFromPool(world,x,y+dy,z);if(collision!=null&&collision.intersectsWith(box))return false;
        }
        return world.getCollidingBoundingBoxes(owner,box).isEmpty();
    }
    @Override public PathEntity getPathToXYZ(double px,double py,double pz){
        int sx=MathHelper.floor_double(owner.posX),sy=MathHelper.floor_double(owner.boundingBox.minY+.5),sz=MathHelper.floor_double(owner.posZ),tx=MathHelper.floor_double(px),ty=MathHelper.floor_double(py),tz=MathHelper.floor_double(pz);
        if(Math.abs(tx-sx)>32||Math.abs(tz-sz)>32)return null;
        PriorityQueue<Node> open=new PriorityQueue<Node>(64,new Comparator<Node>(){public int compare(Node a,Node b){return Double.compare(a.score,b.score);}});
        Map<Long,Double> costs=new HashMap<Long,Double>();Node start=new Node(sx,sy,sz,null,0,tx,ty,tz),best=start;open.add(start);costs.put(start.key(),0D);
        for(int expanded=0;expanded<384&&!open.isEmpty();expanded++){
            Node node=open.poll();if(node.cost>costs.get(node.key()))continue;
            if(Math.abs(node.x-tx)+Math.abs(node.z-tz)+Math.abs(node.y-ty)<Math.abs(best.x-tx)+Math.abs(best.z-tz)+Math.abs(best.y-ty))best=node;
            if(node.x==tx&&node.z==tz&&Math.abs(node.y-ty)<=1){best=node;break;}
            for(int side=0;side<4;side++){
                int x=node.x+(side==0?1:side==1?-1:0),z=node.z+(side==2?1:side==3?-1:0);if(Math.abs(x-sx)>32||Math.abs(z-sz)>32)continue;
                for(int dy:HEIGHT_OPTIONS){
                    int y=node.y+dy;if(!walkable(x,y,z))continue;
                    // A one-block rise also needs headroom over the departing cell.
                    if(dy>0&&!walkable(node.x,node.y+1,node.z)){
                        double h=owner.width/2;if(!world.getCollidingBoundingBoxes(owner,AxisAlignedBB.getBoundingBox(node.x+.5-h,node.y+1,node.z+.5-h,node.x+.5+h,node.y+1+owner.height,node.z+.5+h)).isEmpty())continue;
                    }
                    double cost=node.cost+(world.getBlock(x,y-1,z).getMaterial()==Material.lava?1:1.25)+Math.abs(dy)*.25;Node next=new Node(x,y,z,node,cost,tx,ty,tz);Double known=costs.get(next.key());if(known==null||cost<known){costs.put(next.key(),cost);open.add(next);}break;
                }
            }
        }
        if(best==start)return null;ArrayList<PathPoint> points=new ArrayList<PathPoint>();for(Node n=best;n!=null;n=n.parent)points.add(new PathPoint(n.x,n.y,n.z));Collections.reverse(points);return new PathEntity(points.toArray(new PathPoint[points.size()]));
    }
    @Override public PathEntity getPathToEntityLiving(Entity target){return getPathToXYZ(target.posX,target.boundingBox.minY,target.posZ);}
    @Override public boolean tryMoveToXYZ(double x,double y,double z,double speed){return setPath(getPathToXYZ(x,y,z),speed);}
    @Override public boolean tryMoveToEntityLiving(Entity target,double speed){return setPath(getPathToEntityLiving(target),speed);}
    @Override public boolean setPath(PathEntity path,double speed){route=path;this.speed=speed;stuck=0;lastX=owner.posX;lastY=owner.posY;lastZ=owner.posZ;return !noPath();}
    @Override public void setSpeed(double speed){this.speed=speed;}
    @Override public PathEntity getPath(){return route;}
    @Override public boolean noPath(){return route==null||route.isFinished();}
    @Override public void clearPathEntity(){route=null;}
    @Override public void onUpdateNavigation(){
        if(noPath())return;if(owner.riddenByEntity instanceof net.minecraft.entity.player.EntityPlayer){clearPathEntity();return;}
        Vec3 next=route.getPosition(owner);double dx=next.xCoord-owner.posX,dz=next.zCoord-owner.posZ;
        if(dx*dx+dz*dz<.25&&Math.abs(next.yCoord-owner.boundingBox.minY)<1){route.incrementPathIndex();if(noPath())return;next=route.getPosition(owner);}
        if(!walkable(MathHelper.floor_double(next.xCoord),MathHelper.floor_double(next.yCoord),MathHelper.floor_double(next.zCoord))){clearPathEntity();return;}
        double targetY=next.yCoord;if(world.getBlock(MathHelper.floor_double(next.xCoord),MathHelper.floor_double(next.yCoord)-1,MathHelper.floor_double(next.zCoord)).getMaterial()==Material.lava)targetY-=.5;
        owner.getMoveHelper().setMoveTo(next.xCoord,targetY,next.zCoord,speed);
        if(++stuck>=100){if(owner.getDistanceSq(lastX,lastY,lastZ)<.25)clearPathEntity();stuck=0;lastX=owner.posX;lastY=owner.posY;lastZ=owner.posZ;}
    }
}
