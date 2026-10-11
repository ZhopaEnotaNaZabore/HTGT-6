package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.block.greg.technological.hulls.HullEnergyBuffer;

public final class HullEnergyBufferCheck {
    public static void main(String[] args) {
        HullEnergyBuffer b=new HullEnergyBuffer(4096);
        check(b.receive(0,32,128,128,true)==0,"overvoltage");
        check(b.receive(0,32,32,32,false)==32 && b.energy()==0,"simulation mutated buffer");
        check(b.receive(0,32,128,32,true)==32,"more than one amp accepted");
        check(b.receive(0,32,32,32,true)==0,"duplicate input");
        check(b.offer(0)==0,"same-tick forwarding");
        check(b.offer(1)==32,"packet not offered");
        b.consume(1,16);
        check(b.offer(1)==0 && b.energy()==16,"duplicate output in a tick");
        b.consume(2,16);
        check(b.energy()==0,"energy loss/conservation");
        check(b.receive(2,512,8,8,true)==8 && b.voltage()==8,"voltage transformed upwards");
        b.restore(512,8,8);
        check(b.offer(100)==8 && b.voltage()==8,"restore packet");
        b.restore(32,Double.NaN,32);
        check(b.energy()==0,"invalid NBT");
        long max=34359738368L;
        b.setCapacity(max*128);
        check(b.receive(200,max,max,max,true)==max,"MAX voltage overflow");
        b.consume(201,max);
        check(b.energy()==0,"MAX output");
        check(b.receive(201,8,8,8,true)==8,"ULV packet");
        check(b.receive(201,8,Double.POSITIVE_INFINITY,8,true)==0,"non-finite input");
        b.restore(32,0,0);
        check(b.receive(300,32,32,32,true)==32,"first buffered packet");
        for (long tick=301;tick<320;tick++) {
            check(b.receive(tick,32,32,32,true)==32,"input-first throughput");
            check(b.offer(tick)==32,"old packet blocked by fresh packet");
            b.consume(tick,32);
            check(b.energy()==32 && b.offer(tick)==0,"one amp output limit");
        }
        check(b.receive(320,32,8,8,true)==0,"mixed voltages merged");
        b.restore(32,64,32);
        check(b.energy()==64 && b.offer(321)==32,"two-packet NBT restore");
        b.setCapacity(256);
        b.restore(8,512,8);
        check(b.energy()==256 && !b.canReceive(400),"ULV capacity clamp");
        b.setCapacity(4096);
        b.restore(32,4096,32);
        check(b.energy()==4096 && b.offer(400)==32,"LV buffer vs one amp output");
        System.out.println("HullEnergyBufferCheck: OK");
    }
    private static void check(boolean result,String message) { if(!result) throw new AssertionError(message); }
}
