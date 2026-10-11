package com.mod.htgt6.asm;

import java.io.*;
import java.lang.reflect.*;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldInfo;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;

/** Execute transformed vanilla survival and neighbor callbacks, not a parallel implementation. */
public final class PlantHeightASMFixture120 {
    public static void validate() throws Exception {
        FuturaWorldType120.registerIdentity();
        for(String simple:new String[]{"BlockMushroom","BlockLilyPad"}) {
            String name="net.minecraft.block."+simple;
            byte[] original=read("/"+name.replace('.','/')+".class");
            HTGT6Transformer transformer=new HTGT6Transformer();
            byte[] patched=transformer.transform(name,name,original);
            require(hooks(patched)==1,simple+" MCP hook");
            require(hooks(transformer.transform(name,name,patched))==1,simple+" idempotence");
            ClassNode srg=new ClassNode();new ClassReader(original).accept(srg,0);
            for(MethodNode m:srg.methods)if(m.name.equals("canBlockStay"))m.name="func_149718_j";
            ClassWriter writer=new ClassWriter(0);srg.accept(writer);
            require(hooks(transformer.transform(name,name,writer.toByteArray()))==1,simple+" SRG hook");
            Constructor<?> constructor=new IsolatedLoader().define(name,patched).getDeclaredConstructor();
            constructor.setAccessible(true);BlockBush plant=(BlockBush)constructor.newInstance();
            boolean lily=simple.equals("BlockLilyPad");
            if(lily)require(callCount(patched,"lilySurvival120")==1
                    && callCount(transformer.transform(name,name,patched),"lilySurvival120")==1,"survival wrapper idempotence");
            for(boolean futura:new boolean[]{false,true}) {
                Grid world=world(futura);world.support=lily?Blocks.water:Blocks.mycelium;
                int ceiling=futura?384:256;
                for(int y:new int[]{-1,0,1,255,256,300,383,384}) {
                    world.y=y;world.plant=plant;
                    require(plant.canBlockStay(world,0,y,0)==(y>=0&&y<ceiling),simple+" height "+y+" futura="+futura);
                }
                world.y=futura?383:255;world.plant=plant;
                plant.onNeighborBlockChange(world,0,world.y,0,Blocks.stone);
                require(world.plant==plant,simple+" valid neighbor update");
                world.support=Blocks.air;
                plant.onNeighborBlockChange(world,0,world.y,0,Blocks.air);
                require(world.plant==Blocks.air,simple+" lost support still removes plant");
            }
            Grid world=world(true);world.y=300;world.plant=plant;
            if(lily) {
                world.support=Blocks.water;world.meta=1;
                require(!plant.canBlockStay(world,0,300,0),"flowing water remains invalid");
                world.meta=0;plant.updateTick(world,0,300,0,new Random(1));
                require(world.plant==plant,"lily tick survives above 255");
                verifyLily44(plant);

            } else {
                world.support=Blocks.dirt;world.meta=2;
                require(plant.canBlockStay(world,0,300,0),"podzol survives bright light above 255");
                world.meta=0;
                require(!plant.canBlockStay(world,0,300,0),"bright ordinary dirt still rejected");
                world.support=Blocks.stone;world.light=12;
                require(plant.canBlockStay(world,0,300,0),"dark solid substrate above 255");
                world.light=13;
                require(!plant.canBlockStay(world,0,300,0),"mushroom light threshold preserved");
            }
        }
        System.out.println("Plant height ASM OK: real MCP/SRG bytecode, idempotence, Futura/legacy boundaries, neighbor removal, tick and survival predicates");
    }
    private static void verifyLily44(BlockBush plant)throws Exception {
        for(boolean futura:new boolean[]{false,true})for(Block ice:new Block[]{Blocks.ice,
                new com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockFrostedIce()}) {
            Grid w=world(futura);w.y=futura?383:100;w.support=ice;w.plant=plant;
            for(int age=0;age<4;age++) {
                w.meta=age;
                require(plant.canBlockStay(w,0,w.y,0),"ice supports lily at every frost age");
                require(HTGT6Transformer.lilyPlacementMaterial120(ice)==net.minecraft.block.material.Material.water
                        && HTGT6Transformer.lilyPlacementMetadata120(w,0,w.y-1,0)==0,"item accepts ice/frost age");
            }
            plant.onNeighborBlockChange(w,0,w.y,0,ice);require(w.plant==plant,"ice neighbor callback");
            w.support=Blocks.water;w.meta=0;plant.updateTick(w,0,w.y,0,new Random(1));
            require(w.plant==plant,"melt to source preserves lily");
            w.meta=1;plant.onNeighborBlockChange(w,0,w.y,0,Blocks.water);
            require(w.plant==Blocks.air,"melt to flowing water removes lily");
        }
        Grid w=world(true);w.y=300;w.plant=plant;
        for(Block invalid:new Block[]{Blocks.packed_ice,Blocks.stone,Blocks.lava}) {
            w.support=invalid;require(!plant.canBlockStay(w,0,300,0),"invalid lily substrate");
        }
        w.support=Blocks.ice;
        for(Block fluid:new Block[]{Blocks.water,Blocks.lava}) {
            w.plant=fluid;require(!plant.canBlockStay(w,0,300,0),"fluid at lily position rejected");
        }
        w.plant=Blocks.air;require(HTGT6Transformer.lilyPlacementAir120(w,0,300,0),"item air at extended height");
        w.support=Blocks.ice;require(plant.canPlaceBlockAt(w,0,300,0),"generic placement accepts ice above 255");
        w.plant=Blocks.stone;require(!plant.canPlaceBlockAt(w,0,300,0),"generic placement rejects occupied solid");
        w.plant=Blocks.air;w.support=Blocks.water;w.meta=1;
        require(!plant.canPlaceBlockAt(w,0,300,0),"generic placement checks water level");
        w.support=Blocks.ice;w.y=384;require(!plant.canPlaceBlockAt(w,0,384,0),"generic placement maximum height");

        w.y=384;require(!HTGT6Transformer.lilyPlacementAir120(w,0,384,0),"item upper boundary");
        boolean enabled=HTGT6Config.RUN_MINECRAFT_1_7_9999;
        try { HTGT6Config.RUN_MINECRAFT_1_7_9999=false;w.y=100;w.support=Blocks.ice;w.plant=plant;
            require(!plant.canBlockStay(w,0,100,0),"disabled backport keeps legacy ice rejection");
            require(HTGT6Transformer.lilyPlacementMaterial120(Blocks.ice)==Blocks.ice.getMaterial(),"disabled item material");
        } finally { HTGT6Config.RUN_MINECRAFT_1_7_9999=enabled; }
        String name="net.minecraft.item.ItemLilyPad";byte[] raw=read("/net/minecraft/item/ItemLilyPad.class");
        byte[] result=new HTGT6Transformer().transform(name,name,raw);
        require(lilyItemHooks(result)==3,"three targeted item predicates");
        require(lilyItemHooks(new HTGT6Transformer().transform(name,name,result))==3,"item idempotence");
        for(String call:new String[]{"canMineBlock","canPlayerEdit","getBlockSnapshot","onPlayerBlockPlace","restore"})
            require(callCount(raw,call)>0&&callCount(raw,call)==callCount(result,call),"Forge item path preserved: "+call);
        System.out.println("Lily 44 OK: ice, frost ages, melt lifecycle, fluids, item predicates, permissions, Forge callbacks and disabled mode");
    }
    static int lilyItemHooks(byte[] bytes) {
        return callCount(bytes,"lilyPlacementMaterial120")+callCount(bytes,"lilyPlacementMetadata120")+callCount(bytes,"lilyPlacementAir120");
    }
    static int survivalReturns(byte[] bytes) {
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int count=0;
        for(MethodNode m:c.methods)if(m.name.equals("canBlockStay")||m.name.equals("func_149718_j"))
            for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==Opcodes.IRETURN)count++;
        return count;
    }
    static int callCount(byte[] bytes,String name) {
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int count=0;
        for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions.toArray())
            if(n instanceof MethodInsnNode && ((MethodInsnNode)n).name.equals(name))count++;
        return count;
    }
    static int hooks(byte[] bytes) {
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int count=0;
        for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions.toArray())
            if(n instanceof MethodInsnNode && ((MethodInsnNode)n).owner.equals("com/mod/htgt6/asm/HTGT6Transformer")
                    && ((MethodInsnNode)n).name.equals("interactionHeight120"))count++;
        return count;
    }
    private static byte[] read(String path)throws Exception {
        try(InputStream in=PlantHeightASMFixture120.class.getResourceAsStream(path)) {
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];
            for(int n;(n=in.read(buffer))!=-1;)out.write(buffer,0,n);return out.toByteArray();
        }
    }
    private static Grid world(boolean futura)throws Exception {
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");Field f=unsafe.getDeclaredField("theUnsafe");f.setAccessible(true);
        Grid w=(Grid)unsafe.getMethod("allocateInstance",Class.class).invoke(f.get(null),Grid.class);
        w.info=new WorldInfo(new WorldSettings(1,WorldSettings.GameType.SURVIVAL,false,false,
                futura?FuturaWorldType120.instance():WorldType.DEFAULT),"plant fixture");
        w.light=15;w.isRemote=false;w.rand=new Random(1);w.rules=new GameRules();
        w.rules.setOrCreateGameRule("doTileDrops","false");return w;
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static final class IsolatedLoader extends ClassLoader {
        IsolatedLoader(){super(PlantHeightASMFixture120.class.getClassLoader());}
        Class<?> define(String name,byte[] bytes){return defineClass(name,bytes,0,bytes.length);}
    }
    public static final class Grid extends World {
        WorldInfo info;GameRules rules;Block support,plant;int y,meta,light;
        public GameRules getGameRules(){return rules;}
        private Grid(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
        public WorldInfo getWorldInfo(){return info;}
        public Block getBlock(int x,int y,int z){return y==this.y?plant:y==this.y-1?support:Blocks.air;}
        public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
        public int getBlockMetadata(int x,int y,int z){return y==this.y-1?meta:0;}
        public int getFullBlockLightValue(int x,int y,int z){return light;}
        public boolean setBlockToAir(int x,int y,int z){plant=Blocks.air;return true;}
        public boolean setBlock(int x,int y,int z,Block block,int meta,int flags){plant=block;return true;}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
}
