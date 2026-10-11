import io.github.crucible.util.HashedArrayList;
import java.lang.reflect.Field;
import java.util.*;

/** Read-only audit: executes the original kernel class, not a reimplementation. */
public final class CrucibleCollectionAudit {
    public static void main(String[] args) throws Exception {
        HashedArrayList<Object> list = new HashedArrayList<Object>();
        Object oldValue = new Object(), replacement = new Object();
        list.add(oldValue);
        try {
            list.listIterator();
            throw new AssertionError("Expected recursive iterator defect");
        } catch (StackOverflowError expected) {
            System.out.println("CONFIRMED: listIterator() causes StackOverflowError");
        }
        list.set(0, replacement);
        Field field = HashedArrayList.class.getDeclaredField("hashed");
        field.setAccessible(true);
        Set<?> retained = (Set<?>) field.get(list);
        if (!retained.contains(oldValue) || retained.contains(replacement))
            throw new AssertionError("Unexpected set behavior");
        System.out.println("CONFIRMED: set retains replaced object and omits replacement in hashed set");
        list.clear();
        list.add(oldValue);
        list.addAll(Arrays.asList(oldValue, replacement));
        list.remove(oldValue);
        if (list.indexOf(oldValue) < 0 || list.contains(oldValue))
            throw new AssertionError("Unexpected bulk-add behavior");
        System.out.println("CONFIRMED: addAll duplicates an existing object; remove leaves it behind but contains returns false");
        list.clear();
        list.add(oldValue);
        list.subList(0, 1).clear();
        if (!list.isEmpty() || !retained.contains(oldValue))
            throw new AssertionError("Unexpected subList behavior");
        System.out.println("CONFIRMED: subList.clear removes list entries but retains objects in hashed set");
        list.clear();
        list.add(oldValue);
        list.removeIf(value -> true);
        if (!list.isEmpty() || !retained.contains(oldValue))
            throw new AssertionError("Unexpected removeIf behavior");
        System.out.println("CONFIRMED: Java 8 removeIf retains removed objects in hashed set");
        list.clear();
        System.out.println("Audit complete; no kernel files modified");
    }
}
