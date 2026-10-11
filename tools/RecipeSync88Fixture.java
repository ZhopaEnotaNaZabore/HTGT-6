import com.mod.htgt6.minecraft1202.Module.Network.Inventory.PacketRecipeBookSync112;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import java.util.*;
import java.lang.reflect.*;
import io.netty.buffer.*;
import net.minecraft.item.ItemStack;
public final class RecipeSync88Fixture {
 static Field field(String name)throws Exception{Field f=PacketRecipeBookSync112.class.getDeclaredField(name);f.setAccessible(true);return f;}
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.initialize();
  List<String> keys=new ArrayList<String>();for(int i=0;i<70000;i++)keys.add("fixture:recipe_"+i);
  List<PacketRecipeBookSync112> packets=PacketRecipeBookSync112.batches(true,3,keys,null);
  int count=0,index=0;
  for(PacketRecipeBookSync112 packet:packets) {
   ByteBuf bytes=Unpooled.buffer();packet.toBytes(bytes);
   check(bytes.readableBytes()<=PacketRecipeBookSync112.MAX_BYTES,"byte bound");
   PacketRecipeBookSync112 decoded=new PacketRecipeBookSync112();decoded.fromBytes(bytes);check(bytes.readableBytes()==0,"packet fully decoded");bytes.release();
   check(field("replace").getBoolean(decoded)==(index==0),"replace only first");
   check(field("tutorialStep").getInt(decoded)==(index==0?3:-1),"tutorial only first");
   check(!field("notifications").getBoolean(decoded),"login suppresses all toasts");
   List<String> actual=(List<String>)field("keys").get(decoded);
   check(actual.size()<=128,"entry bound");
   for(String key:actual)check(keys.get(count++).equals(key),"order and completeness");
   index++;
  }
  check(count==70000,"more than unsigned short entries transferred");
  char[] chars=new char[2000];Arrays.fill(chars,'x');List<String> longKeys=new ArrayList<String>();
  for(int i=0;i<40;i++)longKeys.add(new String(chars)+i);
  List<PacketRecipeBookSync112> byteLimited=PacketRecipeBookSync112.batches(false,-1,longKeys,null);
  check(byteLimited.size()>1,"byte limit splits before count limit");
  for(PacketRecipeBookSync112 packet:byteLimited){ByteBuf bytes=Unpooled.buffer();packet.toBytes(bytes);check(bytes.readableBytes()<=24576,"long key byte bound");bytes.release();check(field("notifications").getBoolean(packet),"incremental notifications enabled");}
  List<PacketRecipeBookSync112> empty=PacketRecipeBookSync112.batches(true,0,Collections.<String>emptyList(),null);
  check(empty.size()==1&&field("replace").getBoolean(empty.get(0)),"empty snapshot clears old book");
  ItemStack oversized=new ItemStack(net.minecraft.init.Items.diamond);
  net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();byte[] random=new byte[40000];new Random(88).nextBytes(random);tag.setByteArray("payload",random);oversized.setTagCompound(tag);
  List<PacketRecipeBookSync112> large=PacketRecipeBookSync112.batches(false,-1,Collections.singletonList("large:recipe"),Collections.singletonList(oversized));
  check(((List)field("keys").get(large.get(0))).size()==1&&((List)field("outputs").get(large.get(0))).get(0)==null,"oversized preview omitted without dropping unlock");
  ByteBuf invalid=Unpooled.buffer();invalid.writeBoolean(false);invalid.writeByte(-1);invalid.writeBoolean(false);invalid.writeShort(129);
  try{new PacketRecipeBookSync112().fromBytes(invalid);throw new AssertionError("oversized count accepted");}catch(IllegalArgumentException expected){}finally{invalid.release();}
  System.out.println("PASS recipe sync: 70000 ordered keys, "+packets.size()+" packets, byte/count limits, silent login, incremental notifications, empty reset, invalid count");
 }
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
}
