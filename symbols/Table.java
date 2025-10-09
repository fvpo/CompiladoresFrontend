package symbols;
import java.util.HashMap;
import java.util.Map;

public class Table {
    private Map<String, Type> symbols = new HashMap<>();

    public void put(String name, Type type) {
        symbols.put(name, type);
    }

    public Type get(String name) {
        return symbols.get(name);
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }
  }
