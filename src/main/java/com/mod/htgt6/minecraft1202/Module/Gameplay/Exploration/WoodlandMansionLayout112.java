package com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Exact pure-Java port of the Minecraft 1.12 WoodlandMansionPieces layout stage.
 * It has no World or NBT dependencies and is deliberately not wired to generation.
 */
public final class WoodlandMansionLayout112 {
    public static List<Piece> generate(long seed) {
        return generate(new Random(seed), Rotation.NONE);
    }

    /** Preserves the caller-owned Random state used by the 1.12 structure start. */
    public static List<Piece> generate(Random random, Rotation initialRotation) {
        if (random == null || initialRotation == null) throw new IllegalArgumentException("random/rotation");
        Grid grid = new Grid(random);
        Placer placer = new Placer(null, random);
        List<Piece> pieces = new ArrayList<Piece>();
        placer.createMansion(new BlockPos(0, 0, 0), initialRotation, pieces, grid);
        return Collections.unmodifiableList(new ArrayList<Piece>(pieces));
    }

    public static long fingerprint(long seed) {
        long hash = 0xcbf29ce484222325L;
        for (Piece piece : generate(seed)) {
            hash = mix(hash, piece.name);
            hash = mix(hash, piece.x); hash = mix(hash, piece.y); hash = mix(hash, piece.z);
            hash = mix(hash, piece.rotation.ordinal()); hash = mix(hash, piece.mirror.ordinal());
        }
        return hash;
    }

    private static long mix(long hash, String value) {
        for (int i = 0; i < value.length(); i++) hash = mix(hash, value.charAt(i));
        return mix(hash, 0xff);
    }
    private static long mix(long hash, int value) {
        hash ^= value & 255; hash *= 0x100000001b3L;
        hash ^= value >>> 8 & 255; hash *= 0x100000001b3L;
        hash ^= value >>> 16 & 255; hash *= 0x100000001b3L;
        hash ^= value >>> 24 & 255; return hash * 0x100000001b3L;
    }

    public static class Piece {
        public final String name;
        public final int x, y, z;
        public final Rotation rotation;
        public final Mirror mirror;
        Piece(String name, int x, int y, int z, Rotation rotation, Mirror mirror) {
            this.name = name; this.x = x; this.y = y; this.z = z;
            this.rotation = rotation; this.mirror = mirror;
        }
        @Override public String toString() {
            return name + "@" + x + "," + y + "," + z + ":" + rotation + ":" + mirror;
        }
    }

    static final class MansionTemplate extends Piece {
        MansionTemplate(TemplateManager ignored, String name, BlockPos pos, Rotation rotation) {
            this(ignored, name, pos, rotation, Mirror.NONE);
        }
        MansionTemplate(TemplateManager ignored, String name, BlockPos pos, Rotation rotation, Mirror mirror) {
            super(name, pos.x, pos.y, pos.z, rotation, mirror);
        }
    }
    static final class TemplateManager { }

    public enum Mirror { NONE, LEFT_RIGHT, FRONT_BACK }
    public enum Rotation {
        NONE(0), CLOCKWISE_90(1), CLOCKWISE_180(2), COUNTERCLOCKWISE_90(3);
        public final int quarters;
        Rotation(int quarters) { this.quarters = quarters; }
        Rotation add(Rotation other) { return from(quarters + other.quarters); }
        EnumFacing rotate(EnumFacing facing) {
            if (facing == EnumFacing.UP) return facing;
            EnumFacing result = facing;
            for (int i = 0; i < quarters; i++) result = result.rotateY();
            return result;
        }
        static Rotation from(int quarters) {
            switch (quarters & 3) {
                case 1: return CLOCKWISE_90;
                case 2: return CLOCKWISE_180;
                case 3: return COUNTERCLOCKWISE_90;
                default: return NONE;
            }
        }
    }

    enum EnumFacing {
        NORTH(0, -1), SOUTH(0, 1), WEST(-1, 0), EAST(1, 0), UP(0, 0);
        final int x, z;
        EnumFacing(int x, int z) { this.x = x; this.z = z; }
        int getFrontOffsetX() { return x; }
        int getFrontOffsetZ() { return z; }
        EnumFacing getOpposite() {
            switch (this) { case NORTH:return SOUTH; case SOUTH:return NORTH;
                case WEST:return EAST; case EAST:return WEST; default:return UP; }
        }
        EnumFacing rotateY() {
            switch (this) { case NORTH:return EAST; case EAST:return SOUTH;
                case SOUTH:return WEST; case WEST:return NORTH; default:throw new IllegalStateException(); }
        }
        EnumFacing rotateYCCW() { return rotateY().getOpposite(); }
        static EnumFacing getHorizontal(int index) {
            EnumFacing[] horizontal = {SOUTH, WEST, NORTH, EAST};
            return horizontal[Math.abs(index % horizontal.length)];
        }
        static final class Plane {
            static final Plane HORIZONTAL = new Plane();
            EnumFacing[] facings() { return new EnumFacing[]{NORTH, EAST, SOUTH, WEST}; }
        }
    }
    static final class Plane { static final EnumFacing.Plane HORIZONTAL = EnumFacing.Plane.HORIZONTAL; }

    static final class BlockPos {
        final int x, y, z;
        BlockPos(int x, int y, int z) { this.x=x; this.y=y; this.z=z; }
        int getX(){return x;} int getZ(){return z;}
        BlockPos add(int dx,int dy,int dz){return new BlockPos(x+dx,y+dy,z+dz);}
        BlockPos up(){return up(1);} BlockPos up(int amount){return new BlockPos(x,y+amount,z);}
        BlockPos offset(EnumFacing facing){return offset(facing,1);}
        BlockPos offset(EnumFacing facing,int amount){return new BlockPos(x+facing.x*amount,y,z+facing.z*amount);}
        BlockPos rotate(Rotation rotation) {
            switch(rotation){case CLOCKWISE_90:return new BlockPos(-z,y,x);
                case CLOCKWISE_180:return new BlockPos(-x,y,-z);
                case COUNTERCLOCKWISE_90:return new BlockPos(z,y,-x);default:return this;}
        }
    }

    static final class Template {
        static BlockPos getZeroPositionWithTransform(BlockPos pos, Mirror mirror, Rotation rotation,
                int width, int depth) {
            int maxX=width-1,maxZ=depth-1;
            int mirrorX=mirror==Mirror.FRONT_BACK?maxX:0;
            int mirrorZ=mirror==Mirror.LEFT_RIGHT?maxZ:0;
            switch(rotation){
                case COUNTERCLOCKWISE_90:return pos.add(mirrorZ,0,maxX-mirrorX);
                case CLOCKWISE_90:return pos.add(maxZ-mirrorZ,0,mirrorX);
                case CLOCKWISE_180:return pos.add(maxX-mirrorX,0,maxZ-mirrorZ);
                default:return pos.add(mirrorX,0,mirrorZ);
            }
        }
    }

    static final class Lists {
        static <T> ArrayList<T> newArrayList(){return new ArrayList<T>();}
    }
    static final class Tuple<A, B> {
        private final A first; private final B second;
        Tuple(A first, B second){this.first=first;this.second=second;}
        A getFirst(){return first;} B getSecond(){return second;}
    }
   static class FirstFloor extends WoodlandMansionLayout112.RoomCollection {

      private FirstFloor() {
         super(null);
      }

      public String get1x1(Random p_191104_1_) {
         return "1x1_a" + (p_191104_1_.nextInt(5) + 1);
      }

      public String get1x1Secret(Random p_191099_1_) {
         return "1x1_as" + (p_191099_1_.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(Random p_191100_1_, boolean p_191100_2_) {
         return "1x2_a" + (p_191100_1_.nextInt(9) + 1);
      }

      public String get1x2FrontEntrance(Random p_191098_1_, boolean p_191098_2_) {
         return "1x2_b" + (p_191098_1_.nextInt(5) + 1);
      }

      public String get1x2Secret(Random p_191102_1_) {
         return "1x2_s" + (p_191102_1_.nextInt(2) + 1);
      }

      public String get2x2(Random p_191101_1_) {
         return "2x2_a" + (p_191101_1_.nextInt(4) + 1);
      }

      public String get2x2Secret(Random p_191103_1_) {
         return "2x2_s1";
      }

      // $FF: synthetic method
      FirstFloor(Object p_i47364_1_) {
         this();
      }
   }

   static class Grid {

      private final Random random;
      private final WoodlandMansionLayout112.SimpleGrid baseGrid;
      private final WoodlandMansionLayout112.SimpleGrid thirdFloorGrid;
      private final WoodlandMansionLayout112.SimpleGrid[] floorRooms;
      private final int entranceX;
      private final int entranceY;


      public Grid(Random p_i47362_1_) {
         this.random = p_i47362_1_;
         boolean lvt_2_1_ = true;
         this.entranceX = 7;
         this.entranceY = 4;
         this.baseGrid = new WoodlandMansionLayout112.SimpleGrid(11, 11, 5);
         this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
         this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
         this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
         this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
         this.baseGrid.set(0, 0, 11, 1, 5);
         this.baseGrid.set(0, 9, 11, 11, 5);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, EnumFacing.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, EnumFacing.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, EnumFacing.WEST, 3);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, EnumFacing.WEST, 3);

         while(this.cleanEdges(this.baseGrid)) {
            ;
         }

         this.floorRooms = new WoodlandMansionLayout112.SimpleGrid[3];
         this.floorRooms[0] = new WoodlandMansionLayout112.SimpleGrid(11, 11, 5);
         this.floorRooms[1] = new WoodlandMansionLayout112.SimpleGrid(11, 11, 5);
         this.floorRooms[2] = new WoodlandMansionLayout112.SimpleGrid(11, 11, 5);
         this.identifyRooms(this.baseGrid, this.floorRooms[0]);
         this.identifyRooms(this.baseGrid, this.floorRooms[1]);
         this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.thirdFloorGrid = new WoodlandMansionLayout112.SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
         this.setupThirdFloor();
         this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
      }

      public static boolean isHouse(WoodlandMansionLayout112.SimpleGrid p_191109_0_, int p_191109_1_, int p_191109_2_) {
         int lvt_3_1_ = p_191109_0_.get(p_191109_1_, p_191109_2_);
         return lvt_3_1_ == 1 || lvt_3_1_ == 2 || lvt_3_1_ == 3 || lvt_3_1_ == 4;
      }

      public boolean isRoomId(WoodlandMansionLayout112.SimpleGrid p_191114_1_, int p_191114_2_, int p_191114_3_, int p_191114_4_, int p_191114_5_) {
         return (this.floorRooms[p_191114_4_].get(p_191114_2_, p_191114_3_) & '\uffff') == p_191114_5_;
      }

      public EnumFacing get1x2RoomDirection(WoodlandMansionLayout112.SimpleGrid p_191113_1_, int p_191113_2_, int p_191113_3_, int p_191113_4_, int p_191113_5_) {
         EnumFacing[] var6 = Plane.HORIZONTAL.facings();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            EnumFacing lvt_9_1_ = var6[var8];
            if(this.isRoomId(p_191113_1_, p_191113_2_ + lvt_9_1_.getFrontOffsetX(), p_191113_3_ + lvt_9_1_.getFrontOffsetZ(), p_191113_4_, p_191113_5_)) {
               return lvt_9_1_;
            }
         }

         return null;
      }

      private void recursiveCorridor(WoodlandMansionLayout112.SimpleGrid p_191110_1_, int p_191110_2_, int p_191110_3_, EnumFacing p_191110_4_, int p_191110_5_) {
         if(p_191110_5_ > 0) {
            p_191110_1_.set(p_191110_2_, p_191110_3_, 1);
            p_191110_1_.setIf(p_191110_2_ + p_191110_4_.getFrontOffsetX(), p_191110_3_ + p_191110_4_.getFrontOffsetZ(), 0, 1);

            EnumFacing lvt_7_2_;
            for(int lvt_6_2_ = 0; lvt_6_2_ < 8; ++lvt_6_2_) {
               lvt_7_2_ = EnumFacing.getHorizontal(this.random.nextInt(4));
               if(lvt_7_2_ != p_191110_4_.getOpposite() && (lvt_7_2_ != EnumFacing.EAST || !this.random.nextBoolean())) {
                  int lvt_8_1_ = p_191110_2_ + p_191110_4_.getFrontOffsetX();
                  int lvt_9_1_ = p_191110_3_ + p_191110_4_.getFrontOffsetZ();
                  if(p_191110_1_.get(lvt_8_1_ + lvt_7_2_.getFrontOffsetX(), lvt_9_1_ + lvt_7_2_.getFrontOffsetZ()) == 0 && p_191110_1_.get(lvt_8_1_ + lvt_7_2_.getFrontOffsetX() * 2, lvt_9_1_ + lvt_7_2_.getFrontOffsetZ() * 2) == 0) {
                     this.recursiveCorridor(p_191110_1_, p_191110_2_ + p_191110_4_.getFrontOffsetX() + lvt_7_2_.getFrontOffsetX(), p_191110_3_ + p_191110_4_.getFrontOffsetZ() + lvt_7_2_.getFrontOffsetZ(), lvt_7_2_, p_191110_5_ - 1);
                     break;
                  }
               }
            }

            EnumFacing var10 = p_191110_4_.rotateY();
            lvt_7_2_ = p_191110_4_.rotateYCCW();
            p_191110_1_.setIf(p_191110_2_ + var10.getFrontOffsetX(), p_191110_3_ + var10.getFrontOffsetZ(), 0, 2);
            p_191110_1_.setIf(p_191110_2_ + lvt_7_2_.getFrontOffsetX(), p_191110_3_ + lvt_7_2_.getFrontOffsetZ(), 0, 2);
            p_191110_1_.setIf(p_191110_2_ + p_191110_4_.getFrontOffsetX() + var10.getFrontOffsetX(), p_191110_3_ + p_191110_4_.getFrontOffsetZ() + var10.getFrontOffsetZ(), 0, 2);
            p_191110_1_.setIf(p_191110_2_ + p_191110_4_.getFrontOffsetX() + lvt_7_2_.getFrontOffsetX(), p_191110_3_ + p_191110_4_.getFrontOffsetZ() + lvt_7_2_.getFrontOffsetZ(), 0, 2);
            p_191110_1_.setIf(p_191110_2_ + p_191110_4_.getFrontOffsetX() * 2, p_191110_3_ + p_191110_4_.getFrontOffsetZ() * 2, 0, 2);
            p_191110_1_.setIf(p_191110_2_ + var10.getFrontOffsetX() * 2, p_191110_3_ + var10.getFrontOffsetZ() * 2, 0, 2);
            p_191110_1_.setIf(p_191110_2_ + lvt_7_2_.getFrontOffsetX() * 2, p_191110_3_ + lvt_7_2_.getFrontOffsetZ() * 2, 0, 2);
         }
      }

      private boolean cleanEdges(WoodlandMansionLayout112.SimpleGrid p_191111_1_) {
         boolean lvt_2_1_ = false;

         for(int lvt_3_1_ = 0; lvt_3_1_ < p_191111_1_.height; ++lvt_3_1_) {
            for(int lvt_4_1_ = 0; lvt_4_1_ < p_191111_1_.width; ++lvt_4_1_) {
               if(p_191111_1_.get(lvt_4_1_, lvt_3_1_) == 0) {
                  byte lvt_5_1_ = 0;
                  int var7 = lvt_5_1_ + (isHouse(p_191111_1_, lvt_4_1_ + 1, lvt_3_1_)?1:0);
                  var7 += isHouse(p_191111_1_, lvt_4_1_ - 1, lvt_3_1_)?1:0;
                  var7 += isHouse(p_191111_1_, lvt_4_1_, lvt_3_1_ + 1)?1:0;
                  var7 += isHouse(p_191111_1_, lvt_4_1_, lvt_3_1_ - 1)?1:0;
                  if(var7 >= 3) {
                     p_191111_1_.set(lvt_4_1_, lvt_3_1_, 2);
                     lvt_2_1_ = true;
                  } else if(var7 == 2) {
                     byte lvt_6_1_ = 0;
                     int var8 = lvt_6_1_ + (isHouse(p_191111_1_, lvt_4_1_ + 1, lvt_3_1_ + 1)?1:0);
                     var8 += isHouse(p_191111_1_, lvt_4_1_ - 1, lvt_3_1_ + 1)?1:0;
                     var8 += isHouse(p_191111_1_, lvt_4_1_ + 1, lvt_3_1_ - 1)?1:0;
                     var8 += isHouse(p_191111_1_, lvt_4_1_ - 1, lvt_3_1_ - 1)?1:0;
                     if(var8 <= 1) {
                        p_191111_1_.set(lvt_4_1_, lvt_3_1_, 2);
                        lvt_2_1_ = true;
                     }
                  }
               }
            }
         }

         return lvt_2_1_;
      }

      private void setupThirdFloor() {
         ArrayList lvt_1_1_ = Lists.newArrayList();
         WoodlandMansionLayout112.SimpleGrid lvt_2_1_ = this.floorRooms[1];

         int lvt_4_2_;
         int lvt_6_2_;
         for(int lvt_3_2_ = 0; lvt_3_2_ < this.thirdFloorGrid.height; ++lvt_3_2_) {
            for(lvt_4_2_ = 0; lvt_4_2_ < this.thirdFloorGrid.width; ++lvt_4_2_) {
               int lvt_5_2_ = lvt_2_1_.get(lvt_4_2_, lvt_3_2_);
               lvt_6_2_ = lvt_5_2_ & 983040;
               if(lvt_6_2_ == 131072 && (lvt_5_2_ & 2097152) == 2097152) {
                  lvt_1_1_.add(new Tuple(Integer.valueOf(lvt_4_2_), Integer.valueOf(lvt_3_2_)));
               }
            }
         }

         if(lvt_1_1_.isEmpty()) {
            this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
         } else {
            Tuple var13 = (Tuple)lvt_1_1_.get(this.random.nextInt(lvt_1_1_.size()));
            lvt_4_2_ = lvt_2_1_.get(((Integer)var13.getFirst()).intValue(), ((Integer)var13.getSecond()).intValue());
            lvt_2_1_.set(((Integer)var13.getFirst()).intValue(), ((Integer)var13.getSecond()).intValue(), lvt_4_2_ | 4194304);
            EnumFacing var14 = this.get1x2RoomDirection(this.baseGrid, ((Integer)var13.getFirst()).intValue(), ((Integer)var13.getSecond()).intValue(), 1, lvt_4_2_ & '\uffff');
            lvt_6_2_ = ((Integer)var13.getFirst()).intValue() + var14.getFrontOffsetX();
            int lvt_7_1_ = ((Integer)var13.getSecond()).intValue() + var14.getFrontOffsetZ();

            for(int lvt_8_2_ = 0; lvt_8_2_ < this.thirdFloorGrid.height; ++lvt_8_2_) {
               for(int lvt_9_2_ = 0; lvt_9_2_ < this.thirdFloorGrid.width; ++lvt_9_2_) {
                  if(!isHouse(this.baseGrid, lvt_9_2_, lvt_8_2_)) {
                     this.thirdFloorGrid.set(lvt_9_2_, lvt_8_2_, 5);
                  } else if(lvt_9_2_ == ((Integer)var13.getFirst()).intValue() && lvt_8_2_ == ((Integer)var13.getSecond()).intValue()) {
                     this.thirdFloorGrid.set(lvt_9_2_, lvt_8_2_, 3);
                  } else if(lvt_9_2_ == lvt_6_2_ && lvt_8_2_ == lvt_7_1_) {
                     this.thirdFloorGrid.set(lvt_9_2_, lvt_8_2_, 3);
                     this.floorRooms[2].set(lvt_9_2_, lvt_8_2_, 8388608);
                  }
               }
            }

            ArrayList var15 = Lists.newArrayList();
            EnumFacing[] var16 = Plane.HORIZONTAL.facings();
            int var10 = var16.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               EnumFacing lvt_12_1_ = var16[var11];
               if(this.thirdFloorGrid.get(lvt_6_2_ + lvt_12_1_.getFrontOffsetX(), lvt_7_1_ + lvt_12_1_.getFrontOffsetZ()) == 0) {
                  var15.add(lvt_12_1_);
               }
            }

            if(var15.isEmpty()) {
               this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
               lvt_2_1_.set(((Integer)var13.getFirst()).intValue(), ((Integer)var13.getSecond()).intValue(), lvt_4_2_);
            } else {
               EnumFacing var17 = (EnumFacing)var15.get(this.random.nextInt(var15.size()));
               this.recursiveCorridor(this.thirdFloorGrid, lvt_6_2_ + var17.getFrontOffsetX(), lvt_7_1_ + var17.getFrontOffsetZ(), var17, 4);

               while(this.cleanEdges(this.thirdFloorGrid)) {
                  ;
               }

            }
         }
      }

      private void identifyRooms(WoodlandMansionLayout112.SimpleGrid p_191116_1_, WoodlandMansionLayout112.SimpleGrid p_191116_2_) {
         ArrayList lvt_3_1_ = Lists.newArrayList();

         int lvt_4_2_;
         for(lvt_4_2_ = 0; lvt_4_2_ < p_191116_1_.height; ++lvt_4_2_) {
            for(int lvt_5_1_ = 0; lvt_5_1_ < p_191116_1_.width; ++lvt_5_1_) {
               if(p_191116_1_.get(lvt_5_1_, lvt_4_2_) == 2) {
                  lvt_3_1_.add(new Tuple(Integer.valueOf(lvt_5_1_), Integer.valueOf(lvt_4_2_)));
               }
            }
         }

         Collections.shuffle(lvt_3_1_, this.random);
         lvt_4_2_ = 10;
         Iterator var19 = lvt_3_1_.iterator();

         while(var19.hasNext()) {
            Tuple lvt_6_1_ = (Tuple)var19.next();
            int lvt_7_1_ = ((Integer)lvt_6_1_.getFirst()).intValue();
            int lvt_8_1_ = ((Integer)lvt_6_1_.getSecond()).intValue();
            if(p_191116_2_.get(lvt_7_1_, lvt_8_1_) == 0) {
               int lvt_9_1_ = lvt_7_1_;
               int lvt_10_1_ = lvt_7_1_;
               int lvt_11_1_ = lvt_8_1_;
               int lvt_12_1_ = lvt_8_1_;
               int lvt_13_1_ = 65536;
               if(p_191116_2_.get(lvt_7_1_ + 1, lvt_8_1_) == 0 && p_191116_2_.get(lvt_7_1_, lvt_8_1_ + 1) == 0 && p_191116_2_.get(lvt_7_1_ + 1, lvt_8_1_ + 1) == 0 && p_191116_1_.get(lvt_7_1_ + 1, lvt_8_1_) == 2 && p_191116_1_.get(lvt_7_1_, lvt_8_1_ + 1) == 2 && p_191116_1_.get(lvt_7_1_ + 1, lvt_8_1_ + 1) == 2) {
                  lvt_10_1_ = lvt_7_1_ + 1;
                  lvt_12_1_ = lvt_8_1_ + 1;
                  lvt_13_1_ = 262144;
               } else if(p_191116_2_.get(lvt_7_1_ - 1, lvt_8_1_) == 0 && p_191116_2_.get(lvt_7_1_, lvt_8_1_ + 1) == 0 && p_191116_2_.get(lvt_7_1_ - 1, lvt_8_1_ + 1) == 0 && p_191116_1_.get(lvt_7_1_ - 1, lvt_8_1_) == 2 && p_191116_1_.get(lvt_7_1_, lvt_8_1_ + 1) == 2 && p_191116_1_.get(lvt_7_1_ - 1, lvt_8_1_ + 1) == 2) {
                  lvt_9_1_ = lvt_7_1_ - 1;
                  lvt_12_1_ = lvt_8_1_ + 1;
                  lvt_13_1_ = 262144;
               } else if(p_191116_2_.get(lvt_7_1_ - 1, lvt_8_1_) == 0 && p_191116_2_.get(lvt_7_1_, lvt_8_1_ - 1) == 0 && p_191116_2_.get(lvt_7_1_ - 1, lvt_8_1_ - 1) == 0 && p_191116_1_.get(lvt_7_1_ - 1, lvt_8_1_) == 2 && p_191116_1_.get(lvt_7_1_, lvt_8_1_ - 1) == 2 && p_191116_1_.get(lvt_7_1_ - 1, lvt_8_1_ - 1) == 2) {
                  lvt_9_1_ = lvt_7_1_ - 1;
                  lvt_11_1_ = lvt_8_1_ - 1;
                  lvt_13_1_ = 262144;
               } else if(p_191116_2_.get(lvt_7_1_ + 1, lvt_8_1_) == 0 && p_191116_1_.get(lvt_7_1_ + 1, lvt_8_1_) == 2) {
                  lvt_10_1_ = lvt_7_1_ + 1;
                  lvt_13_1_ = 131072;
               } else if(p_191116_2_.get(lvt_7_1_, lvt_8_1_ + 1) == 0 && p_191116_1_.get(lvt_7_1_, lvt_8_1_ + 1) == 2) {
                  lvt_12_1_ = lvt_8_1_ + 1;
                  lvt_13_1_ = 131072;
               } else if(p_191116_2_.get(lvt_7_1_ - 1, lvt_8_1_) == 0 && p_191116_1_.get(lvt_7_1_ - 1, lvt_8_1_) == 2) {
                  lvt_9_1_ = lvt_7_1_ - 1;
                  lvt_13_1_ = 131072;
               } else if(p_191116_2_.get(lvt_7_1_, lvt_8_1_ - 1) == 0 && p_191116_1_.get(lvt_7_1_, lvt_8_1_ - 1) == 2) {
                  lvt_11_1_ = lvt_8_1_ - 1;
                  lvt_13_1_ = 131072;
               }

               int lvt_14_1_ = this.random.nextBoolean()?lvt_9_1_:lvt_10_1_;
               int lvt_15_1_ = this.random.nextBoolean()?lvt_11_1_:lvt_12_1_;
               int lvt_16_1_ = 2097152;
               if(!p_191116_1_.edgesTo(lvt_14_1_, lvt_15_1_, 1)) {
                  lvt_14_1_ = lvt_14_1_ == lvt_9_1_?lvt_10_1_:lvt_9_1_;
                  lvt_15_1_ = lvt_15_1_ == lvt_11_1_?lvt_12_1_:lvt_11_1_;
                  if(!p_191116_1_.edgesTo(lvt_14_1_, lvt_15_1_, 1)) {
                     lvt_15_1_ = lvt_15_1_ == lvt_11_1_?lvt_12_1_:lvt_11_1_;
                     if(!p_191116_1_.edgesTo(lvt_14_1_, lvt_15_1_, 1)) {
                        lvt_14_1_ = lvt_14_1_ == lvt_9_1_?lvt_10_1_:lvt_9_1_;
                        lvt_15_1_ = lvt_15_1_ == lvt_11_1_?lvt_12_1_:lvt_11_1_;
                        if(!p_191116_1_.edgesTo(lvt_14_1_, lvt_15_1_, 1)) {
                           lvt_16_1_ = 0;
                           lvt_14_1_ = lvt_9_1_;
                           lvt_15_1_ = lvt_11_1_;
                        }
                     }
                  }
               }

               for(int lvt_17_1_ = lvt_11_1_; lvt_17_1_ <= lvt_12_1_; ++lvt_17_1_) {
                  for(int lvt_18_1_ = lvt_9_1_; lvt_18_1_ <= lvt_10_1_; ++lvt_18_1_) {
                     if(lvt_18_1_ == lvt_14_1_ && lvt_17_1_ == lvt_15_1_) {
                        p_191116_2_.set(lvt_18_1_, lvt_17_1_, 1048576 | lvt_16_1_ | lvt_13_1_ | lvt_4_2_);
                     } else {
                        p_191116_2_.set(lvt_18_1_, lvt_17_1_, lvt_13_1_ | lvt_4_2_);
                     }
                  }
               }

               ++lvt_4_2_;
            }
         }

      }
   }

   static class PlacementData {

      public Rotation rotation;
      public BlockPos position;
      public String wallType;


      private PlacementData() {}

      // $FF: synthetic method
      PlacementData(Object p_i47360_1_) {
         this();
      }
   }

   static class Placer {

      private final TemplateManager templateManager;
      private final Random random;
      private int startX;
      private int startY;


      public Placer(TemplateManager p_i47361_1_, Random p_i47361_2_) {
         this.templateManager = p_i47361_1_;
         this.random = p_i47361_2_;
      }

      public void createMansion(BlockPos p_191125_1_, Rotation p_191125_2_, List p_191125_3_, WoodlandMansionLayout112.Grid p_191125_4_) {
         WoodlandMansionLayout112.PlacementData lvt_5_1_ = new WoodlandMansionLayout112.PlacementData(null);
         lvt_5_1_.position = p_191125_1_;
         lvt_5_1_.rotation = p_191125_2_;
         lvt_5_1_.wallType = "wall_flat";
         WoodlandMansionLayout112.PlacementData lvt_6_1_ = new WoodlandMansionLayout112.PlacementData(null);
         this.entrance(p_191125_3_, lvt_5_1_);
         lvt_6_1_.position = lvt_5_1_.position.up(8);
         lvt_6_1_.rotation = lvt_5_1_.rotation;
         lvt_6_1_.wallType = "wall_window";
         if(!p_191125_3_.isEmpty()) {
            ;
         }

         WoodlandMansionLayout112.SimpleGrid lvt_7_1_ = p_191125_4_.baseGrid;
         WoodlandMansionLayout112.SimpleGrid lvt_8_1_ = p_191125_4_.thirdFloorGrid;
         this.startX = p_191125_4_.entranceX + 1;
         this.startY = p_191125_4_.entranceY + 1;
         int lvt_9_1_ = p_191125_4_.entranceX + 1;
         int lvt_10_1_ = p_191125_4_.entranceY;
         this.traverseOuterWalls(p_191125_3_, lvt_5_1_, lvt_7_1_, EnumFacing.SOUTH, this.startX, this.startY, lvt_9_1_, lvt_10_1_);
         this.traverseOuterWalls(p_191125_3_, lvt_6_1_, lvt_7_1_, EnumFacing.SOUTH, this.startX, this.startY, lvt_9_1_, lvt_10_1_);
         WoodlandMansionLayout112.PlacementData lvt_11_1_ = new WoodlandMansionLayout112.PlacementData(null);
         lvt_11_1_.position = lvt_5_1_.position.up(19);
         lvt_11_1_.rotation = lvt_5_1_.rotation;
         lvt_11_1_.wallType = "wall_window";
         boolean lvt_12_1_ = false;

         int lvt_14_2_;
         for(int lvt_13_2_ = 0; lvt_13_2_ < lvt_8_1_.height && !lvt_12_1_; ++lvt_13_2_) {
            for(lvt_14_2_ = lvt_8_1_.width - 1; lvt_14_2_ >= 0 && !lvt_12_1_; --lvt_14_2_) {
               if(WoodlandMansionLayout112.Grid.isHouse(lvt_8_1_, lvt_14_2_, lvt_13_2_)) {
                  lvt_11_1_.position = lvt_11_1_.position.offset(p_191125_2_.rotate(EnumFacing.SOUTH), 8 + (lvt_13_2_ - this.startY) * 8);
                  lvt_11_1_.position = lvt_11_1_.position.offset(p_191125_2_.rotate(EnumFacing.EAST), (lvt_14_2_ - this.startX) * 8);
                  this.traverseWallPiece(p_191125_3_, lvt_11_1_);
                  this.traverseOuterWalls(p_191125_3_, lvt_11_1_, lvt_8_1_, EnumFacing.SOUTH, lvt_14_2_, lvt_13_2_, lvt_14_2_, lvt_13_2_);
                  lvt_12_1_ = true;
               }
            }
         }

         this.createRoof(p_191125_3_, p_191125_1_.up(16), p_191125_2_, lvt_7_1_, lvt_8_1_);
         this.createRoof(p_191125_3_, p_191125_1_.up(27), p_191125_2_, lvt_8_1_, (WoodlandMansionLayout112.SimpleGrid)null);
         if(!p_191125_3_.isEmpty()) {
            ;
         }

         WoodlandMansionLayout112.RoomCollection[] var33 = new WoodlandMansionLayout112.RoomCollection[]{new WoodlandMansionLayout112.FirstFloor(null), new WoodlandMansionLayout112.SecondFloor(null), new WoodlandMansionLayout112.ThirdFloor(null)};

         for(lvt_14_2_ = 0; lvt_14_2_ < 3; ++lvt_14_2_) {
            BlockPos lvt_15_1_ = p_191125_1_.up(8 * lvt_14_2_ + (lvt_14_2_ == 2?3:0));
            WoodlandMansionLayout112.SimpleGrid lvt_16_1_ = p_191125_4_.floorRooms[lvt_14_2_];
            WoodlandMansionLayout112.SimpleGrid lvt_17_1_ = lvt_14_2_ == 2?lvt_8_1_:lvt_7_1_;
            String lvt_18_1_ = lvt_14_2_ == 0?"carpet_south":"carpet_south_2";
            String lvt_19_1_ = lvt_14_2_ == 0?"carpet_west":"carpet_west_2";

            for(int lvt_20_2_ = 0; lvt_20_2_ < lvt_17_1_.height; ++lvt_20_2_) {
               for(int lvt_21_2_ = 0; lvt_21_2_ < lvt_17_1_.width; ++lvt_21_2_) {
                  if(lvt_17_1_.get(lvt_21_2_, lvt_20_2_) == 1) {
                     BlockPos lvt_22_2_ = lvt_15_1_.offset(p_191125_2_.rotate(EnumFacing.SOUTH), 8 + (lvt_20_2_ - this.startY) * 8);
                     lvt_22_2_ = lvt_22_2_.offset(p_191125_2_.rotate(EnumFacing.EAST), (lvt_21_2_ - this.startX) * 8);
                     p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "corridor_floor", lvt_22_2_, p_191125_2_));
                     if(lvt_17_1_.get(lvt_21_2_, lvt_20_2_ - 1) == 1 || (lvt_16_1_.get(lvt_21_2_, lvt_20_2_ - 1) & 8388608) == 8388608) {
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "carpet_north", lvt_22_2_.offset(p_191125_2_.rotate(EnumFacing.EAST), 1).up(), p_191125_2_));
                     }

                     if(lvt_17_1_.get(lvt_21_2_ + 1, lvt_20_2_) == 1 || (lvt_16_1_.get(lvt_21_2_ + 1, lvt_20_2_) & 8388608) == 8388608) {
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "carpet_east", lvt_22_2_.offset(p_191125_2_.rotate(EnumFacing.SOUTH), 1).offset(p_191125_2_.rotate(EnumFacing.EAST), 5).up(), p_191125_2_));
                     }

                     if(lvt_17_1_.get(lvt_21_2_, lvt_20_2_ + 1) == 1 || (lvt_16_1_.get(lvt_21_2_, lvt_20_2_ + 1) & 8388608) == 8388608) {
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, lvt_18_1_, lvt_22_2_.offset(p_191125_2_.rotate(EnumFacing.SOUTH), 5).offset(p_191125_2_.rotate(EnumFacing.WEST), 1), p_191125_2_));
                     }

                     if(lvt_17_1_.get(lvt_21_2_ - 1, lvt_20_2_) == 1 || (lvt_16_1_.get(lvt_21_2_ - 1, lvt_20_2_) & 8388608) == 8388608) {
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, lvt_19_1_, lvt_22_2_.offset(p_191125_2_.rotate(EnumFacing.WEST), 1).offset(p_191125_2_.rotate(EnumFacing.NORTH), 1), p_191125_2_));
                     }
                  }
               }
            }

            String var34 = lvt_14_2_ == 0?"indoors_wall":"indoors_wall_2";
            String var35 = lvt_14_2_ == 0?"indoors_door":"indoors_door_2";
            ArrayList var36 = Lists.newArrayList();

            for(int lvt_23_1_ = 0; lvt_23_1_ < lvt_17_1_.height; ++lvt_23_1_) {
               for(int lvt_24_1_ = 0; lvt_24_1_ < lvt_17_1_.width; ++lvt_24_1_) {
                  boolean lvt_25_1_ = lvt_14_2_ == 2 && lvt_17_1_.get(lvt_24_1_, lvt_23_1_) == 3;
                  if(lvt_17_1_.get(lvt_24_1_, lvt_23_1_) == 2 || lvt_25_1_) {
                     int lvt_26_1_ = lvt_16_1_.get(lvt_24_1_, lvt_23_1_);
                     int lvt_27_1_ = lvt_26_1_ & 983040;
                     int lvt_28_1_ = lvt_26_1_ & '\uffff';
                     lvt_25_1_ = lvt_25_1_ && (lvt_26_1_ & 8388608) == 8388608;
                     var36.clear();
                     if((lvt_26_1_ & 2097152) == 2097152) {
                        EnumFacing[] lvt_29_1_ = Plane.HORIZONTAL.facings();
                        int lvt_30_1_ = lvt_29_1_.length;

                        for(int lvt_31_5_ = 0; lvt_31_5_ < lvt_30_1_; ++lvt_31_5_) {
                           EnumFacing lvt_32_2_ = lvt_29_1_[lvt_31_5_];
                           if(lvt_17_1_.get(lvt_24_1_ + lvt_32_2_.getFrontOffsetX(), lvt_23_1_ + lvt_32_2_.getFrontOffsetZ()) == 1) {
                              var36.add(lvt_32_2_);
                           }
                        }
                     }

                     EnumFacing var37 = null;
                     if(!var36.isEmpty()) {
                        var37 = (EnumFacing)var36.get(this.random.nextInt(var36.size()));
                     } else if((lvt_26_1_ & 1048576) == 1048576) {
                        var37 = EnumFacing.UP;
                     }

                     BlockPos var38 = lvt_15_1_.offset(p_191125_2_.rotate(EnumFacing.SOUTH), 8 + (lvt_23_1_ - this.startY) * 8);
                     var38 = var38.offset(p_191125_2_.rotate(EnumFacing.EAST), -1 + (lvt_24_1_ - this.startX) * 8);
                     if(WoodlandMansionLayout112.Grid.isHouse(lvt_17_1_, lvt_24_1_ - 1, lvt_23_1_) && !p_191125_4_.isRoomId(lvt_17_1_, lvt_24_1_ - 1, lvt_23_1_, lvt_14_2_, lvt_28_1_)) {
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, var37 == EnumFacing.WEST?var35:var34, var38, p_191125_2_));
                     }

                     BlockPos var39;
                     if(lvt_17_1_.get(lvt_24_1_ + 1, lvt_23_1_) == 1 && !lvt_25_1_) {
                        var39 = var38.offset(p_191125_2_.rotate(EnumFacing.EAST), 8);
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, var37 == EnumFacing.EAST?var35:var34, var39, p_191125_2_));
                     }

                     if(WoodlandMansionLayout112.Grid.isHouse(lvt_17_1_, lvt_24_1_, lvt_23_1_ + 1) && !p_191125_4_.isRoomId(lvt_17_1_, lvt_24_1_, lvt_23_1_ + 1, lvt_14_2_, lvt_28_1_)) {
                        var39 = var38.offset(p_191125_2_.rotate(EnumFacing.SOUTH), 7);
                        var39 = var39.offset(p_191125_2_.rotate(EnumFacing.EAST), 7);
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, var37 == EnumFacing.SOUTH?var35:var34, var39, p_191125_2_.add(Rotation.CLOCKWISE_90)));
                     }

                     if(lvt_17_1_.get(lvt_24_1_, lvt_23_1_ - 1) == 1 && !lvt_25_1_) {
                        var39 = var38.offset(p_191125_2_.rotate(EnumFacing.NORTH), 1);
                        var39 = var39.offset(p_191125_2_.rotate(EnumFacing.EAST), 7);
                        p_191125_3_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, var37 == EnumFacing.NORTH?var35:var34, var39, p_191125_2_.add(Rotation.CLOCKWISE_90)));
                     }

                     if(lvt_27_1_ == 65536) {
                        this.addRoom1x1(p_191125_3_, var38, p_191125_2_, var37, var33[lvt_14_2_]);
                     } else {
                        EnumFacing var40;
                        if(lvt_27_1_ == 131072 && var37 != null) {
                           var40 = p_191125_4_.get1x2RoomDirection(lvt_17_1_, lvt_24_1_, lvt_23_1_, lvt_14_2_, lvt_28_1_);
                           boolean var41 = (lvt_26_1_ & 4194304) == 4194304;
                           this.addRoom1x2(p_191125_3_, var38, p_191125_2_, var40, var37, var33[lvt_14_2_], var41);
                        } else if(lvt_27_1_ == 262144 && var37 != null && var37 != EnumFacing.UP) {
                           var40 = var37.rotateY();
                           if(!p_191125_4_.isRoomId(lvt_17_1_, lvt_24_1_ + var40.getFrontOffsetX(), lvt_23_1_ + var40.getFrontOffsetZ(), lvt_14_2_, lvt_28_1_)) {
                              var40 = var40.getOpposite();
                           }

                           this.addRoom2x2(p_191125_3_, var38, p_191125_2_, var40, var37, var33[lvt_14_2_]);
                        } else if(lvt_27_1_ == 262144 && var37 == EnumFacing.UP) {
                           this.addRoom2x2Secret(p_191125_3_, var38, p_191125_2_, var33[lvt_14_2_]);
                        }
                     }
                  }
               }
            }
         }

      }

      private void traverseOuterWalls(List p_191130_1_, WoodlandMansionLayout112.PlacementData p_191130_2_, WoodlandMansionLayout112.SimpleGrid p_191130_3_, EnumFacing p_191130_4_, int p_191130_5_, int p_191130_6_, int p_191130_7_, int p_191130_8_) {
         int lvt_9_1_ = p_191130_5_;
         int lvt_10_1_ = p_191130_6_;
         EnumFacing lvt_11_1_ = p_191130_4_;

         do {
            if(!WoodlandMansionLayout112.Grid.isHouse(p_191130_3_, lvt_9_1_ + p_191130_4_.getFrontOffsetX(), lvt_10_1_ + p_191130_4_.getFrontOffsetZ())) {
               this.traverseTurn(p_191130_1_, p_191130_2_);
               p_191130_4_ = p_191130_4_.rotateY();
               if(lvt_9_1_ != p_191130_7_ || lvt_10_1_ != p_191130_8_ || lvt_11_1_ != p_191130_4_) {
                  this.traverseWallPiece(p_191130_1_, p_191130_2_);
               }
            } else if(WoodlandMansionLayout112.Grid.isHouse(p_191130_3_, lvt_9_1_ + p_191130_4_.getFrontOffsetX(), lvt_10_1_ + p_191130_4_.getFrontOffsetZ()) && WoodlandMansionLayout112.Grid.isHouse(p_191130_3_, lvt_9_1_ + p_191130_4_.getFrontOffsetX() + p_191130_4_.rotateYCCW().getFrontOffsetX(), lvt_10_1_ + p_191130_4_.getFrontOffsetZ() + p_191130_4_.rotateYCCW().getFrontOffsetZ())) {
               this.traverseInnerTurn(p_191130_1_, p_191130_2_);
               lvt_9_1_ += p_191130_4_.getFrontOffsetX();
               lvt_10_1_ += p_191130_4_.getFrontOffsetZ();
               p_191130_4_ = p_191130_4_.rotateYCCW();
            } else {
               lvt_9_1_ += p_191130_4_.getFrontOffsetX();
               lvt_10_1_ += p_191130_4_.getFrontOffsetZ();
               if(lvt_9_1_ != p_191130_7_ || lvt_10_1_ != p_191130_8_ || lvt_11_1_ != p_191130_4_) {
                  this.traverseWallPiece(p_191130_1_, p_191130_2_);
               }
            }
         } while(lvt_9_1_ != p_191130_7_ || lvt_10_1_ != p_191130_8_ || lvt_11_1_ != p_191130_4_);

      }

      private void createRoof(List p_191123_1_, BlockPos p_191123_2_, Rotation p_191123_3_, WoodlandMansionLayout112.SimpleGrid p_191123_4_, WoodlandMansionLayout112.SimpleGrid p_191123_5_) {
         int lvt_6_3_;
         int lvt_7_3_;
         BlockPos lvt_8_3_;
         boolean lvt_9_3_;
         BlockPos lvt_10_14_;
         for(lvt_6_3_ = 0; lvt_6_3_ < p_191123_4_.height; ++lvt_6_3_) {
            for(lvt_7_3_ = 0; lvt_7_3_ < p_191123_4_.width; ++lvt_7_3_) {
               lvt_8_3_ = p_191123_2_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 8 + (lvt_6_3_ - this.startY) * 8);
               lvt_8_3_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), (lvt_7_3_ - this.startX) * 8);
               lvt_9_3_ = p_191123_5_ != null && WoodlandMansionLayout112.Grid.isHouse(p_191123_5_, lvt_7_3_, lvt_6_3_);
               if(WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_) && !lvt_9_3_) {
                  p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof", lvt_8_3_.up(3), p_191123_3_));
                  if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ + 1, lvt_6_3_)) {
                     lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 6);
                     p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_front", lvt_10_14_, p_191123_3_));
                  }

                  if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ - 1, lvt_6_3_)) {
                     lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 0);
                     lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 7);
                     p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_front", lvt_10_14_, p_191123_3_.add(Rotation.CLOCKWISE_180)));
                  }

                  if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ - 1)) {
                     lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.WEST), 1);
                     p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_front", lvt_10_14_, p_191123_3_.add(Rotation.COUNTERCLOCKWISE_90)));
                  }

                  if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ + 1)) {
                     lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 6);
                     lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 6);
                     p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_front", lvt_10_14_, p_191123_3_.add(Rotation.CLOCKWISE_90)));
                  }
               }
            }
         }

         if(p_191123_5_ != null) {
            for(lvt_6_3_ = 0; lvt_6_3_ < p_191123_4_.height; ++lvt_6_3_) {
               for(lvt_7_3_ = 0; lvt_7_3_ < p_191123_4_.width; ++lvt_7_3_) {
                  lvt_8_3_ = p_191123_2_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 8 + (lvt_6_3_ - this.startY) * 8);
                  lvt_8_3_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), (lvt_7_3_ - this.startX) * 8);
                  lvt_9_3_ = WoodlandMansionLayout112.Grid.isHouse(p_191123_5_, lvt_7_3_, lvt_6_3_);
                  if(WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_) && lvt_9_3_) {
                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ + 1, lvt_6_3_)) {
                        lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 7);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall", lvt_10_14_, p_191123_3_));
                     }

                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ - 1, lvt_6_3_)) {
                        lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.WEST), 1);
                        lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 6);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall", lvt_10_14_, p_191123_3_.add(Rotation.CLOCKWISE_180)));
                     }

                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ - 1)) {
                        lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.WEST), 0);
                        lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.NORTH), 1);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall", lvt_10_14_, p_191123_3_.add(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ + 1)) {
                        lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 6);
                        lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 7);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall", lvt_10_14_, p_191123_3_.add(Rotation.CLOCKWISE_90)));
                     }

                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ + 1, lvt_6_3_)) {
                        if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ - 1)) {
                           lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 7);
                           lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.NORTH), 2);
                           p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall_corner", lvt_10_14_, p_191123_3_));
                        }

                        if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ + 1)) {
                           lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 8);
                           lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 7);
                           p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall_corner", lvt_10_14_, p_191123_3_.add(Rotation.CLOCKWISE_90)));
                        }
                     }

                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ - 1, lvt_6_3_)) {
                        if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ - 1)) {
                           lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.WEST), 2);
                           lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.NORTH), 1);
                           p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall_corner", lvt_10_14_, p_191123_3_.add(Rotation.COUNTERCLOCKWISE_90)));
                        }

                        if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ + 1)) {
                           lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.WEST), 1);
                           lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 8);
                           p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "small_wall_corner", lvt_10_14_, p_191123_3_.add(Rotation.CLOCKWISE_180)));
                        }
                     }
                  }
               }
            }
         }

         for(lvt_6_3_ = 0; lvt_6_3_ < p_191123_4_.height; ++lvt_6_3_) {
            for(lvt_7_3_ = 0; lvt_7_3_ < p_191123_4_.width; ++lvt_7_3_) {
               lvt_8_3_ = p_191123_2_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 8 + (lvt_6_3_ - this.startY) * 8);
               lvt_8_3_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), (lvt_7_3_ - this.startX) * 8);
               lvt_9_3_ = p_191123_5_ != null && WoodlandMansionLayout112.Grid.isHouse(p_191123_5_, lvt_7_3_, lvt_6_3_);
               if(WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_) && !lvt_9_3_) {
                  BlockPos lvt_11_8_;
                  if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ + 1, lvt_6_3_)) {
                     lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 6);
                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ + 1)) {
                        lvt_11_8_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 6);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_corner", lvt_11_8_, p_191123_3_));
                     } else if(WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ + 1, lvt_6_3_ + 1)) {
                        lvt_11_8_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 5);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_inner_corner", lvt_11_8_, p_191123_3_));
                     }

                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ - 1)) {
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_corner", lvt_10_14_, p_191123_3_.add(Rotation.COUNTERCLOCKWISE_90)));
                     } else if(WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ + 1, lvt_6_3_ - 1)) {
                        lvt_11_8_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 9);
                        lvt_11_8_ = lvt_11_8_.offset(p_191123_3_.rotate(EnumFacing.NORTH), 2);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_inner_corner", lvt_11_8_, p_191123_3_.add(Rotation.CLOCKWISE_90)));
                     }
                  }

                  if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ - 1, lvt_6_3_)) {
                     lvt_10_14_ = lvt_8_3_.offset(p_191123_3_.rotate(EnumFacing.EAST), 0);
                     lvt_10_14_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 0);
                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ + 1)) {
                        lvt_11_8_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 6);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_corner", lvt_11_8_, p_191123_3_.add(Rotation.CLOCKWISE_90)));
                     } else if(WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ - 1, lvt_6_3_ + 1)) {
                        lvt_11_8_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 8);
                        lvt_11_8_ = lvt_11_8_.offset(p_191123_3_.rotate(EnumFacing.WEST), 3);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_inner_corner", lvt_11_8_, p_191123_3_.add(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if(!WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_, lvt_6_3_ - 1)) {
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_corner", lvt_10_14_, p_191123_3_.add(Rotation.CLOCKWISE_180)));
                     } else if(WoodlandMansionLayout112.Grid.isHouse(p_191123_4_, lvt_7_3_ - 1, lvt_6_3_ - 1)) {
                        lvt_11_8_ = lvt_10_14_.offset(p_191123_3_.rotate(EnumFacing.SOUTH), 1);
                        p_191123_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "roof_inner_corner", lvt_11_8_, p_191123_3_.add(Rotation.CLOCKWISE_180)));
                     }
                  }
               }
            }
         }

      }

      private void entrance(List p_191133_1_, WoodlandMansionLayout112.PlacementData p_191133_2_) {
         EnumFacing lvt_3_1_ = p_191133_2_.rotation.rotate(EnumFacing.WEST);
         p_191133_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "entrance", p_191133_2_.position.offset(lvt_3_1_, 9), p_191133_2_.rotation));
         p_191133_2_.position = p_191133_2_.position.offset(p_191133_2_.rotation.rotate(EnumFacing.SOUTH), 16);
      }

      private void traverseWallPiece(List p_191131_1_, WoodlandMansionLayout112.PlacementData p_191131_2_) {
         p_191131_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191131_2_.wallType, p_191131_2_.position.offset(p_191131_2_.rotation.rotate(EnumFacing.EAST), 7), p_191131_2_.rotation));
         p_191131_2_.position = p_191131_2_.position.offset(p_191131_2_.rotation.rotate(EnumFacing.SOUTH), 8);
      }

      private void traverseTurn(List p_191124_1_, WoodlandMansionLayout112.PlacementData p_191124_2_) {
         p_191124_2_.position = p_191124_2_.position.offset(p_191124_2_.rotation.rotate(EnumFacing.SOUTH), -1);
         p_191124_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, "wall_corner", p_191124_2_.position, p_191124_2_.rotation));
         p_191124_2_.position = p_191124_2_.position.offset(p_191124_2_.rotation.rotate(EnumFacing.SOUTH), -7);
         p_191124_2_.position = p_191124_2_.position.offset(p_191124_2_.rotation.rotate(EnumFacing.WEST), -6);
         p_191124_2_.rotation = p_191124_2_.rotation.add(Rotation.CLOCKWISE_90);
      }

      private void traverseInnerTurn(List p_191126_1_, WoodlandMansionLayout112.PlacementData p_191126_2_) {
         p_191126_2_.position = p_191126_2_.position.offset(p_191126_2_.rotation.rotate(EnumFacing.SOUTH), 6);
         p_191126_2_.position = p_191126_2_.position.offset(p_191126_2_.rotation.rotate(EnumFacing.EAST), 8);
         p_191126_2_.rotation = p_191126_2_.rotation.add(Rotation.COUNTERCLOCKWISE_90);
      }

      private void addRoom1x1(List p_191129_1_, BlockPos p_191129_2_, Rotation p_191129_3_, EnumFacing p_191129_4_, WoodlandMansionLayout112.RoomCollection p_191129_5_) {
         Rotation lvt_6_1_ = Rotation.NONE;
         String lvt_7_1_ = p_191129_5_.get1x1(this.random);
         if(p_191129_4_ != EnumFacing.EAST) {
            if(p_191129_4_ == EnumFacing.NORTH) {
               lvt_6_1_ = lvt_6_1_.add(Rotation.COUNTERCLOCKWISE_90);
            } else if(p_191129_4_ == EnumFacing.WEST) {
               lvt_6_1_ = lvt_6_1_.add(Rotation.CLOCKWISE_180);
            } else if(p_191129_4_ == EnumFacing.SOUTH) {
               lvt_6_1_ = lvt_6_1_.add(Rotation.CLOCKWISE_90);
            } else {
               lvt_7_1_ = p_191129_5_.get1x1Secret(this.random);
            }
         }

         BlockPos lvt_8_1_ = Template.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, lvt_6_1_, 7, 7);
         lvt_6_1_ = lvt_6_1_.add(p_191129_3_);
         lvt_8_1_ = lvt_8_1_.rotate(p_191129_3_);
         BlockPos lvt_9_1_ = p_191129_2_.add(lvt_8_1_.getX(), 0, lvt_8_1_.getZ());
         p_191129_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, lvt_7_1_, lvt_9_1_, lvt_6_1_));
      }

      private void addRoom1x2(List p_191132_1_, BlockPos p_191132_2_, Rotation p_191132_3_, EnumFacing p_191132_4_, EnumFacing p_191132_5_, WoodlandMansionLayout112.RoomCollection p_191132_6_, boolean p_191132_7_) {
         BlockPos lvt_8_14_;
         if(p_191132_5_ == EnumFacing.EAST && p_191132_4_ == EnumFacing.SOUTH) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 1);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_));
         } else if(p_191132_5_ == EnumFacing.EAST && p_191132_4_ == EnumFacing.NORTH) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 1);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.SOUTH), 6);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_, Mirror.LEFT_RIGHT));
         } else if(p_191132_5_ == EnumFacing.WEST && p_191132_4_ == EnumFacing.NORTH) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 7);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.SOUTH), 6);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.CLOCKWISE_180)));
         } else if(p_191132_5_ == EnumFacing.WEST && p_191132_4_ == EnumFacing.SOUTH) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 7);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_, Mirror.FRONT_BACK));
         } else if(p_191132_5_ == EnumFacing.SOUTH && p_191132_4_ == EnumFacing.EAST) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 1);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT));
         } else if(p_191132_5_ == EnumFacing.SOUTH && p_191132_4_ == EnumFacing.WEST) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 7);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.CLOCKWISE_90)));
         } else if(p_191132_5_ == EnumFacing.NORTH && p_191132_4_ == EnumFacing.WEST) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 7);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.SOUTH), 6);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK));
         } else if(p_191132_5_ == EnumFacing.NORTH && p_191132_4_ == EnumFacing.EAST) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 1);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.SOUTH), 6);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2SideEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.COUNTERCLOCKWISE_90)));
         } else if(p_191132_5_ == EnumFacing.SOUTH && p_191132_4_ == EnumFacing.NORTH) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 1);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.NORTH), 8);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2FrontEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_));
         } else if(p_191132_5_ == EnumFacing.NORTH && p_191132_4_ == EnumFacing.SOUTH) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 7);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.SOUTH), 14);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2FrontEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.CLOCKWISE_180)));
         } else if(p_191132_5_ == EnumFacing.WEST && p_191132_4_ == EnumFacing.EAST) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 15);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2FrontEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.CLOCKWISE_90)));
         } else if(p_191132_5_ == EnumFacing.EAST && p_191132_4_ == EnumFacing.WEST) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.WEST), 7);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.SOUTH), 6);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2FrontEntrance(this.random, p_191132_7_), lvt_8_14_, p_191132_3_.add(Rotation.COUNTERCLOCKWISE_90)));
         } else if(p_191132_5_ == EnumFacing.UP && p_191132_4_ == EnumFacing.EAST) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 15);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2Secret(this.random), lvt_8_14_, p_191132_3_.add(Rotation.CLOCKWISE_90)));
         } else if(p_191132_5_ == EnumFacing.UP && p_191132_4_ == EnumFacing.SOUTH) {
            lvt_8_14_ = p_191132_2_.offset(p_191132_3_.rotate(EnumFacing.EAST), 1);
            lvt_8_14_ = lvt_8_14_.offset(p_191132_3_.rotate(EnumFacing.NORTH), 0);
            p_191132_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191132_6_.get1x2Secret(this.random), lvt_8_14_, p_191132_3_));
         }

      }

      private void addRoom2x2(List p_191127_1_, BlockPos p_191127_2_, Rotation p_191127_3_, EnumFacing p_191127_4_, EnumFacing p_191127_5_, WoodlandMansionLayout112.RoomCollection p_191127_6_) {
         byte lvt_7_1_ = 0;
         byte lvt_8_1_ = 0;
         Rotation lvt_9_1_ = p_191127_3_;
         Mirror lvt_10_1_ = Mirror.NONE;
         if(p_191127_5_ == EnumFacing.EAST && p_191127_4_ == EnumFacing.SOUTH) {
            lvt_7_1_ = -7;
         } else if(p_191127_5_ == EnumFacing.EAST && p_191127_4_ == EnumFacing.NORTH) {
            lvt_7_1_ = -7;
            lvt_8_1_ = 6;
            lvt_10_1_ = Mirror.LEFT_RIGHT;
         } else if(p_191127_5_ == EnumFacing.NORTH && p_191127_4_ == EnumFacing.EAST) {
            lvt_7_1_ = 1;
            lvt_8_1_ = 14;
            lvt_9_1_ = p_191127_3_.add(Rotation.COUNTERCLOCKWISE_90);
         } else if(p_191127_5_ == EnumFacing.NORTH && p_191127_4_ == EnumFacing.WEST) {
            lvt_7_1_ = 7;
            lvt_8_1_ = 14;
            lvt_9_1_ = p_191127_3_.add(Rotation.COUNTERCLOCKWISE_90);
            lvt_10_1_ = Mirror.LEFT_RIGHT;
         } else if(p_191127_5_ == EnumFacing.SOUTH && p_191127_4_ == EnumFacing.WEST) {
            lvt_7_1_ = 7;
            lvt_8_1_ = -8;
            lvt_9_1_ = p_191127_3_.add(Rotation.CLOCKWISE_90);
         } else if(p_191127_5_ == EnumFacing.SOUTH && p_191127_4_ == EnumFacing.EAST) {
            lvt_7_1_ = 1;
            lvt_8_1_ = -8;
            lvt_9_1_ = p_191127_3_.add(Rotation.CLOCKWISE_90);
            lvt_10_1_ = Mirror.LEFT_RIGHT;
         } else if(p_191127_5_ == EnumFacing.WEST && p_191127_4_ == EnumFacing.NORTH) {
            lvt_7_1_ = 15;
            lvt_8_1_ = 6;
            lvt_9_1_ = p_191127_3_.add(Rotation.CLOCKWISE_180);
         } else if(p_191127_5_ == EnumFacing.WEST && p_191127_4_ == EnumFacing.SOUTH) {
            lvt_7_1_ = 15;
            lvt_10_1_ = Mirror.FRONT_BACK;
         }

         BlockPos lvt_11_1_ = p_191127_2_.offset(p_191127_3_.rotate(EnumFacing.EAST), lvt_7_1_);
         lvt_11_1_ = lvt_11_1_.offset(p_191127_3_.rotate(EnumFacing.SOUTH), lvt_8_1_);
         p_191127_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191127_6_.get2x2(this.random), lvt_11_1_, lvt_9_1_, lvt_10_1_));
      }

      private void addRoom2x2Secret(List p_191128_1_, BlockPos p_191128_2_, Rotation p_191128_3_, WoodlandMansionLayout112.RoomCollection p_191128_4_) {
         BlockPos lvt_5_1_ = p_191128_2_.offset(p_191128_3_.rotate(EnumFacing.EAST), 1);
         p_191128_1_.add(new WoodlandMansionLayout112.MansionTemplate(this.templateManager, p_191128_4_.get2x2Secret(this.random), lvt_5_1_, p_191128_3_, Mirror.NONE));
      }
   }

   abstract static class RoomCollection {

      private RoomCollection() {}

      public abstract String get1x1(Random var1);

      public abstract String get1x1Secret(Random var1);

      public abstract String get1x2SideEntrance(Random var1, boolean var2);

      public abstract String get1x2FrontEntrance(Random var1, boolean var2);

      public abstract String get1x2Secret(Random var1);

      public abstract String get2x2(Random var1);

      public abstract String get2x2Secret(Random var1);

      // $FF: synthetic method
      RoomCollection(Object p_i47363_1_) {
         this();
      }
   }

   static class SecondFloor extends WoodlandMansionLayout112.RoomCollection {

      private SecondFloor() {
         super(null);
      }

      public String get1x1(Random p_191104_1_) {
         return "1x1_b" + (p_191104_1_.nextInt(4) + 1);
      }

      public String get1x1Secret(Random p_191099_1_) {
         return "1x1_as" + (p_191099_1_.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(Random p_191100_1_, boolean p_191100_2_) {
         return p_191100_2_?"1x2_c_stairs":"1x2_c" + (p_191100_1_.nextInt(4) + 1);
      }

      public String get1x2FrontEntrance(Random p_191098_1_, boolean p_191098_2_) {
         return p_191098_2_?"1x2_d_stairs":"1x2_d" + (p_191098_1_.nextInt(5) + 1);
      }

      public String get1x2Secret(Random p_191102_1_) {
         return "1x2_se" + (p_191102_1_.nextInt(1) + 1);
      }

      public String get2x2(Random p_191101_1_) {
         return "2x2_b" + (p_191101_1_.nextInt(5) + 1);
      }

      public String get2x2Secret(Random p_191103_1_) {
         return "2x2_s1";
      }

      // $FF: synthetic method
      SecondFloor(Object p_i47359_1_) {
         this();
      }
   }

   static class SimpleGrid {

      private final int[][] grid;
      private final int width;
      private final int height;
      private final int valueIfOutside;


      public SimpleGrid(int p_i47358_1_, int p_i47358_2_, int p_i47358_3_) {
         this.width = p_i47358_1_;
         this.height = p_i47358_2_;
         this.valueIfOutside = p_i47358_3_;
         this.grid = new int[p_i47358_1_][p_i47358_2_];
      }

      public void set(int p_191144_1_, int p_191144_2_, int p_191144_3_) {
         if(p_191144_1_ >= 0 && p_191144_1_ < this.width && p_191144_2_ >= 0 && p_191144_2_ < this.height) {
            this.grid[p_191144_1_][p_191144_2_] = p_191144_3_;
         }

      }

      public void set(int p_191142_1_, int p_191142_2_, int p_191142_3_, int p_191142_4_, int p_191142_5_) {
         for(int lvt_6_1_ = p_191142_2_; lvt_6_1_ <= p_191142_4_; ++lvt_6_1_) {
            for(int lvt_7_1_ = p_191142_1_; lvt_7_1_ <= p_191142_3_; ++lvt_7_1_) {
               this.set(lvt_7_1_, lvt_6_1_, p_191142_5_);
            }
         }

      }

      public int get(int p_191145_1_, int p_191145_2_) {
         return p_191145_1_ >= 0 && p_191145_1_ < this.width && p_191145_2_ >= 0 && p_191145_2_ < this.height?this.grid[p_191145_1_][p_191145_2_]:this.valueIfOutside;
      }

      public void setIf(int p_191141_1_, int p_191141_2_, int p_191141_3_, int p_191141_4_) {
         if(this.get(p_191141_1_, p_191141_2_) == p_191141_3_) {
            this.set(p_191141_1_, p_191141_2_, p_191141_4_);
         }

      }

      public boolean edgesTo(int p_191147_1_, int p_191147_2_, int p_191147_3_) {
         return this.get(p_191147_1_ - 1, p_191147_2_) == p_191147_3_ || this.get(p_191147_1_ + 1, p_191147_2_) == p_191147_3_ || this.get(p_191147_1_, p_191147_2_ + 1) == p_191147_3_ || this.get(p_191147_1_, p_191147_2_ - 1) == p_191147_3_;
      }
   }

   static class ThirdFloor extends WoodlandMansionLayout112.SecondFloor {

      private ThirdFloor() {
         super(null);
      }

      // $FF: synthetic method
      ThirdFloor(Object p_i47357_1_) {
         this();
      }
   }

    private WoodlandMansionLayout112() { }
}
