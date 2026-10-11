package com.mod.htgt6.minecraft1202.Module.Client.Modern;

/** Headless contract for the 1.20.2 HangingSignRenderer layer and block transforms. */
public final class HangingSignModelFixture120 {
    private HangingSignModelFixture120(){}

    public static long assertContract(){
        // Official LayerDefinition: board, wall plank, four crossed chain planes and one V plane.
        checkBox("board",Backport120SignRenderer.BOARD_BOX,new int[]{-7,0,-1,14,10,2});
        checkBox("plank",Backport120SignRenderer.PLANK_BOX,new int[]{-8,-6,-2,16,2,4});
        checkBox("vChains",Backport120SignRenderer.V_CHAINS_BOX,new int[]{-6,-6,0,12,6,0});
        check(close(Backport120SignRenderer.HANGING_TRANSLATE_Y+Backport120SignRenderer.HANGING_MODEL_OFFSET_Y,.625F),"model origin Y");
        check(close(Backport120SignRenderer.MODEL_PIXEL_SCALE,1F/16F),"single pixel-to-block scale");
        check(Backport120SignRenderer.HANGING_TEXTURE_WIDTH==64
                &&Backport120SignRenderer.HANGING_TEXTURE_HEIGHT==32,"texture atlas");
        check(close(Backport120SignRenderer.CHAIN_ANGLE,(float)Math.PI/4F),"crossed chain angles");
        check(close(Backport120SignRenderer.ceilingRotation(15),337.5F),"ceiling rotation");
        check(close(Backport120SignRenderer.wallRotation(2),180F)&&close(Backport120SignRenderer.wallRotation(3),0F)
                &&close(Backport120SignRenderer.wallRotation(4),90F)&&close(Backport120SignRenderer.wallRotation(5),-90F),"wall rotations");
        check(Backport120SignRenderer.hangingPartMask(false,false)==0x1D,"ceiling normal chains");
        check(Backport120SignRenderer.hangingPartMask(false,true)==0x21,"ceiling V chains");
        check(Backport120SignRenderer.hangingPartMask(true,false)==0x1F
                &&Backport120SignRenderer.hangingPartMask(true,true)==0x1F,"wall plank/chains");
        return 0x1202A4610320625L;
    }

    private static void checkBox(String name,int[] actual,int[] expected){
        check(actual.length==expected.length,name+" length");
        for(int i=0;i<actual.length;i++)check(actual[i]==expected[i],name+"["+i+"]");
    }
    private static boolean close(float a,float b){return Math.abs(a-b)<1.0E-6F;}
    private static void check(boolean condition,String label){if(!condition)throw new IllegalStateException(label);}
    public static void main(String[] ignored){System.out.println(Long.toHexString(assertContract()));}
}
