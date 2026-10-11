package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** Headless deterministic template-pool connector expansion used by structure adapters. */
public final class JigsawPlacementRuntime120 {
    public interface Registry {
        Pool pool(String id);
        Template template(String id);
        default boolean tolerateMissingTemplates(){return false;}
    }

    public interface Terrain {
        int surfaceY(int x,int z);
    }

    public static Result generate(Registry registry,Terrain terrain,String startPool,long seed,
                                  int startX,int startY,int startZ,int maxDepth,int maxDistance) {
        return generate(registry,terrain,startPool,seed,startX,startY,startZ,maxDepth,maxDistance,null,false);
    }

    public static Result generate(Registry registry,Terrain terrain,String startPool,long seed,
                                  int startX,int startY,int startZ,int maxDepth,int maxDistance,
                                  String startJigsawName) {
        return generate(registry,terrain,startPool,seed,startX,startY,startZ,maxDepth,maxDistance,
                startJigsawName,false);
    }

    /**
     * @param projectStartToTerrain mirrors JigsawStructure.project_start_to_heightmap.
     * The sampled point is the centre of the rotated root box, as in 1.20.2.
     */
    public static Result generate(Registry registry,Terrain terrain,String startPool,long seed,
                                  int startX,int startY,int startZ,int maxDepth,int maxDistance,
                                  String startJigsawName,boolean projectStartToTerrain) {
        return generateVersioned(registry,terrain,startPool,seed,startX,startY,startZ,maxDepth,maxDistance,
                startJigsawName,projectStartToTerrain,false);
    }

    /** Reconstructs pre-fix persisted starts without changing their unplaced chunk slices. */
    public static Result generateLegacy(Registry registry,Terrain terrain,String startPool,long seed,
                                  int startX,int startY,int startZ,int maxDepth,int maxDistance,
                                  String startJigsawName,boolean projectStartToTerrain) {
        return generateVersioned(registry,terrain,startPool,seed,startX,startY,startZ,maxDepth,maxDistance,
                startJigsawName,projectStartToTerrain,true);
    }

    private static Result generateVersioned(Registry registry,Terrain terrain,String startPool,long seed,
                                  int startX,int startY,int startZ,int maxDepth,int maxDistance,
                                  String startJigsawName,boolean projectStartToTerrain,boolean legacy) {
        if(registry==null||terrain==null)throw new NullPointerException();
        if(maxDepth<0||maxDepth>20)throw new IllegalArgumentException("maxDepth");
        Pool rootPool=requiredPool(registry,startPool);
        boolean useExpansionHack=startPool.contains("village/")||!legacy&&startPool.contains("pillager_outpost/");
        Random random=new Random(seed);
        Element rootElement=rootPool.random(random);
        if(rootElement==null||rootElement.empty)throw new IllegalStateException("Empty start pool "+startPool);
        Template rootTemplate=requiredTemplate(registry,rootElement,legacy);
        if(rootTemplate==null)throw new MissingDataException("Missing start template "+rootElement.template);
        int rootRotation=random.nextInt(4),rootX=startX,rootY=startY,rootZ=startZ;
        if(startJigsawName!=null){
            List<Connector> anchors=new ArrayList<Connector>();
            for(Connector connector:rootTemplate.rotatedConnectors(rootRotation,0,0,0))
                if(startJigsawName.equals(connector.name))anchors.add(connector);
            if(anchors.isEmpty())throw new MissingDataException("Missing start jigsaw "+startJigsawName
                    +" in "+rootTemplate.id);
            Connector anchor=anchors.get(random.nextInt(anchors.size()));
            rootX-=anchor.x;rootY-=anchor.y;rootZ-=anchor.z;
        }
        Piece root=new Piece(rootTemplate,rootElement,rootRotation,rootX,rootY,rootZ,0);
        if(projectStartToTerrain){
            Box rootBox=root.box();
            int centerX=(rootBox.minX+rootBox.maxX)/2;
            int centerZ=(rootBox.minZ+rootBox.maxZ)/2;
            int projectedY=root.y+(legacy?0:startY)+terrain.surfaceY(centerX,centerZ)
                    -(rootBox.minY+rootTemplate.groundLevelDelta);
            root=root.atY(projectedY);
        }else if(rootElement.projection==Projection.TERRAIN_MATCHING)
            root=root.atY(terrain.surfaceY(startX,startZ)-rootTemplate.groundLevelDelta);
        // Once the start piece is projected to a heightmap, vanilla's expansion
        // bounds are centred on that projected piece rather than the configured
        // relative start-height value (for Trail Ruins this value is -15).
        int boundsCenterY=root.y+(legacy?0:rootTemplate.groundLevelDelta+startY-rootY);
        int boundsCenterX=legacy?startX:(root.box().minX+root.box().maxX)/2;
        int boundsCenterZ=legacy?startZ:(root.box().minZ+root.box().maxZ)/2;

        List<Piece> accepted=new ArrayList<Piece>();
        accepted.add(root);
        ArrayDeque<Piece> queue=new ArrayDeque<Piece>();
        if(legacy||maxDepth>0)queue.add(root);
        Map<Piece,Space> spaces=new java.util.IdentityHashMap<Piece,Space>();
        Map<Piece,Space> interiors=new java.util.IdentityHashMap<Piece,Space>();
        Space outer=new Space(new Box(boundsCenterX-maxDistance,boundsCenterY-maxDistance,boundsCenterZ-maxDistance,
                boundsCenterX+maxDistance,boundsCenterY+maxDistance,boundsCenterZ+maxDistance));
        outer.occupied.add(root);spaces.put(root,outer);
        int rejected=0;
        Map<String,Integer> attemptedByPool=new LinkedHashMap<String,Integer>();
        Map<String,Integer> acceptedByPool=new LinkedHashMap<String,Integer>();
        Map<String,Integer> rejectedByPool=new LinkedHashMap<String,Integer>();
        Map<String,String> firstRejectionByPool=new LinkedHashMap<String,String>();
        while(!queue.isEmpty()){
            Piece parent=queue.removeFirst();
            List<Connector> connectors=parent.worldConnectors();
            Collections.shuffle(connectors,random);
            for(Connector parentConnector:connectors){
                if("minecraft:empty".equals(parentConnector.pool)||"empty".equals(parentConnector.pool))continue;
                increment(attemptedByPool,parentConnector.pool);
                Pool pool=requiredPool(registry,parentConnector.pool);
                // JigsawPlacement.Placer from 1.20.2 omits the primary pool at
                // maxDepth but still runs the fallback pool. This final pass is
                // what caps village streets with terminators instead of leaving
                // visibly severed paths.
                List<Element> candidates=new ArrayList<Element>();
                if(parent.depth!=maxDepth)candidates.addAll(pool.shuffled(random));
                Pool fallback=registry.pool(pool.fallback);
                if(fallback!=null)candidates.addAll(fallback.shuffled(random));
                boolean attached=false;
                for(Element element:candidates){
                    // EmptyPoolElement is a deliberate stop marker in the
                    // shuffled weighted list, not a candidate to skip over.
                    if(element.empty)break;
                    Template template=requiredTemplate(registry,element,legacy);
                    if(template==null)continue;
                    List<Integer> rotations=new ArrayList<Integer>();
                    for(int rotation=0;rotation<4;rotation++)rotations.add(rotation);
                    Collections.shuffle(rotations,random);
                    for(int rotation:rotations){
                        List<Connector> childConnectors=template.rotatedConnectors(rotation,0,0,0);
                        Collections.shuffle(childConnectors,random);
                        for(Connector childConnector:childConnectors){
                            if(!attaches(parentConnector,childConnector))continue;
                            int targetX=parentConnector.x+parentConnector.front.dx;
                            int targetY=parentConnector.y+parentConnector.front.dy;
                            int targetZ=parentConnector.z+parentConnector.front.dz;
                            Piece child=new Piece(template,element,rotation,targetX-childConnector.x,
                                    targetY-childConnector.y,targetZ-childConnector.z,parent.depth+1);
                            if(parent.element.projection==Projection.TERRAIN_MATCHING
                                    ||element.projection==Projection.TERRAIN_MATCHING){
                                // 1.20.2 JigsawPlacement uses the heightmap whenever either
                                // side of the junction is terrain matching. This is essential
                                // for rigid village houses attached to terrain-matching streets.
                                int projected=terrain.surfaceY(parentConnector.x,parentConnector.z)
                                        -childConnector.y;
                                child=child.atY(projected);
                            }
                            if(useExpansionHack&&child.box().height()<=16){
                                int expansion=maxConnectorPoolHeight(registry,template,legacy);
                                if(expansion>0)child=child.expandedUpTo(expansion+(legacy?1:2));
                            }
                            Box childBox=child.box();
                            boolean outside=!insideRadius(childBox,boundsCenterX,boundsCenterY,boundsCenterZ,maxDistance);
                            boolean internal=parent.box().contains(targetX,targetY,targetZ);
                            Space space=spaces.get(parent);
                            if(internal){
                                space=interiors.get(parent);
                                if(space==null){space=new Space(parent.box());interiors.put(parent,space);}
                            }
                            if(legacy){if(internal&&!parent.box().contains(childBox))outside=true;}
                            else outside=!space.bounds.contains(childBox);
                            Piece collision=outside?null:firstCollision(childBox,legacy?accepted:space.occupied,legacy&&internal?parent:null);
                            if(outside||collision!=null){
                                rejected++;
                                increment(rejectedByPool,parentConnector.pool);
                                if(!firstRejectionByPool.containsKey(parentConnector.pool))
                                    firstRejectionByPool.put(parentConnector.pool,"parent="+parent.template.id
                                            +" parentBox="+parent.box()+" connector="+parentConnector
                                            +" child="+template.id+" childBox="+childBox
                                            +(outside?" outside bounds":" collision="+collision.template.id
                                                    +" collisionBox="+collision.box()));
                                continue;
                            }
                            accepted.add(child);
                            if(!legacy){space.occupied.add(child);spaces.put(child,space);}
                            increment(acceptedByPool,parentConnector.pool);
                            if(child.depth<=maxDepth)queue.addLast(child);
                            attached=true;
                            break;
                        }
                        if(attached)break;
                    }
                    if(attached)break;
                }
            }
        }
        return new Result(accepted,rejected,attemptedByPool,acceptedByPool,rejectedByPool,firstRejectionByPool);
    }

    private static void increment(Map<String,Integer> counts,String key){
        Integer old=counts.get(key);counts.put(key,Integer.valueOf(old==null?1:old.intValue()+1));
    }

    /** Maximum template height reachable from this piece's connector pools. */
    private static int maxConnectorPoolHeight(Registry registry,Template template,boolean legacy){
        int maximum=0;
        for(Connector connector:template.connectors){
            if(!legacy){int x=connector.x+connector.front.dx,y=connector.y+connector.front.dy,z=connector.z+connector.front.dz;
                if(x<0||x>=template.sizeX||y<0||y>=template.sizeY||z<0||z>=template.sizeZ)continue;}
            Pool pool=registry.pool(connector.pool);if(pool==null)continue;
            maximum=Math.max(maximum,maxPoolHeight(registry,pool));
            Pool fallback=registry.pool(pool.fallback);
            if(fallback!=null)maximum=Math.max(maximum,maxPoolHeight(registry,fallback));
        }
        return maximum;
    }

    private static int maxPoolHeight(Registry registry,Pool pool){
        int maximum=0;
        for(Weighted entry:pool.entries)maximum=Math.max(maximum,elementHeight(registry,entry.element));
        return maximum;
    }

    private static int elementHeight(Registry registry,Element element){
        if(element==null||element.empty||element.kind==ElementKind.FEATURE)return 0;
        if(element.kind==ElementKind.LIST){int maximum=0;for(Element child:element.children)
            maximum=Math.max(maximum,elementHeight(registry,child));return maximum;}
        Template template=registry.template(element.template);return template==null?0:template.sizeY;
    }

    private static boolean attaches(Connector parent,Connector child){
        return parent.front.opposite()==child.front
                && parent.target.equals(child.name)
                && (parent.joint==Joint.ROLLABLE||parent.top==child.top);
    }

    private static Piece firstCollision(Box box,List<Piece> pieces,Piece ignored){
        // Vanilla tests the candidate voxel shape deflated by 0.25 blocks.
        for(Piece piece:pieces)if(piece!=ignored&&box.intersectsDeflated(piece.box(),0.25D))return piece;
        return null;
    }

    private static boolean insideRadius(Box box,int x,int y,int z,int radius){
        return box.minX>=x-radius&&box.maxX<=x+radius&&box.minY>=y-radius&&box.maxY<=y+radius
                &&box.minZ>=z-radius&&box.maxZ<=z+radius;
    }

    private static Pool requiredPool(Registry registry,String id){
        Pool pool=registry.pool(id);
        if(pool==null)throw new MissingDataException("Missing template pool "+id);
        return pool;
    }

    private static Template requiredTemplate(Registry registry,String id){
        Template template=registry.template(id);
        if(template==null)throw new MissingDataException("Missing structure template "+id);
        return template;
    }

    private static Template requiredTemplate(Registry registry,Element element,boolean legacy){
        if(element.kind==ElementKind.FEATURE)return new Template("feature:"+element.feature,1,1,1,0,
                Collections.<Connector>emptyList());
        if(element.kind==ElementKind.LIST){
            int sizeX=1,sizeY=1,sizeZ=1,ground=0;List<Connector> connectors=new ArrayList<Connector>();
            boolean first=true;
            for(Element child:element.children){
                if(child.empty){if(!legacy)first=false;continue;}
                Template template=requiredTemplate(registry,child,legacy);
                if(template==null)continue;
                sizeX=Math.max(sizeX,template.sizeX);sizeY=Math.max(sizeY,template.sizeY);sizeZ=Math.max(sizeZ,template.sizeZ);
                if(legacy||first)connectors.addAll(template.connectors);if(first){ground=template.groundLevelDelta;first=false;}
            }
            return new Template("list",sizeX,sizeY,sizeZ,ground,connectors);
        }
        Template template=registry.template(element.template);
        if(template==null&&!registry.tolerateMissingTemplates())
            throw new MissingDataException("Missing structure template "+element.template);
        return template;
    }

    public enum Projection { RIGID,TERRAIN_MATCHING }
    private static final class Space {
        final Box bounds;final List<Piece> occupied=new ArrayList<Piece>();
        Space(Box bounds){this.bounds=bounds;}
    }
    public enum Joint { ALIGNED,ROLLABLE }
    public enum Direction {
        DOWN(0,-1,0),UP(0,1,0),NORTH(0,0,-1),SOUTH(0,0,1),WEST(-1,0,0),EAST(1,0,0);
        public final int dx,dy,dz;
        Direction(int x,int y,int z){dx=x;dy=y;dz=z;}
        public Direction opposite(){switch(this){case DOWN:return UP;case UP:return DOWN;case NORTH:return SOUTH;
            case SOUTH:return NORTH;case WEST:return EAST;default:return WEST;}}
        public Direction rotate(int turns){
            if(this==UP||this==DOWN)return this;
            Direction result=this;
            for(int i=0;i<(turns&3);i++)switch(result){case NORTH:result=EAST;break;case EAST:result=SOUTH;break;
                case SOUTH:result=WEST;break;default:result=NORTH;break;}
            return result;
        }
    }

    public static final class Connector {
        public final int x,y,z;
        public final Direction front,top;
        public final Joint joint;
        public final String name,target,pool;
        public Connector(int x,int y,int z,Direction front,Direction top,Joint joint,
                         String name,String target,String pool){
            this.x=x;this.y=y;this.z=z;this.front=front;this.top=top;this.joint=joint;
            this.name=name;this.target=target;this.pool=pool;
        }
        Connector rotate(int turns,int sizeX,int sizeZ,int originX,int originY,int originZ){
            int[] point=rotatePoint(x,z,sizeX,sizeZ,turns);
            return new Connector(originX+point[0],originY+y,originZ+point[1],front.rotate(turns),
                    top.rotate(turns),joint,name,target,pool);
        }
        @Override public String toString(){return "["+x+","+y+","+z+" "+front
                +" name="+name+" target="+target+" pool="+pool+"]";}
    }

    public static final class Template {
        public final String id;
        public final int sizeX,sizeY,sizeZ,groundLevelDelta;
        public final List<Connector> connectors;
        public Template(String id,int sizeX,int sizeY,int sizeZ,int groundLevelDelta,List<Connector> connectors){
            if(sizeX<1||sizeY<1||sizeZ<1)throw new IllegalArgumentException("template size");
            this.id=id;this.sizeX=sizeX;this.sizeY=sizeY;this.sizeZ=sizeZ;
            this.groundLevelDelta=groundLevelDelta;
            this.connectors=Collections.unmodifiableList(new ArrayList<Connector>(connectors));
        }
        List<Connector> rotatedConnectors(int rotation,int x,int y,int z){
            List<Connector> result=new ArrayList<Connector>();
            for(Connector connector:connectors)result.add(connector.rotate(rotation,sizeX,sizeZ,x,y,z));
            return result;
        }
    }

    public enum ElementKind { SINGLE,LIST,EMPTY,FEATURE }
    public static final class Element {
        public final String template;
        public final Projection projection;
        public final List<String> processors;
        public final ElementKind kind;
        public final List<Element> children;
        public final String feature;
        /** LegacySinglePoolElement installs STRUCTURE_AND_AIR instead of STRUCTURE_BLOCK. */
        public final boolean ignoreTemplateAir;
        public final boolean empty;
        public Element(String template,Projection projection,List<String> processors){
            this(template,projection,processors,false);
        }
        public Element(String template,Projection projection,List<String> processors,boolean ignoreTemplateAir){
            this.template=template;this.projection=projection;
            this.processors=Collections.unmodifiableList(new ArrayList<String>(processors));
            this.ignoreTemplateAir=ignoreTemplateAir;empty=false;kind=ElementKind.SINGLE;
            children=Collections.emptyList();feature=null;
        }
        private Element(){template="minecraft:empty";projection=Projection.RIGID;
            processors=Collections.emptyList();ignoreTemplateAir=true;empty=true;kind=ElementKind.EMPTY;
            children=Collections.emptyList();feature=null;}
        private Element(ElementKind kind,Projection projection,List<Element> children,String feature){
            this.kind=kind;this.projection=projection;this.children=Collections.unmodifiableList(new ArrayList<Element>(children));
            this.feature=feature;template=kind==ElementKind.LIST?"minecraft:list":"feature:"+feature;
            processors=Collections.emptyList();ignoreTemplateAir=true;empty=false;
        }
        public static Element empty(){return new Element();}
        public static Element list(Projection projection,List<Element> children){
            if(children==null||children.isEmpty())throw new IllegalArgumentException("empty list element");
            return new Element(ElementKind.LIST,projection,children,null);
        }
        public static Element feature(String feature,Projection projection){
            if(feature==null)throw new NullPointerException("feature");
            return new Element(ElementKind.FEATURE,projection,Collections.<Element>emptyList(),feature);
        }
    }

    public static final class Weighted {
        public final Element element;
        public final int weight;
        public Weighted(Element element,int weight){if(weight<1)throw new IllegalArgumentException("weight");
            this.element=element;this.weight=weight;}
    }

    public static final class Pool {
        public final String id,fallback;
        public final List<Weighted> entries;
        public Pool(String id,String fallback,List<Weighted> entries){this.id=id;this.fallback=fallback;
            this.entries=Collections.unmodifiableList(new ArrayList<Weighted>(entries));}
        Element random(Random random){int total=0;for(Weighted entry:entries)total+=entry.weight;
            if(total==0)return null;int selected=random.nextInt(total);for(Weighted entry:entries){
                selected-=entry.weight;if(selected<0)return entry.element;}throw new AssertionError();}
        List<Element> shuffled(Random random){List<Element> result=new ArrayList<Element>();
            for(Weighted entry:entries)for(int i=0;i<entry.weight;i++)result.add(entry.element);
            Collections.shuffle(result,random);return result;}
    }

    public static final class Piece {
        public final Template template;
        public final Element element;
        public final int rotation,x,y,z,depth;
        private final Box bounds;
        Piece(Template template,Element element,int rotation,int x,int y,int z,int depth){
            this(template,element,rotation,x,y,z,depth,null);}
        private Piece(Template template,Element element,int rotation,int x,int y,int z,int depth,Box bounds){
            this.template=template;this.element=element;this.rotation=rotation&3;
            this.x=x;this.y=y;this.z=z;this.depth=depth;this.bounds=bounds;}
        Piece atY(int newY){int dy=newY-y;Box moved=bounds==null?null:bounds.moved(0,dy,0);
            return new Piece(template,element,rotation,x,newY,z,depth,moved);}
        Piece expandedUpTo(int height){Box box=box();if(height<=box.height())return this;
            return new Piece(template,element,rotation,x,y,z,depth,
                    new Box(box.minX,box.minY,box.minZ,box.maxX,box.minY+height-1,box.maxZ));}
        public Box box(){int sx=(rotation&1)==0?template.sizeX:template.sizeZ;
            int sz=(rotation&1)==0?template.sizeZ:template.sizeX;return bounds!=null?bounds:
                    new Box(x,y,z,x+sx-1,y+template.sizeY-1,z+sz-1);}
        public List<Connector> worldConnectors(){return template.rotatedConnectors(rotation,x,y,z);}
    }

    public static final class Box {
        public final int minX,minY,minZ,maxX,maxY,maxZ;
        Box(int minX,int minY,int minZ,int maxX,int maxY,int maxZ){this.minX=minX;this.minY=minY;this.minZ=minZ;
            this.maxX=maxX;this.maxY=maxY;this.maxZ=maxZ;}
        boolean intersects(Box other){return maxX>=other.minX&&minX<=other.maxX&&maxY>=other.minY
                &&minY<=other.maxY&&maxZ>=other.minZ&&minZ<=other.maxZ;}
        boolean intersectsDeflated(Box other,double amount){
            return minX+amount<other.maxX+1D&&maxX+1D-amount>other.minX
                    &&minY+amount<other.maxY+1D&&maxY+1D-amount>other.minY
                    &&minZ+amount<other.maxZ+1D&&maxZ+1D-amount>other.minZ;
        }
        int height(){return maxY-minY+1;}
        boolean contains(int x,int y,int z){return x>=minX&&x<=maxX&&y>=minY&&y<=maxY&&z>=minZ&&z<=maxZ;}
        boolean contains(Box other){return other.minX>=minX&&other.maxX<=maxX&&other.minY>=minY
                &&other.maxY<=maxY&&other.minZ>=minZ&&other.maxZ<=maxZ;}
        Box moved(int dx,int dy,int dz){return new Box(minX+dx,minY+dy,minZ+dz,maxX+dx,maxY+dy,maxZ+dz);}
        @Override public String toString(){return "["+minX+","+minY+","+minZ+" -> "+maxX+","+maxY+","+maxZ+"]";}
    }

    public static final class Result {
        public final List<Piece> pieces;
        public final int rejectedByBoundsOrCollision;
        public final Map<String,Integer> attemptedByPool,acceptedByPool,rejectedByPool;
        public final Map<String,String> firstRejectionByPool;
        Result(List<Piece> pieces,int rejected,Map<String,Integer> attempted,Map<String,Integer> accepted,
               Map<String,Integer> rejectedPools,Map<String,String> firstRejections){this.pieces=Collections.unmodifiableList(new ArrayList<Piece>(pieces));
            rejectedByBoundsOrCollision=rejected;
            attemptedByPool=Collections.unmodifiableMap(new LinkedHashMap<String,Integer>(attempted));
            acceptedByPool=Collections.unmodifiableMap(new LinkedHashMap<String,Integer>(accepted));
            rejectedByPool=Collections.unmodifiableMap(new LinkedHashMap<String,Integer>(rejectedPools));
            firstRejectionByPool=Collections.unmodifiableMap(new LinkedHashMap<String,String>(firstRejections));}
    }

    public static final class MissingDataException extends IllegalStateException {
        public MissingDataException(String message){super(message);}
    }

    /** Save the accepted plan, rather than relying on a future version's RNG/algorithm. */
    public static net.minecraft.nbt.NBTTagCompound save(Result result){
        net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();
        tag.setInteger("Version",2);net.minecraft.nbt.NBTTagList pieces=new net.minecraft.nbt.NBTTagList();
        for(Piece p:result.pieces){net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();
            n.setString("Template",p.template.id);n.setIntArray("Size",new int[]{p.template.sizeX,p.template.sizeY,p.template.sizeZ,p.template.groundLevelDelta});
            n.setIntArray("Pose",new int[]{p.x,p.y,p.z,p.rotation,p.depth});Box b=p.box();
            n.setIntArray("Box",new int[]{b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ});
            n.setTag("Element",saveElement(p.element));pieces.appendTag(n);
        }
        tag.setTag("Pieces",pieces);return tag;
    }
    public static Result load(net.minecraft.nbt.NBTTagCompound tag){
        if(tag.getInteger("Version")!=2)throw new IllegalArgumentException("Unsupported jigsaw plan version");
        List<Piece> pieces=new ArrayList<Piece>();net.minecraft.nbt.NBTTagList list=tag.getTagList("Pieces",10);
        for(int i=0;i<list.tagCount();i++){net.minecraft.nbt.NBTTagCompound n=list.getCompoundTagAt(i);
            int[] s=n.getIntArray("Size"),p=n.getIntArray("Pose"),b=n.getIntArray("Box");
            if(s.length!=4||p.length!=5||b.length!=6)throw new IllegalArgumentException("Malformed jigsaw piece");
            Template t=new Template(n.getString("Template"),s[0],s[1],s[2],s[3],Collections.<Connector>emptyList());
            pieces.add(new Piece(t,loadElement(n.getCompoundTag("Element")),p[3],p[0],p[1],p[2],p[4],new Box(b[0],b[1],b[2],b[3],b[4],b[5])));
        }
        if(pieces.isEmpty())throw new IllegalArgumentException("Empty saved jigsaw plan");
        return new Result(pieces,0,Collections.<String,Integer>emptyMap(),Collections.<String,Integer>emptyMap(),Collections.<String,Integer>emptyMap(),Collections.<String,String>emptyMap());
    }
    private static net.minecraft.nbt.NBTTagCompound saveElement(Element e){
        net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();n.setString("Kind",e.kind.name());n.setString("Projection",e.projection.name());
        n.setString("Template",e.template);n.setBoolean("IgnoreAir",e.ignoreTemplateAir);
        if(e.feature!=null)n.setString("Feature",e.feature);
        net.minecraft.nbt.NBTTagList children=new net.minecraft.nbt.NBTTagList();for(Element c:e.children)children.appendTag(saveElement(c));n.setTag("Children",children);
        net.minecraft.nbt.NBTTagList processors=new net.minecraft.nbt.NBTTagList();for(String p:e.processors)processors.appendTag(new net.minecraft.nbt.NBTTagString(p));n.setTag("Processors",processors);return n;
    }
    private static Element loadElement(net.minecraft.nbt.NBTTagCompound n){
        ElementKind kind=ElementKind.valueOf(n.getString("Kind"));Projection projection=Projection.valueOf(n.getString("Projection"));
        if(kind==ElementKind.EMPTY)return Element.empty();if(kind==ElementKind.FEATURE)return Element.feature(n.getString("Feature"),projection);
        if(kind==ElementKind.LIST){List<Element> children=new ArrayList<Element>();net.minecraft.nbt.NBTTagList list=n.getTagList("Children",10);
            for(int i=0;i<list.tagCount();i++)children.add(loadElement(list.getCompoundTagAt(i)));return Element.list(projection,children);}
        List<String> processors=new ArrayList<String>();net.minecraft.nbt.NBTTagList list=n.getTagList("Processors",8);
        for(int i=0;i<list.tagCount();i++)processors.add(list.getStringTagAt(i));return new Element(n.getString("Template"),projection,processors,n.getBoolean("IgnoreAir"));
    }

    private static int[] rotatePoint(int x,int z,int sizeX,int sizeZ,int turns){
        switch(turns&3){case 1:return new int[]{sizeZ-1-z,x};case 2:return new int[]{sizeX-1-x,sizeZ-1-z};
            case 3:return new int[]{z,sizeX-1-x};default:return new int[]{x,z};}
    }

    private JigsawPlacementRuntime120(){ }
}
