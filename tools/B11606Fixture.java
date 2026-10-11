import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockRespawnAnchor116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.enchantment.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
public final class B11606Fixture {
    static int checks;
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
    static final class Player extends EntityPlayer {
        Player(World w){super(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"AnchorFixture"));}
        public void addChatMessage(IChatComponent c){}
        public boolean canCommandSenderUseCommand(int l,String c){return true;}
        public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,80,0);}
    }
    public static final class Victim extends net.minecraft.entity.Entity {
        float lastDamage;
        Victim(World w){super(w);setPosition(10.5,80.5,1.5);}
        protected void entityInit(){}
        protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){}
        protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){}
        public boolean attackEntityFrom(DamageSource source,float amount){lastDamage=amount;return true;}
    }
    public static final class WetBlock extends net.minecraft.block.Block implements com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 {
        WetBlock(){super(net.minecraft.block.material.Material.rock);}
        public boolean isWaterlogged(net.minecraft.world.IBlockAccess w,int x,int y,int z){return true;}
        public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){return false;}
    }
    public static final class BlastObserver {
        int affected=-1,entities=-1;
        @cpw.mods.fml.common.eventhandler.SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.LOWEST)
        public void blast(net.minecraftforge.event.world.ExplosionEvent.Detonate e){affected=e.getAffectedBlocks().size();entities=e.getAffectedEntities().size();}
    }
    public static void main(String[] args)throws Exception{
        Grid w=B11605Fixture.initialize();w.provider.dimensionId=-1;
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(EtFVanillaGapContent120.cryingObsidian,"backport_1_20_crying_obsidian");
        NetherUtilityContent116.register();NetherUtilityContent116.register();
        BlockRespawnAnchor116 anchor=NetherUtilityContent116.anchor;w.setBlock(0,80,0,anchor,0,3);Player p=new Player(w);p.dimension=-1;p.setPosition(.5,80,2);
        check(RespawnAnchor116.works(w),"Nether policy");w.provider.dimensionId=0;check(!RespawnAnchor116.works(w),"Overworld policy");w.provider.dimensionId=-1;
        for(int i=0;i<4;i++){check(anchor.charge(w,0,80,0),"charge "+i);check(anchor.getLightValue(w,0,80,0)==(i+1)*15/4,"light "+i);check(anchor.getComparatorInputOverride(w,0,80,0,0)==(i+1)*15/4,"comparator "+i);}
        check(!anchor.charge(w,0,80,0)&&w.getBlockMetadata(0,80,0)==4,"charge capped");
        w.setBlockMetadataWithNotify(0,80,0,0,3);w.rejectWrites=true;check(!anchor.charge(w,0,80,0),"rejected world write must not consume glowstone");w.rejectWrites=false;
        p.inventory.mainInventory[0]=new ItemStack(Blocks.glowstone,2);
        check(anchor.onBlockActivated(w,0,80,0,p,1,0,0,0),"native activate");check(p.getHeldItem().stackSize==1&&w.getBlockMetadata(0,80,0)==1,"one block consumed");
        p.capabilities.isCreativeMode=true;anchor.onBlockActivated(w,0,80,0,p,1,0,0,0);check(p.getHeldItem().stackSize==1&&w.getBlockMetadata(0,80,0)==2,"creative charge");p.capabilities.isCreativeMode=false;
        Player off=new Player(w);off.dimension=-1;off.inventory=new B11605Fixture.OffhandInventory(off);
        ((B11605Fixture.OffhandInventory)off.inventory).offhand[0]=new ItemStack(Blocks.glowstone);
        anchor.onBlockActivated(w,0,80,0,off,1,0,0,0);check(w.getBlockMetadata(0,80,0)==3&&((B11605Fixture.OffhandInventory)off.inventory).offhand[0]==null,"native offhand charge consumes correct slot");
        w.setBlock(0,80,1,Blocks.dispenser,2,3);
        net.minecraft.dispenser.IBehaviorDispenseItem dispenser=(net.minecraft.dispenser.IBehaviorDispenseItem)net.minecraft.block.BlockDispenser.dispenseBehaviorRegistry.getObject(Item.getItemFromBlock(Blocks.glowstone));
        ItemStack glow=new ItemStack(Blocks.glowstone,3);dispenser.dispense(new net.minecraft.block.BlockSourceImpl(w,0,80,1),glow);
        check(glow.stackSize==2&&w.getBlockMetadata(0,80,0)==4,"registered dispenser charges");
        dispenser.dispense(new net.minecraft.block.BlockSourceImpl(w,0,80,1),glow);check(glow.stackSize==1&&w.lastSpawn instanceof net.minecraft.entity.item.EntityItem&&w.getBlockMetadata(0,80,0)==4,"full anchor delegates default dispense");
        w.setBlockMetadataWithNotify(0,80,0,2,3);
        check(RespawnAnchor116.bind(p,0,80,0),"first bind");check(!RespawnAnchor116.bind(p,0,80,0),"same target idempotence");
        check(p.getBedLocation(-1).equals(new ChunkCoordinates(0,80,0)),"Forge dimensional spawn stored");
        p.setSpawnChunk(new ChunkCoordinates(30,80,0),false,0);check(!RespawnAnchor116.persisted(p).hasKey(RespawnAnchor116.TAG),"real ASM clears anchor on bed selection");RespawnAnchor116.bind(p,0,80,0);
        Player clone=new Player(w);clone.clonePlayer(p,false);check(RespawnAnchor116.persisted(clone).hasKey(RespawnAnchor116.TAG),"vanilla clone preserves target");
        w.setBlock(0,79,-1,Blocks.stone,0,3);Vec3 safe=RespawnAnchor116.findSafe(w,new ChunkCoordinates(0,80,0),p);
        check(safe!=null&&safe.xCoord==.5&&safe.yCoord==80&&safe.zCoord==-.5,"ordered north safe exit");
        w.collision=true;check(RespawnAnchor116.findSafe(w,new ChunkCoordinates(0,80,0),p)==null,"obstruction rejects all exits");w.collision=false;w.liquid=true;check(RespawnAnchor116.findSafe(w,new ChunkCoordinates(0,80,0),p)==null,"liquids rejected");w.liquid=false;
        w.setBlock(0,79,-1,Blocks.stone_slab,0,3);safe=RespawnAnchor116.findSafe(w,new ChunkCoordinates(0,80,0),p);check(safe!=null&&safe.yCoord==79.5,"slab floor preserved");
        // Charge is committed once, only on death; End return retains charge.
        Class<?> plan=Class.forName("com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RespawnAnchor116$Plan");Constructor<?> ctor=plan.getDeclaredConstructors()[0];ctor.setAccessible(true);
        Field plans=RespawnAnchor116.class.getDeclaredField("PLANS");plans.setAccessible(true);java.util.Map map=(java.util.Map)plans.get(null);
        Object unsafe;Field uf=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=uf.get(null);
        EntityPlayerMP old=(EntityPlayerMP)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,EntityPlayerMP.class);
        for(boolean end:new boolean[]{true,false}){map.put(old,ctor.newInstance(w,new ChunkCoordinates(0,80,0),safe));RespawnAnchor116.finish(null,old,end);check(w.getBlockMetadata(0,80,0)==(end?2:1),"commit charge end="+end);RespawnAnchor116.finish(null,old,end);check(w.getBlockMetadata(0,80,0)==(end?2:1),"no duplicate charge");}
        // Crucible cancellation, explicit teleport and Bukkit destination override must not consume charge.
        EntityPlayerMP next=(EntityPlayerMP)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,EntityPlayerMP.class);
        for(EntityPlayerMP cancelled:new EntityPlayerMP[]{null,old}){
            map.put(old,ctor.newInstance(w,new ChunkCoordinates(0,80,0),safe));
            RespawnAnchor116.finishHybrid(cancelled,old,false);
            check(w.getBlockMetadata(0,80,0)==1&&!map.containsKey(old),"hybrid cancelled transaction");
        }
        map.put(old,ctor.newInstance(w,new ChunkCoordinates(0,80,0),safe));
        check(RespawnAnchor116.prepareHybrid(old,7,false,new Object())==7&&!map.containsKey(old),"explicit Bukkit destination retained");
        map.put(old,ctor.newInstance(w,new ChunkCoordinates(0,80,0),safe));
        check(RespawnAnchor116.prepareHybrid(old,0,true,null)==0&&!map.containsKey(old),"End portal transfer retained");
        map.put(old,ctor.newInstance(w,new ChunkCoordinates(0,80,0),safe));
        check(RespawnAnchor116.hybridCoordinate(123,old,0)==safe.xCoord&&RespawnAnchor116.hybridCoordinate(123,old,1)==safe.yCoord&&RespawnAnchor116.hybridCoordinate(123,old,2)==safe.zCoord,"Bukkit proposed position preserves safe slab exit");
        next.worldObj=w;next.posX=123;next.posY=safe.yCoord;next.posZ=safe.zCoord;
        RespawnAnchor116.finishHybrid(next,old,false);
        check(w.getBlockMetadata(0,80,0)==1&&!map.containsKey(old),"plugin redirect does not consume charge");
        check(RespawnAnchor116.hybridCoordinate(123,old,1)==123,"ordinary Bukkit position unchanged");
        map.put(old,ctor.newInstance(w,new ChunkCoordinates(0,80,0),safe));
        next.worldObj=null;next.posX=safe.xCoord;
        RespawnAnchor116.finishHybrid(next,old,false);
        check(w.getBlockMetadata(0,80,0)==1&&!map.containsKey(old),"plugin world redirect does not consume charge");
        map.put(old,ctor.newInstance(w,new ChunkCoordinates(0,80,0),safe));next.worldObj=w;
        RespawnAnchor116.finishHybrid(next,old,false);RespawnAnchor116.finishHybrid(next,old,false);
        check(w.getBlockMetadata(0,80,0)==0&&!map.containsKey(old),"hybrid success consumes exactly one charge");
        net.minecraft.inventory.InventoryCrafting craft=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer a){return true;}},3,3);
        for(int i=0;i<9;i++)craft.setInventorySlotContents(i,new ItemStack(i>=3&&i<6?Blocks.glowstone:EtFVanillaGapContent120.cryingObsidian));
        ItemStack result=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(craft,w);check(result!=null&&result.getItem()==Item.getItemFromBlock(anchor),"survival recipe");
        Enchantment soul=SoulSpeed116.enchantment;check(soul.getMaxLevel()==3&&soul.getMinEnchantability(3)==30&&soul.getMaxEnchantability(3)==45,"level and cost");
        check(!soul.canApplyAtEnchantingTable(new ItemStack(Items.diamond_boots))&&!soul.isAllowedOnBooks(),"excluded from table and librarian/mansion pool");check(soul.canApply(new ItemStack(Items.diamond_boots))&&!soul.canApply(new ItemStack(Items.diamond_helmet)),"boots only");
        check(Items.enchanted_book.func_92110_g(SoulSpeed116.book(3)).tagCount()==1,"loot integration book");
        for(int level=1;level<=3;level++){
            net.minecraft.inventory.ContainerRepair anvil=new net.minecraft.inventory.ContainerRepair(p.inventory,w,0,80,0,p);
            anvil.getSlot(0).putStack(new ItemStack(Items.diamond_boots));anvil.getSlot(1).putStack(SoulSpeed116.book(level));
            ItemStack enchanted=anvil.getSlot(2).getStack();check(enchanted!=null&&EnchantmentHelper.getEnchantmentLevel(soul.effectId,enchanted)==level,"real anvil book application "+level);
            anvil.getSlot(0).putStack(new ItemStack(Items.diamond_helmet));check(anvil.getSlot(2).getStack()==null,"anvil rejects non-boots "+level);
        }
        w.setBlock(0,79,2,Blocks.soul_sand,0,3);p.yOffset=0;p.setPosition(.5,79.875,2.5);p.onGround=true;
        double base=p.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getAttributeValue();
        for(int level=1;level<=3;level++){
            ItemStack boots=new ItemStack(Items.diamond_boots);boots.addEnchantment(soul,level);p.setCurrentItemOrArmor(1,boots);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent(p));
            check(Math.abs(p.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getAttributeValue()-base-SoulSpeed116.boost(level))<1E-7,"registered speed event "+level);
            int wear=boots.getItemDamage();for(int tick=0;tick<50;tick++)SoulSpeed116.tick(p);check(boots.getItemDamage()==wear,"standing still does not repeatedly wear "+level);
            p.motionX=1;p.motionZ=1;Blocks.soul_sand.onEntityCollidedWithBlock(w,0,79,2,p);check(p.motionX==1&&p.motionZ==1,"real soul sand ASM bypass "+level);
        }
        p.setCurrentItemOrArmor(1,null);SoulSpeed116.tick(p);check(p.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getModifier(SoulSpeed116.MODIFIER)==null,"unequipping removes boost");
        p.motionX=1;p.motionZ=1;Blocks.soul_sand.onEntityCollidedWithBlock(w,0,79,2,p);check(p.motionX==.4&&p.motionZ==.4,"unenchanted vanilla drag preserved");
        ItemStack soilBoots=new ItemStack(Items.diamond_boots);soilBoots.addEnchantment(soul,2);p.setCurrentItemOrArmor(1,soilBoots);
        w.setBlock(0,79,2,EtFVanillaGapContent120.soulSoil,0,3);SoulSpeed116.tick(p);check(p.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getModifier(SoulSpeed116.MODIFIER)!=null,"existing own soul soil participates");
        p.capabilities.isFlying=true;SoulSpeed116.tick(p);check(p.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getModifier(SoulSpeed116.MODIFIER)==null,"creative flight removes modifier");p.capabilities.isFlying=false;
        check(!AnchorExplosion116.waterProtected(w,10,80,0),"dry anchor");
        w.setBlock(11,80,0,Blocks.flowing_water,7,3);check(!AnchorExplosion116.waterProtected(w,10,80,0),"thin flowing water does not protect");
        w.setBlockMetadataWithNotify(11,80,0,6,3);check(AnchorExplosion116.waterProtected(w,10,80,0),"flow amount two protects");
        w.setBlock(11,81,0,Blocks.water,0,3);check(!AnchorExplosion116.waterProtected(w,10,80,0),"flowing column does not protect");
        w.setBlockMetadataWithNotify(11,80,0,0,3);check(AnchorExplosion116.waterProtected(w,10,80,0),"side source protects");
        w.setBlock(12,80,0,new WetBlock(),0,3);check(AnchorExplosion116.waterProtected(w,13,80,0),"waterlogged neighbor is a source");
        w.setBlock(15,81,0,new WetBlock(),0,3);check(AnchorExplosion116.waterProtected(w,15,80,0),"waterlogged block above protects");
        BlastObserver observer=new BlastObserver();net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(observer);
        Victim victim=new Victim(w);w.loadedEntityList.add(victim);
        w.setBlock(10,80,0,anchor,1,3);AnchorExplosion116.explode(w,10,80,0);
        check(victim.lastDamage==60,"real anchor explosion uses modern factor seven, not legacy eight");
        net.minecraft.world.Explosion unrelated=new net.minecraft.world.Explosion(w,null,10.5,80.5,.5,5);AnchorExplosion116.damage(victim,DamageSource.generic,37,unrelated);check(victim.lastDamage==37,"other explosions preserve their damage");
        check(w.getBlock(10,80,0)==Blocks.air&&observer.affected==0,"real Forge explosion removes anchor but protects blocks");
        check(w.getBlock(11,80,0)==Blocks.flowing_water,"water never replaced by temporary block");
        Class.forName("net.minecraft.server.management.ServerConfigurationManager").getDeclaredMethods();
        verifyAsm("net/minecraft/server/management/ServerConfigurationManager",false);verifyAsm("net/minecraft/entity/player/EntityPlayer",true);
        System.out.println("PASS B116-06/07: "+checks+" mechanics/registration/ASM checks");
    }
    static void verifyAsm(String path,boolean player)throws Exception{
        java.io.InputStream in=B11606Fixture.class.getClassLoader().getResourceAsStream(path+".class");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);in.close();
        byte[] once=com.mod.htgt6.asm.RespawnAnchorASM116.transform(out.toByteArray(),player);check(java.util.Arrays.equals(once,com.mod.htgt6.asm.RespawnAnchorASM116.transform(once,player)),"idempotent ASM "+path);
        ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);int hooks=0;
        for(MethodNode m:node.methods){boolean hooked=false;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.endsWith("/RespawnAnchor116")){hooks++;hooked=true;}if(hooked)new Analyzer(new BasicVerifier()).analyze(node.name,m);}
        check(hooks==(player?1:6),"all ASM decisions hooked "+path);
    }
}
