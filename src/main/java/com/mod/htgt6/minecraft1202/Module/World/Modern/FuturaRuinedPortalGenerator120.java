package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/** Deterministic Futura placement of the thirteen official Minecraft 1.20.1 ruined-portal templates. */
public final class FuturaRuinedPortalGenerator120 implements IWorldGenerator {
    private static final String[] NORMAL={"portal_1","portal_2","portal_3","portal_4","portal_5","portal_6","portal_7","portal_8","portal_9","portal_10"};
    private static final String[] GIANT={"giant_portal_1","giant_portal_2","giant_portal_3"};
    private static final Map<String,Template> CACHE=new HashMap<String,Template>();private static Field listField;
    public static void validateTemplates(){Set<String> missing=new TreeSet<String>();for(String n:NORMAL)validate(load(n),missing);for(String n:GIANT)validate(load(n),missing);if(!missing.isEmpty())throw new IllegalStateException("Unsupported ruined portal palette: "+missing);}
    private static void validate(Template t,Set<String> missing){for(PaletteState state:t.palette){String p=plainName(state.name);if(map(state,0)==null&&!ignored(p))missing.add(p);}}
    @Override public void generate(Random ignored,int cx,int cz,World world,IChunkProvider generator,IChunkProvider provider){
        if(world.provider.dimensionId!=0||!FuturaWorldType120.isFutura(world)||!world.getWorldInfo().isMapFeaturesEnabled()||!candidate(world.getSeed(),cx,cz))return;
        Random random=new Random(world.getSeed()^(cx*341873128712L)^(cz*132897987541L)^34222645L);
        String[] pool=random.nextInt(20)==0?GIANT:NORMAL;Template t=load(pool[random.nextInt(pool.length)]);int rot=random.nextInt(4);
        int w=(rot&1)==0?t.x:t.z,d=(rot&1)==0?t.z:t.x,bx=(cx<<4)+8-w/2,bz=(cz<<4)+8-d/2;
        int y=Math.max(5,Math.min(world.getActualHeight()-t.y-2,minimumSurface(world,bx,bz,w,d)-1));place(world,t,bx,y,bz,rot);
    }
    public static boolean candidate(long seed,int cx,int cz){int rx=Math.floorDiv(cx,40),rz=Math.floorDiv(cz,40);Random r=new Random(seed+rx*341873128712L+rz*132897987541L+34222645L);return cx==rx*40+r.nextInt(25)&&cz==rz*40+r.nextInt(25);}
    private static int minimumSurface(World w,int x,int z,int sx,int sz){int y=w.getActualHeight()-1;for(int dx=0;dx<sx;dx++)for(int dz=0;dz<sz;dz++)y=Math.min(y,w.getHeightValue(x+dx,z+dz));return y;}
    private static void place(World world,Template t,int bx,int by,int bz,int rot){for(Entry e:t.blocks){if(e.state<0||e.state>=t.palette.length)continue;Placed p=map(t.palette[e.state],rot);if(p==null)continue;int[] q=rotate(e.x,e.z,t.x,t.z,rot);int y=by+e.y;if(y>1&&y<world.getActualHeight()-1)world.setBlock(bx+q[0],y,bz+q[1],p.block,p.meta,2);}}
    private static boolean ignored(String p){return "structure_void".equals(p)||"jigsaw".equals(p)||"structure_block".equals(p);}
    private static String plainName(String name){return name.startsWith("minecraft:")?name.substring(10):name;}
    private static Placed map(PaletteState state,int rotation){
        String p=plainName(state.name);
        if(ignored(p))return null;
        if("air".equals(p)||"cave_air".equals(p))return new Placed(Blocks.air,0);
        if("obsidian".equals(p))return new Placed(Blocks.obsidian,0);
        if("crying_obsidian".equals(p))return new Placed(EtFVanillaGapContent120.cryingObsidian,0);
        if("netherrack".equals(p))return new Placed(Blocks.netherrack,0);
        if("magma_block".equals(p))return new Placed(FrostburnContent.magmaBlock,0);
        if("gold_block".equals(p))return new Placed(Blocks.gold_block,0);
        if("lava".equals(p))return new Placed(Blocks.lava,level(state));
        if("chest".equals(p))return new Placed(Blocks.chest,chestMeta(rotateFacing(state.property("facing","north"),rotation)));
        if("iron_bars".equals(p))return new Placed(Blocks.iron_bars,0);
        if("stone".equals(p))return new Placed(Blocks.stone,0);
        if("stone_bricks".equals(p))return new Placed(Blocks.stonebrick,0);
        if("mossy_stone_bricks".equals(p))return new Placed(Blocks.stonebrick,1);
        if("cracked_stone_bricks".equals(p))return new Placed(Blocks.stonebrick,2);
        if("chiseled_stone_bricks".equals(p))return new Placed(Blocks.stonebrick,3);
        if("stone_brick_stairs".equals(p))return new Placed(Blocks.stone_brick_stairs,
                stairMeta(rotateFacing(state.property("facing","north"),rotation),"top".equals(state.property("half","bottom"))));
        if("smooth_stone_slab".equals(p)||"stone_slab".equals(p))return new Placed(Blocks.stone_slab,
                slabMeta(0,state.property("type",state.property("half","bottom"))));
        if("stone_brick_slab".equals(p))return new Placed(Blocks.stone_slab,
                slabMeta(5,state.property("type",state.property("half","bottom"))));
        if("stone_brick_wall".equals(p))return new Placed(Blocks.cobblestone_wall,0);
        if("vine".equals(p))return new Placed(Blocks.vine,vineMeta(state,rotation));
        return null;
    }
    private static int level(PaletteState state){try{return Math.max(0,Math.min(7,Integer.parseInt(state.property("level","0"))));}catch(NumberFormatException ignored){return 0;}}
    private static int slabMeta(int material,String half){return material|("top".equals(half)?8:0);}
    private static int stairMeta(String facing,boolean top){int meta="east".equals(facing)?0:"west".equals(facing)?1:"south".equals(facing)?2:3;return meta|(top?4:0);}
    private static int chestMeta(String facing){return "south".equals(facing)?3:"west".equals(facing)?4:"east".equals(facing)?5:2;}
    private static int vineMeta(PaletteState state,int rotation){int meta=0;String[] faces={"south","west","north","east"};int[] bits={1,2,4,8};for(int i=0;i<faces.length;i++)if("true".equals(state.property(faces[i],"false"))){String rotated=rotateFacing(faces[i],rotation);for(int j=0;j<faces.length;j++)if(faces[j].equals(rotated))meta|=bits[j];}return meta;}
    private static String rotateFacing(String facing,int rotation){String[] order={"north","east","south","west"};int index=-1;for(int i=0;i<order.length;i++)if(order[i].equals(facing)){index=i;break;}return index<0?facing:order[(index+(rotation&3))&3];}
    private static int[] rotate(int x,int z,int sx,int sz,int r){switch(r&3){case 1:return new int[]{sz-1-z,x};case 2:return new int[]{sx-1-x,sz-1-z};case 3:return new int[]{z,sx-1-x};default:return new int[]{x,z};}}
    private static synchronized Template load(String name){Template old=CACHE.get(name);if(old!=null)return old;String path="/assets/htgt6/structures/ruined_portal/"+name+".nbt";InputStream in=FuturaRuinedPortalGenerator120.class.getResourceAsStream(path);if(in==null)throw new IllegalStateException("Missing ruined portal template "+path);try{NBTTagCompound root=CompressedStreamTools.readCompressed(in);int[] size=ints(root.getTagList("size",3));NBTTagList pt=root.getTagList("palette",10);PaletteState[] palette=new PaletteState[pt.tagCount()];for(int i=0;i<palette.length;i++){NBTTagCompound rawState=pt.getCompoundTagAt(i);Map<String,String> properties=new HashMap<String,String>();if(rawState.hasKey("Properties",10)){NBTTagCompound rawProperties=rawState.getCompoundTag("Properties");for(Object key:rawProperties.func_150296_c())properties.put(String.valueOf(key),rawProperties.getString(String.valueOf(key)));}palette[i]=new PaletteState(rawState.getString("Name"),properties);}NBTTagList bt=root.getTagList("blocks",10);List<Entry> blocks=new ArrayList<Entry>();for(int i=0;i<bt.tagCount();i++){NBTTagCompound b=bt.getCompoundTagAt(i);int[] pos=ints(b.getTagList("pos",3));blocks.add(new Entry(pos[0],pos[1],pos[2],b.getInteger("state")));}Template t=new Template(size[0],size[1],size[2],palette,blocks);CACHE.put(name,t);return t;}catch(IOException e){throw new IllegalStateException("Cannot read "+path,e);}finally{try{in.close();}catch(IOException ignored){}}}
    private static int[] ints(NBTTagList tag){List<NBTBase> l=raw(tag);int[] out=new int[l.size()];for(int i=0;i<out.length;i++)out[i]=((NBTBase.NBTPrimitive)l.get(i)).func_150287_d();return out;}
    @SuppressWarnings("unchecked") private static List<NBTBase> raw(NBTTagList tag){try{if(listField==null)for(Field f:NBTTagList.class.getDeclaredFields())if(List.class.isAssignableFrom(f.getType())){f.setAccessible(true);listField=f;break;}return (List<NBTBase>)listField.get(tag);}catch(Exception e){throw new IllegalStateException(e);}}
    public static int mappedMetadataForFixture(String name,String facing,String half,int rotation){Map<String,String> properties=new HashMap<String,String>();properties.put("facing",facing);properties.put("half",half);properties.put("type",half);Placed placed=map(new PaletteState(name,properties),rotation);return placed==null?-1:placed.meta;}
    private static final class PaletteState{final String name;final Map<String,String> properties;PaletteState(String name,Map<String,String> properties){this.name=name;this.properties=properties;}String property(String key,String fallback){String value=properties.get(key);return value==null?fallback:value;}}
    private static final class Template{final int x,y,z;final PaletteState[] palette;final List<Entry> blocks;Template(int x,int y,int z,PaletteState[] p,List<Entry>b){this.x=x;this.y=y;this.z=z;palette=p;blocks=b;}}
    private static final class Entry{final int x,y,z,state;Entry(int x,int y,int z,int s){this.x=x;this.y=y;this.z=z;state=s;}}
    private static final class Placed{final Block block;final int meta;Placed(Block b,int m){block=b;meta=m;}}
}
