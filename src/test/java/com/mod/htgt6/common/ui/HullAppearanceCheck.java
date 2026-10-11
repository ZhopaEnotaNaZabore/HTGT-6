package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.block.greg.technological.hulls.HullAppearance;
import java.util.HashSet;
import java.util.Set;

public final class HullAppearanceCheck {
    public static void main(String[] args) {
        Set<Integer> colors=new HashSet<Integer>();
        for (int tier:new int[] {0,1,2,3,4,5,6,7,8,9,16}) {
            require(colors.add(HullAppearance.color(tier)),"duplicate tier color");
            for(int facing=2;facing<=5;facing++) {
                int inputs=0,outputs=0;
                for(int side=0;side<6;side++) {
                    if(HullAppearance.accepts(facing,side)) inputs++;
                    if(HullAppearance.emits(facing,side)) outputs++;
                    require(HullAppearance.accepts(facing,side)!=HullAppearance.emits(facing,side),"shared input/output face");
                    int expected=tier<3 || side==facing ? 0 : side<2 ? 2 : 1;
                    require(HullAppearance.texture(tier,facing,side)==expected,"wrong tier/face texture");
                }
                require(inputs==5 && outputs==1,"face counts");
                require(!HullAppearance.accepts(facing,-1) && !HullAppearance.accepts(facing,6),"invalid input side");
            }
        }
        require(HullAppearance.front(0)==2,"legacy facing");
        System.out.println("HullAppearanceCheck: OK (11 tiers, four facings, five inputs / one output)");
    }
    private static void require(boolean value,String message) { if(!value) throw new AssertionError(message); }
}
