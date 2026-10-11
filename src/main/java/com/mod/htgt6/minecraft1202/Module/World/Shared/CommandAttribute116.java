package com.mod.htgt6.minecraft1202.Module.World.Shared;
import java.util.*;
import net.minecraft.command.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
/** Attribute operations on the native synchronized and saved attribute map. */
public final class CommandAttribute116 extends CommandBase {
    public String getCommandName(){return "attribute";}
    public String getCommandUsage(ICommandSender sender){return "/attribute <target> <attribute> get [scale] | base get [scale] | base set <value> | modifier add <uuid> <name> <value> <add|multiply_base|multiply> | modifier remove <uuid> | modifier value get <uuid> [scale]";}
    public int getRequiredPermissionLevel(){return 2;}
    public void processCommand(ICommandSender sender,String[] args){if(args.length<3)throw new WrongUsageException(getCommandUsage(sender));EntityLivingBase target=target(sender,args[0]);double result=apply(target,args[1],Arrays.copyOfRange(args,2,args.length));sender.addChatMessage(new ChatComponentText(target.getCommandSenderName()+": "+args[1]+" = "+result));}
    public static double apply(EntityLivingBase target,String name,String[] args){
        IAttributeInstance attribute=target.getAttributeMap().getAttributeInstanceByName(name);if(attribute==null)attribute=target.getAttributeMap().getAttributeInstanceByName(legacyName(name));if(attribute==null)throw new CommandException("Attribute is not registered on this entity: "+name);
        if(args.length>=1&&args[0].equals("get")&&args.length<=2)return attribute.getAttributeValue()*(args.length==2?number(args[1]):1);
        if(args.length>=2&&args[0].equals("base")){
            if(args[1].equals("get")&&args.length<=3)return attribute.getBaseValue()*(args.length==3?number(args[2]):1);
            if(args[1].equals("set")&&args.length==3){attribute.setBaseValue(number(args[2]));return attribute.getBaseValue();}
        }
        if(args.length>=3&&args[0].equals("modifier")){
            if(args[1].equals("add")&&args.length>=6){UUID id=id(args[2]);if(attribute.getModifier(id)!=null)throw new CommandException("Attribute modifier already exists: "+id);double amount=number(args[args.length-2]);String operation=args[args.length-1];int op=operation.equals("add")?0:operation.equals("multiply_base")?1:operation.equals("multiply")?2:-1;if(op<0)throw new CommandException("Unknown modifier operation: "+operation);
                StringBuilder label=new StringBuilder();for(int i=3;i<args.length-2;i++){if(label.length()>0)label.append(' ');label.append(args[i]);}String display=label.toString();if(display.length()>=2&&display.startsWith("\"")&&display.endsWith("\""))display=display.substring(1,display.length()-1);if(display.isEmpty())throw new CommandException("Modifier name is empty");attribute.applyModifier(new AttributeModifier(id,display,amount,op).setSaved(true));return attribute.getAttributeValue();}
            if(args[1].equals("remove")&&args.length==3){AttributeModifier modifier=attribute.getModifier(id(args[2]));if(modifier==null)throw new CommandException("Attribute modifier does not exist: "+args[2]);attribute.removeModifier(modifier);return attribute.getAttributeValue();}
            if(args.length>=4&&args.length<=5&&args[1].equals("value")&&args[2].equals("get")){AttributeModifier modifier=attribute.getModifier(id(args[3]));if(modifier==null)throw new CommandException("Attribute modifier does not exist: "+args[3]);return modifier.getAmount()*(args.length==5?number(args[4]):1);}
        }
        throw new WrongUsageException("Invalid attribute operation");
    }
    private static double number(String value){try{double d=Double.parseDouble(value);if(Double.isNaN(d)||Double.isInfinite(d))throw new NumberFormatException();return d;}catch(NumberFormatException e){throw new CommandException("Expected a finite number: "+value);}}
    private static UUID id(String value){try{return UUID.fromString(value);}catch(IllegalArgumentException e){throw new CommandException("Invalid modifier UUID: "+value);}}
    public static String legacyName(String name){if(name.startsWith("minecraft:"))name=name.substring(10);StringBuilder result=new StringBuilder();boolean upper=false;for(char c:name.toCharArray()){if(c=='_'){upper=true;continue;}result.append(upper?Character.toUpperCase(c):c);upper=false;}return result.toString();}
    private static String modernName(String name){StringBuilder out=new StringBuilder("minecraft:");for(char c:name.toCharArray()){if(Character.isUpperCase(c))out.append('_');out.append(Character.toLowerCase(c));}return out.toString();}
    public static EntityLivingBase target(ICommandSender sender,String token){
        if(token.equals("@s")){if(sender instanceof EntityLivingBase)return (EntityLivingBase)sender;throw new CommandException("This sender is not a living entity");}
        if(token.equals("@e")||token.startsWith("@e["))return entitySelector(sender,token);
        if(token.startsWith("@")){EntityPlayerMP[] players=PlayerSelector.matchPlayers(sender,token);if(players==null||players.length!=1)throw new CommandException("Target must select exactly one entity");return players[0];}
        World world=sender.getEntityWorld();UUID uuid=null;try{uuid=UUID.fromString(token);}catch(IllegalArgumentException ignored){}
        for(Object value:world.loadedEntityList)if(value instanceof EntityLivingBase){EntityLivingBase e=(EntityLivingBase)value;if(uuid!=null?uuid.equals(e.getUniqueID()):e instanceof EntityPlayer&&token.equals(e.getCommandSenderName()))return e;}
        if(uuid!=null)for(net.minecraft.world.WorldServer other:net.minecraftforge.common.DimensionManager.getWorlds())if(other!=world)for(Object value:other.loadedEntityList)if(value instanceof EntityLivingBase&&uuid.equals(((Entity)value).getUniqueID()))return (EntityLivingBase)value;
        throw new CommandException("Living entity not found: "+token);
    }
    private static EntityLivingBase entitySelector(ICommandSender sender,String token){
        Map<String,String> filters=new HashMap<String,String>();if(!token.equals("@e")){if(!token.endsWith("]"))throw new CommandException("Invalid entity selector");String body=token.substring(3,token.length()-1);if(!body.isEmpty())for(String part:body.split(",")){int eq=part.indexOf('=');if(eq<1)throw new CommandException("Invalid selector argument: "+part);String key=part.substring(0,eq);if(!Arrays.asList("type","name","distance","limit","sort").contains(key)||filters.put(key,part.substring(eq+1))!=null)throw new CommandException("Unsupported or duplicate selector argument: "+key);}}
        int limit=Integer.MAX_VALUE;if(filters.containsKey("limit")){double value=number(filters.get("limit"));if(value<1||value!=Math.floor(value)||value>Integer.MAX_VALUE)throw new CommandException("Invalid selector limit");limit=(int)value;}
        String sort=filters.containsKey("sort")?filters.get("sort"):"arbitrary";if(!Arrays.asList("arbitrary","nearest","furthest","random").contains(sort))throw new CommandException("Invalid selector sort");
        final double x=sender instanceof Entity?((Entity)sender).posX:sender.getPlayerCoordinates().posX,y=sender instanceof Entity?((Entity)sender).posY:sender.getPlayerCoordinates().posY,z=sender instanceof Entity?((Entity)sender).posZ:sender.getPlayerCoordinates().posZ;
        double min=0,max=Double.POSITIVE_INFINITY;if(filters.containsKey("distance")){String value=filters.get("distance");int dots=value.indexOf("..");if(dots<0)min=max=number(value);else{if(dots>0)min=number(value.substring(0,dots));if(dots+2<value.length())max=number(value.substring(dots+2));}if(min<0||max<min)throw new CommandException("Invalid distance range");}
        List<Entity> matches=new ArrayList<Entity>();for(Object value:sender.getEntityWorld().loadedEntityList){Entity e=(Entity)value;if(!e.isEntityAlive())continue;double distance=e.getDistanceSq(x,y,z);if(distance<min*min||distance>max*max)continue;
            if(filters.containsKey("name")&&!test(filters.get("name"),e.getCommandSenderName(),false))continue;
            if(filters.containsKey("type")){String type=filters.get("type"),actual=e instanceof EntityPlayer?"player":EntityList.getEntityString(e);String own=e.getClass().getSimpleName().replaceFirst("^Entity","").replaceFirst("116$","");boolean negative=type.startsWith("!");String wanted=negative?type.substring(1):type;boolean hit=test(wanted,actual,true)||e.getClass().getName().startsWith("com.mod.htgt6.minecraft1202.")&&test(wanted,own,true);if(hit==negative)continue;}
            matches.add(e);
        }
        if(sort.equals("nearest")||sort.equals("furthest")){final int order=sort.equals("nearest")?1:-1;Collections.sort(matches,(a,b)->order*Double.compare(a.getDistanceSq(x,y,z),b.getDistanceSq(x,y,z)));}else if(sort.equals("random"))Collections.shuffle(matches,sender.getEntityWorld().rand);
        if(Math.min(matches.size(),limit)!=1||!(matches.get(0) instanceof EntityLivingBase))throw new CommandException("Target must select exactly one living entity");return (EntityLivingBase)matches.get(0);
    }
    private static boolean test(String wanted,String actual,boolean type){boolean negative=wanted.startsWith("!");if(negative)wanted=wanted.substring(1);boolean match=actual!=null&&(type?normalize(wanted).equals(normalize(actual)):wanted.equals(actual));return match!=negative;}
    private static String normalize(String text){int colon=text.indexOf(':');if(colon>=0)text=text.substring(colon+1);return text.replaceAll("[^A-Za-z0-9]","").toLowerCase(Locale.ROOT);}
    public List addTabCompletionOptions(ICommandSender sender,String[] args){
        if(args.length==1)return getListOfStringsMatchingLastWord(args,"@s","@p","@e[limit=1,sort=nearest]");
        if(args.length==2){try{List<String> names=new ArrayList<String>();for(Object value:target(sender,args[0]).getAttributeMap().getAllAttributes())names.add(modernName(((IAttributeInstance)value).getAttribute().getAttributeUnlocalizedName()));return getListOfStringsFromIterableMatchingLastWord(args,names);}catch(CommandException ignored){return null;}}
        if(args.length==3)return getListOfStringsMatchingLastWord(args,"get","base","modifier");if(args.length==4)return getListOfStringsMatchingLastWord(args,args[2].equals("base")?new String[]{"get","set"}:new String[]{"add","remove","value"});if(args.length>=8&&args[2].equals("modifier")&&args[3].equals("add"))return getListOfStringsMatchingLastWord(args,"add","multiply_base","multiply");return null;
    }
}
