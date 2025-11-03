package inter;

import symbols.Env;
import symbols.Type;
import java.util.List;

/**
 * Represents a method declared inside a class. Parameters are not fully
 * implemented (empty list supported). The method body is a Stmt.
 */
public class MethodDecl extends Stmt {
    public final String name;
    public final List<String> params; // names of parameters (may be empty)
    public final Stmt body;
    public final Type returnType;

    public MethodDecl(String name, List<String> params, Stmt body, Type returnType) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.returnType = returnType;
    }

    @Override
    public void exec(Env env) {
        // When a method is declared at top-level (if allowed), register it by name
        symbols.Env.putStatic(name, this);
    }
}
