package symbols;

public class Env {
    private Table table;
    private Env prev;

    public Env(Env n) {
        table = new Table();
        prev = n;
    }

    public void put(String name, Type type) {
        table.put(name, type);
    }

    public Type get(String name) {
        for (Env e = this; e != null; e = e.prev) {
            Type found = e.table.get(name);
            if (found != null) return found;
        }
        return null; // não encontrado
    }

    public boolean containsInCurrent(String name) {
        return table.contains(name);
    }
}