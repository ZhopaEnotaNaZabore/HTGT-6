package com.mod.htgt6.minecraft1202.Module.Data.Frostburn;

import com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120;

import java.io.InputStream;
import java.security.MessageDigest;

/** Headless vectors copied from Minecraft 1.20.2 Entity bubble methods. */
public final class FuturaBubbleColumnFixture120 {
    public static void main(String[] arguments){
        exact("inside magma",-.3D,FuturaBubbleColumn120.velocityAfter(true,false,-2.0D));
        exact("inside magma step",-.13D,FuturaBubbleColumn120.velocityAfter(true,false,-.1D));
        exact("inside soul sand",.7D,FuturaBubbleColumn120.velocityAfter(false,false,2.0D));
        exact("inside soul sand step",.16D,FuturaBubbleColumn120.velocityAfter(false,false,.1D));
        exact("top magma",-.9D,FuturaBubbleColumn120.velocityAfter(true,true,-2.0D));
        exact("top magma step",-.13D,FuturaBubbleColumn120.velocityAfter(true,true,-.1D));
        exact("top soul sand",1.8D,FuturaBubbleColumn120.velocityAfter(false,true,2.0D));
        exact("top soul sand step",.2D,FuturaBubbleColumn120.velocityAfter(false,true,.1D));
        resource("/assets/htgt6/textures/items/frostburn/structure_void.png");
        resource("/assets/htgt6/textures/blocks/frostburn/structure_void.png");
        System.out.println("Futura bubble-column 1.20.2 fixture OK; inside=-0.3/+0.7, top=-0.9/+1.8");
    }

    private static void resource(String path){
        try{
            InputStream input=FuturaBubbleColumnFixture120.class.getResourceAsStream(path);
            if(input==null)throw new IllegalStateException("missing resource "+path);
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            try{byte[] buffer=new byte[1024];for(int count;(count=input.read(buffer))>=0;)
                digest.update(buffer,0,count);}finally{input.close();}
            StringBuilder hash=new StringBuilder();for(byte value:digest.digest())
                hash.append(String.format("%02X",value&255));
            if(!"4DAA186327DB2AFC6A5D1E9F88855F7246A676B562FEE758C31942F3C3B5F9CB"
                    .equals(hash.toString()))throw new IllegalStateException("wrong structure-void asset "+path);
        }catch(Exception exception){throw exception instanceof RuntimeException?(RuntimeException)exception:
                new IllegalStateException(exception);}
    }

    private static void exact(String name,double expected,double actual){
        if(Double.doubleToLongBits(expected)!=Double.doubleToLongBits(actual))
            throw new IllegalStateException(name+": expected "+expected+", got "+actual);
    }

    private FuturaBubbleColumnFixture120(){ }
}
