package com.mod.htgt6.minecraft1202.Module.World.Modern;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Opens bundled backport data both from a development classpath and from the FML mod container. */
public final class BackportResourceLoader120 {
    private static final String MOD_ID="HTGT6";

    public static InputStream open(Class<?> owner,String resource){
        String absolute=resource.startsWith("/")?resource:"/"+resource;
        InputStream input=owner.getResourceAsStream(absolute);
        if(input!=null)return input;
        String relative=absolute.substring(1);
        ClassLoader ownerLoader=owner.getClassLoader();
        if(ownerLoader!=null){input=ownerLoader.getResourceAsStream(relative);if(input!=null)return input;}
        ClassLoader context=Thread.currentThread().getContextClassLoader();
        if(context!=null&&context!=ownerLoader){input=context.getResourceAsStream(relative);if(input!=null)return input;}
        try{
            ModContainer container=Loader.instance().getIndexedModList().get(MOD_ID);
            if(container!=null)return openContainer(container.getSource(),relative);
        }catch(Throwable ignored){/* FML is not initialized in standalone contract fixtures. */}
        return null;
    }

    static InputStream openContainer(File source,String relative) throws IOException {
        if(source==null)return null;
        if(source.isDirectory()){
            File root=source.getCanonicalFile();
            File target=new File(root,relative.replace('/',File.separatorChar)).getCanonicalFile();
            String rootPath=root.getPath()+File.separator;
            if(!target.getPath().startsWith(rootPath)||!target.isFile())return null;
            return new FileInputStream(target);
        }
        if(!source.isFile())return null;
        final JarFile jar=new JarFile(source);
        JarEntry entry=jar.getJarEntry(relative);
        if(entry==null){jar.close();return null;}
        return new FilterInputStream(jar.getInputStream(entry)){
            @Override public void close() throws IOException {try{super.close();}finally{jar.close();}}
        };
    }

    private BackportResourceLoader120(){ }
}
