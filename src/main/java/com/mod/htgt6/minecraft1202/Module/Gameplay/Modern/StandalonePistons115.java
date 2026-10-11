package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

/** Independent vanilla-piston solver. Planning never mutates the world. */
public final class StandalonePistons115 {
    private StandalonePistons115(){}
    private static int dx(int d){return Facing.offsetsXForSide[d];}
    private static int dy(int d){return Facing.offsetsYForSide[d];}
    private static int dz(int d){return Facing.offsetsZForSide[d];}
    public static boolean sticky(Block b){return b==EtFVanillaGapContent120.honeyBlock||b==BountifulContent.slimeBlock;}
    public static boolean adhere(Block a,Block b){
        if(a==EtFVanillaGapContent120.honeyBlock&&b==BountifulContent.slimeBlock||b==EtFVanillaGapContent120.honeyBlock&&a==BountifulContent.slimeBlock)return false;
        String name=String.valueOf(Block.blockRegistry.getNameForObject(b));
        return !name.contains("glazed_terracotta");
    }
    private static final class Pos {
        final int x,y,z;Pos(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
        Pos step(int side){return new Pos(x+dx(side),y+dy(side),z+dz(side));}
        @Override public boolean equals(Object o){if(!(o instanceof Pos))return false;Pos p=(Pos)o;return x==p.x&&y==p.y&&z==p.z;}
        @Override public int hashCode(){return (x*31+y)*31+z;}
    }
    public static final class Plan {
        final World world;final Pos base,head;final int side,direction;final boolean extending;
        final LinkedHashMap<Pos,State> moving=new LinkedHashMap<Pos,State>();
        final LinkedHashMap<Pos,State> destroyed=new LinkedHashMap<Pos,State>();
        public final boolean possible;
        Plan(World w,int x,int y,int z,int side,boolean extend){world=w;base=new Pos(x,y,z);this.side=side;head=base.step(side);direction=extend?side:side^1;extending=extend;possible=side>=0&&side<6&&resolve(extend?head:head.step(side),false);}
        public int movingCount(){return moving.size();}
        public int destroyedCount(){return destroyed.size();}
        private boolean movable(Pos p,Block b,boolean breakable){
            if(!world.blockExists(p.x,p.y,p.z)||p.y<0||p.y>=world.getHeight()||p.equals(base))return false;
            if(b==Blocks.obsidian||b==Blocks.piston_extension||b.getBlockHardness(world,p.x,p.y,p.z)<0||b.getMobilityFlag()==2)return false;
            if(b==Blocks.piston||b==Blocks.sticky_piston)return (world.getBlockMetadata(p.x,p.y,p.z)&8)==0;
            return !b.hasTileEntity(world.getBlockMetadata(p.x,p.y,p.z))&&(b.getMobilityFlag()!=1||breakable);
        }
        private boolean resolve(Pos p,boolean attached){
            if(!extending&&p.equals(head))return true;
            if(!world.blockExists(p.x,p.y,p.z)||p.y<0||p.y>=world.getHeight())return false;
            Block b=world.getBlock(p.x,p.y,p.z);
            if(b.isAir(world,p.x,p.y,p.z))return world.blockExists(p.x,p.y,p.z)&&p.y>=0&&p.y<world.getHeight();
            if(moving.containsKey(p)||destroyed.containsKey(p))return true;
            if(attached&&!movable(p,b,false))return true;
            if(!movable(p,b,extending&&!attached))return false;
            if(b.getMobilityFlag()==1){destroyed.put(p,new State(world,p));return true;}
            if(moving.size()>=12)return false;
            moving.put(p,new State(world,p));
            if(!resolve(p.step(direction),false))return false;
            if(sticky(b))for(int neighbour=0;neighbour<6;neighbour++){
                if(neighbour==direction)continue;Pos next=p.step(neighbour);if(!world.blockExists(next.x,next.y,next.z))return false;Block other=world.getBlock(next.x,next.y,next.z);
                if(adhere(b,other)&&!resolve(next,true))return false;
            }
            return true;
        }
    }
    private static final class State {
        final Block block;final int metadata;State(World w,Pos p){block=w.getBlock(p.x,p.y,p.z);metadata=w.getBlockMetadata(p.x,p.y,p.z);}
        void restore(World w,Pos p){w.removeTileEntity(p.x,p.y,p.z);w.setBlock(p.x,p.y,p.z,block,metadata,2);}
    }
    public static Plan plan(World w,int x,int y,int z,int side,boolean extending){if(side<0||side>5)throw new IllegalArgumentException("piston facing");return new Plan(w,x,y,z,side,extending);}
    public static boolean powered(World w,int x,int y,int z,int face){
        for(int side=0;side<6;side++)if(side!=face&&w.getIndirectPowerOutput(x+dx(side),y+dy(side),z+dz(side),side))return true;
        if(w.getIndirectPowerOutput(x,y,z,0)||w.getIndirectPowerOutput(x,y+2,z,1))return true;
        for(int side=2;side<6;side++)if(w.getIndirectPowerOutput(x+dx(side),y+1,z+dz(side),side))return true;
        return false;
    }
    public static void update(BlockPistonBase piston,World w,int x,int y,int z){
        if(w.isRemote||w.getBlock(x,y,z)!=piston)return;int meta=w.getBlockMetadata(x,y,z),face=meta&7;if(face>5)return;
        boolean power=powered(w,x,y,z,face);
        if(power&&(meta&8)==0&&plan(w,x,y,z,face,true).possible)w.addBlockEvent(x,y,z,piston,0,face);
        else if(!power&&(meta&8)!=0)w.addBlockEvent(x,y,z,piston,1,face);
    }
    public static boolean event(BlockPistonBase piston,World w,int x,int y,int z,int event,int face){
        if(face<0||face>5||event<0||event>1)return false;
        if(!w.isRemote){boolean power=powered(w,x,y,z,face);if(power&&event==1){w.setBlockMetadataWithNotify(x,y,z,face|8,2);return false;}if(!power&&event==0)return false;}
        Pos base=new Pos(x,y,z),head=base.step(face);boolean extend=event==0,stickyPiston=piston==Blocks.sticky_piston;
        if(!extend){
            TileEntity t=w.getTileEntity(head.x,head.y,head.z);if(t instanceof TileEntityPiston)((TileEntityPiston)t).clearPistonTileEntity();
            Pos front=head.step(face);TileEntity moving=w.getTileEntity(front.x,front.y,front.z);
            // A short pulse drops the extending block instead of pulling it back.
            if(moving instanceof TileEntityPiston&&((TileEntityPiston)moving).isExtending()&&((TileEntityPiston)moving).getPistonOrientation()==face){((TileEntityPiston)moving).clearPistonTileEntity();stickyPiston=false;}
        }
        Plan plan=plan(w,x,y,z,face,extend);
        if(extend&&!plan.possible)return false;
        if(!extend&&(!stickyPiston||!plan.possible)){plan.moving.clear();plan.destroyed.clear();}
        LinkedHashMap<Pos,State> before=new LinkedHashMap<Pos,State>();before.put(base,new State(w,base));before.put(head,new State(w,head));
        for(Pos p:plan.moving.keySet()){if(!before.containsKey(p))before.put(p,new State(w,p));Pos dest=p.step(plan.direction);if(!before.containsKey(dest))before.put(dest,new State(w,dest));}
        for(Pos p:plan.destroyed.keySet())if(!before.containsKey(p))before.put(p,new State(w,p));
        boolean accepted=true;
        for(Pos p:plan.moving.keySet())accepted&=w.setBlock(p.x,p.y,p.z,Blocks.air,0,4);
        for(Pos p:plan.destroyed.keySet())accepted&=w.setBlock(p.x,p.y,p.z,Blocks.air,0,4);
        if(!extend&&w.getBlock(head.x,head.y,head.z)!=Blocks.air)accepted&=w.setBlock(head.x,head.y,head.z,Blocks.air,0,4);
        for(Map.Entry<Pos,State> entry:plan.moving.entrySet()){
            Pos dest=entry.getKey().step(plan.direction);State state=entry.getValue();accepted&=moving(w,dest,state.block,state.metadata,face,extend,false);
        }
        if(extend){accepted&=moving(w,head,Blocks.piston_head,face|(stickyPiston?8:0),face,true,false);if(w.getBlockMetadata(x,y,z)!=(face|8))accepted&=w.setBlockMetadataWithNotify(x,y,z,face|8,2);}
        else accepted&=moving(w,base,piston,face,face,false,true);
        if(!accepted){for(Map.Entry<Pos,State> e:before.entrySet())e.getValue().restore(w,e.getKey());return false;}
        for(Map.Entry<Pos,State> e:plan.destroyed.entrySet()){Pos p=e.getKey();State s=e.getValue();s.block.dropBlockAsItemWithChance(w,p.x,p.y,p.z,s.metadata,s.block instanceof BlockSnow?-1F:1F,0);}
        for(Map.Entry<Pos,State> e:before.entrySet()){Pos p=e.getKey();w.notifyBlocksOfNeighborChange(p.x,p.y,p.z,e.getValue().block);}
        w.playSoundEffect(x+.5,y+.5,z+.5,"tile.piston."+(extend?"out":"in"),.5F,.6F+w.rand.nextFloat()*.2F);return true;
    }
    private static boolean moving(World w,Pos p,Block block,int meta,int face,boolean extend,boolean head){
        if(!w.setBlock(p.x,p.y,p.z,Blocks.piston_extension,meta,4))return false;
        w.setTileEntity(p.x,p.y,p.z,BlockPistonMoving.getTileEntity(block,meta,face,extend,head));return true;
    }
    /** Additional sticky surface motion; vanilla still resolves the moving block's front collision. */
    public static void tick(TileEntityPiston tile){
        World w=tile.getWorldObj();if(w==null)return;Block block=tile.getStoredBlockID();if(!sticky(block))return;
        float progress=tile.func_145860_a(1);if(progress>=1)return;int face=tile.getPistonOrientation();if(face<0||face>5)return;
        double offset=tile.isExtending()?progress-1:1-progress;int direction=tile.isExtending()?face:face^1;
        double x=tile.xCoord+dx(face)*offset,y=tile.yCoord+dy(face)*offset,z=tile.zCoord+dz(face)*offset;
        AxisAlignedBB surface=AxisAlignedBB.getBoundingBox(x,y+.9,z,x+1,y+1.5,z+1);
        if(block==EtFVanillaGapContent120.honeyBlock&&dy(direction)==0)for(Object raw:w.getEntitiesWithinAABBExcludingEntity(null,surface)){
            Entity e=(Entity)raw;if(e.onGround&&e.boundingBox.minY>=y+.9)carry(e,w.getTotalWorldTime(),direction,.5);
        }
        AxisAlignedBB body=AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1).addCoord(dx(direction)*.5,dy(direction)*.5,dz(direction)*.5);
        for(Object raw:w.getEntitiesWithinAABBExcludingEntity(null,body)){
            Entity e=(Entity)raw;
            if(block==BountifulContent.slimeBlock){if(dx(direction)!=0)e.motionX=dx(direction);if(dy(direction)!=0)e.motionY=dy(direction);if(dz(direction)!=0)e.motionZ=dz(direction);}
            if(!tile.isExtending())carry(e,w.getTotalWorldTime(),direction,.5);
        }
    }
    private static final Map<Entity,double[]> carried=new WeakHashMap<Entity,double[]>();
    private static synchronized void carry(Entity e,long tick,int direction,double distance){
        double[] state=carried.get(e);if(state==null||state[0]!=tick){state=new double[]{tick,0,0,0};carried.put(e,state);}int axis=dx(direction)!=0?1:dy(direction)!=0?2:3;int sign=dx(direction)+dy(direction)+dz(direction);double next=Math.max(-.51,Math.min(.51,state[axis]+distance*sign)),delta=next-state[axis];state[axis]=next;if(Math.abs(delta)<1E-6)return;e.moveEntity(axis==1?delta:0,axis==2?delta:0,axis==3?delta:0);
    }
}
