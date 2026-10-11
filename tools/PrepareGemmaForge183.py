"""Prepare a small, compiler-checked Forge 1.7.10 adaptation pilot; no model training."""
import hashlib, json, pathlib, re, subprocess, zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
OUT = ROOT / 'build/gemma183'
OUT.mkdir(parents=True, exist_ok=True)
SRC = pathlib.Path.home() / '.gradle/caches/minecraft/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forgeSrc-1.7.10-10.13.4.1614-1.7.10-sources.jar'
CP = (ROOT / 'build/sounds179/classpath.txt').read_text().strip()
JAVAC = r'C:\Program Files\BellSoft\LibericaJDK-8\bin\javac.exe'
sources = ['net/minecraft/block/Block.java', 'net/minecraft/block/BlockContainer.java',
           'net/minecraft/inventory/IInventory.java', 'net/minecraft/inventory/Container.java',
           'net/minecraft/nbt/NBTTagCompound.java', 'cpw/mods/fml/common/registry/GameRegistry.java']
records = []
with zipfile.ZipFile(SRC) as z:
    for name in sources:
        data = z.read(name)
        target = OUT / 'reference' / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        records.append({'path': name, 'sha256': hashlib.sha256(data).hexdigest()})
(OUT / 'source-manifest.json').write_text(json.dumps(records, indent=2), encoding='utf8')

# Held-out cases use different operations, not just renamed copies of training classes.
cases = [
('train','NamedBlock','Create NamedBlock extending Block: iron, name pilot_iron, texture pilot:iron, hardness 3.',
 'import net.minecraft.block.Block; import net.minecraft.block.material.Material; public final class NamedBlock extends Block { public NamedBlock(){super(Material.iron);setBlockName("pilot_iron");setBlockTextureName("pilot:iron");setHardness(3F);} }'),
('train','RegistryExample','Create RegistryExample.register(Block block): register under pilot_block using default ItemBlock.',
 'import net.minecraft.block.Block; import cpw.mods.fml.common.registry.GameRegistry; public final class RegistryExample { public static void register(Block block){GameRegistry.registerBlock(block,"pilot_block");} }'),
('train','FactoryBlock','Create FactoryBlock extending BlockContainer, iron, create a new vanilla TileEntityChest for each createNewTileEntity call.',
 'import net.minecraft.block.BlockContainer; import net.minecraft.block.material.Material; import net.minecraft.tileentity.TileEntity; import net.minecraft.tileentity.TileEntityChest; import net.minecraft.world.World; public final class FactoryBlock extends BlockContainer { public FactoryBlock(){super(Material.iron);} @Override public TileEntity createNewTileEntity(World world,int meta){return new TileEntityChest();} }'),
('train','CounterTile','Create CounterTile extending TileEntity. Persist public int count under Count, calling super in both NBT methods.',
 'import net.minecraft.tileentity.TileEntity; import net.minecraft.nbt.NBTTagCompound; public final class CounterTile extends TileEntity { public int count; @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setInteger("Count",count);} @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);count=n.getInteger("Count");} }'),
('train','OpenInventory','Create OpenInventory.open(IInventory inventory), using the 1.7.10 openInventory API.',
 'import net.minecraft.inventory.IInventory; public final class OpenInventory { public static void open(IInventory inventory){inventory.openInventory();} }'),
('train','EmptyContainer','Create EmptyContainer extending Container with no slots, canInteractWith returns true. This is only an API stub, not a usable storage inventory.',
 'import net.minecraft.inventory.Container; import net.minecraft.entity.player.EntityPlayer; public final class EmptyContainer extends Container { @Override public boolean canInteractWith(EntityPlayer player){return true;} }'),
('eval','CloseInventory','Create CloseInventory.close(IInventory inventory), calling the correct inventory-close API.',
 'import net.minecraft.inventory.IInventory; public final class CloseInventory { public static void close(IInventory inventory){inventory.closeInventory();} }'),
('eval','StackNbt','Create StackNbt.save(ItemStack stack) returning a new NBTTagCompound containing the stack, and load(NBTTagCompound n) returning the decoded ItemStack.',
 'import net.minecraft.item.ItemStack; import net.minecraft.nbt.NBTTagCompound; public final class StackNbt { public static NBTTagCompound save(ItemStack stack){NBTTagCompound n=new NBTTagCompound();stack.writeToNBT(n);return n;} public static ItemStack load(NBTTagCompound n){return ItemStack.loadItemStackFromNBT(n);} }'),
]
outputs = {'train': [], 'eval': []}
java = OUT / 'reference-java'; java.mkdir(exist_ok=True)
for split,name,task,code in cases:
    (java / (name+'.java')).write_text(code+'\n', encoding='utf8')
    outputs[split].append({'id':name,'messages':[
        {'role':'system','content':'Write Java 8 code for Minecraft 1.7.10 Forge 10.13.4.1614. Use its API only. Return a complete Java class, no prose or placeholders.'},
        {'role':'user','content':task}, {'role':'assistant','content':code}]})
classes=OUT/'reference-classes';classes.mkdir(exist_ok=True)
cmd=[JAVAC,'-encoding','UTF-8','-source','8','-target','8','-proc:none','-cp',CP,'-d',str(classes)]+[str(p) for p in java.glob('*.java')]
r=subprocess.run(cmd,capture_output=True,text=True)
(OUT/'javac.log').write_text(r.stdout+r.stderr,encoding='utf8')
if r.returncode: raise SystemExit('Reference compilation failed: see javac.log')
for split,rows in outputs.items():
    (OUT/(split+'.jsonl')).write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in rows),encoding='utf8')
print('Compiled all 8 reference classes: 6 training examples, 2 held-out evaluation examples. No model weights changed.')
