import java.lang.reflect.*;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.Tessellator;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Executes the renderer's actual sprite transform without requiring an OpenGL context. */
public final class CrossbowOrientationFixture {
    static int checks;
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    static void near(double a,double b,String message){require(Math.abs(a-b)<1E-6,message+": "+a+" != "+b);}
    public static void main(String[] args)throws Exception {
        String name="com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CrossbowItemRenderer139";
        ClassNode node=new ClassNode();new ClassReader(name).accept(node,0);
        int callers=0;
        for(MethodNode method:node.methods)for(AbstractInsnNode i:method.instructions.toArray()) {
            if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;
                if(call.name.equals("renderHeldSprite")){require(method.name.equals("renderItem")||method.name.equals("renderOffhand"),"held paths only");callers++;}
                if(method.name.equals("renderHeldSprite")){
                    if(call.owner.equals("org/lwjgl/opengl/GL11"))call.owner="CrossbowOrientationFixture$Capture";
                    if(call.name.equals("renderItemIn2D"))call.owner="CrossbowOrientationFixture$Capture";
                }
            }
            if(method.name.equals("renderHeldSprite")&&i instanceof FieldInsnNode&&((FieldInsnNode)i).owner.equals("net/minecraft/client/renderer/Tessellator"))method.instructions.set(i,new InsnNode(Opcodes.ACONST_NULL));
        }
        require(callers==2,"main player/pillager and offhand share orientation correction");
        ClassWriter writer=new ClassWriter(0);node.accept(writer);final byte[] bytes=writer.toByteArray();
        Class<?> type=new ClassLoader(CrossbowOrientationFixture.class.getClassLoader()){
            Class<?> define(){return defineClass(name,bytes,0,bytes.length);}
        }.define();
        Method render=type.getDeclaredMethod("renderHeldSprite",IIcon.class);render.setAccessible(true);
        IIcon icon=(IIcon)Proxy.newProxyInstance(type.getClassLoader(),new Class[]{IIcon.class},(proxy,method,a)->{
            if(method.getReturnType()==float.class)return method.getName().startsWith("getMin")?.2F:.8F;
            if(method.getReturnType()==int.class)return 16;return "fixture";
        });
        render.invoke(null,icon);
        require(Capture.draws==1,"one extrusion");
        System.out.println("PASS crossbow orientation: "+checks+" checks; actual held-sprite bytecode, modern SOUTH/NORTH UV and centred bounds");
    }
    public static final class Capture {
        static double sx=1,sz=1,tx,ty,tz;static int draws;
        public static void glRotatef(float a,float x,float y,float z){near(a,180,"half turn");near(x,0,"axis X");near(y,1,"axis Y");near(z,0,"axis Z");sx=-1;sz=-1;}
        public static void glTranslatef(float x,float y,float z){tx+=sx*x;ty+=y;tz+=sz*z;}
        public static void renderItemIn2D(Tessellator ignored,float u0,float v0,float u1,float v1,int width,int height,float depth){
            draws++;near(depth,1.0/16,"JSON generated thickness");
            // Modern generated SOUTH: x=0 is minU, x=16 is maxU.
            // After conversion this is the legacy back face, with correct +Z normal.
            near(sx+tx,-.5,"SOUTH left");near(tx,.5,"SOUTH right");near(sz*(-depth)+tz,1.0/32,"SOUTH depth");
            near(u1,.2,"SOUTH left UV");near(u0,.8,"SOUTH right UV");
            near(tz,-1.0/32,"NORTH depth");near(ty,-.5,"bottom");near(1+ty,.5,"top");near(v0,.2,"top UV");near(v1,.8,"bottom UV");
        }
    }
}
