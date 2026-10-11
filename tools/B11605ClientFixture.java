import com.mod.htgt6.minecraft1202.Module.Client.Modern.LodestoneCompassClient116;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneBinding116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
/** Real per-stack client selector, independent of an OpenGL drawing context. */
public final class B11605ClientFixture {
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);}
    public static void main(String[] args)throws Exception{
        BountifulMechanicsFixture120.Grid world=B11605Fixture.initialize();
        EntityItemFrame north=new EntityItemFrame(world),east=new EntityItemFrame(world);
        north.setPosition(0,80,0);east.setPosition(50,80,0);
        north.setDisplayedItem(LodestoneBinding116.boundCopy(new ItemStack(Items.compass),0,0,80,-50));
        east.setDisplayedItem(LodestoneBinding116.boundCopy(new ItemStack(Items.compass),0,100,80,0));
        north.getDisplayedItem().setItemFrame(null);east.getDisplayedItem().setItemFrame(null);
        LodestoneCompassClient116.register();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderItemInFrameEvent(north,null));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderItemInFrameEvent(east,null));
        check(north.getDisplayedItem().getItemFrame()==north&&east.getDisplayedItem().getItemFrame()==east,"client DataWatcher frame association");
        EntityItem dummy=new EntityItem(world,0,0,0,north.getDisplayedItem());
        float first=LodestoneCompassClient116.angle(north.getDisplayedItem(),dummy);
        float other=LodestoneCompassClient116.angle(east.getDisplayedItem(),dummy);
        check(Math.abs(first-other)>.1F,"frames use their own positions and targets, not dummy EntityItem(0,0,0)");
        for(int i=0;i<200;i++){
            world.time++;
            check(Math.abs(LodestoneCompassClient116.angle(north.getDisplayedItem(),dummy)-first)<.00001,"other frame cannot overwrite first target");
            check(Math.abs(LodestoneCompassClient116.angle(east.getDisplayedItem(),dummy)-other)<.00001,"independent second target");
        }
        north.setItemRotation(1);
        check(Math.abs(LodestoneCompassClient116.angle(north.getDisplayedItem(),dummy)-first)>.1F,"frame rotation accounted for");
        world.provider.dimensionId=-1;
        check(Float.isFinite(LodestoneCompassClient116.angle(north.getDisplayedItem(),dummy)),"wrong dimension spins safely");
        System.out.println("PASS B116-05 client: per-stack selection, independent frames, temporary render holder, rotation, dimension");
    }
}
