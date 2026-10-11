import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.Random;

public final class LlamaTaming78Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        Field collisions=World.class.getDeclaredField("collidingBoundingBoxes");collisions.setAccessible(true);collisions.set(world,new java.util.ArrayList());
        Field info=World.class.getDeclaredField("worldInfo"); info.setAccessible(true);
        info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
        Class<?> type=Class.forName("LlamaInteraction77Fixture$Player");
        Constructor<?> ctor=type.getDeclaredConstructor(World.class);ctor.setAccessible(true);
        EntityPlayer player=(EntityPlayer)ctor.newInstance(world);
        EntityLlama112 llama=new EntityLlama112(world);
        player.inventory.setInventorySlotContents(0,new ItemStack(Items.wheat,20));
        llama.interact(player);
        check(!llama.isTamed()&&llama.getTemper()==3,"food instantly tames");
        for(int i=0;i<9;i++)llama.interact(player);
        check(llama.getTemper()==30&&player.getHeldItem().stackSize==10,"temper cap");
        llama.interact(player);
        check(player.getHeldItem().stackSize==10,"useless food consumed");
        llama.setGrowingAge(-2000);llama.interact(player);
        check(llama.getGrowingAge()==-1800,"wheat growth seconds to ticks");
        llama.setGrowingAge(0);
        player.inventory.setInventorySlotContents(0,null);
        check(llama.interact(player)&&player.ridingEntity==llama,"wild mounting");
        Field random=Entity.class.getDeclaredField("rand");random.setAccessible(true);
        random.set(llama,new Random(){@Override public int nextInt(int n){return 0;}});
        EntityAIBase goal=null;
        for(Object value:llama.tasks.taskEntries){EntityAIBase action=((EntityAITasks.EntityAITaskEntry)value).action;if(action.getClass().getSimpleName().equals("TamingAI"))goal=action;}
        check(goal!=null,"missing taming task");goal.updateTask();
        check(llama.isTamed()&&player.ridingEntity==llama&&world.lastEntityState==7,"successful riding tame");
        NBTTagCompound saved=new NBTTagCompound();llama.writeEntityToNBT(saved);
        check(saved.getString("OwnerUUID").equals(player.getUniqueID().toString()),"owner persistence");
        EntityLlama112 restored=new EntityLlama112(world);restored.readEntityFromNBT(saved);
        check(restored.isTamed()&&restored.getTemper()==30,"tame NBT");
        player.mountEntity(null);
        saved.setBoolean("Tame",false);saved.setInteger("Temper",0);llama.readEntityFromNBT(saved);
        llama.interact(player);goal.updateTask();
        check(!llama.isTamed()&&llama.getTemper()==5&&player.ridingEntity==null&&llama.riddenByEntity==null&&world.lastEntityState==6,"failed attempt must buck and raise temper");
        System.out.println("PASS: food temper/cap, useful consumption, baby growth, mounting, success/failure and owner NBT");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
