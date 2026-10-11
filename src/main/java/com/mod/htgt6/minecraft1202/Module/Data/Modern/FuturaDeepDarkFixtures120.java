package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.ChunkProviderFutura120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeDecorator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaDeepDarkRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaLushCavesRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldChunkManager120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;

import com.mod.htgt6.minecraft1202.Module.World.Modern.AquiferSampler120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.OverworldBiomeParameterMap120;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Headless 1.20.2 Deep Dark configured-feature and production-wiring contract. */
public final class FuturaDeepDarkFixtures120 {
    public static void main(String[] arguments)throws Exception{
        config(FuturaDeepDarkRuntime120.DEEP_DARK,0,0);
        config(FuturaDeepDarkRuntime120.ANCIENT_CITY,1,3);
        require(FuturaDeepDarkRuntime120.PLACEMENT_COUNT==256,"Deep Dark placed-feature count");
        require(FuturaDeepDarkRuntime120.SCULK_VEIN_MIN_COUNT==204
                        &&FuturaDeepDarkRuntime120.SCULK_VEIN_MAX_COUNT==250
                        &&FuturaDeepDarkRuntime120.SCULK_VEIN_SEARCH_RANGE==20,
                "Deep Dark sculk_vein placed-feature contract");
        require(FuturaWorldChunkManager120.class.getMethod("getUndergroundBiomeAt",
                        int.class,int.class,int.class,FuturaMountainDensityRouter120.class,
                        MutableDensityContext120.class)!=null,
                "Exact zoomed cave-biome query seam");
        require(hasExactBiomeCall(FuturaDeepDarkRuntime120.class)
                        &&hasExactBiomeCall(FuturaAncientCityGenerator120.class)
                        &&hasExactBiomeCall(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaUndergroundFeatures120.class),
                "Cave generators do not use the exact zoomed biome query");
        boolean climateEntry=false;
        for(OverworldBiomeParameterMap120.Entry entry:new OverworldBiomeParameterMap120().entries())
            climateEntry|=entry.biome==OverworldBiomeParameterMap120.Biome.DEEP_DARK;
        require(climateEntry,"Deep Dark missing from 1.20.2 multi-noise parameter map");
        require(AquiferSampler120.isDeepDarkRegion(-.2250001D,.9000001D),"Deep Dark aquifer dry-zone thresholds");
        require(!AquiferSampler120.isDeepDarkRegion(-.2249999D,.9000001D)
                &&!AquiferSampler120.isDeepDarkRegion(-.2250001D,.8999999D),
                "Deep Dark aquifer dry-zone boundary");
        require(FuturaAncientCityGenerator120.supportsOfficialFeature("minecraft:sculk_patch_ancient_city"),
                "Ancient City configured feature wiring");
        Field field=FuturaBiomeDecorator120.class.getDeclaredField("deepDark");
        require(field.getType()==FuturaDeepDarkRuntime120.class&&Modifier.isFinal(field.getModifiers()),
                "Biome decorator Deep Dark runtime field");
        require(hasProductionCall(),"Biome decorator does not invoke Deep Dark runtime");
        System.out.println("Deep Dark 1.20.2 fixture OK; patch=256, veins=204..250, configs=10/32/64/0/1");
    }
    private static void config(FuturaDeepDarkRuntime120.PatchConfig c,int extraMin,int extraMax){
        require(c.chargeCount==10&&c.amountPerCharge==32&&c.spreadAttempts==64,
                "official SculkPatch charge/spread parameters");
        require(c.growthRounds==0&&c.spreadRounds==1,"official SculkPatch rounds");
        require(c.extraMin==extraMin&&c.extraMax==extraMax,"official extra rare growth range");
        require(Float.floatToIntBits(c.catalystChance)==Float.floatToIntBits(.5F),"catalyst chance");
    }
    private static boolean hasProductionCall()throws Exception{
        InputStream in=FuturaBiomeDecorator120.class.getResourceAsStream("FuturaBiomeDecorator120.class");
        if(in==null)return false;final boolean[] found={false};
        try{new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){
            @Override public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){
                if(!"dispatch".equals(name))return null;return new MethodVisitor(Opcodes.ASM5){
                    @Override public void visitMethodInsn(int opcode,String owner,String name,String desc,boolean itf){
                        if(owner.endsWith("/FuturaDeepDarkRuntime120")&&"populateFeature".equals(name))found[0]=true;
                    }};}
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}finally{in.close();}
        return found[0];
    }
    private static boolean hasExactBiomeCall(Class<?> type)throws Exception{
        InputStream in=type.getResourceAsStream(type.getSimpleName()+".class");
        if(in==null)return false;final boolean[] found={false};
        try{new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){
            @Override public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){
                return new MethodVisitor(Opcodes.ASM5){
                    @Override public void visitMethodInsn(int opcode,String owner,String name,String desc,boolean itf){
                        if(owner.endsWith("/FuturaWorldChunkManager120")&&("getUndergroundBiomeAt".equals(name)||"getBiomeKeyAt".equals(name))
                                &&desc.contains("FuturaMountainDensityRouter120")
                                &&desc.contains("MutableDensityContext120"))found[0]=true;
                    }};}
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}finally{in.close();}
        return found[0];
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private FuturaDeepDarkFixtures120(){ }
}
