import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.*;
public final class B11608Fixture {
    static int checks;
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
    static final class Player extends EntityPlayer {
        boolean deny;
        Player(World w){super(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ShapesFixture"));}
        public void addChatMessage(IChatComponent c){}
        public boolean canCommandSenderUseCommand(int l,String c){return true;}
        public ChunkCoordinates getPlayerCoordinates(){return new ChunkCoordinates(0,80,0);}
        @Override public boolean canPlayerEdit(int x,int y,int z,int side,ItemStack stack){return !deny;}
    }
    public static final class CancelBucket {
        @cpw.mods.fml.common.eventhandler.SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST)
        public void cancel(net.minecraftforge.event.entity.player.FillBucketEvent event){event.setCanceled(true);}
    }
    static String boxes(Block stairs,Grid w) {
        List<AxisAlignedBB> list=new ArrayList<AxisAlignedBB>();stairs.addCollisionBoxesToList(w,0,80,0,AxisAlignedBB.getBoundingBox(-1,79,-1,2,82,2),list,null);
        StringBuilder s=new StringBuilder();for(AxisAlignedBB box:list)s.append(box.toString());return s.toString();
    }
    static double volume(Block block,Grid w){List<AxisAlignedBB> list=new ArrayList<AxisAlignedBB>();block.addCollisionBoxesToList(w,0,80,0,AxisAlignedBB.getBoundingBox(0,80,0,1,81,1),list,null);double sum=0;for(AxisAlignedBB a:list)sum+=(a.maxX-a.minX)*(a.maxY-a.minY)*(a.maxZ-a.minZ);return sum;}
    public static void main(String[] args)throws Exception {
        Grid w=B11605Fixture.initialize();final String[] sound={null};
        Object listener=java.lang.reflect.Proxy.newProxyInstance(B11608Fixture.class.getClassLoader(),new Class[]{net.minecraft.world.IWorldAccess.class},(obj,method,values)->{if(method.getName().equals("playSound"))sound[0]=(String)values[0];return null;});
        B11605Fixture.field(World.class,w,"worldAccesses",java.util.Collections.singletonList(listener));Player p=new Player(w);p.setPosition(5,80,5);
        for(BlackstoneShapes116.Family f:BlackstoneShapes116.FAMILIES)GameRegistry.registerBlock(f.source,"source_"+f.name);
        GameRegistry.registerBlock(EtFVanillaGapContent120.chiseledPolishedBlackstone,"chiseled_polished");
        BlackstoneShapes116.Family polished=BlackstoneShapes116.POLISHED;
        GameRegistry.registerBlock(polished.slab,ItemBlackstoneSlab116.class,"backport_1_20_polished_blackstone_slab",polished.slab,polished.doubleSlab,Boolean.FALSE);
        GameRegistry.registerBlock(polished.doubleSlab,ItemBlockBackport120Technical.class,"backport_1_20_double_polished_blackstone_slab");
        BlackstoneShapes116.register();int recipes=net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList().size();BlackstoneShapes116.register();check(recipes==net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList().size(),"registration idempotent");
        check(polished.slab==EtFVanillaGapContent120.polishedBlackstoneSlab,"old slab identity retained");
        for(BlackstoneShapes116.Family f:BlackstoneShapes116.FAMILIES) {
            check(Item.getItemFromBlock(f.stairs)!=null&&Item.getItemFromBlock(f.slab)!=null,"registered "+f.name);
            for(int m=0;m<16;m++)check(f.slab.damageDropped(m)==0&&f.stairs.damageDropped(m)==0,"no water/axis item metadata");
            check(f.doubleSlab.quantityDropped(w.rand)==2&&f.doubleSlab.getItemDropped(0,w.rand,0)==Item.getItemFromBlock(f.slab),"double drops two halves");
            check(f.doubleSlab.getCreativeTabToDisplayOn()==null,"technical double hidden");
            check(f.slab.getBlockHardness(w,0,80,0)==2F&&f.doubleSlab.getBlockHardness(w,0,80,0)==2F,"official hardness of all blackstone slabs");
            IWaterloggedBlock150 wet=(IWaterloggedBlock150)f.slab;
            w.setBlock(0,80,0,f.slab,0,3);check(f.slab.getLightOpacity(w,0,80,0)==0,"dry slab transmits sky light");
            wet.setWaterlogged(w,0,80,0,true);check(f.slab.getLightOpacity(w,0,80,0)==1,"wet slab light attenuation");
            w.setBlock(0,80,0,f.doubleSlab,0,3);check(f.doubleSlab.getLightOpacity(w,0,80,0)==255,"full block remains opaque");
            net.minecraft.inventory.InventoryCrafting craft=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
            for(int slot:new int[]{0,3,4,6,7,8})craft.setInventorySlotContents(slot,new ItemStack(f.source));
            ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(craft,w);
            check(crafted!=null&&crafted.getItem()==Item.getItemFromBlock(f.stairs)&&crafted.stackSize==4,"stairs survival recipe");
            if(f!=polished){for(int i=0;i<9;i++)craft.setInventorySlotContents(i,i<3?new ItemStack(f.source):null);crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(craft,w);check(crafted!=null&&crafted.getItem()==Item.getItemFromBlock(f.slab)&&crafted.stackSize==6,"slab survival recipe");}

            for(boolean top:new boolean[]{false,true}) {
                w.setBlock(0,80,0,Blocks.water,0,3);int m=f.slab.onBlockPlaced(w,0,80,0,top?0:1,.5F,.5F,.5F,0);
                check(m==(top?12:4),"water placement half");w.setBlock(0,80,0,f.slab,m,3);
                ItemStack stack=new ItemStack(f.slab,2);p.inventory.mainInventory[0]=stack;
                stack.getItem().onItemUse(stack,p,w,0,80,0,top?0:1,.5F,.5F,.5F);
                check(w.getBlock(0,80,0)==f.doubleSlab&&w.getBlockMetadata(0,80,0)==0&&stack.stackSize==1,"wet merge atomic "+top);
            }
            w.setBlock(0,80,0,f.slab,4,3);ItemStack stack=new ItemStack(f.slab,2);w.rejectWrites=true;
            stack.getItem().onItemUse(stack,p,w,0,80,0,1,.5F,.5F,.5F);check(stack.stackSize==2&&w.getBlock(0,80,0)==f.slab,"failed merge does not consume");w.rejectWrites=false;
            p.deny=true;stack.getItem().onItemUse(stack,p,w,0,80,0,1,.5F,.5F,.5F);check(stack.stackSize==2&&wet.isWaterlogged(w,0,80,0),"permission rejection preserves water");p.deny=false;
            p.capabilities.isCreativeMode=true;stack.getItem().onItemUse(stack,p,w,0,80,0,1,.5F,.5F,.5F);check(stack.stackSize==2&&w.getBlock(0,80,0)==f.doubleSlab,"creative merge");p.capabilities.isCreativeMode=false;
            check(!((IWaterloggedBlock150)f.doubleSlab).setWaterlogged(w,0,80,0,true),"double cannot fill");
            w.setBlock(0,80,0,f.slab,0,3);p.inventory.mainInventory[0]=new ItemStack(Items.water_bucket);
            check(f.slab.onBlockActivated(w,0,80,0,p,1,0,0,0)&&wet.isWaterlogged(w,0,80,0)&&p.getHeldItem().getItem()==Items.bucket,"bucket fills single");check("htgt6:item.bucket.empty".equals(sound[0]),"cloud pour sound");
            check(f.slab.onBlockActivated(w,0,80,0,p,1,0,0,0)&&!wet.isWaterlogged(w,0,80,0)&&p.getHeldItem().getItem()==Items.water_bucket,"bucket drains single");check("htgt6:item.bucket.fill".equals(sound[0]),"cloud pickup sound");
            wet.setWaterlogged(w,0,80,0,true);f.slab.removedByPlayer(w,p,0,80,0,true);check(w.getBlock(0,80,0)==Blocks.water,"harvest keeps water");
            for(int yaw=0;yaw<4;yaw++)for(int side:new int[]{0,1}) {
                w.setBlock(0,80,0,Blocks.water,0,3);int m=f.stairs.onBlockPlaced(w,0,80,0,side,.5F,.5F,.5F,0);w.setBlock(0,80,0,f.stairs,m,3);p.rotationYaw=yaw*90;
                f.stairs.onBlockPlacedBy(w,0,80,0,p,new ItemStack(f.stairs));
                check(w.getBlockMetadata(0,80,0)==(8|(side==0?4:0)|new int[]{2,1,3,0}[yaw]),"stairs retain water during orientation");
            }
            // Real transformed vanilla corner predicate: compare all orientations and mixed wet/dry neighbors.
            for(Block actor:new Block[]{f.stairs,Blocks.stone_stairs})for(int center=0;center<8;center++)for(int east=0;east<8;east++)for(int south=0;south<8;south++) {
                w.setBlock(0,80,0,actor,center,3);w.setBlock(1,80,0,f.stairs,east,3);w.setBlock(0,80,1,f.stairs,south,3);
                String dry=boxes(actor,w);
                w.setBlock(0,80,0,actor,center|(actor==f.stairs?8:0),3);w.setBlockMetadataWithNotify(1,80,0,east|8,3);
                check(dry.equals(boxes(actor,w)),"wet bit cannot alter corner collision");
            }
            w.setBlockToAir(1,80,0);w.setBlockToAir(0,80,1);
            w.setBlock(0,80,0,f.stairs,0,3);
            List<AxisAlignedBB> bb=new ArrayList<AxisAlignedBB>();f.stairs.addCollisionBoxesToList(w,0,80,0,AxisAlignedBB.getBoundingBox(0,80,0,1,81,1),bb,null);double volume=0;for(AxisAlignedBB a:bb)volume+=(a.maxX-a.minX)*(a.maxY-a.minY)*(a.maxZ-a.minZ);check(volume==.75,"straight stair volume");
            check(f.stairs.collisionRayTrace(w,0,80,0,Vec3.createVectorHelper(.25,80.75,-1),Vec3.createVectorHelper(.25,80.75,2))==null,"ray passes through empty stair quarter");
            check(f.stairs.collisionRayTrace(w,0,80,0,Vec3.createVectorHelper(.75,80.75,-1),Vec3.createVectorHelper(.75,80.75,2))!=null,"ray hits solid stair quarter");
            w.setBlock(1,80,0,f.stairs,3|8,3);
            check(volume(f.stairs,w)==.625,"outer corner volume");
            check(f.stairs.collisionRayTrace(w,0,80,0,Vec3.createVectorHelper(-1,80.75,.75),Vec3.createVectorHelper(2,80.75,.75))==null,"outer empty corner has no phantom hit");
            w.setBlockToAir(1,80,0);w.setBlock(-1,80,0,f.stairs,3|8,3);
            check(volume(f.stairs,w)==.875,"inner corner volume");w.setBlockToAir(-1,80,0);
            w.setBlock(0,80,0,f.slab,0,3);w.provider.isHellWorld=true;p.inventory.mainInventory[0]=new ItemStack(Items.water_bucket);
            f.slab.onBlockActivated(w,0,80,0,p,1,0,0,0);check(!wet.isWaterlogged(w,0,80,0)&&p.getHeldItem().getItem()==Items.bucket,"ultrawarm bucket evaporates");w.provider.isHellWorld=false;
            for(Block shape:new Block[]{f.slab,f.stairs}) {
                w.setBlock(0,80,0,shape,0,3);
                check(shape.getExplosionResistance(null,w,0,80,0,0,80,0)==6F,"dry explosion resistance");
                ((IWaterloggedBlock150)shape).setWaterlogged(w,0,80,0,true);
                check(shape.getExplosionResistance(null,w,0,80,0,0,80,0)==100F,"water protects from explosion");
                check(p.func_145772_a(new net.minecraft.world.Explosion(w,p,0,80,0,4),w,0,80,0,shape)==100F,"entity explosion uses coordinate-aware resistance");
            }
            w.setBlock(0,80,0,f.slab,4,3);w.rayHit=new MovingObjectPosition(0,80,0,1,Vec3.createVectorHelper(.5,80.5,.5));p.setSneaking(true);
            ItemStack bucket=Items.bucket.onItemRightClick(new ItemStack(Items.bucket),w,p);
            check(bucket.getItem()==Items.water_bucket&&bucket.stackSize==1&&!wet.isWaterlogged(w,0,80,0),"native sneaking bucket drains once");
            bucket=Items.water_bucket.onItemRightClick(bucket,w,p);
            check(bucket.getItem()==Items.bucket&&bucket.stackSize==1&&wet.isWaterlogged(w,0,80,0),"native sneaking bucket fills once");
            CancelBucket cancel=new CancelBucket();net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(cancel);
            bucket=Items.bucket.onItemRightClick(bucket,w,p);
            check(bucket.getItem()==Items.bucket&&wet.isWaterlogged(w,0,80,0),"canceled bucket event leaves block untouched");net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(cancel);
            p.deny=true;bucket=Items.bucket.onItemRightClick(bucket,w,p);check(bucket.getItem()==Items.bucket&&wet.isWaterlogged(w,0,80,0),"native bucket edit permission");p.deny=false;p.setSneaking(false);w.rayHit=null;
            List<ItemStack> cuts=new ArrayList<ItemStack>();BlackstoneShapes116.appendStonecutting(new ItemStack(f.source),cuts);
            int expected=f==BlackstoneShapes116.BLACKSTONE?12:f==polished?8:3;
            check(cuts.size()==expected,"stonecut closure count "+f.name);for(ItemStack cut:cuts)check(cut.getItem()!=null,"all stonecut outputs registered");
            BlackstoneShapes116.appendStonecutting(new ItemStack(f.source),cuts);check(cuts.size()==expected,"no duplicate choices");
        }
        java.io.InputStream in=net.minecraft.block.BlockStairs.class.getResourceAsStream("/net/minecraft/block/BlockStairs.class");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);in.close();
        byte[] once=com.mod.htgt6.asm.BlackstoneStairsASM116.transform(out.toByteArray());check(Arrays.equals(once,com.mod.htgt6.asm.BlackstoneStairsASM116.transform(once)),"ASM idempotent");
        BlackstoneWall116Fixture.run(w);
        System.out.println("Blackstone shapes: "+checks+" checks passed");
    }
}
