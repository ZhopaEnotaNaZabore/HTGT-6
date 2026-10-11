package com.mod.htgt6.minecraft1202.Module.World.Shared;

import com.mod.htgt6.asm.HTGT6Transformer;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldChunkManager120;
import com.mod.htgt6.common.util.WorldSafety;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/** Server-side biome locator for vanilla and extended Futura biome IDs. */
public final class CommandFindBiome120 extends CommandBase {
    public static final int SEARCH_RADIUS = 1500;
    public String getCommandName(){return "findbiome";}
    public String getCommandUsage(ICommandSender sender){return "/findbiome <name|id> [radius] (current dimension)";}
    public int getRequiredPermissionLevel(){return 2;}

    public void processCommand(ICommandSender sender,String[] args){
        if(args.length<1)throw new net.minecraft.command.WrongUsageException(getCommandUsage(sender));
        EntityPlayerMP player=getCommandSenderAsPlayer(sender);
        int nameEnd=args.length;
        int radius=SEARCH_RADIUS;
        if(args.length>1&&isInteger(args[args.length-1])){
            radius=parseIntBounded(sender,args[args.length-1],64,SEARCH_RADIUS);nameEnd--;
        }
        StringBuilder query=new StringBuilder();
        for(int i=0;i<nameEnd;i++){if(i>0)query.append(' ');query.append(args[i]);}
        BiomeGenBase target=resolve(query.toString());
        if(target==null)throw new CommandException("Unknown biome name/ID: "+query
                +". Use underscores for tab completion, e.g. minecraft:cherry_grove");
        int originX=(int)Math.floor(player.posX),originZ=(int)Math.floor(player.posZ);
        Result found;
        boolean cave=isCaveBiome(target);
        try{
            found=cave?findNearestUnderground(player.worldObj,target,originX,originZ,radius,
                    16,16,System.nanoTime()+2000000000L)
                    :findNearest(player.worldObj,target,originX,originZ,radius,4,System.nanoTime()+2000000000L);
        }
        catch(SearchTimeout timeout){throw new CommandException("Biome search timed out; retry with a smaller radius");}
        if(found==null)throw new CommandException("Biome not found within "+radius+" blocks in dimension "
                +player.worldObj.provider.dimensionId+": "+target.biomeName);
        int y=cave?safeCaveY(player.worldObj,target,found):safeY(player.worldObj,found.x,found.z);
        if(y<0)throw new CommandException("No safe teleport position in biome at "+found.x+", "+found.z);
        player.mountEntity(null);
        player.playerNetServerHandler.setPlayerLocation(found.teleportX+.5D,y,found.teleportZ+.5D,
                player.rotationYaw,player.rotationPitch);
        sender.addChatMessage(new ChatComponentText("Found "+target.biomeName+" ["+target.biomeID+"] at "
                +found.teleportX+", "+y+", "+found.teleportZ+" ("+(int)Math.sqrt(found.distanceSq)+" blocks)"));
    }

    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender,String[] args){
        if(args.length!=1)return null;
        ArrayList<String> names=new ArrayList<String>();
        for(BiomeGenBase biome:HTGT6Transformer.getRegisteredBiomesSnapshot120())if(biome!=null)
            names.add("minecraft:"+normalize(biome.biomeName).replace(' ','_'));
        return getListOfStringsMatchingLastWord(args,names.toArray(new String[names.size()]));
    }

    public static BiomeGenBase resolve(String token){
        try{return HTGT6Transformer.getRegisteredBiomeExact(Integer.parseInt(token));}
        catch(NumberFormatException ignored){ }
        String wanted=normalize(token);
        for(BiomeGenBase biome:HTGT6Transformer.getRegisteredBiomesSnapshot120())
            if(biome!=null&&normalize(biome.biomeName).equals(wanted))return biome;
        return null;
    }

    static Result findNearest(World world,BiomeGenBase target,int ox,int oz,int radius,int step,long deadline){
        Result best=null;
        for(int r=0;r<=radius;r+=step){
            for(int d=-r;d<=r;d+=step){
                if((d&255)==0&&System.nanoTime()>deadline)throw new SearchTimeout();
                best=sample(world,target,ox+d,oz-r,ox,oz,best);
                if(r!=0)best=sample(world,target,ox+d,oz+r,ox,oz,best);
                if(d!=-r&&d!=r){best=sample(world,target,ox-r,oz+d,ox,oz,best);
                    best=sample(world,target,ox+r,oz+d,ox,oz,best);}
            }
            // Everything outside the visited square has Euclidean distance > r.
            if(best!=null&&(long)r*r>=best.distanceSq)break;
        }
        if(best==null)return null;
        Result refined=best;
        for(int x=best.x-step;x<=best.x+step;x++)for(int z=best.z-step;z<=best.z+step;z++)
            refined=sample(world,target,x,z,ox,oz,refined);
        return refined;
    }

    public static Result findNearestUnderground(World world,BiomeGenBase target,int ox,int oz,int radius,
                                         int horizontalStep,int verticalStep,long deadline){
        if(!(world.getWorldChunkManager() instanceof FuturaWorldChunkManager120))return null;
        return findNearestUnderground((FuturaWorldChunkManager120)world.getWorldChunkManager(),target,
                ox,oz,radius,horizontalStep,verticalStep,
                Math.min(FuturaHeight120.lightingHeight(world),world.provider.getActualHeight())-2,deadline);
    }

    /** Pure climate search: it never asks World for a block or a chunk. */
    public static Result findNearestUnderground(FuturaWorldChunkManager120 manager,BiomeGenBase target,
                                         int ox,int oz,int radius,int horizontalStep,int verticalStep,
                                         int maxY,long deadline){
        Result best=null;
        int minY=2;
        for(int r=0;;r=Math.min(radius,r+horizontalStep)){
            for(int d=-r;;d=Math.min(r,d+horizontalStep)){
                if((d&255)==0&&System.nanoTime()>deadline)throw new SearchTimeout();
                best=sampleUnderground(manager,target,ox+d,oz-r,ox,oz,minY,maxY,verticalStep,best);
                if(r!=0)best=sampleUnderground(manager,target,ox+d,oz+r,ox,oz,minY,maxY,verticalStep,best);
                if(d!=-r&&d!=r){
                    best=sampleUnderground(manager,target,ox-r,oz+d,ox,oz,minY,maxY,verticalStep,best);
                    best=sampleUnderground(manager,target,ox+r,oz+d,ox,oz,minY,maxY,verticalStep,best);
                }
                if(d==r)break;
            }
            if(best!=null&&(long)r*r>=best.distanceSq)break;
            if(r==radius)break;
        }
        if(best==null)return null;
        Result refined=best;
        for(int x=best.x-horizontalStep;x<=best.x+horizontalStep;x+=4)
            for(int z=best.z-horizontalStep;z<=best.z+horizontalStep;z+=4)
                if(Math.abs(x-ox)<=radius&&Math.abs(z-oz)<=radius)
                    refined=sampleUnderground(manager,target,x,z,ox,oz,minY,maxY,4,refined);
        return refined;
    }

    private static Result sampleUnderground(FuturaWorldChunkManager120 manager,BiomeGenBase target,
                                            int x,int z,int ox,int oz,int minY,int maxY,int yStep,Result best){
        long dx=x-ox,dz=z-oz,distance=dx*dx+dz*dz;
        if(best!=null&&distance>best.distanceSq)return best;
        for(int y=minY;y<=maxY;y+=yStep){
            BiomeGenBase biome=manager.getUndergroundBiomeAt(x,y,z);
            if(biome!=null&&biome.biomeID==target.biomeID)
                return best==null||distance<best.distanceSq?new Result(x,y,z,distance):best;
        }
        return best;
    }

    private static Result sample(World world,BiomeGenBase target,int x,int z,int ox,int oz,Result best){
        BiomeGenBase biome=world.getWorldChunkManager() instanceof FuturaWorldChunkManager120
                ?((FuturaWorldChunkManager120)world.getWorldChunkManager()).getSurfaceBiomeAt(x,z)
                :world.getWorldChunkManager().getBiomeGenAt(x,z);
        if(biome==null||biome.biomeID!=target.biomeID)return best;
        long dx=x-ox,dz=z-oz,distance=dx*dx+dz*dz;
        return best==null||distance<best.distanceSq?new Result(x,z,distance):best;
    }

    private static int safeY(World world,int x,int z){
        int max=Math.min(FuturaHeight120.lightingHeight(world),world.provider.getActualHeight())-2;
        int y=Math.min(max,Math.max(1,world.getTopSolidOrLiquidBlock(x,z)+1));
        while(y<max&&!isSafe(world,x,y,z))y++;
        while(y>1&&!isSafe(world,x,y,z))y--;
        return isSafe(world,x,y,z)?y:-1;
    }

    private static int safeCaveY(World world,BiomeGenBase target,Result climate){
        FuturaWorldChunkManager120 manager=(FuturaWorldChunkManager120)world.getWorldChunkManager();
        int maxY=Math.min(FuturaHeight120.lightingHeight(world),world.provider.getActualHeight())-2;
        int bestY=-1,bestX=climate.x,bestZ=climate.z;
        long best=Long.MAX_VALUE;
        int centerChunkX=climate.x>>4,centerChunkZ=climate.z>>4;
        // A climate cell can exist inside solid rock.  Search the selected
        // chunk and its immediate neighbours for an actually traversable cave
        // instead of rejecting the biome after testing only one chunk.
        for(int radius=0;radius<=1&&bestY<0;radius++)for(int dcx=-radius;dcx<=radius;dcx++)
            for(int dcz=-radius;dcz<=radius;dcz++){
                if(Math.max(Math.abs(dcx),Math.abs(dcz))!=radius)continue;
                int chunkX=centerChunkX+dcx,chunkZ=centerChunkZ+dcz;
                if(!containsCaveClimate(manager,target,chunkX,chunkZ,maxY))continue;
                world.getChunkFromChunkCoords(chunkX,chunkZ);
                int minX=chunkX<<4,minZ=chunkZ<<4;
                for(int x=minX;x<minX+16;x++)for(int z=minZ;z<minZ+16;z++){
                    int top=WorldSafety.getSafeTopBlock(world,x,z);
                    if(top<3)continue;
                    for(int y=2;y<Math.min(maxY,top-1);y++){
                        if(!isSafeLoaded(world,x,y,z))continue;
                        BiomeGenBase biome=manager.getUndergroundBiomeAt(x,y,z);
                        if(biome==null||biome.biomeID!=target.biomeID)continue;
                        long dx=x-climate.x,dy=y-climate.y,dz=z-climate.z;
                        long score=dx*dx+dz*dz+dy*dy;
                        if(score<best){best=score;bestX=x;bestY=y;bestZ=z;}
                    }
                }
            }
        if(bestY>=0){climate.teleportX=bestX;climate.teleportZ=bestZ;}
        return bestY;
    }

    private static boolean containsCaveClimate(FuturaWorldChunkManager120 manager,BiomeGenBase target,
                                                int chunkX,int chunkZ,int maxY){
        int minX=chunkX<<4,minZ=chunkZ<<4;
        int[] offsets={2,8,14};
        for(int ox:offsets)for(int oz:offsets)for(int y=2;y<=maxY;y+=8){
            BiomeGenBase biome=manager.getUndergroundBiomeAt(minX+ox,y,minZ+oz);
            if(biome!=null&&biome.biomeID==target.biomeID)return true;
        }
        return false;
    }

    private static boolean isSafeLoaded(World world,int x,int y,int z){
        return WorldSafety.getSafeBlock(world,x,y,z).isAir(world,x,y,z)
                &&WorldSafety.getSafeBlock(world,x,y+1,z).isAir(world,x,y+1,z)
                &&WorldSafety.getSafeBlock(world,x,y-1,z).getMaterial().blocksMovement();
    }

    private static boolean isSafe(World world,int x,int y,int z){
        return world.isAirBlock(x,y,z)&&world.isAirBlock(x,y+1,z)
                &&world.getBlock(x,y-1,z).getMaterial().blocksMovement();
    }

    public static boolean isCaveBiome(BiomeGenBase biome){
        return biome!=null&&isCaveBiomeId(biome.biomeID);
    }

    public static boolean isCaveBiomeId(int id){
        return id==FuturaBiomeIds120.LUSH_CAVES||id==FuturaBiomeIds120.DRIPSTONE_CAVES
                ||id==FuturaBiomeIds120.DEEP_DARK;
    }

    private static String normalize(String text){
        return text.toLowerCase(Locale.ROOT).replace("minecraft:","").replace('_',' ').replace('-',' ').trim();
    }

    private static boolean isInteger(String text){
        try{Integer.parseInt(text);return true;}catch(NumberFormatException ignored){return false;}
    }

    public static final class Result{public final int x,y,z;public final long distanceSq;public int teleportX,teleportZ;
        Result(int x,int z,long distanceSq){this(x,-1,z,distanceSq);}
        Result(int x,int y,int z,long distanceSq){this.x=x;this.y=y;this.z=z;this.distanceSq=distanceSq;
            this.teleportX=x;this.teleportZ=z;}}
    private static final class SearchTimeout extends RuntimeException{private static final long serialVersionUID=1L;}
}
