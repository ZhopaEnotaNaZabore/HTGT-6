import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

/** Boundary rolls exercise both parent orders and the exact modern mutation threshold. */
public final class LlamaBreeding76Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        EntityLlama112 first = new EntityLlama112(world), second = new EntityLlama112(world);
        Field random = Entity.class.getDeclaredField("rand");
        random.setAccessible(true);
        int cases = 0;
        for (int a = 1; a <= 5; a++) for (int b = 1; b <= 5; b++) {
            state(first, a, 1); state(second, b, 3);
            for (int roll = 0; roll < Math.max(a,b); roll++) {
                for (float chance : new float[] {0.029999F, 0.03F}) {
                    for (boolean parent : new boolean[] {false,true}) {
                        random.set(first, new Rolls(Math.max(a,b), roll, chance, parent));
                        EntityLlama112 child = (EntityLlama112)first.createChild(second);
                        require(child.getStrength() == Math.min(5, roll+1+(chance < 0.03F ? 1:0)), "strength inheritance");
                        require(child.getVariant() == (parent ? 1:3), "variant inheritance");
                        require(first.getStrength() == a && second.getStrength() == b, "parents changed");
                        cases++;
                    }
                }
            }
        }
        System.out.println("PASS: llama inheritance " + cases + " boundary cases, both parents, 3% threshold, cap and variants");
    }
    private static void state(EntityLlama112 llama, int strength, int variant) {
        NBTTagCompound tag = new NBTTagCompound();
        llama.writeEntityToNBT(tag);
        tag.setInteger("Strength",strength); tag.setInteger("Variant",variant);
        llama.readEntityFromNBT(tag);
    }
    private static final class Rolls extends Random {
        private final int bound, roll; private final float chance; private final boolean parent;
        Rolls(int bound,int roll,float chance,boolean parent) {this.bound=bound;this.roll=roll;this.chance=chance;this.parent=parent;}
        @Override public int nextInt(int n) {require(n==bound,"must use stronger parent's strength"); return roll;}
        @Override public float nextFloat() {return chance;}
        @Override public boolean nextBoolean() {return parent;}
    }
    private static void require(boolean value,String message) {if(!value)throw new AssertionError(message);}
}
