package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Direct Java-8 port of the 1.20.2 OverworldBiomeBuilder parameter list. */
public final class OverworldBiomeParameterMap120 {
    public enum Biome {
        PLAINS,SUNFLOWER_PLAINS,SNOWY_PLAINS,ICE_SPIKES,DESERT,SWAMP,MANGROVE_SWAMP,
        FOREST,FLOWER_FOREST,BIRCH_FOREST,DARK_FOREST,OLD_GROWTH_BIRCH_FOREST,
        OLD_GROWTH_PINE_TAIGA,OLD_GROWTH_SPRUCE_TAIGA,TAIGA,SNOWY_TAIGA,SAVANNA,
        SAVANNA_PLATEAU,WINDSWEPT_HILLS,WINDSWEPT_GRAVELLY_HILLS,WINDSWEPT_FOREST,
        WINDSWEPT_SAVANNA,JUNGLE,SPARSE_JUNGLE,BAMBOO_JUNGLE,BADLANDS,
        ERODED_BADLANDS,WOODED_BADLANDS,MEADOW,CHERRY_GROVE,GROVE,SNOWY_SLOPES,
        FROZEN_PEAKS,JAGGED_PEAKS,STONY_PEAKS,RIVER,FROZEN_RIVER,BEACH,SNOWY_BEACH,
        STONY_SHORE,WARM_OCEAN,LUKEWARM_OCEAN,DEEP_LUKEWARM_OCEAN,OCEAN,DEEP_OCEAN,
        COLD_OCEAN,DEEP_COLD_OCEAN,FROZEN_OCEAN,DEEP_FROZEN_OCEAN,MUSHROOM_FIELDS,
        DRIPSTONE_CAVES,LUSH_CAVES,DEEP_DARK
    }

    public static final class Range {
        public final long min,max;
        private Range(float min,float max){this.min=quantize(min);this.max=quantize(max);}
        static Range point(float value){return new Range(value,value);}
        static Range span(float min,float max){return new Range(min,max);}
        static Range span(Range first,Range second){
            return new Range(unquantize(Math.min(first.min,second.min)),unquantize(Math.max(first.max,second.max)));
        }
        long distance(long value){return value<min?min-value:value>max?value-max:0L;}
    }

    public static final class Entry {
        public final Range temperature,humidity,continentalness,erosion,depth,weirdness;
        public final float offset;
        public final Biome biome;
        Entry(Range temperature,Range humidity,Range continentalness,Range erosion,
              Range depth,Range weirdness,float offset,Biome biome){
            this.temperature=temperature;this.humidity=humidity;this.continentalness=continentalness;
            this.erosion=erosion;this.depth=depth;this.weirdness=weirdness;this.offset=offset;this.biome=biome;
        }
        long fitness(FuturaClimateSampler120.Point point){
            long[] values={quantize(point.temperature),quantize(point.humidity),quantize(point.continentalness),
                    quantize(point.erosion),quantize(point.depth),quantize(point.weirdness)};
            return fitness(values);
        }
        long fitness(long[] values){
            long result=square(quantize(offset));
            result+=square(temperature.distance(values[0]));
            result+=square(humidity.distance(values[1]));
            result+=square(continentalness.distance(values[2]));
            result+=square(erosion.distance(values[3]));
            result+=square(depth.distance(values[4]));
            result+=square(weirdness.distance(values[5]));
            return result;
        }
    }

    private final Range all=Range.span(-1,1);
    private final Range[] temperatures={r(-1,-.45F),r(-.45F,-.15F),r(-.15F,.2F),r(.2F,.55F),r(.55F,1)};
    private final Range[] humidities={r(-1,-.35F),r(-.35F,-.1F),r(-.1F,.1F),r(.1F,.3F),r(.3F,1)};
    private final Range[] erosions={r(-1,-.78F),r(-.78F,-.375F),r(-.375F,-.2225F),r(-.2225F,.05F),r(.05F,.45F),r(.45F,.55F),r(.55F,1)};
    private final Range mushroom=r(-1.2F,-1.05F),deepOcean=r(-1.05F,-.455F),ocean=r(-.455F,-.19F);
    private final Range coast=r(-.19F,-.11F),inland=r(-.11F,.55F),near=r(-.11F,.03F),mid=r(.03F,.3F),far=r(.3F,1);
    private final Biome[][] oceans={
            {Biome.DEEP_FROZEN_OCEAN,Biome.DEEP_COLD_OCEAN,Biome.DEEP_OCEAN,Biome.DEEP_LUKEWARM_OCEAN,Biome.WARM_OCEAN},
            {Biome.FROZEN_OCEAN,Biome.COLD_OCEAN,Biome.OCEAN,Biome.LUKEWARM_OCEAN,Biome.WARM_OCEAN}};
    private final Biome[][] middle={
            {Biome.SNOWY_PLAINS,Biome.SNOWY_PLAINS,Biome.SNOWY_PLAINS,Biome.SNOWY_TAIGA,Biome.TAIGA},
            {Biome.PLAINS,Biome.PLAINS,Biome.FOREST,Biome.TAIGA,Biome.OLD_GROWTH_SPRUCE_TAIGA},
            {Biome.FLOWER_FOREST,Biome.PLAINS,Biome.FOREST,Biome.BIRCH_FOREST,Biome.DARK_FOREST},
            {Biome.SAVANNA,Biome.SAVANNA,Biome.FOREST,Biome.JUNGLE,Biome.JUNGLE},
            {Biome.DESERT,Biome.DESERT,Biome.DESERT,Biome.DESERT,Biome.DESERT}};
    private final Biome[][] middleVariant={
            {Biome.ICE_SPIKES,null,Biome.SNOWY_TAIGA,null,null},{null,null,null,null,Biome.OLD_GROWTH_PINE_TAIGA},
            {Biome.SUNFLOWER_PLAINS,null,null,Biome.OLD_GROWTH_BIRCH_FOREST,null},
            {null,null,Biome.PLAINS,Biome.SPARSE_JUNGLE,Biome.BAMBOO_JUNGLE},{null,null,null,null,null}};
    private final Biome[][] plateau={
            {Biome.SNOWY_PLAINS,Biome.SNOWY_PLAINS,Biome.SNOWY_PLAINS,Biome.SNOWY_TAIGA,Biome.SNOWY_TAIGA},
            {Biome.GROVE,Biome.GROVE,Biome.FOREST,Biome.TAIGA,Biome.OLD_GROWTH_SPRUCE_TAIGA},
            {Biome.GROVE,Biome.GROVE,Biome.GROVE,Biome.GROVE,Biome.DARK_FOREST},
            {Biome.SNOWY_SLOPES,Biome.SNOWY_SLOPES,Biome.FOREST,Biome.FOREST,Biome.JUNGLE},
            {Biome.BADLANDS,Biome.BADLANDS,Biome.BADLANDS,Biome.WOODED_BADLANDS,Biome.WOODED_BADLANDS}};
    private final Biome[][] plateauVariant={
            {Biome.ICE_SPIKES,null,null,null,null},{Biome.CHERRY_GROVE,null,Biome.GROVE,Biome.GROVE,Biome.OLD_GROWTH_PINE_TAIGA},
            {Biome.CHERRY_GROVE,Biome.CHERRY_GROVE,Biome.FOREST,Biome.BIRCH_FOREST,null},
            {null,null,null,null,null},{Biome.ERODED_BADLANDS,Biome.ERODED_BADLANDS,null,null,null}};
    private final Biome[][] extreme={
            {Biome.WINDSWEPT_GRAVELLY_HILLS,Biome.WINDSWEPT_GRAVELLY_HILLS,Biome.WINDSWEPT_HILLS,Biome.WINDSWEPT_FOREST,Biome.WINDSWEPT_FOREST},
            {Biome.WINDSWEPT_GRAVELLY_HILLS,Biome.WINDSWEPT_GRAVELLY_HILLS,Biome.WINDSWEPT_HILLS,Biome.WINDSWEPT_FOREST,Biome.WINDSWEPT_FOREST},
            {Biome.WINDSWEPT_HILLS,Biome.WINDSWEPT_HILLS,Biome.WINDSWEPT_HILLS,Biome.WINDSWEPT_FOREST,Biome.WINDSWEPT_FOREST},
            {null,null,null,null,null},{null,null,null,null,null}};
    private final List<Entry> entries=new ArrayList<Entry>();
    private final SearchIndex searchIndex;
    private final AtomicLong lookupCount=new AtomicLong(),visitedNodeCount=new AtomicLong(),testedEntryCount=new AtomicLong();
    private final ThreadLocal<SearchState> searchState=new ThreadLocal<SearchState>(){
        @Override protected SearchState initialValue(){return new SearchState();}
    };

    public OverworldBiomeParameterMap120(){addOffCoast();addInland();addCaves();searchIndex=new SearchIndex(entries);}
    public List<Entry> entries(){return Collections.unmodifiableList(entries);}
    public Biome select(FuturaClimateSampler120.Point point){
        SearchState state=searchState.get();state.reset(point);
        Entry best=searchIndex.nearest(state.values,state.best,state.metrics);
        lookupCount.incrementAndGet();visitedNodeCount.addAndGet(state.metrics.nodes);
        testedEntryCount.addAndGet(state.metrics.entries);
        return best.biome;
    }
    public Biome selectLinear(FuturaClimateSampler120.Point point){
        long[] values=values(point);
        Entry best=entries.get(0);long distance=best.fitness(values);
        for(int i=1;i<entries.size();i++){Entry next=entries.get(i);long candidate=next.fitness(values);
            if(candidate<distance){best=next;distance=candidate;}}
        return best.biome;
    }

    public LookupStatistics lookupStatistics(){return new LookupStatistics(lookupCount.get(),
            visitedNodeCount.get(),testedEntryCount.get(),entries.size());}
    public void resetLookupStatistics(){lookupCount.set(0);visitedNodeCount.set(0);testedEntryCount.set(0);}

    public static final class LookupStatistics {
        public final long lookups,visitedNodes,testedEntries;
        public final int totalEntries;
        private LookupStatistics(long lookups,long visitedNodes,long testedEntries,int totalEntries){
            this.lookups=lookups;this.visitedNodes=visitedNodes;this.testedEntries=testedEntries;this.totalEntries=totalEntries;
        }
        public double averageTestedEntries(){return lookups==0?0.0D:testedEntries/(double)lookups;}
    }

    private static long[] values(FuturaClimateSampler120.Point point){return new long[]{
            quantize(point.temperature),quantize(point.humidity),quantize(point.continentalness),
            quantize(point.erosion),quantize(point.depth),quantize(point.weirdness),0L};}

    /** Immutable bounding-volume tree over the seven vanilla climate dimensions. */
    private static final class SearchIndex {
        private static final int DIMENSIONS=7,LEAF_SIZE=16;
        private final Node root;
        SearchIndex(List<Entry> source){
            EntryRef[] refs=new EntryRef[source.size()];
            for(int i=0;i<refs.length;i++)refs[i]=new EntryRef(source.get(i),i);
            root=build(refs,0,refs.length);
        }
        Entry nearest(long[] values,Best best,SearchMetrics metrics){
            search(root,values,best,metrics);return best.ref.entry;
        }
        private static Node build(final EntryRef[] refs,int from,int to){
            long[] minimum=new long[DIMENSIONS],maximum=new long[DIMENSIONS];
            Arrays.fill(minimum,Long.MAX_VALUE);Arrays.fill(maximum,Long.MIN_VALUE);
            for(int i=from;i<to;i++)for(int d=0;d<DIMENSIONS;d++){
                minimum[d]=Math.min(minimum[d],refs[i].minimum(d));
                maximum[d]=Math.max(maximum[d],refs[i].maximum(d));
            }
            if(to-from<=LEAF_SIZE)return new Node(minimum,maximum,
                    Arrays.copyOfRange(refs,from,to),null,null);
            int splitDimension=0;long widest=Long.MIN_VALUE;
            for(int d=0;d<DIMENSIONS;d++){
                long width=maximum[d]-minimum[d];if(width>widest){widest=width;splitDimension=d;}
            }
            final int dimension=splitDimension;
            Arrays.sort(refs,from,to,new Comparator<EntryRef>(){
                @Override public int compare(EntryRef a,EntryRef b){
                    long first=a.center(dimension),second=b.center(dimension);
                    return first<second?-1:first>second?1:a.index-b.index;
                }});
            int middle=(from+to)>>>1;
            return new Node(minimum,maximum,null,build(refs,from,middle),build(refs,middle,to));
        }
        private static void search(Node node,long[] values,Best best,SearchMetrics metrics){
            if(node.lowerBound(values)>best.distance)return;
            metrics.nodes++;
            if(node.entries!=null){for(EntryRef ref:node.entries){metrics.entries++;
                long distance=ref.entry.fitness(values);
                if(distance<best.distance||(distance==best.distance&&(best.ref==null||ref.index<best.ref.index))){
                    best.ref=ref;best.distance=distance;}}
                return;
            }
            long left=node.left.lowerBound(values),right=node.right.lowerBound(values);
            if(left<=right){search(node.left,values,best,metrics);search(node.right,values,best,metrics);}
            else{search(node.right,values,best,metrics);search(node.left,values,best,metrics);}
        }
    }

    private static final class Node {
        final long[] minimum,maximum;final EntryRef[] entries;final Node left,right;
        Node(long[] minimum,long[] maximum,EntryRef[] entries,Node left,Node right){
            this.minimum=minimum;this.maximum=maximum;this.entries=entries;this.left=left;this.right=right;}
        long lowerBound(long[] values){long result=0L;for(int d=0;d<values.length;d++){
            long distance=values[d]<minimum[d]?minimum[d]-values[d]:values[d]>maximum[d]?values[d]-maximum[d]:0L;
            result+=square(distance);}return result;}
    }
    private static final class EntryRef {
        final Entry entry;final int index;EntryRef(Entry entry,int index){this.entry=entry;this.index=index;}
        long minimum(int dimension){Range range=range(dimension);return range==null?quantize(entry.offset):range.min;}
        long maximum(int dimension){Range range=range(dimension);return range==null?quantize(entry.offset):range.max;}
        long center(int dimension){return minimum(dimension)+(maximum(dimension)-minimum(dimension))/2L;}
        Range range(int dimension){switch(dimension){case 0:return entry.temperature;case 1:return entry.humidity;
            case 2:return entry.continentalness;case 3:return entry.erosion;case 4:return entry.depth;
            case 5:return entry.weirdness;default:return null;}}
    }
    private static final class Best {EntryRef ref;long distance=Long.MAX_VALUE;}
    private static final class SearchMetrics {long nodes,entries;}
    private static final class SearchState {
        final long[] values=new long[7];final Best best=new Best();final SearchMetrics metrics=new SearchMetrics();
        void reset(FuturaClimateSampler120.Point point){
            values[0]=quantize(point.temperature);values[1]=quantize(point.humidity);
            values[2]=quantize(point.continentalness);values[3]=quantize(point.erosion);
            values[4]=quantize(point.depth);values[5]=quantize(point.weirdness);values[6]=0L;
            best.ref=null;best.distance=Long.MAX_VALUE;metrics.nodes=0L;metrics.entries=0L;
        }
    }

    private void addOffCoast(){
        addSurface(all,all,mushroom,all,all,0,Biome.MUSHROOM_FIELDS);
        for(int t=0;t<5;t++){addSurface(temperatures[t],all,deepOcean,all,all,0,oceans[0][t]);
            addSurface(temperatures[t],all,ocean,all,all,0,oceans[1][t]);}
    }
    private void addInland(){
        addPeaks(r(-1,-.93333334F));addHigh(r(-.93333334F,-.7666667F));
        addMid(r(-.7666667F,-.56666666F));addHigh(r(-.56666666F,-.4F));
        addPeaks(r(-.4F,-.26666668F));addLow(r(-.26666668F,-.05F));addValleys(r(-.05F,.05F));
        addLow(r(.05F,.26666668F));addPeaks(r(.26666668F,.4F));addHigh(r(.4F,.56666666F));
        addMid(r(.56666666F,.7666667F));addHigh(r(.7666667F,.93333334F));addPeaks(r(.93333334F,1));
    }

    private void addMid(Range w){for(int t=0;t<5;t++)for(int h=0;h<5;h++){
        Biome m=middle(t,h,w),p=plateau(t,h,w),s=shore(t,h,w),pv=plateauVariant(t,h,w),e=extreme(t,h,w);
        addSurface(temperatures[t],humidities[h],span(coast,far),erosions[0],w,0,highSlope(t,h,w));
        addSurface(temperatures[t],humidities[h],span(coast,near),erosions[1],w,0,s);
        addSurface(temperatures[t],humidities[h],span(mid,far),erosions[1],w,0,e);
        addSurface(temperatures[t],humidities[h],span(coast,near),span(erosions[2],erosions[3]),w,0,m);
        addSurface(temperatures[t],humidities[h],span(mid,far),erosions[2],w,0,p);
        addSurface(temperatures[t],humidities[h],mid,erosions[3],w,0,shore(t,h,w));
        addSurface(temperatures[t],humidities[h],far,erosions[3],w,0,p);
        addSurface(temperatures[t],humidities[h],span(coast,far),erosions[4],w,0,m);
        addSurface(temperatures[t],humidities[h],span(coast,near),erosions[5],w,0,mountainOverride(t,h,w,e));
        addSurface(temperatures[t],humidities[h],span(mid,far),erosions[5],w,0,e);
        addSurface(temperatures[t],humidities[h],span(coast,far),erosions[6],w,0,m);
    }}

    private void addHigh(Range w){for(int t=0;t<5;t++)for(int h=0;h<5;h++){
        Biome m=middle(t,h,w),p=plateau(t,h,w),s=shore(t,h,w),pv=plateauVariant(t,h,w),hi=highSlope(t,h,w),e=extreme(t,h,w);
        addSurface(temperatures[t],humidities[h],coast,span(erosions[0],erosions[1]),w,0,m);
        addSurface(temperatures[t],humidities[h],near,erosions[0],w,0,mountainOverride(t,h,w,m));
        addSurface(temperatures[t],humidities[h],span(mid,far),erosions[0],w,0,hi);
        addSurface(temperatures[t],humidities[h],near,erosions[1],w,0,s);
        addSurface(temperatures[t],humidities[h],span(mid,far),erosions[1],w,0,mountainOverride(t,h,w,m));
        addSurface(temperatures[t],humidities[h],span(coast,near),span(erosions[2],erosions[3]),w,0,m);
        addSurface(temperatures[t],humidities[h],span(mid,far),erosions[2],w,0,p);
        addSurface(temperatures[t],humidities[h],mid,erosions[3],w,0,s);
        addSurface(temperatures[t],humidities[h],far,erosions[3],w,0,p);
        addSurface(temperatures[t],humidities[h],span(coast,far),erosions[4],w,0,m);
        addSurface(temperatures[t],humidities[h],span(coast,near),erosions[5],w,0,mountainOverride(t,h,w,m));
        addSurface(temperatures[t],humidities[h],span(mid,far),erosions[5],w,0,pv);
        addSurface(temperatures[t],humidities[h],span(coast,far),erosions[6],w,0,m);
    }}

    private void addPeaks(Range w){
        addSurface(all,all,coast,span(erosions[0],erosions[2]),w,0,Biome.STONY_SHORE);
        addSurface(span(temperatures[1],temperatures[2]),all,span(near,far),erosions[6],w,0,Biome.SWAMP);
        addSurface(span(temperatures[3],temperatures[4]),all,span(near,far),erosions[6],w,0,Biome.MANGROVE_SWAMP);
        for(int t=0;t<5;t++)for(int h=0;h<5;h++){
        Biome m=middle(t,h,w),p=plateau(t,h,w),s=shore(t,h,w),pv=plateauVariant(t,h,w),e=extreme(t,h,w);
            addSurface(temperatures[t],humidities[h],span(near,far),erosions[0],w,0,slope(t,h,w));
            addSurface(temperatures[t],humidities[h],span(near,mid),erosions[1],w,0,s);
            addSurface(temperatures[t],humidities[h],far,erosions[1],w,0,t==0?slope(t,h,w):pv);
            addSurface(temperatures[t],humidities[h],near,erosions[2],w,0,m);
            addSurface(temperatures[t],humidities[h],mid,erosions[2],w,0,s);
            addSurface(temperatures[t],humidities[h],far,erosions[2],w,0,pv);
            addSurface(temperatures[t],humidities[h],span(near,mid),erosions[3],w,0,m);
            addSurface(temperatures[t],humidities[h],far,erosions[3],w,0,s);
            if(w.min<0){addSurface(temperatures[t],humidities[h],near,erosions[4],w,0,beach(t,h));
                addSurface(temperatures[t],humidities[h],span(mid,far),erosions[4],w,0,m);}
            else addSurface(temperatures[t],humidities[h],span(near,far),erosions[4],w,0,m);
            addSurface(temperatures[t],humidities[h],near,erosions[5],w,0,eroded(t,h,w));
            addSurface(temperatures[t],humidities[h],mid,erosions[5],w,0,mountainOverride(t,h,w,m));
            addSurface(temperatures[t],humidities[h],far,erosions[5],w,0,e);
            if(w.min<0)addSurface(temperatures[t],humidities[h],near,erosions[6],w,0,beach(t,h));
            else addSurface(temperatures[t],humidities[h],near,erosions[6],w,0,m);
            if(t==0)addSurface(temperatures[t],humidities[h],span(mid,far),erosions[6],w,0,m);
        }
    }

    private void addLow(Range w){
        addSurface(all,all,coast,span(erosions[0],erosions[2]),w,0,Biome.STONY_SHORE);
        addSurface(span(temperatures[1],temperatures[2]),all,span(near,far),erosions[6],w,0,Biome.SWAMP);
        addSurface(span(temperatures[3],temperatures[4]),all,span(near,far),erosions[6],w,0,Biome.MANGROVE_SWAMP);
        for(int t=0;t<5;t++)for(int h=0;h<5;h++){
            Biome m=middle(t,h,w),p=plateau(t,h,w),s=shore(t,h,w),pv=plateauVariant(t,h,w);
            addSurface(temperatures[t],humidities[h],near,span(erosions[0],erosions[1]),w,0,s);
            addSurface(temperatures[t],humidities[h],span(mid,far),span(erosions[0],erosions[1]),w,0,pv);
            addSurface(temperatures[t],humidities[h],near,span(erosions[2],erosions[3]),w,0,m);
            addSurface(temperatures[t],humidities[h],span(mid,far),span(erosions[2],erosions[3]),w,0,s);
            addSurface(temperatures[t],humidities[h],coast,span(erosions[3],erosions[4]),w,0,beach(t,h));
            addSurface(temperatures[t],humidities[h],span(near,far),erosions[4],w,0,m);
            addSurface(temperatures[t],humidities[h],near,erosions[5],w,0,eroded(t,h,w));
            addSurface(temperatures[t],humidities[h],mid,erosions[5],w,0,mountainOverride(t,h,w,m));
            addSurface(temperatures[t],humidities[h],far,erosions[5],w,0,m);
            addSurface(temperatures[t],humidities[h],near,erosions[6],w,0,beach(t,h));
            if(t==0)addSurface(temperatures[t],humidities[h],span(mid,far),erosions[6],w,0,m);
        }
    }

    private void addValleys(Range w){
        addSurface(temperatures[0],all,coast,span(erosions[0],erosions[1]),w,0,w.min<0?Biome.STONY_SHORE:Biome.FROZEN_RIVER);
        addSurface(span(temperatures[1],temperatures[4]),all,coast,span(erosions[0],erosions[1]),w,0,w.min<0?Biome.STONY_SHORE:Biome.RIVER);
        addSurface(temperatures[0],all,near,span(erosions[0],erosions[1]),w,0,Biome.FROZEN_RIVER);
        addSurface(span(temperatures[1],temperatures[4]),all,near,span(erosions[0],erosions[1]),w,0,Biome.RIVER);
        addSurface(temperatures[0],all,span(coast,far),span(erosions[2],erosions[5]),w,0,Biome.FROZEN_RIVER);
        addSurface(span(temperatures[1],temperatures[4]),all,span(coast,far),span(erosions[2],erosions[5]),w,0,Biome.RIVER);
        addSurface(temperatures[0],all,coast,erosions[6],w,0,Biome.FROZEN_RIVER);
        addSurface(span(temperatures[1],temperatures[4]),all,coast,erosions[6],w,0,Biome.RIVER);
        addSurface(span(temperatures[1],temperatures[2]),all,span(inland,far),erosions[6],w,0,Biome.SWAMP);
        addSurface(span(temperatures[3],temperatures[4]),all,span(inland,far),erosions[6],w,0,Biome.MANGROVE_SWAMP);
        addSurface(temperatures[0],all,span(inland,far),erosions[6],w,0,Biome.FROZEN_RIVER);
    }

    private void addCaves(){
        addUnderground(all,all,r(.8F,1),all,all,0,Biome.DRIPSTONE_CAVES);
        addUnderground(all,r(.7F,1),all,all,all,0,Biome.LUSH_CAVES);
        addBottom(all,all,all,span(erosions[0],erosions[1]),all,0,Biome.DEEP_DARK);
    }

    private Biome middle(int t,int h,Range w){return w.min<0?middle[t][h]:middleVariant[t][h]==null?middle[t][h]:middleVariant[t][h];}
    private Biome plateau(int t,int h,Range w){return t==4?badlands(h,w):middle(t,h,w);}
    private Biome plateauVariant(int t,int h,Range w){return w.min>=0&&plateauVariant[t][h]!=null?plateauVariant[t][h]:plateau[t][h];}
    private Biome shore(int t,int h,Range w){return t==0?highSlope(t,h,w):plateau(t,h,w);}
    private Biome mountainOverride(int t,int h,Range w,Biome fallback){return t>1&&h<4&&w.min>=0?Biome.WINDSWEPT_SAVANNA:fallback;}
    private Biome eroded(int t,int h,Range w){return mountainOverride(t,h,w,w.min>=0?middle(t,h,w):beach(t,h));}
    private Biome beach(int t,int h){return t==0?Biome.SNOWY_BEACH:t==4?Biome.DESERT:Biome.BEACH;}
    private Biome badlands(int humidity,Range w){return humidity<2?(w.min<0?Biome.BADLANDS:Biome.ERODED_BADLANDS):humidity<3?Biome.BADLANDS:Biome.WOODED_BADLANDS;}
    private Biome highSlope(int t,int h,Range w){if(t<=2)return w.min<0?Biome.JAGGED_PEAKS:Biome.FROZEN_PEAKS;return t==3?Biome.STONY_PEAKS:badlands(h,w);}
    private Biome slope(int t,int h,Range w){if(t>=3)return plateauVariant(t,h,w);return h<=1?Biome.SNOWY_SLOPES:Biome.GROVE;}
    private Biome extreme(int t,int h,Range w){Biome b=extreme[t][h];return b==null?middle(t,h,w):b;}

    private void addSurface(Range t,Range h,Range c,Range e,Range w,float offset,Biome biome){
        add(t,h,c,e,Range.point(0),w,offset,biome);add(t,h,c,e,Range.point(1),w,offset,biome);}
    private void addUnderground(Range t,Range h,Range c,Range e,Range w,float offset,Biome biome){add(t,h,c,e,r(.2F,.9F),w,offset,biome);}
    private void addBottom(Range t,Range h,Range c,Range e,Range w,float offset,Biome biome){add(t,h,c,e,Range.point(1.1F),w,offset,biome);}
    private void add(Range t,Range h,Range c,Range e,Range d,Range w,float offset,Biome biome){entries.add(new Entry(t,h,c,e,d,w,offset,biome));}
    private static Range r(float min,float max){return Range.span(min,max);}private static Range span(Range a,Range b){return Range.span(a,b);}
    private static long quantize(float value){return (long)(value*10000.0F);}private static float unquantize(long value){return value/10000.0F;}
    private static long square(long value){return value*value;}
}
