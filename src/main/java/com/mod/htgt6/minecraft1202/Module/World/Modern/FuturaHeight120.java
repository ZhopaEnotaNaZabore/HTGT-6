package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import com.mod.htgt6.asm.HTGT6Config;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

/** Central conditional height contract used by method-targeted Stage-8 ASM patches. */
public final class FuturaHeight120 {
    public static final int LEGACY_HEIGHT=256;
    public static final int MODERN_MIN_Y=-64;
    public static final int MODERN_MAX_Y=319;
    public static final int Y_TRANSLATION=64;
    /** Minecraft 1.20.2 -64..319 translated by +64 into 0..383. */
    public static final int PHYSICAL_HEIGHT=384;
    /** Default render plane: one block above the highest buildable Futura block. */
    public static final int PHYSICAL_CLOUD_Y=PHYSICAL_HEIGHT;
    public static final int SECTION_COUNT=24;
    public static final int MIN_BUILD_Y=0;
    public static final int MAX_BUILD_Y=383;

    /**
     * Explicit activation checklist.  A pure codec or accessor seam is not
     * enough to make a 384-block world safe: every entry must be ready before
     * {@link #isExtended(World)} can change vanilla allocation sizes.
     */
    public enum ActivationGate {
        HEIGHT_ACCESSORS(true),
        GENERATOR_24_SECTIONS(true),
        CHUNK_STORAGE_24_SECTIONS(true),
        ANVIL_NBT_24_SECTIONS(true),
        S21_CHUNK_PACKET_24_SECTIONS(true),
        S26_BULK_PACKET_24_SECTIONS(true),
        BLOCK_UPDATE_PACKET_9_BIT_Y(true),
        LIGHTING_384(true),
        COLLISION_AND_INTERACTION_384(true),
        CLIENT_RENDER_24_SECTIONS(true);

        private final boolean ready;
        ActivationGate(boolean ready){this.ready=ready;}
        public boolean isReady(){return ready;}
    }

    /* Deliberately derived from every gate so a partial edit cannot activate the pipeline. */
    private static final boolean COMPLETE_PIPELINE=allActivationGatesReady();

    public static boolean isExtended(World world){
        return COMPLETE_PIPELINE&&FuturaWorldType120.isFutura(world);
    }

    public static int height(World world){return isExtended(world)?PHYSICAL_HEIGHT:LEGACY_HEIGHT;}
    public static int actualHeight(WorldProvider provider){
        if(provider!=null&&isExtended(provider.worldObj))return PHYSICAL_HEIGHT;
        return provider!=null&&provider.hasNoSky?128:LEGACY_HEIGHT;
    }
    public static int sectionCount(World world){return isExtended(world)?SECTION_COUNT:16;}

    /** Generator activation excludes its own readiness bit to avoid a circular gate dependency. */
    public static int generatorHeight(World world){
        return generatorHeight(FuturaWorldType120.isFutura(world),allOtherGatesReady(ActivationGate.GENERATOR_24_SECTIONS));
    }
    /** Pure dormant/ready simulation used by generator fixtures. */
    public static int generatorHeight(boolean futura,boolean allOtherReady){
        return ActivationGate.GENERATOR_24_SECTIONS.isReady()&&futura&&allOtherReady
                ?PHYSICAL_HEIGHT:LEGACY_HEIGHT;
    }
    public static int generatorSectionCount(boolean futura,boolean allOtherReady){
        return generatorHeight(futura,allOtherReady)>>4;
    }

    /** Futura-only lighting bound; COMPLETE_PIPELINE remains independently gated. */
    public static int lightingHeight(World world){
        return ActivationGate.LIGHTING_384.isReady()&&FuturaWorldType120.isFutura(world)
                ?PHYSICAL_HEIGHT:LEGACY_HEIGHT;
    }
    public static int lightingSectionCount(World world){return lightingHeight(world)>>4;}
    /** Pure fixture overload. */
    public static int lightingHeight(boolean futura){
        return ActivationGate.LIGHTING_384.isReady()&&futura?PHYSICAL_HEIGHT:LEGACY_HEIGHT;
    }
    public static int lightingSectionCount(boolean futura){return lightingHeight(futura)>>4;}

    /**
     * Storage is safe to widen before the complete height pipeline is enabled.
     * The extra slots remain dormant while World height accessors still expose
     * 0..255, but allow Anvil to preserve already-created Futura sections.
     */
    public static int storageSectionCount(World world){
        return ActivationGate.CHUNK_STORAGE_24_SECTIONS.isReady()
                && FuturaWorldType120.isFutura(world)?SECTION_COUNT:16;
    }

    /** Pure overload used by the NBT/ASM contract fixtures. */
    public static int storageSectionCount(boolean futura){
        return ActivationGate.CHUNK_STORAGE_24_SECTIONS.isReady()&&futura?SECTION_COUNT:16;
    }

    /** Futura-only bound used by World/Chunk interaction seams before full pipeline activation. */
    public static int interactionHeight(World world){
        return ActivationGate.COLLISION_AND_INTERACTION_384.isReady()
                && FuturaWorldType120.isFutura(world)?PHYSICAL_HEIGHT:LEGACY_HEIGHT;
    }
    /** Pure overload for matcher/runtime fixtures. */
    public static int interactionHeight(boolean futura){
        return ActivationGate.COLLISION_AND_INTERACTION_384.isReady()&&futura?PHYSICAL_HEIGHT:LEGACY_HEIGHT;
    }

    /** NBT Section.Y is an unsigned byte even though 1.7 exposes getByte(). */
    public static int sectionIndexFromNbt(byte encodedY){return encodedY&255;}
    public static int modernToPhysicalY(int modernY){return modernY+Y_TRANSLATION;}
    public static int physicalToModernY(int physicalY){return physicalY-Y_TRANSLATION;}
    /** Configurable physical render plane; it may sit at the exclusive world-height boundary. */
    public static int cloudHeight(){return cloudHeight(HTGT6Config.FUTURA_CLOUD_HEIGHT);}
    public static int cloudHeight(int physicalY){
        return Math.max(192,Math.min(PHYSICAL_HEIGHT,physicalY));
    }
    public static int sectionIndex(int physicalY){
        if(physicalY<MIN_BUILD_Y||physicalY>MAX_BUILD_Y)
            throw new IllegalArgumentException("Y outside Futura build range: "+physicalY);
        return physicalY>>4;
    }
    public static boolean isBuildY(World world,int y){
        return y>=MIN_BUILD_Y&&y<=(isExtended(world)?MAX_BUILD_Y:LEGACY_HEIGHT-1);
    }
    public static boolean isPipelineComplete(){return COMPLETE_PIPELINE;}

    public static String[] missingActivationGates(){
        int count=0;
        for(ActivationGate gate:ActivationGate.values())if(!gate.isReady())count++;
        String[] missing=new String[count];
        int index=0;
        for(ActivationGate gate:ActivationGate.values())if(!gate.isReady())missing[index++]=gate.name();
        return missing;
    }

    public static void validateContract(){
        if(MODERN_MIN_Y+Y_TRANSLATION!=MIN_BUILD_Y
                ||MODERN_MAX_Y+Y_TRANSLATION!=MAX_BUILD_Y
                ||PHYSICAL_CLOUD_Y!=PHYSICAL_HEIGHT
                ||PHYSICAL_HEIGHT!=SECTION_COUNT*16
                ||MIN_BUILD_Y!=0||MAX_BUILD_Y!=PHYSICAL_HEIGHT-1)
            throw new IllegalStateException("Invalid Futura height contract");
        if(modernToPhysicalY(MODERN_MIN_Y)!=MIN_BUILD_Y
                ||modernToPhysicalY(MODERN_MAX_Y)!=MAX_BUILD_Y
                ||physicalToModernY(MIN_BUILD_Y)!=MODERN_MIN_Y
                ||physicalToModernY(MAX_BUILD_Y)!=MODERN_MAX_Y
                ||sectionIndex(MIN_BUILD_Y)!=0||sectionIndex(MAX_BUILD_Y)!=SECTION_COUNT-1)
            throw new IllegalStateException("Futura Y translation is not reversible");
        if(COMPLETE_PIPELINE!=allActivationGatesReady())
            throw new IllegalStateException("Futura activation-gate state is inconsistent");
        if(ActivationGate.ANVIL_NBT_24_SECTIONS.isReady()
                &&!ActivationGate.CHUNK_STORAGE_24_SECTIONS.isReady())
            throw new IllegalStateException("24-section Anvil NBT requires 24-section Chunk storage");
    }

    private static boolean allActivationGatesReady(){
        for(ActivationGate gate:ActivationGate.values())if(!gate.isReady())return false;
        return true;
    }

    private static boolean allOtherGatesReady(ActivationGate excluded){
        for(ActivationGate gate:ActivationGate.values())if(gate!=excluded&&!gate.isReady())return false;
        return true;
    }
    private FuturaHeight120(){ }
}
