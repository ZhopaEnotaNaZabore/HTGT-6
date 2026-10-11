package com.mod.htgt6.orehelper;

import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import cpw.mods.fml.common.network.ByteBufUtils;
import gregapi.data.CS;
import gregapi.data.MT;
import gregapi.worldgen.*;
import net.minecraft.nbt.NBTTagCompound;
import java.lang.reflect.Field;
import java.util.*;
import sun.misc.Unsafe;

/** Real GT6 generator objects and real Minecraft NBT, without launching a world. */
public final class OreHelperFixture {
    private static final class DisplayOre extends net.minecraft.block.Block implements gregapi.block.IBlockPlacable {
        DisplayOre() { super(net.minecraft.block.material.Material.rock); }
        public boolean placeBlock(net.minecraft.world.World w,int x,int y,int z,byte side,short meta,NBTTagCompound n,boolean cause,boolean updates) { return false; }
    }
    private static Unsafe unsafe;
    private static void field(Object target, String name, Object value) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try { Field f = type.getDeclaredField(name); f.setAccessible(true); f.set(target, value); return; }
            catch (NoSuchFieldException e) { type = type.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }
    private static <T extends WorldgenObject> T generator(Class<T> type) throws Exception {
        T g = (T) unsafe.allocateInstance(type);
        field(g,"mName","same_name");field(g,"mCategory","worldgenerator.fixture");
        field(g,"mEnabled",true);
        Map<Integer,Boolean> dimensions = new HashMap<Integer,Boolean>(); dimensions.put(0,true); dimensions.put(-1,false); dimensions.put(1,false);
        field(g,"mDimEnabled",dimensions);
        return g;
    }
    private static WorldgenOresLarge vein(int weight) throws Exception {
        WorldgenOresLarge g=generator(WorldgenOresLarge.class);
        field(g,"mTop",MT.Fe);field(g,"mBottom",MT.Cu);field(g,"mBetween",MT.Sn);field(g,"mSpread",MT.Au);
        field(g,"mMinY",(short)10);field(g,"mMaxY",(short)50);field(g,"mWeight",weight);
        field(g,"mDensity",(short)4);field(g,"mSize",(short)16);
        return g;
    }
    private static void require(boolean condition,String message) { if(!condition)throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        CrusherOreImportFixture.initializeGT6();
        Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(Unsafe)f.get(null);
        CS.GENERATE_STONE=false;GT6WorldGenerator.TFC=false;GT6WorldGenerator.PFAA=false;
        CS.GEN_OVERWORLD.clear();CS.ORE_OVERWORLD.clear();
        WorldgenOresLarge a=vein(7),b=vein(29),disabled=vein(91);
        disabled.mDimEnabled.put(0,false);
        CS.ORE_OVERWORLD.add(a);CS.ORE_OVERWORLD.add(b);CS.ORE_OVERWORLD.add(disabled);
        List<NBTTagCompound> rows=OreCatalog.query(MT.Cu.mID);
        require(rows.size()==2,"same names must not merge distinct generators; disabled dimension must be excluded");
        Set<Integer> weights=new HashSet<Integer>();
        for(NBTTagCompound n:rows) {
            weights.add(n.getInteger("weight"));
            require(n.getInteger("min")==9 && n.getInteger("max")==49,"actual vein vertical envelope");
            require(n.getIntArray("ores")[1]==MT.Cu.mID,"all four roles searchable and ordered");
            require(n.getTagList("worlds",10).getCompoundTagAt(0).getInteger("id")==0,"actual dimension ID");
            io.netty.buffer.ByteBuf bytes=io.netty.buffer.Unpooled.buffer();
            ByteBufUtils.writeTag(bytes,n);require(n.equals(ByteBufUtils.readTag(bytes)),"network NBT round trip");bytes.release();
        }
        require(weights.contains(7)&&weights.contains(29),"weights preserved, not normalized into fake percentages");
        WorldgenOresSmall small=generator(WorldgenOresSmall.class);
        field(small,"mMaterial",MT.Sn);field(small,"mMinY",(short)5);field(small,"mMaxY",(short)20);field(small,"mAmount",(short)9);
        CS.GEN_OVERWORLD.add(small);
        NBTTagCompound tiny=null;for(NBTTagCompound n:OreCatalog.query(MT.Sn.mID))if(n.getInteger("type")==2)tiny=n;
        require(tiny!=null&&tiny.getInteger("max")==19,"small ore upper height is exclusive");
        a.mEnabled=false;b.mInvalid=true;
        require(OreCatalog.query(MT.Cu.mID).isEmpty(),"disabled/invalid generators excluded");
        CS.GENERATE_STONE=true;CS.GEN_GT.clear();StoneLayer.LAYERS.clear();StoneLayer.MAP.clear();
        gregtech.worldgen.WorldgenStoneLayers layers=generator(gregtech.worldgen.WorldgenStoneLayers.class);
        layers.mEnabled=false;layers.mDimEnabled.put(0,false);CS.GEN_GT.add(layers);
        StoneLayerOres ore=new StoneLayerOres(MT.Sn,12345,21,55,Arrays.asList("Desert"));
        StoneLayer host=new StoneLayer(net.minecraft.init.Blocks.stone,MT.Stone,ore);
        StoneLayer.LAYERS.add(host);StoneLayer.LAYERS.add(host);
        StoneLayer.topbottom(MT.Stone,MT.Basalt,ore);
        rows=OreCatalog.query(MT.Sn.mID);
        require(rows.size()==2,"layer enabled override, repeated geology identity and ordered boundary preserved");
        boolean boundary=false;
        for(NBTTagCompound n:rows) {
            require(n.getInteger("type")==1&&n.getInteger("min")==21&&n.getInteger("max")==55,"inclusive layer heights");
            require(n.getLong("chance")==12345,"exact chance numerator retained");
            require(n.getTagList("biomes",8).getStringTagAt(0).equals("Desert"),"biome restriction retained");
            boundary|=n.getBoolean("boundary");
        }
        require(boundary,"contact deposit exposed separately");
        DisplayOre normal=new DisplayOre(),smallBlock=new DisplayOre(),basalt=new DisplayOre();
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(normal,"ore_display_normal");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(smallBlock,"ore_display_small");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(basalt,"ore_display_basalt");
        gregapi.code.ItemStackContainer stoneKey=new gregapi.code.ItemStackContainer(net.minecraft.init.Blocks.stone,1,0);
        gregapi.code.ItemStackContainer basaltKey=new gregapi.code.ItemStackContainer(net.minecraft.init.Blocks.cobblestone,1,0);
        CS.BlocksGT.stoneToNormalOres.put(stoneKey,normal);CS.BlocksGT.stoneToNormalOres.put(basaltKey,basalt);
        CS.BlocksGT.stoneToSmallOres.put(stoneKey,smallBlock);
        net.minecraft.item.ItemStack[] hosts={new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.cobblestone)};
        net.minecraft.item.ItemStack icon=OreDisplayStacks.resolve(0,MT.Fe.mID,hosts);
        require(icon!=null && icon.getItem()==net.minecraft.item.Item.getItemFromBlock(normal) && icon.getItemDamage()==MT.Fe.mID,"normal ore display without prefix unification");
        require(OreDisplayStacks.resolve(1,MT.Fe.mID,hosts).getItem()==net.minecraft.item.Item.getItemFromBlock(basalt),"layer-specific host display");
        require(OreDisplayStacks.resolve(2,MT.Fe.mID,hosts).getItem()==net.minecraft.item.Item.getItemFromBlock(smallBlock),"small ore uses small block");
        require(OreDisplayStacks.resolve(0,0,hosts)==null,"empty vein role remains empty");
        gregapi.block.prefixblock.PrefixBlock bedrock=(gregapi.block.prefixblock.PrefixBlock)unsafe.allocateInstance(gregapi.block.prefixblock.PrefixBlock.class);
        field(bedrock,"delegate",new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<net.minecraft.block.Block>(bedrock,net.minecraft.block.Block.class));
        field(bedrock,"mNameInternal","ore_display_bedrock");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(bedrock,"ore_display_bedrock");
        CS.BlocksGT.oreBedrock=bedrock;
        icon=OreDisplayStacks.resolve(3,MT.Fe.mID,hosts);
        require(icon!=null && icon.getItem()==net.minecraft.item.Item.getItemFromBlock(bedrock) && icon.getItemDamage()==MT.Fe.mID,"bedrock keeps its block and material metadata, never normal ore");
        net.minecraft.item.Item indicator=new net.minecraft.item.Item();
        cpw.mods.fml.common.registry.GameRegistry.registerItem(indicator,"indicator_rock_fixture");
        gregapi.oredict.OreDictManager.INSTANCE.setTarget(gregapi.data.OP.rockGt,MT.Fe,new net.minecraft.item.ItemStack(indicator));
        CS.GENERATE_STONE=false;CS.GEN_OVERWORLD.clear();CS.ORE_OVERWORLD.clear();
        WorldgenOresBedrock deposit=generator(WorldgenOresBedrock.class);
        field(deposit,"mMaterial",MT.Fe);field(deposit,"mProbability",20);
        field(deposit,"mIndicatorRocks",true);field(deposit,"mIndicatorFlowers",true);
        field(deposit,"mFlower",net.minecraft.init.Blocks.red_flower);field(deposit,"mFlowerMeta",(byte)4);
        CS.GEN_OVERWORLD.add(deposit);
        NBTTagCompound depositTag=OreCatalog.query(MT.Fe.mID).get(0);
        net.minecraft.item.ItemStack flower=net.minecraft.item.ItemStack.loadItemStackFromNBT(depositTag.getTagList("indicators",10).getCompoundTagAt(0));
        require(flower!=null && flower.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.red_flower) && flower.getItemDamage()==4,"indicator flower block and variant preserved");
        require(depositTag.getTagList("rockIndicators",10).tagCount()==1,"indicator rock provided before vein display");
        com.mod.htgt6.orehelper.client.OreHelperScreen screen=new com.mod.htgt6.orehelper.client.OreHelperScreen(null,MT.Fe.mID);
        Field token=screen.getClass().getDeclaredField("token");token.setAccessible(true);depositTag.setInteger("token",token.getInt(screen));screen.receive(depositTag);
        field(screen,"tab",3);
        java.lang.reflect.Method filtered=screen.getClass().getDeclaredMethod("filtered");filtered.setAccessible(true);
        Object first=filtered.invoke(screen);require(first==filtered.invoke(screen),"unchanged frames reuse filtered catalog");
        field(screen,"dimension",0);Object changed=filtered.invoke(screen);
        require(changed!=first && ((List<?>)changed).size()==1,"dimension filter invalidates cache");
        Field displayCache=screen.getClass().getDeclaredField("displayEntries");displayCache.setAccessible(true);
        require(((Map<?,?>)displayCache.get(screen)).isEmpty(),"filtering does not decode item stacks for invisible pages");
        field(deposit,"mIndicatorFlowers",false);field(deposit,"mIndicatorRocks",false);
        depositTag=OreCatalog.query(MT.Fe.mID).get(0);
        require(depositTag.getTagList("indicators",10).tagCount()==0 && depositTag.getTagList("rockIndicators",10).tagCount()==0,"disabled indicators are absent; server data not stale");
        System.out.println("OreHelperFixture: identity, server filters, composition, heights, layers, biomes, NBT, ore blocks, indicators and view cache passed");
    }
}
