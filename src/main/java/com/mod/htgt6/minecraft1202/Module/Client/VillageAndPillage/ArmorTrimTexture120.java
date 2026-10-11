package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import java.util.*;import java.io.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;import net.minecraft.client.resources.*;import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.item.*;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.util.ResourceLocation;
/** Paletted modern trims composited into the native armor texture; no new renderer API. */
public final class ArmorTrimTexture120 implements IResourceManagerReloadListener {
    private static final Map<String,String> CACHE=new HashMap<String,String>();
    public void onResourceManagerReload(IResourceManager manager){
        for(String value:new HashSet<String>(CACHE.values()))if(value.startsWith("htgt6:armor_trim/"))Minecraft.getMinecraft().getTextureManager().deleteTexture(new ResourceLocation(value));CACHE.clear();
    }
    public static String texture(String base,ItemStack stack,int slot,String type){
        if(stack==null||!(stack.getItem() instanceof ItemArmor)||!stack.hasTagCompound()||!stack.getTagCompound().hasKey("Trim",10))return base;
        ItemArmor armor=(ItemArmor)stack.getItem();
        if(armor.getArmorMaterial()==ItemArmor.ArmorMaterial.CLOTH&&!"overlay".equals(type))return base;
        NBTTagCompound trim=stack.getTagCompound().getCompoundTag("Trim");
        String pattern=trim.getString("pattern"),material=trim.getString("material");
        if(!pattern.startsWith("minecraft:")||!material.startsWith("minecraft:"))return base;
        pattern=pattern.substring(10);material=material.substring(10);
        if(!pattern.matches("[a-z_]+")||!material.matches("[a-z_]+"))return base;
        String armorMaterial=armor.getArmorMaterial().name().toLowerCase(java.util.Locale.ROOT);
        if(armorMaterial.equals("gold"))armorMaterial="gold";
        if(armorMaterial.equals(material))material+="_darker";
        String key=base+"|"+pattern+"|"+material+"|"+slot;
        String cached=CACHE.get(key);if(cached!=null)return cached;
        try{
            BufferedImage source=read(base),overlay=read("htgt6:textures/trims/models/armor/"+pattern+(armor.armorType==2?"_leggings":"")+".png");
            BufferedImage palette=read("htgt6:textures/trims/color_palettes/trim_palette.png"),colors=read("htgt6:textures/trims/color_palettes/"+material+".png");
            // Third-party UV layouts cannot safely use the vanilla trim coordinates.
            if(source.getWidth()*overlay.getHeight()!=source.getHeight()*overlay.getWidth())return base;
            Map<Integer,Integer> map=new HashMap<Integer,Integer>();
            for(int x=0;x<palette.getWidth();x++)map.put(palette.getRGB(x,0)&0xFFFFFF,colors.getRGB(x,0)&0xFFFFFF);
            BufferedImage result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++){
                int pixel=overlay.getRGB(x*overlay.getWidth()/source.getWidth(),y*overlay.getHeight()/source.getHeight());
                Integer color=map.get(pixel&0xFFFFFF);
                result.setRGB(x,y,(pixel>>>24)==0?source.getRGB(x,y):(pixel&0xFF000000)|(color==null?pixel&0xFFFFFF:color));
            }
            ResourceLocation resource=new ResourceLocation("htgt6","armor_trim/"+java.util.UUID.nameUUIDFromBytes(key.getBytes("UTF-8")));
            Minecraft.getMinecraft().getTextureManager().loadTexture(resource,new DynamicTexture(result));
            CACHE.put(key,resource.toString());return resource.toString();
        }catch(IOException|IllegalArgumentException failure){CACHE.put(key,base);return base;}
    }
    private static BufferedImage read(String name)throws IOException{
        try(InputStream stream=Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(name)).getInputStream()){
            BufferedImage result=ImageIO.read(stream);if(result==null)throw new IOException(name);return result;
        }
    }
}
