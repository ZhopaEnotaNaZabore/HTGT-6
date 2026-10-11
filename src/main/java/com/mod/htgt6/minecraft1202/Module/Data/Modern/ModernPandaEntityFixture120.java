package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelPanda120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.RenderPanda120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;

/** Panda genetics, legacy protocol, model, official assets and registry contract. */
public final class ModernPandaEntityFixture120 {
    public static void main(String[] ignored)throws Exception{
        require(EntityPanda120.class.getSuperclass()==EntityAnimal.class,"legacy EntityAnimal base");
        require(EntityPanda120.MAIN_GENE_WATCHER>=0&&EntityPanda120.EAT_WATCHER<=31,"1.7 watcher range");
        require(EntityPanda120.class.getMethod("createChild",EntityAgeable.class)!=null,"ageable breeding bridge");
        require(EntityPanda120.class.getMethod("writeEntityToNBT",NBTTagCompound.class)!=null,"NBT genes");
        require(EntityPanda120.MAX_HEALTH==20D&&EntityPanda120.WEAK_MAX_HEALTH==10D
                &&EntityPanda120.ATTACK_DAMAGE==6D,"1.20 panda attributes");
        int[] counts=new int[EntityPanda120.Gene.values().length];
        for(int roll=0;roll<16;roll++)counts[EntityPanda120.Gene.fromRoll(roll).id]++;
        require(counts[0]==5&&counts[1]==1&&counts[2]==1&&counts[3]==1&&counts[4]==2
                &&counts[5]==5&&counts[6]==1,"exact sixteen-way gene distribution");
        require(EntityPanda120.Gene.product(EntityPanda120.Gene.BROWN,EntityPanda120.Gene.NORMAL)
                ==EntityPanda120.Gene.NORMAL,"recessive brown gene");
        require(EntityPanda120.Gene.product(EntityPanda120.Gene.WEAK,EntityPanda120.Gene.WEAK)
                ==EntityPanda120.Gene.WEAK,"paired recessive weak gene");
        require(Backport120Entities.WARDEN_MOD_ID==46&&Backport120Entities.PANDA_MOD_ID==47,
                "IDs must not alias Combat 43..45");
        require(RenderLiving.class.isAssignableFrom(RenderPanda120.class),"legacy renderer");
        ModelPanda120 model=new ModelPanda120();
        require(model.head.cubeList.size()==4&&model.body.cubeList.size()==1,"official panda mesh parts");
        require(model.head.rotationPointY==11.5F&&model.head.rotationPointZ==-17F
                &&model.body.rotationPointY==10F,"official panda root pivots");
        box(model.body,0,-9.5F,-13,-6.5F,19,26,13);
        box(model.head,0,-6.5F,-5,-4,13,10,9);
        box(model.head,1,-3.5F,0,-6,7,5,2);
        box(model.head,2,3.5F,-8,-1,5,4,1);
        box(model.head,3,-8.5F,-8,-1,5,4,1);
        for(net.minecraft.client.model.ModelRenderer leg:new net.minecraft.client.model.ModelRenderer[]{
                model.rightHindLeg,model.leftHindLeg,model.rightFrontLeg,model.leftFrontLeg}) {
            box(leg,0,-3,0,-3,6,9,6);
            require(leg.rotationPointY+9==24,"feet at ground plane");
        }
        require(Math.abs(model.body.rotateAngleX-(float)Math.PI/2)<0.00001F,"body long local Y axis rotates horizontally");
        for(String name:new String[]{"panda","brown_panda","aggressive_panda","lazy_panda",
                "weak_panda","worried_panda","playful_panda"})image(name);
        System.out.println("Modern panda OK; 1.7 animal/AI/NBT protocol, 1.20 genes/model/assets, entityId=47");
    }
    private static void image(String name)throws Exception{String path="assets/htgt6/textures/entity/panda/"+name+".png";
        InputStream in=ModernPandaEntityFixture120.class.getClassLoader().getResourceAsStream(path);require(in!=null,path);
        BufferedImage image;try{image=ImageIO.read(in);}finally{in.close();}
        require(image!=null&&image.getWidth()==64&&image.getHeight()==64,path+" dimensions");}
    private static void box(net.minecraft.client.model.ModelRenderer part,int index,float x,float y,float z,float w,float h,float d){
        net.minecraft.client.model.ModelBox box=(net.minecraft.client.model.ModelBox)part.cubeList.get(index);
        require(box.posX1==x&&box.posY1==y&&box.posZ1==z&&box.posX2==x+w&&box.posY2==y+h&&box.posZ2==z+d,
                "reference panda cuboid "+index);
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private ModernPandaEntityFixture120(){}
}
