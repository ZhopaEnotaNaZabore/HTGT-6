package com.mod.htgt6.common.item.htgt6.fluids;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;



public class FluidItemsSub {

    public static Item DustOsIrRhRu;
    public static Item dustPdKeNiCu;
    public static Item dustPtCl;

    public static Item dustPdNH;
    public static Item dustRhSo4;
    public static Item dustOsIr;
    public static Item dustIrO;
    public static Item dustKeNiCu;
    public static Item dustNiCu;


public static void InitFluidSubItems() {
    //reg
    DustOsIrRhRu = register( new Item(), "DustOsIrRhRu");
    dustPdKeNiCu = register( new Item(), "dustPdKeNiCu");
    dustPtCl = register( new Item(), "dustPtCl");
    dustPdNH = register( new Item(), "dustPdNH");
    dustRhSo4 = register( new Item(), "dustRhSo4");
    dustOsIr = register( new Item(), "dustOsIr");
    dustIrO= register( new Item(), "dustIrO");
    dustKeNiCu= register( new Item(), "dustKeNiCu");
    dustNiCu= register( new Item(), "dustNiCu");



    //name
    DustOsIrRhRu.setUnlocalizedName("DustOsIrRhRu");
    dustPdKeNiCu.setUnlocalizedName("dustPdKeNiCu");
    dustPtCl.setUnlocalizedName("dustPtCl");
    dustPdNH.setUnlocalizedName("dustPdNH");
    dustRhSo4.setUnlocalizedName("dustRhSo4");
    dustOsIr.setUnlocalizedName("dustOsIr");
    dustIrO.setUnlocalizedName("dustIrO");
    dustKeNiCu.setUnlocalizedName("dustKeNiCu");
    dustNiCu.setUnlocalizedName("dustNiCu");

    //texture
    DustOsIrRhRu.setTextureName(HTGT6.MOD_ID+":DustOsIrRhRu");//
    dustPdKeNiCu.setTextureName(HTGT6.MOD_ID+":dustPdKeNiCu");//
    dustPtCl.setTextureName(HTGT6.MOD_ID+":dustPtCl"); //
    dustPdNH.setTextureName(HTGT6.MOD_ID+":dustPdNH"); //
    dustRhSo4.setTextureName(HTGT6.MOD_ID+":dustRhSo4");//
    dustOsIr.setTextureName(HTGT6.MOD_ID+":dustOsIr");//
    dustIrO.setTextureName(HTGT6.MOD_ID+":dustIrO");//
    dustKeNiCu.setTextureName(HTGT6.MOD_ID+":dustKeNiCu");//
    dustNiCu.setTextureName(HTGT6.MOD_ID+":dustNiCu"); //



}


    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}