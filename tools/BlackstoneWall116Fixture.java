import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import java.util.*;
public final class BlackstoneWall116Fixture {
    static int checks;
    static void check(boolean b,String text){if(!b)throw new AssertionError(text);checks++;}
    static void clear(Grid w){for(int x=-2;x<=2;x++)for(int y=79;y<=84;y++)for(int z=-2;z<=2;z++)w.setBlock(x,y,z,Blocks.air,0,3);}
    static boolean occupied(List<AxisAlignedBB> boxes,double x,double y,double z){for(AxisAlignedBB b:boxes)if(x>b.minX&&x<b.maxX&&y>b.minY&&y<b.maxY&&z>b.minZ&&z<b.maxZ)return true;return false;}
    public static void run(Grid w)throws Exception {
        int[] dx={0,1,0,-1},dz={-1,0,1,0};
        for(BlackstoneShapes116.Family f:BlackstoneShapes116.FAMILIES) {
            BlockBlackstoneWall116 wall=f.wall;
            check(Item.getItemFromBlock(wall)!=null,"wall registered");
            check(wall.getExplosionResistance(null)==6F,"wall resistance is not legacy BlockWall multiplier");
            check(wall.getBlockHardness(w,0,80,0)==f.source.getBlockHardness(w,0,80,0),"wall source hardness");
            net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;}},3,3);
            for(int i=0;i<6;i++)grid.setInventorySlotContents(i,new ItemStack(f.source));
            ItemStack output=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(grid,w);
            check(output!=null&&output.getItem()==Item.getItemFromBlock(wall)&&output.stackSize==6,"wall crafting");
            List<ItemStack> cuts=new ArrayList<ItemStack>();BlackstoneShapes116.appendStonecutting(new ItemStack(f.source),cuts);
            boolean found=false;for(ItemStack cut:cuts)if(cut.getItem()==Item.getItemFromBlock(wall)&&cut.stackSize==1)found=true;check(found,"wall stonecutting");
            for(int mask=0;mask<16;mask++)for(int cover=0;cover<4;cover++)for(int wet=0;wet<2;wet++) {
                clear(w);w.setBlock(0,80,0,wall,wet,3);
                for(int i=0;i<4;i++)if((mask&(1<<i))!=0)w.setBlock(dx[i],80,dz[i],Blocks.stone,0,3);
                if(cover==1)w.setBlock(0,81,0,Blocks.stone,0,3);
                if(cover>=2)w.setBlock(0,81,0,Blocks.stone_slab,cover==2?0:8,3);
                boolean tall=cover==1||cover==2,n=(mask&1)!=0,e=(mask&2)!=0,s=(mask&4)!=0,west=(mask&8)!=0;
                boolean up=mask==0||n!=s||e!=west||(!(tall&&(n&&s||e&&west))&&tall);
                BlockBlackstoneWall116.State state=wall.state(w,0,80,0);
                check(state.up==up,"post mask="+mask+" cover="+cover);
                for(int i=0;i<4;i++)check(state.side[i]==((mask&(1<<i))==0?0:tall?2:1),"side state");
                check(wall.getLightOpacity(w,0,80,0)==wet,"light opacity");
                check(wall.damageDropped(wet)==0,"water bit not dropped");
                List<AxisAlignedBB> collision=new ArrayList<AxisAlignedBB>();wall.addCollisionBoxesToList(w,0,80,0,AxisAlignedBB.getBoundingBox(-1,79,-1,2,82,2),collision,null);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
                    boolean hit=up&&x>=4&&x<12&&z>=4&&z<12||n&&x>=5&&x<11&&z<11||e&&x>=5&&z>=5&&z<11||s&&x>=5&&x<11&&z>=5||west&&x<11&&z>=5&&z<11;
                    check(occupied(collision,(x+.5)/16,81.4,(z+.5)/16)==hit,"collision union footprint");
                }
                check(!occupied(collision,.5,81.51,.5),"collision stops at 1.5 blocks");
            }
            clear(w);w.setBlock(0,80,0,wall,0,3);
            for(int i=0;i<4;i++)for(int meta=0;meta<8;meta++) {
                w.setBlock(dx[i],80,dz[i],Blocks.fence_gate,meta,3);
                check(wall.connects(w,0,80,0,i)==((meta&1)==(i%2==0?1:0)),"fence gate axis, independent of open bit");
                w.setBlock(dx[i],80,dz[i],Blocks.air,0,3);
            }
            w.setBlock(0,80,-1,Blocks.glass,0,3);check(wall.connects(w,0,80,0,0),"full glass face connects");
            w.setBlock(0,80,-1,Blocks.farmland,0,3);check(!wall.connects(w,0,80,0,0),"partial farmland side rejected");
            w.setBlock(0,80,-1,Blocks.pumpkin,0,3);check(!wall.connects(w,0,80,0,0),"pumpkin exception");
            w.setBlock(0,80,-1,Blocks.leaves,0,3);check(!wall.connects(w,0,80,0,0),"leaves exception");
            for(int direction=0;direction<4;direction++)for(int meta=0;meta<8;meta++)for(int neighbour=0;neighbour<5;neighbour++) {
                clear(w);w.setBlock(0,80,0,wall,0,3);int nx=dx[direction],nz=dz[direction];w.setBlock(nx,80,nz,f.stairs,meta,3);
                if(neighbour<4)w.setBlock(nx+dx[neighbour],80,nz+dz[neighbour],f.stairs,(meta&4)|((meta+1)&3),3);
                List<AxisAlignedBB> stairBoxes=new ArrayList<AxisAlignedBB>();f.stairs.addCollisionBoxesToList(w,nx,80,nz,AxisAlignedBB.getBoundingBox(-3,79,-3,3,82,3),stairBoxes,null);
                boolean solid=true;
                for(int across=0;across<16;across++)for(int height=0;height<16;height++) {
                    double a=(across+.5)/16D,h=80+(height+.5)/16D;
                    double px=nx+(direction==1?.0001:direction==3?.9999:a),pz=nz+(direction==0?.9999:direction==2?.0001:a);
                    solid&=occupied(stairBoxes,px,h,pz);
                }
                check(wall.connects(w,0,80,0,direction)==solid,"stair side agrees with actual corner collision");
            }
            clear(w);w.setBlock(0,80,0,wall,0,3);
            w.setBlock(0,80,-1,Blocks.glass_pane,0,3);check(wall.state(w,0,80,0).side[0]==1,"pane connection");
            w.setBlock(0,80,1,Blocks.cobblestone_wall,0,3);check(!wall.state(w,0,80,0).up,"mixed straight wall");
            w.setBlock(0,81,0,Blocks.torch,5,3);check(wall.state(w,0,80,0).up,"torch forces post");
            w.setBlock(0,81,0,Blocks.torch,1,3);check(!wall.state(w,0,80,0).up,"wall-mounted torch is not standing-torch override");
            w.setBlock(0,81,0,Blocks.air,0,3);
            check(wall.collisionRayTrace(w,0,80,0,Vec3.createVectorHelper(.05,80.5,-1),Vec3.createVectorHelper(.05,80.5,2))==null,"ray misses empty corner");
            check(wall.collisionRayTrace(w,0,80,0,Vec3.createVectorHelper(.5,80.9,-1),Vec3.createVectorHelper(.5,80.9,2))==null,"ray above low arm");
            for(int meta=0;meta<8;meta++) {
                w.setBlock(0,81,0,Blocks.stone_stairs,meta,3);
                BlockBlackstoneWall116.State state=wall.state(w,0,80,0);
                if((meta&4)==0)check(state.side[0]==2&&state.side[2]==2&&!state.up,"bottom stair covers bottom face fully");
                else check(!(state.side[0]==2&&state.side[2]==2),"top stair does not cover both opposing probes");
            }
            clear(w);w.setBlock(0,80,0,wall,0,3);w.setBlock(0,81,0,wall,0,3);check(wall.state(w,0,80,0).up,"stacked post");
            w.setBlock(0,81,0,Blocks.cobblestone_wall,0,3);
            for(int i=0;i<4;i++){w.setBlock(dx[i],80,dz[i],Blocks.stone,0,3);w.setBlock(dx[i],81,dz[i],Blocks.stone,0,3);}
            check(!wall.state(w,0,80,0).up,"modern native cross above continues tall arms without legacy post");
            for(int layers=0;layers<8;layers++) {
                w.setBlock(0,81,0,Blocks.snow_layer,layers,3);BlockBlackstoneWall116.State snowy=wall.state(w,0,80,0);
                check(snowy.side[0]==(layers==0?1:2),"snow collision bottom coverage");
            }
            clear(w);w.setBlock(0,80,0,wall,0,3);
            B11608Fixture.Player player=new B11608Fixture.Player(w);player.setPosition(5,80,5);player.inventory.mainInventory[0]=new ItemStack(Items.water_bucket);
            check(wall.onBlockActivated(w,0,80,0,player,1,0,0,0)&&wall.isWaterlogged(w,0,80,0)&&player.getHeldItem().getItem()==Items.bucket,"wall bucket fill");
            check(wall.getExplosionResistance(null,w,0,80,0,0,80,0)==100F,"wet wall blast resistance");
            player.setSneaking(true);w.rayHit=new MovingObjectPosition(0,80,0,1,Vec3.createVectorHelper(.5,81,.5));
            ItemStack bucket=Items.bucket.onItemRightClick(player.getHeldItem(),w,player);check(bucket.getItem()==Items.water_bucket&&!wall.isWaterlogged(w,0,80,0),"sneaking bucket drain");w.rayHit=null;
            wall.setWaterlogged(w,0,80,0,true);check(wall.removedByPlayer(w,player,0,80,0,true)&&w.getBlock(0,80,0)==Blocks.water,"breaking restores water");
        }
        List<AxisAlignedBB> union=Arrays.asList(AxisAlignedBB.getBoundingBox(0,0,0,.5,1,1),AxisAlignedBB.getBoundingBox(.5,0,0,1,1,1));
        check(BlockBlackstoneWall116.covers(union,0,0,16,16),"union of partial rectangles covers full face");
        check(!BlockBlackstoneWall116.covers(Arrays.asList(AxisAlignedBB.getBoundingBox(0,0,0,.499,1,1),union.get(1)),0,0,16,16),"fractional gap cannot be filled by bounding envelope");
        clear(w);w.blockUpdates=0;
        BlockBlackstoneWall116 wall=BlackstoneShapes116.BLACKSTONE.wall;
        for(int y=80;y<=82;y++)w.setBlock(0,y,0,wall,0,3);
        wall.onNeighborBlockChange(w,0,82,0,Blocks.stone);check(w.blockUpdates==3,"upper changes invalidate complete wall column");
        for(Block owned:new Block[]{Backport120Content.cobbledDeepslateWall,Backport120Content.polishedDeepslateWall,Backport120Content.deepslateBrickWall,Backport120Content.deepslateTileWall,Backport120Content.mudBrickWall,ModernStoneShapes120.FAMILIES[0].wall,ModernStoneShapes120.FAMILIES[2].wall,ModernStoneShapes120.FAMILIES[4].wall}){
            check(owned instanceof BlockBlackstoneWall116,"all owned wall families use modern geometry");BlockBlackstoneWall116 modern=(BlockBlackstoneWall116)owned;
            for(int mask=0;mask<16;mask++)for(int cover=0;cover<3;cover++){
                clear(w);w.setBlock(0,80,0,owned,0,3);for(int i=0;i<4;i++)if((mask&(1<<i))!=0)w.setBlock(dx[i],80,dz[i],Blocks.stone,0,3);if(cover>0)w.setBlock(0,81,0,Blocks.stone_slab,cover==1?0:8,3);
                String actual=modern.state(w,0,80,0).variant();w.setBlock(0,80,0,wall,0,3);check(actual.equals(wall.state(w,0,80,0).variant()),"same wall topology for every family");
            }
        }
        System.out.println("Blackstone walls: "+checks+" checks passed");
    }
}
