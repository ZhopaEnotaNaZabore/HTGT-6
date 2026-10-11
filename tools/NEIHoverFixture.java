import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.client.nei.*;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.*;
import net.minecraft.item.*;
import java.util.*;

/** Exercise NEI's real widget bounds and stack hit-testing without rendering. */
public final class NEIHoverFixture {
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        CrusherOreImportFixture.initializeGT6();
        check("0.01%".equals(NEIOutputChance.label(1)), "Rare chance rounded away");
        check("12.34%".equals(NEIOutputChance.label(1234)), "Fractional chance lost");
        check("50%".equals(NEIOutputChance.label(5000)), "Chance unit mismatch");
        check("0%".equals(NEIOutputChance.label(0)), "Zero chance mismatch");
        check("".equals(NEIOutputChance.label(10000)), "Guaranteed output should not have a chance label");
        Item item = new Item().setHasSubtypes(true);
        cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"nei_hover_fixture");
        ItemStack[] inputs = new ItemStack[9];
        for(int i=0;i<9;i++) inputs[i]=new ItemStack(item,i==8?0:i+1,i);
        ASrecipeHandler handler=new ASrecipeHandler();
        ASrecipeHandler.CachedAssemblerRecipe recipe=handler.new CachedAssemblerRecipe(
            new MachineRecipe(inputs,new ItemStack[]{new ItemStack(item,1,20)},null,null,1200,480,3));
        handler.arecipes.add(recipe);
        NEIRecipeWidget widget=new NEIRecipeWidget(RecipeHandlerRef.of(handler,0));
        widget.setLocation(30,40);
        check(widget.h>=134,"Card does not cover its footer");
        List<PositionedStack> stacks=new ArrayList<PositionedStack>(recipe.inputs);
        stacks.add(recipe.output); stacks.add(recipe.preview);
        int checked=0;
        for(PositionedStack stack:stacks) for(int dx:new int[]{0,8,15}) for(int dy:new int[]{0,8,15}) {
            int x=widget.x+stack.relx+dx,y=widget.y+stack.rely+dy;
            check(widget.contains(x,y),"Stack outside widget: "+stack.relx+","+stack.rely);
            PositionedStack hovered=widget.getPositionedStackMouseOver(x,y);
            check(hovered!=null && hovered.relx==stack.relx && hovered.rely==stack.rely,"Wrong NEI hover target");
            checked++;
        }
        String[] handlers={"ASrecipeHandler","CSrecipeHandler","EMNEIHandler","ChemicalPlantNEIHandler",
            "ElectronicFactoryNEIHandler","IndustrialMixerNEIHandler","FlotationChamberNEIHandler",
            "IndustrialCentrifugeNEIHandler","IndustrialCrusherNEIHandler","LineAssemblerNEIHandler",
            "SolidifierRecipeHandlerNEI","MetalFormerNEIHandler","GasTurbineRecipeHandler",
            "UniversalEngineRecipeHandler","MagicGeneratorRecipeHandler","RadioisotopeGeneratorRecipeHandler"};
        for(String name:handlers) check(MachineNEIRecipeHandler.class.isAssignableFrom(
            Class.forName("com.mod.htgt6.client.nei."+name)),"Handler misses shared bounds: "+name);
        // Layouts extending below the normal panel must enlarge the actual NEI widget too.
        recipe.inputs.add(new PositionedStack(new ItemStack(item,1,30),10,180));
        widget.update();
        check(widget.h>=198 && widget.contains(widget.x+18,widget.y+195),"Dynamic lower row clipped");
        System.out.println("NEI hover PASS: "+checked+" real widget hit points, all 9 assembler inputs, selector, result/preview, 16 machine handlers and tall recipe bounds");
    }
}
