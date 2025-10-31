package symbols;

import java.util.HashMap;
import java.util.Map;

public class Env {
    private static final Map<String, Object> table = new HashMap<>();
    private Env prev;

    public Env(Env n) {
        prev = n;
    }

    public static void put(String name, Object val) {
        table.put(name, val);
    }

    public static Object getValue(String name) {
        return table.get(name);
    }

    public void setValue(String name, Object value) {
        if (table.containsKey(name)) {
            table.put(name, value);
        } else if (prev != null) {
            prev.setValue(name, value);
        } else {
            throw new RuntimeException("Variável não declarada: " + name);
        }
    }

    public static Object get(String name) {
        Object found = table.get(name);
        if (found != null) {
            return found;
        } else {
            return null; // não encontrado
        }
    }

    public static boolean containsInCurrent(String name) {
        return table.containsKey(name);
    }

    /** Remove a symbol from the global table (used to restore bindings after calls). */
    public static void remove(String name) {
        table.remove(name);
    }
}