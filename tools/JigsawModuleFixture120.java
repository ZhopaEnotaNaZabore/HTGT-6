package com.mod.htgt6.minecraft1202.Module.World.Modern;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.world.*;
import com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class JigsawModuleFixture120 {
    static int checks;
    static void check(boolean x,String s){if(!x)throw new AssertionError(s);checks++;}
    public static void main(String[] args)throws Exception{
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
        // Actual block/item registration for loot identities which do not exist in vanilla 1.7.
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.blueIce,"fixture_blue_ice");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.cornflower,"fixture_cornflower");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.lilyOfTheValley,"fixture_lily");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent.barrel,"fixture_barrel");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.smoothStone,"fixture_smooth_stone");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.sweetBerryBush,"fixture_berry_bush");
        List<String> tables=new ArrayList<>();for(File file:new File("src/main/resources/assets/htgt6/loot_tables/chests/village").listFiles())tables.add("minecraft:chests/village/"+file.getName().replace(".json",""));tables.add("minecraft:chests/pillager_outpost");
        for(String table:tables){JsonObject json=VillageStructureLoot120.table(table);
            for(JsonElement pv:json.getAsJsonArray("pools"))for(JsonElement ev:pv.getAsJsonObject().getAsJsonArray("entries")){
                JsonObject e=ev.getAsJsonObject();if(e.has("name"))check(VillageStructureLoot120.item(e.get("name").getAsString()).getItem()!=null,"loot identity "+e);
            }
            for(int seed=0;seed<100;seed++){
                List<ItemStack> a=VillageStructureLoot120.roll(table,new Random(seed)),b=VillageStructureLoot120.roll(table,new Random(seed));check(a.size()==b.size(),"deterministic loot size");
                for(int i=0;i<a.size();i++)check(ItemStack.areItemStacksEqual(a.get(i),b.get(i))&&a.get(i).stackSize>0&&a.get(i).stackSize<=a.get(i).getMaxStackSize(),"loot stack/count/NBT");
            }
        }
        check(VillageStructureLoot120.item("minecraft:dark_oak_log").getItemDamage()==1,"dark oak log metadata");
        check(VillageStructureLoot120.item("minecraft:spruce_sapling").getItemDamage()==1,"spruce sapling metadata");
        check(VillageStructureLoot120.item("minecraft:grass").getItem()==Item.getItemFromBlock(Blocks.tallgrass),"grass loot must not become turf");
        byte[] original;try(InputStream in=net.minecraftforge.event.ForgeEventFactory.class.getResourceAsStream("/net/minecraftforge/event/ForgeEventFactory.class")){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))>=0)bytes.write(buffer,0,n);original=bytes.toByteArray();}
        byte[] transformed=com.mod.htgt6.asm.OutpostSpawnASM120.transform(original);check(Arrays.equals(transformed,com.mod.htgt6.asm.OutpostSpawnASM120.transform(transformed)),"spawn hook idempotence");
        ClassNode node=new ClassNode();new ClassReader(transformed).accept(node,0);int hooks=0;
        for(MethodNode m:node.methods){if(m.name.equals("getPotentialSpawns"))new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name,m);
            for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode&&((MethodInsnNode)n).owner.endsWith("OutpostSpawnRuntime120"))hooks++;}
        check(hooks==1,"spawn hook must be installed exactly once");
        Class<?> u=Class.forName("sun.misc.Unsafe");Field f=u.getDeclaredField("theUnsafe");f.setAccessible(true);Grid world=(Grid)u.getMethod("allocateInstance",Class.class).invoke(f.get(null),Grid.class);
        world.data=new PillagerOutpostData120();world.rules=new GameRules();world.difficultySetting=EnumDifficulty.NORMAL;
        Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(world,new WorldProviderSurface());
        PillagerOutpostData120.Start start=world.data.recordNewStart(0,0,100,1);start.plan=PillagerOutpostGenerator.layout(1,0,0,100);world.data.recordSlice(start,0,0);
        JigsawPlacementRuntime120.Box box=start.plan.pieces.get(0).box();int x=box.minX,y=box.minY,z=box.minZ;
        List source=Collections.singletonList("sentinel");List spawn=OutpostSpawnRuntime120.list(world,net.minecraft.entity.EnumCreatureType.monster,x,y,z,source);
        check(spawn!=source&&spawn.size()==1&&source.get(0).equals("sentinel"),"spawn override must not mutate biome list");
        check(((net.minecraft.world.biome.BiomeGenBase.SpawnListEntry)spawn.get(0)).entityClass==com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager.class,"outpost spawn identity");
        world.difficultySetting=EnumDifficulty.PEACEFUL;check(OutpostSpawnRuntime120.list(world,net.minecraft.entity.EnumCreatureType.monster,x,y,z,source).isEmpty(),"peaceful gate");
        world.difficultySetting=EnumDifficulty.NORMAL;world.rules.setOrCreateGameRule("doMobSpawning","false");check(OutpostSpawnRuntime120.list(world,net.minecraft.entity.EnumCreatureType.monster,x,y,z,source).isEmpty(),"gamerule gate");
        check(OutpostSpawnRuntime120.list(world,net.minecraft.entity.EnumCreatureType.monster,10000,y,z,source)==source,"outside unchanged");
        featurePlans(world);
        System.out.println("PASS Jigsaw module: "+checks+" checks; 17 loot tables, stateful loot, spawn-list isolation/gates, actual Forge ASM verification");
    }
    static void featurePlans(final Grid world)throws Exception{
        Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        world.provider.worldObj=world;world.plans=new JigsawPlansData120();world.writes=new TreeMap<String,String>();world.chunks=new HashMap<String,net.minecraft.world.chunk.Chunk>();
        for(int cx=-2;cx<=2;cx++)for(int cz=-2;cz<=2;cz++)world.chunks.put(cx+","+cz,new FlatChunk(world,cx,cz));
        world.chunkSource=(net.minecraft.world.chunk.IChunkProvider)Proxy.newProxyInstance(JigsawModuleFixture120.class.getClassLoader(),new Class[]{net.minecraft.world.chunk.IChunkProvider.class},(p,m,a)->{
            if(m.getName().equals("chunkExists"))return world.chunks.containsKey(a[0]+","+a[1]);
            if(m.getName().equals("provideChunk"))return world.chunks.get(a[0]+","+a[1]);
            throw new AssertionError("unexpected chunk operation "+m.getName());
        });
        String[] features={"oak","spruce","pine","acacia","pile_hay","pile_melon","pile_pumpkin","pile_snow","pile_ice","flower_plain","patch_taiga_grass","patch_cactus","patch_berry_bush"};
        for(String feature:features){
            world.ground=feature.equals("patch_cactus")?Blocks.sand:Blocks.grass;
            JigsawPlacementRuntime120.Element element=JigsawPlacementRuntime120.Element.feature("minecraft:"+feature,JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING);
            JigsawPlacementRuntime120.Piece piece=new JigsawPlacementRuntime120.Piece(new JigsawPlacementRuntime120.Template("fixture",1,1,1,0,Collections.emptyList()),element,0,15,65,15,0);
            world.writes.clear();world.ownerX=0;world.ownerZ=0;VillageFeaturePlan120.place(world,piece,0,0);
            NBTTagCompound saved=new NBTTagCompound();world.plans.writeToNBT(saved);JigsawPlansData120 restored=new JigsawPlansData120();restored.readFromNBT(saved);world.plans=restored;
            for(int cx=-1;cx<=1;cx++)for(int cz=-1;cz<=1;cz++){world.ownerX=cx;world.ownerZ=cz;VillageFeaturePlan120.place(world,piece,cx,cz);}
            // A random cactus patch may legitimately produce zero blocks for this seed.
            check(!world.writes.isEmpty()||feature.equals("patch_cactus"),"actual feature placement "+feature);Map<String,String> forward=new TreeMap<>(world.writes);world.writes.clear();
            for(int cx=1;cx>=-1;cx--)for(int cz=1;cz>=-1;cz--){world.ownerX=cx;world.ownerZ=cz;VillageFeaturePlan120.place(world,piece,cx,cz);}
            check(forward.equals(world.writes),"saved feature replay independent of chunk order "+feature);
        }
        int cactusBlocks=0;world.ground=Blocks.sand;
        JigsawPlacementRuntime120.Piece cactus=new JigsawPlacementRuntime120.Piece(new JigsawPlacementRuntime120.Template("fixture",1,1,1,0,Collections.emptyList()),JigsawPlacementRuntime120.Element.feature("minecraft:patch_cactus",JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING),0,15,65,15,0);
        for(int seed=0;seed<32;seed++){
            world.featureSeed=seed;world.plans=new JigsawPlansData120();world.writes.clear();
            for(int cx=-1;cx<=1;cx++)for(int cz=-1;cz<=1;cz++){world.ownerX=cx;world.ownerZ=cz;VillageFeaturePlan120.place(world,cactus,cx,cz);}
            cactusBlocks+=world.writes.size();
        }
        check(cactusBlocks>0,"cactus patch must actually place across sampled seeds");
        world.chunks.remove("0,0");world.writes.clear();
        JigsawPlacementRuntime120.Piece deferred=new JigsawPlacementRuntime120.Piece(new JigsawPlacementRuntime120.Template("fixture",1,1,1,0,Collections.emptyList()),JigsawPlacementRuntime120.Element.feature("minecraft:oak",JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING),0,16,65,16,0);
        VillageFeaturePlan120.place(world,deferred,1,1);
        check(world.writes.isEmpty()&&world.plans.feature("minecraft:oak:16,65,16")==null,"missing neighbours must not persist a partial tree");
    }
    static final class FlatChunk extends net.minecraft.world.chunk.Chunk{
        FlatChunk(World w,int x,int z){super(w,x,z);}
        public net.minecraft.block.Block getBlock(int x,int y,int z){return y==64?((Grid)worldObj).ground:y<64?Blocks.dirt:Blocks.air;}
        public int getBlockMetadata(int x,int y,int z){return 0;}
    }
    public static final class Grid extends World {
        PillagerOutpostData120 data;GameRules rules;
        net.minecraft.block.Block ground=Blocks.grass;
        long featureSeed;public long getSeed(){return featureSeed;}
        JigsawPlansData120 plans;Map<String,String> writes;Map<String,net.minecraft.world.chunk.Chunk> chunks;net.minecraft.world.chunk.IChunkProvider chunkSource;int ownerX,ownerZ;
        private Grid(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
        public WorldSavedData loadItemData(Class type,String name){return type==JigsawPlansData120.class?plans:data;}public GameRules getGameRules(){return rules;}
        public net.minecraft.world.chunk.IChunkProvider getChunkProvider(){return chunkSource;}
        public boolean setBlock(int x,int y,int z,net.minecraft.block.Block b,int meta,int flags){check((x>>4)==ownerX&&(z>>4)==ownerZ,"feature write escaped chunk");writes.put(x+","+y+","+z,BlockName(b)+":"+meta);return true;}
        static String BlockName(net.minecraft.block.Block b){return String.valueOf(net.minecraft.block.Block.blockRegistry.getNameForObject(b));}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
}
