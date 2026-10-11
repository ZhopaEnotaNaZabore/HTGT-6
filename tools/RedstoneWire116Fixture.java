import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneWire116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.Blocks;
public final class RedstoneWire116Fixture {
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 static void clear(Grid w){for(int x=-2;x<=2;x++)for(int y=79;y<=82;y++)for(int z=-2;z<=2;z++)w.setBlockToAir(x,y,z);w.setBlock(0,79,0,Blocks.stone,0,3);w.setBlock(0,80,0,Blocks.redstone_wire,15,3);}
 public static void main(String[] args)throws Exception{
  Grid w=B11605Fixture.initialize();w.loaded=true;RedstoneWire116.register();int[] dx={0,1,0,-1},dz={-1,0,1,0},query={3,4,2,5};
  for(int mask=0;mask<16;mask++){clear(w);for(int i=0;i<4;i++)if((mask&(1<<i))!=0)w.setBlock(dx[i],80,dz[i],Blocks.redstone_wire,14,3);int[] sides=RedstoneWire116.sides(w,0,80,0);boolean ns=(mask&5)!=0,ew=(mask&10)!=0;
   for(int i=0;i<4;i++){boolean expected=(mask&(1<<i))!=0||(i%2==0?!ew:!ns);check((sides[i]!=0)==expected,"modern dust connection mask");check(Blocks.redstone_wire.isProvidingWeakPower(w,0,80,0,query[i])==(expected?15:0),"native signal follows receiving connection, including corners and T junctions");}
   check(Blocks.redstone_wire.isProvidingWeakPower(w,0,80,0,0)==0&&Blocks.redstone_wire.isProvidingWeakPower(w,0,80,0,1)==15,"vertical signal rule");
  }
  clear(w);B11605Fixture.Player p=new B11605Fixture.Player(w);check(Blocks.redstone_wire.onBlockActivated(w,0,80,0,p,1,.5F,.5F,.5F)&&w.getBlock(0,80,0)==RedstoneWire116.dot&&w.getBlockMetadata(0,80,0)==15,"isolated cross toggles to persisted dot without changing power");
  for(int side=2;side<6;side++)check(RedstoneWire116.dot.isProvidingWeakPower(w,0,80,0,side)==0,"dot has no horizontal power");
  check(RedstoneWire116.dot.onBlockActivated(w,0,80,0,p,1,.5F,.5F,.5F)&&w.getBlock(0,80,0)==Blocks.redstone_wire,"dot toggles back to cross");
  p.capabilities.allowEdit=false;check(!Blocks.redstone_wire.onBlockActivated(w,0,80,0,p,1,.5F,.5F,.5F),"adventure mode disallows toggle");p.capabilities.allowEdit=true;
  w.setBlock(0,80,0,RedstoneWire116.dot,15,3);w.setBlock(1,80,0,Blocks.redstone_wire,14,3);RedstoneWire116.dot.onNeighborBlockChange(w,0,80,0,Blocks.redstone_wire);check(w.getBlock(0,80,0)==Blocks.redstone_wire,"dot reconnects when wiring is added");
  check(!Blocks.redstone_wire.onBlockActivated(w,0,80,0,p,1,.5F,.5F,.5F),"connected line cannot toggle to dot");
  clear(w);w.setBlock(1,80,0,Blocks.stone,0,3);w.setBlock(1,81,0,Blocks.redstone_wire,0,3);check(RedstoneWire116.sides(w,0,80,0)[1]==2,"wire climbs solid face");w.setBlock(0,81,0,Blocks.stone,0,3);check(RedstoneWire116.raw(w,0,80,0)[1]==0,"ceiling blocks upward connection");
  clear(w);w.setBlock(1,80,0,Blocks.unpowered_repeater,0,3);check(RedstoneWire116.raw(w,0,80,0)[1]==0,"repeater side does not connect");w.setBlockMetadataWithNotify(1,80,0,1,3);check(RedstoneWire116.raw(w,0,80,0)[1]!=0,"repeater axis connects");
  clear(w);net.minecraft.block.Block observer=com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.observer;w.setBlock(1,80,0,observer,5,3);check(RedstoneWire116.raw(w,0,80,0)[1]!=0,"observer output connects");w.setBlockMetadataWithNotify(1,80,0,4,3);check(RedstoneWire116.raw(w,0,80,0)[1]==0,"observer detecting face does not connect");
  clear(w);com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneShapeUpdates116.changed((net.minecraft.block.BlockRedstoneWire)Blocks.redstone_wire,w,0,80,0);int updates=w.blockUpdates;
  w.setBlockMetadataWithNotify(0,80,0,10,3);com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneShapeUpdates116.changed((net.minecraft.block.BlockRedstoneWire)Blocks.redstone_wire,w,0,80,0);check(w.blockUpdates==updates,"power-only changes do not trigger extra shape packets");
  w.setBlock(1,80,0,Blocks.redstone_wire,9,3);com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneShapeUpdates116.changed((net.minecraft.block.BlockRedstoneWire)Blocks.redstone_wire,w,0,80,0);check(w.blockUpdates==updates+1,"shape change invalidates receivers even with unchanged power");
  com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneShapeUpdates116.changed((net.minecraft.block.BlockRedstoneWire)Blocks.redstone_wire,w,0,80,0);check(w.blockUpdates==updates+1,"reentrant notification with same shape terminates");
  clear(w);w.setBlock(0,80,0,RedstoneWire116.dot,15,3);check(w.getStrongestIndirectPower(0,79,0)==15,"powered dot normally feeds supporting block");check(RedstoneWire116.externalPower(w,0,79,0)==0,"native wire calculation suppresses dot feedback too");check(RedstoneWire116.dot.canProvidePower(),"signal gate restored after query");w.setBlock(1,79,0,Blocks.redstone_block,0,3);check(RedstoneWire116.externalPower(w,0,79,0)==15,"real external power survives feedback gate");
  System.out.println("Redstone wire 116: "+checks+" checks passed");
 }
}
