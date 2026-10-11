package com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage;
import java.lang.reflect.*;import net.minecraft.item.*;import net.minecraft.init.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
public final class SmithingRegressionFixture120 {
    public static void main(String[] args)throws Exception{
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,unsafe.getMethod("allocateInstance",Class.class).invoke(singleton.get(null),cpw.mods.fml.common.Loader.class));
        Bootstrap.func_151354_b();SmithingContent120.prepare();
        require(SmithingContent120.patterns.size()==16&&SmithingContent120.upgrades.size()==9,"smithing content coverage");
        Method result=VillageWorkstationLogic120.class.getDeclaredMethod("result",VillageWorkstationType120.class,ItemStack[].class,int.class);result.setAccessible(true);
        for(Item template:SmithingContent120.patterns.keySet()){
            ItemStack original=new ItemStack(Items.diamond_chestplate,1,73);original.setStackDisplayName("Kept name");original.addEnchantment(net.minecraft.enchantment.Enchantment.protection,4);
            ItemStack[] input={new ItemStack(template),original,new ItemStack(Items.gold_ingot)};
            ItemStack trimmed=(ItemStack)result.invoke(null,VillageWorkstationType120.SMITHING,input,0);
            require(trimmed!=null&&trimmed.getItemDamage()==73&&trimmed.hasDisplayName()&&trimmed.isItemEnchanted(),"trim destroyed NBT/durability");
            require(!original.getTagCompound().hasKey("Trim"),"preview mutated input");
            require(trimmed.getTagCompound().getCompoundTag("Trim").getString("pattern").equals("minecraft:"+SmithingContent120.patterns.get(template)),"wrong trim pattern");
            input[1]=trimmed;require(result.invoke(null,VillageWorkstationType120.SMITHING,input,0)==null,"duplicate trim accepted");
        }
        for(Item item:SmithingContent120.upgrades.keySet()){
            ItemStack original=new ItemStack(item,1,113);original.setStackDisplayName("Kept upgrade");
            ItemStack upgraded=(ItemStack)result.invoke(null,VillageWorkstationType120.SMITHING,new ItemStack[]{new ItemStack(SmithingContent120.upgradeTemplate),original,new ItemStack(EtFVanillaGapContent120.netheriteIngot)},0);
            require(upgraded!=null&&upgraded.getItem()==SmithingContent120.upgrades.get(item)&&upgraded.getItemDamage()==113&&upgraded.hasDisplayName(),"upgrade lost item state");
        }
        com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.ModelVillager120 model=new com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.ModelVillager120(0);
        net.minecraft.client.model.ModelBox coat=(net.minecraft.client.model.ModelBox)model.villagerBody.cubeList.get(1);
        require(coat.posY2-coat.posY1==20,"legacy villager coat geometry");
        net.minecraft.client.model.ModelBox rim=(net.minecraft.client.model.ModelBox)model.hatRim.cubeList.get(0);
        require(rim.posZ2-rim.posZ1==1,"villager hat rim must have thickness");
        require(model.textureWidth==64&&model.textureHeight==64,"villager model defaults must match modern atlas");
        for(net.minecraft.client.model.ModelRenderer part:new net.minecraft.client.model.ModelRenderer[]{
                model.villagerHead,model.villagerBody,model.villagerArms,model.rightVillagerLeg,
                model.leftVillagerLeg,model.headwear})verifyVillagerUv(part);
        model.setRotationAngles(0,0,0,30,15,1F/16F,null);
        require(model.headwear.rotateAngleX==model.villagerHead.rotateAngleX
                &&model.headwear.rotateAngleY==model.villagerHead.rotateAngleY,"hat detached from head pose");
        model.setHeadVisible(false);require(!model.villagerHead.showModel&&!model.headwear.showModel,"biome hat masking");
        model.setHeadVisible(true);require(model.villagerHead.showModel&&model.headwear.showModel,"hat masking leaked across villagers");
        System.out.println("Smithing regression OK: 16 trim patterns, 9 upgrades, duplicate guards, input/NBT preservation, villager geometry");
    }
    private static void verifyVillagerUv(net.minecraft.client.model.ModelRenderer part)throws Exception{
        require(part.textureWidth==64&&part.textureHeight==64,"replacement villager part inherited 64x32 atlas");
        Field faces=null;
        for(Field field:net.minecraft.client.model.ModelBox.class.getDeclaredFields())
            if(field.getType()==net.minecraft.client.model.TexturedQuad[].class){faces=field;faces.setAccessible(true);break;}
        require(faces!=null,"model quad field");
        for(Object box:part.cubeList)for(net.minecraft.client.model.TexturedQuad quad:(net.minecraft.client.model.TexturedQuad[])faces.get(box))
            for(net.minecraft.client.model.PositionTextureVertex vertex:quad.vertexPositions)
                require(vertex.texturePositionX>=0&&vertex.texturePositionX<=1
                        &&vertex.texturePositionY>=0&&vertex.texturePositionY<=1,"baked UV wraps outside 64x64 atlas");
        if(part.childModels!=null)for(Object child:part.childModels)verifyVillagerUv((net.minecraft.client.model.ModelRenderer)child);
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
