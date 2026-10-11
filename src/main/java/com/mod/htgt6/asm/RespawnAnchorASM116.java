package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Preserve the Forge respawn transaction; only substitute anchor-specific decisions. */
public final class RespawnAnchorASM116 {
    private static final String H="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/RespawnAnchor116";
    private static final String P="Lnet/minecraft/entity/player/EntityPlayerMP;", C="Lnet/minecraft/util/ChunkCoordinates;";
    public static byte[] transform(byte[] bytes,boolean player){
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int found=0;
        boolean dimensionMethod=false;for(MethodNode m:c.methods)if(m.name.equals("setSpawnChunk")&&m.desc.equals("("+C+"ZI)V"))dimensionMethod=true;
        for(MethodNode m:c.methods){
            boolean hybrid=!player&&m.desc.equals("("+P+"IZLorg/bukkit/Location;Z)"+P);
            boolean match=player?(m.name.equals("setSpawnChunk")||m.name.equals("func_71063_a"))&&m.desc.equals("("+C+(dimensionMethod?"ZI)V":"Z)V")):
                (m.name.equals("respawnPlayer")||m.name.equals("func_72368_a"))&&(m.desc.equals("("+P+"IZ)"+P)||hybrid);
            if(!match)continue;found++;
            for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(H))return bytes;
            InsnList enter=new InsnList();
            if(player){enter.add(new VarInsnNode(Opcodes.ALOAD,0));enter.add(new VarInsnNode(Opcodes.ALOAD,1));enter.add(new VarInsnNode(Opcodes.ILOAD,2));if(dimensionMethod)enter.add(new VarInsnNode(Opcodes.ILOAD,3));else{enter.add(new VarInsnNode(Opcodes.ALOAD,0));enter.add(new FieldInsnNode(Opcodes.GETFIELD,"net/minecraft/entity/player/EntityPlayer",m.name.equals("func_71063_a")?"field_71093_bK":"dimension","I"));}enter.add(call("spawnChanged","(Lnet/minecraft/entity/player/EntityPlayer;"+C+"ZI)V"));}
            else{
                enter.add(new VarInsnNode(Opcodes.ALOAD,1));enter.add(new VarInsnNode(Opcodes.ILOAD,2));enter.add(new VarInsnNode(Opcodes.ILOAD,3));if(hybrid){enter.add(new VarInsnNode(Opcodes.ALOAD,4));enter.add(call("prepareHybrid","("+P+"IZLjava/lang/Object;)I"));}else enter.add(call("prepare","("+P+"IZ)I"));enter.add(new VarInsnNode(Opcodes.ISTORE,2));
                int replaced=0,returns=0,events=0;
                for(AbstractInsnNode i:m.instructions.toArray()){
                    if(i.getOpcode()==Opcodes.ARETURN){InsnList finish=new InsnList();finish.add(new InsnNode(Opcodes.DUP));finish.add(new VarInsnNode(Opcodes.ALOAD,1));finish.add(new VarInsnNode(Opcodes.ILOAD,3));finish.add(call(hybrid?"finishHybrid":"finish","("+P+P+"Z)V"));m.instructions.insertBefore(i,finish);returns++;}
                    if(hybrid&&i instanceof TypeInsnNode&&i.getOpcode()==Opcodes.NEW&&((TypeInsnNode)i).desc.equals("org/bukkit/event/player/PlayerRespawnEvent")){
                        // Amend the proposed location BEFORE plugins inspect or replace it.
                        InsnList location=new InsnList();String[] axes={"X","Y","Z"};
                        for(int axis=0;axis<3;axis++){
                            location.add(new VarInsnNode(Opcodes.ALOAD,4));location.add(new InsnNode(Opcodes.DUP));
                            location.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"org/bukkit/Location","get"+axes[axis],"()D",false));
                            location.add(new VarInsnNode(Opcodes.ALOAD,1));location.add(new InsnNode(Opcodes.ICONST_0+axis));
                            location.add(call("hybridCoordinate","(D"+P+"I)D"));
                            location.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"org/bukkit/Location","set"+axes[axis],"(D)V",false));
                        }
                        m.instructions.insertBefore(i,location);events++;
                    }
                    if(!(i instanceof MethodInsnNode))continue;MethodInsnNode n=(MethodInsnNode)i;String hook=null,desc=null;
                    if((n.name.equals("canRespawnHere")||n.name.equals("func_76567_e"))&&n.desc.equals("()Z")){hook="canRespawn";desc="(Lnet/minecraft/world/WorldProvider;"+P+")Z";}
                    if(n.name.equals("getBedLocation")&&n.desc.equals("(I)"+C)){n.setOpcode(Opcodes.INVOKESTATIC);n.owner=H;n.name="spawnPosition";n.desc="("+P+"I)"+C;n.itf=false;replaced++;continue;}
                    if((n.name.equals("verifyRespawnCoordinates")||n.name.equals("func_71056_a"))&&n.desc.equals("(Lnet/minecraft/world/World;"+C+"Z)"+C)){hook="resolve";desc="(Lnet/minecraft/world/World;"+C+"Z"+P+")"+C;}
                    if((n.name.equals("setLocationAndAngles")||n.name.equals("func_70012_b"))&&n.desc.equals("(DDDFF)V")){hook="place";desc="("+P+"DDDFF"+P+")V";}
                    if(hook!=null){m.instructions.insertBefore(n,new VarInsnNode(Opcodes.ALOAD,1));m.instructions.set(n,call(hook,desc));replaced++;}
                }
                if(replaced!=4||returns!=(hybrid?3:1)||events!=(hybrid?1:0))throw new IllegalStateException("Respawn anchor call sites="+replaced+", returns="+returns+", events="+events);
            }m.instructions.insert(enter);
        }
        if(found!=1)throw new IllegalStateException("Respawn anchor method count="+found+", player="+player);
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);return out.toByteArray();
    }
    private static MethodInsnNode call(String n,String d){return new MethodInsnNode(Opcodes.INVOKESTATIC,H,n,d,false);}
    private RespawnAnchorASM116(){}
}
