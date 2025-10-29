package symbols;
import java.util.HashMap;
import java.util.Map;

public class Table {
    private Map<String, Object> symbols = new HashMap<>();

    public void put(String name, Object val) {
        symbols.put(name, val);
    }

    public Object get(String name) {
        return symbols.get(name);
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }
  }
