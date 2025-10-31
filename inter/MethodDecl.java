package inter;

import symbols.Env;
import java.util.List;

/**
 * Represents a method declared inside a class. Parameters are not fully
 * implemented (empty list supported). The method body is a Stmt.
 */
public class MethodDecl extends Stmt {
    public final String name;
    public final List<String> params; // names of parameters (may be empty)
    public final Stmt body;

    public MethodDecl(String name, List<String> params, Stmt body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    @Override
    public void exec(Env env) {
        // When a method is declared at top-level (if allowed), register it by name
        Env.put(name, this);
    }
}
