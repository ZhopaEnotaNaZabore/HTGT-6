package com.mod.htgt6.common.ui;

import com.mod.htgt6.api.hitech6.multiblock.MultiblockEnergyRole;

public final class MultiblockEnergyRoleCheck {
    public static void main(String[] args) {
        require(MultiblockEnergyRole.CONSUMER.requiredOutputs()==0,"consumer requires output");
        require(MultiblockEnergyRole.GENERATOR.requiredOutputs()==1,"generator can omit output");
        for(boolean formed:new boolean[] {false,true}) for(boolean enabled:new boolean[] {false,true}) {
            require(!MultiblockEnergyRole.CONSUMER.canExport(formed,enabled),"consumer exports stored EU");
            require(MultiblockEnergyRole.GENERATOR.canExport(formed,enabled)==(formed && enabled),"generator state guard");
        }
        System.out.println("MultiblockEnergyRoleCheck: OK");
    }
    private static void require(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
