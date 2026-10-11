package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.ChunkProviderFutura120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeDecorator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaLushCavesRuntime120;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.InputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Headless official-config, translated-depth and production-wiring contract. */
public final class FuturaLushCavesFixtures120 {
    private static final long EXPECTED_FINGERPRINT=0xe8fb37878759a7d8L;
    public static void main(String[] args)throws Exception{
        FuturaLushCavesRuntime120.Config c=FuturaLushCavesRuntime120.config();
        require(c.features.size()==9,"Lush Caves VEGETAL_DECORATION feature count");
        require(c.minAboveBottom==0&&c.maxAbsolute==256&&c.scanSteps==12,"official placement height/scan");
        require(FuturaMountainDensityRouter120.modernToFuturaY(c.maxAbsolute)==320,"Y +64 translation");
        feature(c,"minecraft:glow_lichen",104,157);feature(c,"minecraft:cave_vines",188,188);
        feature(c,"minecraft:lush_caves_clay",62,62);feature(c,"minecraft:lush_caves_vegetation",125,125);
        feature(c,"minecraft:lush_caves_ceiling_vegetation",125,125);
        feature(c,"minecraft:rooted_azalea_tree",1,2);feature(c,"minecraft:spore_blossom",25,25);
        feature(c,"minecraft:classic_vines_cave_feature",256,256);
        feature(c,"minecraft:patch_tall_grass_2",1,1);
        long fingerprint=FuturaLushCavesRuntime120.configFingerprint();
        if(fingerprint!=EXPECTED_FINGERPRINT)throw new IllegalStateException("Lush config fingerprint expected 0x"
                +Long.toHexString(EXPECTED_FINGERPRINT)+", got 0x"+Long.toHexString(fingerprint));
        Field field=FuturaBiomeDecorator120.class.getDeclaredField("lush");
        require(field.getType()==FuturaLushCavesRuntime120.class&&Modifier.isFinal(field.getModifiers()),
                "Biome decorator production runtime field missing");
        require(hasProductionCall(),"Biome decorator does not invoke Lush runtime");
        System.out.println("Lush Caves 1.20.2 fixture OK; features=9, maxPhysicalY=320, fingerprint=0x"
                +Long.toHexString(fingerprint));
    }
    private static boolean hasProductionCall()throws Exception{
        InputStream in=FuturaBiomeDecorator120.class.getResourceAsStream("FuturaBiomeDecorator120.class");
        if(in==null)return false;
        final boolean[] found={false};
        try{new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){
            @Override public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){
                if(!"dispatch".equals(name))return null;
                return new MethodVisitor(Opcodes.ASM5){
                    @Override public void visitMethodInsn(int opcode,String owner,String name,String desc,boolean itf){
                        if(owner.endsWith("/FuturaLushCavesRuntime120")&&"populateFeature".equals(name))found[0]=true;
                    }
                };}
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}finally{in.close();}
        return found[0];
    }
    private static void feature(FuturaLushCavesRuntime120.Config c,String id,int min,int max){
        for(FuturaLushCavesRuntime120.Feature f:c.features)if(id.equals(f.id)){
            require(f.min==min&&f.max==max,id+" count");return;}
        throw new IllegalStateException("Missing "+id);
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    private FuturaLushCavesFixtures120(){ }
}
