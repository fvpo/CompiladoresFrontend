package symbols;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime environment for variables. Supports thread-local current environment
 * so expressions (which don't receive an Env) can still access the right
 * environment when executing inside a method or a parallel thread.
 */
public class Env {
    // Thread-local pointer to the current executing Env for this thread
    private static final ThreadLocal<Env> current = new ThreadLocal<>();

    // fallback global table when no current env is bound
    private static final Map<String, Object> global = new HashMap<>();

    private final Map<String, Object> table = new HashMap<>();
    private final Env prev; // lexical parent
    private Env previousThreadEnv; // used when binding/unbinding into ThreadLocal

    public Env(Env n) {
        this.prev = n;
    }

    /** Bind this Env as the current environment for the running thread. */
    public void bindCurrent() {
        previousThreadEnv = current.get();
        current.set(this);
    }

    /** Restore the previous thread-local environment. */
    public void unbindCurrent() {
        current.set(previousThreadEnv);
        previousThreadEnv = null;
    }

    // Instance-level put/get used when an Env is available
    public void put(String name, Object val) {
        table.put(name, val);
    }

    public Object getValue(String name) {
        if (table.containsKey(name)) return table.get(name);
        if (prev != null) return prev.getValue(name);
        return null;
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

    public boolean containsInCurrent(String name) {
        return table.containsKey(name);
    }

    // Static helpers that delegate to the current thread-local Env if bound,
    // otherwise operate on the global fallback table.
    public static void putStatic(String name, Object val) {
        Env e = current.get();
        if (e != null) e.put(name, val);
        else global.put(name, val);
    }

    public static Object getStatic(String name) {
        Env e = current.get();
        if (e != null) return e.getValue(name);
        return global.get(name);
    }

    public static boolean containsInCurrentStatic(String name) {
        Env e = current.get();
        if (e != null) return e.containsInCurrent(name);
        return global.containsKey(name);
    }
}