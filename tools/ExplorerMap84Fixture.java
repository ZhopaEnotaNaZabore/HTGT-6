import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemExplorerMap112;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import java.lang.reflect.*;
import java.util.Arrays;

public final class ExplorerMap84Fixture {
    public static void main(String[] args) throws Exception {
        World world = BountifulMechanicsFixture120.initialize();
        Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);
        info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        Field providerWorld=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");
        providerWorld.setAccessible(true);providerWorld.set(world.provider,world);
        Constructor<?> constructor = Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);
        constructor.setAccessible(true);
        EntityPlayer player = (EntityPlayer)constructor.newInstance(world);
        Method mark = ItemExplorerMap112.class.getDeclaredMethod("mark", MapData.class, ItemStack.class);
        mark.setAccessible(true);
        ItemExplorerMap112 item = new ItemExplorerMap112();
        for (String kind : new String[]{"mansion", "monument"}) {
            ItemStack stack = new ItemStack(item, 1, 153);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Decorations", kind);
            tag.setInteger("TargetX", -240); tag.setInteger("TargetZ", 140);
            stack.setTagCompound(tag);
            MapData data = new MapData("map_153");
            data.xCenter = -256; data.zCenter = 128; data.scale = 2;
            new java.util.Random(19).nextBytes(data.colors);
            byte[] before = data.colors.clone();
            for (int tick=0; tick<100; tick++) mark.invoke(null, data, stack);
            check(Arrays.equals(before, data.colors), "terrain must remain byte exact");
            check(stack.getItemDamage()==153 && data.xCenter==-256 && data.zCenter==128, "old map identity and center");
            check(kind.equals(tag.getString("TargetKind")) && tag.hasKey("Decorations",9), "legacy NBT migration");
            check(tag.getTagList("Decorations",10).tagCount()==1 && data.playersVisibleOnMap.size()==1, "no duplicate markers");
            MapData.MapCoord marker=(MapData.MapCoord)data.playersVisibleOnMap.values().iterator().next();
            check(marker.centerX==8 && marker.centerZ==6 && marker.iconSize==7, "scaled marker position");
            MapData client=new MapData("map_153");
            data.func_82568_a(player);
            boolean received=false;
            for(int packet=0;packet<20;packet++) {
                byte[] bytes=data.getUpdatePacketData(stack,world,player);
                if(bytes!=null && bytes[0]==1) {
                    client.updateMPMapData(bytes);
                    MapData.MapCoord remote=(MapData.MapCoord)client.playersVisibleOnMap.values().iterator().next();
                    check(remote.centerX==8 && remote.centerZ==6 && remote.iconSize==7,"vanilla map packet round trip");
                    received=true; break;
                }
            }
            check(received,"marker packet emitted");
            tag.setInteger("TargetX",100000); tag.setInteger("TargetZ",-100000);
            mark.invoke(null,data,stack);
            marker=(MapData.MapCoord)data.playersVisibleOnMap.values().iterator().next();
            check(marker.centerX==127 && marker.centerZ==-128,"off-map clamp");
        }
        MapData empty=new MapData("map_0");
        ItemStack malformed=new ItemStack(item);malformed.setTagCompound(new NBTTagCompound());
        mark.invoke(null,empty,malformed);
        check(empty.playersVisibleOnMap.isEmpty(),"missing target must not point to origin");
        check(item.create(null,"mansion",0,0)==null,"no world creation");
        world.provider.worldChunkMgr=new net.minecraft.world.biome.WorldChunkManagerHell(
                net.minecraft.world.biome.BiomeGenBase.ocean,0.5F);
        MapData preview=new MapData("preview");preview.scale=2;
        com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120.renderBiomePreview(world,preview);
        int painted=0;
        for(byte color:preview.colors)if(color!=0)painted++;
        check(painted>0 && painted<16384,"biome preview without loading terrain");
        System.out.println("PASS explorer maps: migration, identity, terrain, idempotence, coordinates, vanilla marker packet, malformed NBT");
    }
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
}
