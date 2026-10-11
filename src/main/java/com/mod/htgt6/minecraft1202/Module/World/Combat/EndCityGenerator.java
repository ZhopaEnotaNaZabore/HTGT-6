package com.mod.htgt6.minecraft1202.Module.World.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityDragonHead;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;

/** 1.12 End City layout and structure-template adapter for the 1.7 world API. */
public final class EndCityGenerator {
    private static final String[] NAMES = {"base_floor","base_roof","bridge_end","bridge_gentle_stairs",
            "bridge_piece","bridge_steep_stairs","fat_tower_base","fat_tower_middle","fat_tower_top",
            "second_floor","second_floor_2","second_roof","ship","third_floor","third_floor_b",
            "third_floor_c","third_roof","tower_base","tower_floor","tower_piece","tower_top"};
    private static final Map<String, Template> CACHE = new HashMap<String, Template>();
    private static Field tagListField;

    public static void validateTemplates() { for (String name : NAMES) load(name); }
    public static boolean shipTemplateHasDragonHead() {
        Template template = load("ship");
        for (BlockEntry entry : template.blocks) if (entry.state >= 0
                && entry.state < template.palette.length && entry.skullType == 5
                && "minecraft:skull".equals(template.palette[entry.state].name)) return true;
        return false;
    }

    public static void populateChunk(World world, ChunkProviderCombatEnd provider, int chunkX, int chunkZ) {
        int regionX = floorDiv(chunkX, 20), regionZ = floorDiv(chunkZ, 20);
        for (int rx = regionX - 1; rx <= regionX + 1; rx++) for (int rz = regionZ - 1; rz <= regionZ + 1; rz++) {
            Random placement = world.setRandomSeed(rx, rz, 10387313);
            int candidateX = rx * 20 + (placement.nextInt(9) + placement.nextInt(9)) / 2;
            int candidateZ = rz * 20 + (placement.nextInt(9) + placement.nextInt(9)) / 2;
            if (!provider.isIslandChunk(candidateX, candidateZ)) continue;
            Random rotationRandom = new Random((long) candidateX + (long) candidateZ * 10387313L);
            int rotation = rotationRandom.nextInt(4);
            int y = provider.getStructureGroundY(candidateX, candidateZ, rotation);
            if (y < 60) continue;
            // MapGenBase seeds its recursive structure RNG from both the world seed and candidate chunk.
            Random layout = new Random(world.getSeed());
            long saltX = layout.nextLong(), saltZ = layout.nextLong();
            layout.setSeed((long)candidateX * saltX ^ (long)candidateZ * saltZ ^ world.getSeed());
            City city = new City(layout, candidateX * 16 + 8, y, candidateZ * 16 + 8, rotation);
            city.build();
            city.place(world, chunkX, chunkZ);
        }
    }

    private static final class City {
        final Random random; final List<Piece> pieces = new ArrayList<Piece>();
        final int x,y,z,rotation; boolean ship;
        City(Random random,int x,int y,int z,int rotation){this.random=random;this.x=x;this.y=y;this.z=z;this.rotation=rotation;}
        void build() {
            Piece p=add(null,"base_floor",x,y,z,rotation,true);
            p=add(p,"second_floor",-1,0,-1,rotation,false);
            p=add(p,"third_floor",-1,4,-1,rotation,false);
            p=add(p,"third_roof",-1,8,-1,rotation,true);
            tower(p,1);
        }
        void tower(Piece parent,int depth) {
            if(depth>8)return;
            Piece p=add(parent,"tower_base",3+random.nextInt(2),-3,3+random.nextInt(2),parent.rot,true);
            p=add(p,"tower_piece",0,7,0,p.rot,true);
            Piece bridge=random.nextInt(3)==0?p:null;
            int count=1+random.nextInt(3);
            for(int i=0;i<count;i++){p=add(p,"tower_piece",0,4,0,p.rot,true);if(i<count-1&&random.nextBoolean())bridge=p;}
            if(bridge!=null){
                int[][] links={{0,1,-1,0},{1,6,-1,1},{3,0,-1,5},{2,5,-1,6}};
                for(int[] link:links)if(random.nextBoolean())bridge(add(bridge,"bridge_end",link[1],link[2],link[3],(bridge.rot+link[0])&3,true),depth+1);
                add(p,"tower_top",-1,4,-1,p.rot,true);
            } else if(depth!=7) fatTower(p,depth+1); else add(p,"tower_top",-1,4,-1,p.rot,true);
        }
        void bridge(Piece parent,int depth) {
            if(depth>8)return;
            int length=random.nextInt(4)+1, rise=0;
            Piece p=add(parent,"bridge_piece",0,0,-4,parent.rot,true);
            for(int i=0;i<length;i++)if(random.nextBoolean()){p=add(p,"bridge_piece",0,rise,-4,p.rot,true);rise=0;}
            else {boolean steep=random.nextBoolean();p=add(p,steep?"bridge_steep_stairs":"bridge_gentle_stairs",0,rise,steep?-4:-8,p.rot,true);rise=4;}
            if(!ship&&random.nextInt(Math.max(1,10-depth))==0){add(p,"ship",-8+random.nextInt(8),rise,-70+random.nextInt(10),p.rot,true);ship=true;}
            else house(p,-3,rise+1,-11,depth+1);
            add(p,"bridge_end",4,rise,0,(p.rot+2)&3,true);
        }
        void house(Piece parent,int ox,int oy,int oz,int depth) {
            if(depth>8)return;
            Piece p=add(parent,"base_floor",ox,oy,oz,parent.rot,true);
            int kind=random.nextInt(3);
            if(kind==0)add(p,"base_roof",-1,4,-1,p.rot,true);
            else if(kind==1){p=add(p,"second_floor_2",-1,0,-1,p.rot,false);p=add(p,"second_roof",-1,8,-1,p.rot,false);tower(p,depth+1);}
            else {p=add(p,"second_floor_2",-1,0,-1,p.rot,false);p=add(p,"third_floor_c",-1,4,-1,p.rot,false);p=add(p,"third_roof",-1,8,-1,p.rot,true);tower(p,depth+1);}
        }
        void fatTower(Piece parent,int depth) {
            Piece p=add(parent,"fat_tower_base",-3,4,-3,parent.rot,true);
            p=add(p,"fat_tower_middle",0,4,0,p.rot,true);
            for(int i=0;i<2&&random.nextInt(3)!=0;i++){
                p=add(p,"fat_tower_middle",0,8,0,p.rot,true);
                int[][] links={{0,4,-1,0},{1,12,-1,4},{3,0,-1,8},{2,8,-1,12}};
                for(int[] link:links)if(random.nextBoolean())bridge(add(p,"bridge_end",link[1],link[2],link[3],(p.rot+link[0])&3,true),depth+1);
            }
            add(p,"fat_tower_top",-2,8,-2,p.rot,true);
        }
        Piece add(Piece parent,String name,int ox,int oy,int oz,int rot,boolean overwrite){
            int px=ox,pz=oz;if(parent!=null){int[] q=rotatePoint(ox,oz,parent.rot);px=parent.x+q[0];oy=parent.y+oy;pz=parent.z+q[1];}
            Piece piece=new Piece(name,px,oy,pz,rot,overwrite);pieces.add(piece);return piece;
        }
        void place(World world,int cx,int cz){for(Piece p:pieces)p.place(world,cx,cz,random);}
    }

    private static final class Piece {
        final String name; final int x,y,z,rot; final boolean overwrite;
        Piece(String n,int x,int y,int z,int r,boolean o){name=n;this.x=x;this.y=y;this.z=z;rot=r;overwrite=o;}
        void place(World world,int chunkX,int chunkZ,Random random){
            Template t=load(name); int minX=chunkX<<4,minZ=chunkZ<<4,maxX=minX+15,maxZ=minZ+15;
            List<Marker> markers=new ArrayList<Marker>();
            for(BlockEntry e:t.blocks){if(e.state<0||e.state>=t.palette.length)continue;State state=t.palette[e.state];int[] q=rotatePoint(e.x,e.z,rot);int wx=x+q[0],wy=y+e.y,wz=z+q[1];
                boolean inside=wx>=minX&&wx<=maxX&&wz>=minZ&&wz<=maxZ;
                if("minecraft:structure_block".equals(state.name)){boolean adjacent=e.marker!=null&&e.marker.startsWith("Elytra")&&wx>=minX-1&&wx<=maxX+1&&wz>=minZ-1&&wz<=maxZ+1;if((inside||adjacent)&&wy>0&&wy<world.getActualHeight())markers.add(new Marker(e.marker,wx,wy,wz));continue;}
                if(!inside||wy<1||wy>=world.getActualHeight())continue;
                if("minecraft:skull".equals(state.name)&&e.skullType==5){
                    int headMeta=sideMeta(rotateFacing(state.get("facing"),rot));
                    world.setBlock(wx,wy,wz,CombatContent.dragonHead,headMeta,2);
                    TileEntity head=world.getTileEntity(wx,wy,wz);
                    if(head instanceof TileEntityDragonHead)
                        ((TileEntityDragonHead)head).setRotation((e.skullRotation+(rot&3)*4)&15);
                    continue;
                }
                Placed mapped=map(state,rot);if(mapped!=null&&(mapped.block!=Blocks.air||overwrite))world.setBlock(wx,wy,wz,mapped.block,mapped.meta,2);
            }
            // Template markers may precede their chest in the NBT block list.
            for(Marker marker:markers)marker(world,random,marker.name,marker.x,marker.y,marker.z,rot);
        }
    }

    private static void marker(World world,Random random,String marker,int x,int y,int z,int rot){
        if(marker==null)return;
        if(marker.startsWith("Chest")){TileEntity te=world.getTileEntity(x,y-1,z);if(te instanceof IInventory)EndCityLoot120.fill((IInventory)te,random);}
        else if(marker.startsWith("Sentry")){EntityCombatShulker shulker=new EntityCombatShulker(world);shulker.setPosition(x+.5,y,z+.5);world.spawnEntityInWorld(shulker);}
        else if(marker.startsWith("Elytra")){spawnElytraFrame(world,x,y,z,rot);}
    }
    private static void spawnElytraFrame(World world,int x,int y,int z,int rot){List existing=world.getEntitiesWithinAABB(EntityItemFrame.class,AxisAlignedBB.getBoundingBox(x-1,y-1,z-1,x+2,y+2,z+2));for(Object object:existing){ItemStack shown=((EntityItemFrame)object).getDisplayedItem();if(shown!=null&&shown.getItem()==CombatContent.elytra)return;}String facing=rotateFacing("south",rot);int direction=facingInt(facing),sx=x,sz=z;if(direction==0)sz--;else if(direction==1)sx++;else if(direction==2)sz++;else sx--;EntityItemFrame frame=new EntityItemFrame(world,sx,y,sz,direction);if(frame.onValidSurface()){frame.setDisplayedItem(new ItemStack(CombatContent.elytra));world.spawnEntityInWorld(frame);}}

    private static Placed map(State s,int rot){String p=s.name.startsWith("minecraft:")?s.name.substring(10):s.name;
        if("air".equals(p))return new Placed(Blocks.air,0);if("purpur_block".equals(p))return new Placed(CombatContent.purpurBlock,0);
        if("purpur_pillar".equals(p)){int m="x".equals(s.get("axis"))?4:"z".equals(s.get("axis"))?8:0;if((rot&1)!=0&&m!=0)m=m==4?8:4;return new Placed(CombatContent.purpurPillar,m);}
        if("purpur_stairs".equals(p)){int m=stairMeta(rotateFacing(s.get("facing"),rot));if("top".equals(s.get("half")))m|=4;return new Placed(CombatContent.purpurStairs,m);}
        if("purpur_slab".equals(p)){return new Placed("double".equals(s.get("type"))?CombatContent.doublePurpurSlab:CombatContent.purpurSlab,"top".equals(s.get("half"))?8:0);}
        if("end_bricks".equals(p)||"end_stone_bricks".equals(p))return new Placed(CombatContent.endBricks,0);if("end_rod".equals(p))return new Placed(CombatContent.endRod,sideMeta(rotateFacing(s.get("facing"),rot)));
        if("chest".equals(p))return new Placed(Blocks.chest,chestMeta(rotateFacing(s.get("facing"),rot)));if("ladder".equals(p))return new Placed(Blocks.ladder,chestMeta(rotateFacing(s.get("facing"),rot)));
        if("brewing_stand".equals(p))return new Placed(Blocks.brewing_stand,0);if("obsidian".equals(p))return new Placed(Blocks.obsidian,0);if("magenta_stained_glass".equals(p)||"stained_glass".equals(p))return new Placed(Blocks.stained_glass,2);return null;}
    private static int[] rotatePoint(int x,int z,int r){switch(r&3){case 1:return new int[]{-z,x};case 2:return new int[]{-x,-z};case 3:return new int[]{z,-x};default:return new int[]{x,z};}}
    private static String rotateFacing(String f,int r){String[] a={"north","east","south","west"};for(int i=0;i<4;i++)if(a[i].equals(f))return a[(i+r)&3];return f;}
    private static int stairMeta(String f){return "east".equals(f)?0:"west".equals(f)?1:"south".equals(f)?2:3;}
    private static int chestMeta(String f){return "south".equals(f)?3:"west".equals(f)?4:"east".equals(f)?5:2;}
    private static int sideMeta(String f){if("down".equals(f))return 0;if("up".equals(f))return 1;if("north".equals(f))return 2;if("south".equals(f))return 3;if("west".equals(f))return 4;return 5;}
    private static int facingInt(String f){return "south".equals(f)?0:"west".equals(f)?1:"north".equals(f)?2:3;}
    private static int floorDiv(int a,int b){int q=a/b,r=a%b;return r!=0&&((a^b)<0)?q-1:q;}

    private static synchronized Template load(String name){Template t=CACHE.get(name);if(t!=null)return t;String path="/assets/htgt6/structures/endcity/"+name+".nbt";InputStream in=EndCityGenerator.class.getResourceAsStream(path);if(in==null)throw new IllegalStateException("Missing End City template: "+path);try{NBTTagCompound root=CompressedStreamTools.readCompressed(in);NBTTagList pal=root.getTagList("palette",10);State[] states=new State[pal.tagCount()];for(int i=0;i<states.length;i++){NBTTagCompound s=pal.getCompoundTagAt(i);Map<String,String> props=new HashMap<String,String>();if(s.hasKey("Properties",10)){NBTTagCompound pt=s.getCompoundTag("Properties");for(Object k:pt.func_150296_c())props.put(String.valueOf(k),pt.getString(String.valueOf(k)));}states[i]=new State(s.getString("Name"),props);}List<BlockEntry> blocks=new ArrayList<BlockEntry>();NBTTagList list=root.getTagList("blocks",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound b=list.getCompoundTagAt(i);int[] pos=intList(b.getTagList("pos",3));NBTTagCompound data=b.hasKey("nbt",10)?b.getCompoundTag("nbt"):null;String marker=data==null?null:data.getString("metadata");int skullType=data==null?-1:data.getByte("SkullType")&255;int skullRotation=data==null?0:data.getByte("Rot")&15;blocks.add(new BlockEntry(pos[0],pos[1],pos[2],b.getInteger("state"),marker,skullType,skullRotation));}t=new Template(states,blocks);CACHE.put(name,t);return t;}catch(IOException e){throw new IllegalStateException("Cannot read End City template: "+path,e);}finally{try{in.close();}catch(IOException ignored){}}}
    private static int[] intList(NBTTagList list){List<NBTBase> values=rawList(list);int[] result=new int[values.size()];for(int i=0;i<result.length;i++)result[i]=((NBTBase.NBTPrimitive)values.get(i)).func_150287_d();return result;}
    @SuppressWarnings("unchecked") private static List<NBTBase> rawList(NBTTagList list){try{if(tagListField==null)for(Field f:NBTTagList.class.getDeclaredFields())if(List.class.isAssignableFrom(f.getType())){f.setAccessible(true);tagListField=f;break;}return (List<NBTBase>)tagListField.get(list);}catch(Exception e){throw new IllegalStateException("Cannot access NBT list",e);}}
    private static final class Template{final State[] palette;final List<BlockEntry> blocks;Template(State[] p,List<BlockEntry>b){palette=p;blocks=b;}}
    private static final class BlockEntry{final int x,y,z,state,skullType,skullRotation;final String marker;BlockEntry(int x,int y,int z,int s,String m,int type,int rotation){this.x=x;this.y=y;this.z=z;state=s;marker=m;skullType=type;skullRotation=rotation;}}
    private static final class State{final String name;final Map<String,String> properties;State(String n,Map<String,String>p){name=n;properties=p;}String get(String k){return properties.get(k);}}
    private static final class Placed{final Block block;final int meta;Placed(Block b,int m){block=b;meta=m;}}
    private static final class Marker{final String name;final int x,y,z;Marker(String n,int x,int y,int z){name=n;this.x=x;this.y=y;this.z=z;}}
    private EndCityGenerator(){}
}
