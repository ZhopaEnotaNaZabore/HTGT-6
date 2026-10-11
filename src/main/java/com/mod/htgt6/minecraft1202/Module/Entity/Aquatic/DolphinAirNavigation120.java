package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/** Local, collision-aware air routes for the 1.7.10 water movement adapter. */
final class DolphinAirNavigation120 {
    private final EntityDolphin120 dolphin;
    private List<Vec3> route=Collections.emptyList();
    private int index, retry;
    private static final int[][] STEPS={{0,1,0},{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,-1,0}};

    DolphinAirNavigation120(EntityDolphin120 dolphin){this.dolphin=dolphin;}

    void clear(){route=Collections.emptyList();index=0;retry=0;}

    void tick(){
        if(dolphin.worldObj.isRemote||!dolphin.isEntityAlive()){clear();return;}
        if(retry>0)--retry;
        if(index<route.size()&&!clearSegment(position(),route.get(index))){route=Collections.emptyList();index=0;retry=0;}
        if(index>=route.size()&&retry==0){route=findRoute();index=0;retry=20;}
        Vec3 current=position();
        while(index<route.size()&&current.squareDistanceTo(route.get(index))<.01D)++index;
        if(index>=route.size()){
            // No reachable opening: brake instead of repeatedly pushing into a ceiling.
            dolphin.motionX*=.5D;dolphin.motionZ*=.5D;
            dolphin.motionY=dolphin.getAttackTarget()==null?.005D/.9D:0D;
            return;
        }
        Vec3 target=route.get(index);
        double x=target.xCoord-current.xCoord,y=target.yCoord-current.yCoord,z=target.zCoord-current.zCoord;
        double distance=Math.sqrt(x*x+y*y+z*z),speed=Math.min(.15D,distance);
        // Axis-aligned waypoints prevent corner cutting; travel applies drag afterwards.
        dolphin.motionX=x/distance*speed;dolphin.motionY=y/distance*speed;dolphin.motionZ=z/distance*speed;
        if(x*x+z*z>1E-8)dolphin.rotationYaw=(float)(Math.atan2(z,x)*180D/Math.PI)-90F;
    }

    private Vec3 position(){return Vec3.createVectorHelper(dolphin.posX,dolphin.boundingBox.minY,dolphin.posZ);}

    private List<Vec3> findRoute(){
        int sx=MathHelper.floor_double(dolphin.posX),sy=MathHelper.floor_double(dolphin.boundingBox.minY),sz=MathHelper.floor_double(dolphin.posZ);
        ArrayDeque<Node> pending=new ArrayDeque<Node>();Set<Long> seen=new HashSet<Long>();
        Node start=new Node(sx,sy,sz,null);pending.add(start);seen.add(key(sx,sy,sz));
        // At most 17*11*17 nodes, never probing unloaded chunks.
        int examined=0;Node fallback=null;
        while(!pending.isEmpty()&&examined++<3179){
            Node n=pending.removeFirst();Vec3 from=n.parent==null?position():point(n);
            for(int[] step:STEPS){
                int x=n.x+step[0],y=n.y+step[1],z=n.z+step[2];
                if(Math.abs(x-sx)>8||Math.abs(z-sz)>8||y<sy-2||y>sy+8||y<0||y>=dolphin.worldObj.getActualHeight())continue;
                if(seen.contains(key(x,y,z)))continue;
                Node next=new Node(x,y,z,n);Vec3 point=point(next);
                if(!loaded(bounds(point))||!clearSegment(from,point))continue;
                seen.add(key(x,y,z));
                Material material=dolphin.worldObj.getBlock(x,y,z).getMaterial();
                // BreathAirGoal's local candidate window: +/-1 horizontally, up to 8 above.
                if(!material.isLiquid()&&Math.abs(x-sx)<=1&&Math.abs(z-sz)<=1&&y>=sy){
                    return path(next);
                }
                if(material==Material.water){
                    pending.addLast(next);
                    if(x==sx&&y==sy+8&&z==sz)fallback=next;
                }
            }
        }
        // Match the goal's upward fallback when the surface is beyond this search window.
        return fallback==null?Collections.<Vec3>emptyList():path(fallback);
    }

    private List<Vec3> path(Node end){
        List<Vec3> result=new ArrayList<Vec3>();
        for(Node n=end;n.parent!=null;n=n.parent)result.add(point(n));
        Collections.reverse(result);return result;
    }
    private Vec3 point(Node n){return Vec3.createVectorHelper(n.x+.5D,n.y,n.z+.5D);}
    private AxisAlignedBB bounds(Vec3 p){double half=dolphin.width*.5D;return AxisAlignedBB.getBoundingBox(p.xCoord-half,p.yCoord,p.zCoord-half,p.xCoord+half,p.yCoord+dolphin.height,p.zCoord+half).contract(.001D,.001D,.001D);}
    private boolean loaded(AxisAlignedBB box){
        if(box.minY<0||box.maxY>=dolphin.worldObj.getActualHeight())return false;
        return dolphin.worldObj.checkChunksExist(MathHelper.floor_double(box.minX),MathHelper.floor_double(box.minY),MathHelper.floor_double(box.minZ),MathHelper.floor_double(box.maxX),MathHelper.floor_double(box.maxY),MathHelper.floor_double(box.maxZ));
    }
    private boolean clearSegment(Vec3 from,Vec3 to){
        AxisAlignedBB a=bounds(from),b=bounds(to);
        AxisAlignedBB swept=AxisAlignedBB.getBoundingBox(Math.min(a.minX,b.minX),Math.min(a.minY,b.minY),Math.min(a.minZ,b.minZ),Math.max(a.maxX,b.maxX),Math.max(a.maxY,b.maxY),Math.max(a.maxZ,b.maxZ));
        if(!loaded(swept)||!dolphin.worldObj.getCollidingBoundingBoxes(dolphin,swept).isEmpty())return false;
        for(int x=MathHelper.floor_double(swept.minX);x<=MathHelper.floor_double(swept.maxX);x++)
            for(int y=MathHelper.floor_double(swept.minY);y<=MathHelper.floor_double(swept.maxY);y++)
                for(int z=MathHelper.floor_double(swept.minZ);z<=MathHelper.floor_double(swept.maxZ);z++){
                    Material m=dolphin.worldObj.getBlock(x,y,z).getMaterial();
                    if(m.isLiquid()&&m!=Material.water)return false;
                }
        return true;
    }
    private static long key(int x,int y,int z){return ((long)x&0x3ffffffL)<<38|((long)z&0x3ffffffL)<<12|y&0xfffL;}
    private static final class Node {
        final int x,y,z;final Node parent;
        Node(int x,int y,int z,Node parent){this.x=x;this.y=y;this.z=z;this.parent=parent;}
    }
}
