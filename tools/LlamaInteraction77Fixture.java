import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class LlamaInteraction77Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        java.lang.reflect.Field info = World.class.getDeclaredField("worldInfo");
        info.setAccessible(true);
        info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        java.lang.reflect.Field providerWorld = net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");
        providerWorld.setAccessible(true); providerWorld.set(world.provider,world);
        Player player = new Player(world);
        EntityLlama112 llama = new EntityLlama112(world);
        tame(llama);
        llama.setHealth(llama.getMaxHealth()-2);
        player.inventory.setInventorySlotContents(0,new ItemStack(Items.wheat,2));
        llama.interact(player);
        check(!llama.isInLove(),"wheat must not trigger love");
        check(player.getHeldItem().stackSize==1,"server consumes once");
        player.inventory.setInventorySlotContents(0,new ItemStack(Blocks.hay_block,3));
        llama.setGrowingAge(100);
        llama.interact(player);
        check(!llama.isInLove(),"breeding cooldown");
        llama.setGrowingAge(0);
        llama.interact(player);
        check(llama.isInLove(),"tamed adult accepts hay for love");
        EntityTraderLlama120 mate = new EntityTraderLlama120(world);
        tame(mate); mate.func_146082_f(player);
        check(llama.canMateWith(mate)&&mate.canMateWith(llama),"compatible llama subclasses");
        check(!llama.canMateWith(llama),"self mating");
        mate.setHealth(mate.getMaxHealth()-1);
        check(!llama.canMateWith(mate),"injured parent");
        mate.setHealth(mate.getMaxHealth());
        mate.riddenByEntity=player;
        check(!llama.canMateWith(mate),"ridden parent");
        mate.riddenByEntity=null; mate.ridingEntity=player;
        check(!llama.canMateWith(mate),"passenger parent");
        mate.ridingEntity=null; mate.setGrowingAge(-100);
        check(!llama.canMateWith(mate),"baby parent");
        mate.setGrowingAge(0); mate.resetInLove();
        check(!llama.canMateWith(mate),"parent not in love");

        llama.resetInLove(); llama.setHealth(5);
        player.inventory.setInventorySlotContents(0,new ItemStack(Blocks.hay_block,2));
        world.isRemote=true;
        try { llama.interact(player); }
        finally { world.isRemote=false; }
        check(llama.getHealth()==5&&!llama.isInLove()&&player.getHeldItem().stackSize==2,"client mutated authoritative state");
        player.capabilities.isCreativeMode=true;
        llama.interact(player);
        check(player.getHeldItem().stackSize==2,"creative consumed food");
        System.out.println("PASS: wheat/hay, cooldown, mating guards, client authority and creative inventory");
    }
    private static void tame(EntityLlama112 llama) {
        NBTTagCompound tag=new NBTTagCompound(); llama.writeEntityToNBT(tag);
        tag.setBoolean("Tame",true); llama.readEntityFromNBT(tag);
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static final class Player extends EntityPlayer {
        Player(World world){super(world,new GameProfile(new UUID(77,77),"LlamaFixture"));}
        @Override public void addChatMessage(IChatComponent message){}
        @Override public boolean canCommandSenderUseCommand(int level,String command){return false;}
        @Override public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,0,0);}
    }
}
