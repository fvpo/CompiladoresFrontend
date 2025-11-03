package inter;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple runtime representation of an instance of a user-declared class.
 * Keeps a map of field names to values. Initialization of fields declared
 * in the class (Decl members) is set to null here; member initializers
 * could be evaluated with a proper environment if needed later.
 */
public class ClassInstance {
    private final ClassDecl decl;
    private final Map<String, Object> fields;

    public ClassInstance(ClassDecl decl) {
        this.decl = decl;
        this.fields = new HashMap<>();

        // initialize declared fields to null (or default)
        if (decl.members != null) {
            for (Stmt s : decl.members) {
                if (s instanceof Decl) {
                    Decl d = (Decl) s;
                    String name = d.id.getName();
                    fields.put(name, null);
                }
            }
        }
    }

    public ClassDecl getDecl() {
        return decl;
    }

    public Object getField(String name) {
        return fields.get(name);
    }

    public void setField(String name, Object value) {
        if (!fields.containsKey(name)) {
            throw new RuntimeException("Campo não declarado: " + name);
        }
        fields.put(name, value);
    }

    @Override
    public String toString() {
        return "instance of " + decl.name;
    }
}
