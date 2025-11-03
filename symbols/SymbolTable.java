package symbols;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple scoped symbol table for types.
 */
public class SymbolTable {
    private final Deque<Map<String, Type>> stack = new ArrayDeque<>();

    public SymbolTable() {
        // global scope
        stack.push(new HashMap<>());
    }

    public void enter() {
        stack.push(new HashMap<>());
    }

    public void exit() {
        if (stack.size() > 1) stack.pop();
    }

    public void declare(String name, Type type) {
        stack.peek().put(name, type);
    }

    public Type lookup(String name) {
        for (Map<String, Type> frame : stack) {
            if (frame.containsKey(name)) return frame.get(name);
        }
        return null;
    }

    public boolean containsInCurrent(String name) {
        return stack.peek().containsKey(name);
    }
}
