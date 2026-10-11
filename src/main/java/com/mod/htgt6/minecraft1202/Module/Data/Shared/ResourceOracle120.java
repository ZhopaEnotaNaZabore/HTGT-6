package com.mod.htgt6.minecraft1202.Module.Data.Shared;

import java.io.*;
import java.nio.file.Files;
import java.awt.image.BufferedImage;
import java.util.zip.*;
import javax.imageio.ImageIO;
import com.google.gson.JsonParser;

/** Compare resource meaning against the pinned vanilla JAR, independent of PNG/JSON encoding. */
public final class ResourceOracle120 {
    private ResourceOracle120(){}
    public static boolean sameData(byte[] local,byte[] reference,String path)throws Exception {
        if(path.endsWith(".nbt"))return net.minecraft.nbt.CompressedStreamTools.readCompressed(new ByteArrayInputStream(local))
                .equals(net.minecraft.nbt.CompressedStreamTools.readCompressed(new ByteArrayInputStream(reference)));
        JsonParser parser=new JsonParser();
        return parser.parse(new String(local,"UTF-8")).equals(parser.parse(new String(reference,"UTF-8")));
    }
    public static boolean matches(File file,String official)throws Exception{return matches(Files.readAllBytes(file.toPath()),official);}
    public static boolean matches(byte[] local,String official)throws Exception{
        File reference=new File(System.getProperty("htgt6.reference1202","tools/decompile1202/reference/client.jar"));
        try(ZipFile zip=new ZipFile(reference)){
            ZipEntry entry=zip.getEntry("assets/minecraft/"+official);if(entry==null)return false;
            try(InputStream input=zip.getInputStream(entry)){
                if(official.endsWith(".png")){
                    BufferedImage a=ImageIO.read(new ByteArrayInputStream(local)),b=ImageIO.read(input);
                    if(a==null||b==null||a.getWidth()!=b.getWidth()||a.getHeight()!=b.getHeight())return false;
                    for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getRGB(x,y)!=b.getRGB(x,y))return false;
                    return true;
                }
                if(official.endsWith(".json")||official.endsWith(".mcmeta")){
                    JsonParser parser=new JsonParser();
                    return parser.parse(new String(local,"UTF-8")).equals(parser.parse(new InputStreamReader(input,"UTF-8")));
                }
                return false;
            }
        }
    }
    public static String texturePath(String local){
        String name=local.substring(local.lastIndexOf('/')+1);
        if(local.startsWith("textures/items/"))return "textures/item/"+name;
        if(local.startsWith("textures/entity/"))return local.replace("/1_20_backport/","/");
        return "textures/block/"+(name.equals("smooth_sandstone.png")?"sandstone_top.png":name);
    }
}
