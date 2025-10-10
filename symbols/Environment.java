package symbols;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private static final Map<String, Object> table = new HashMap<>();

    public static Object getValue(String name) {
        return table.get(name);
    }

    public static void setValue(String name, Object value) {
        table.put(name, value);
    }

    public static boolean contains(String name) {
        return table.containsKey(name);
    }

    public static void clear() {
        table.clear();
    }
}
