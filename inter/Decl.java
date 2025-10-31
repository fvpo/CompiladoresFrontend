package inter;

import lexer.*;
import symbols.*;

/**
 * Represents a variable declaration with an identifier and an optional initializer expression.
 * Minimal implementation so Parser can construct Decl nodes; extend with code generation / semantic checks as needed.
 */
public class Decl extends Stmt {
    public final Id id;
    public final Expr init;

    public Decl(Id id, Expr init) {
        this.id = id;
        this.init = init;
    }

    @Override
    public void exec(Env env) {
        env.put(id.toString(), this);
    }
    public String toString() {
        if (init != null) {
            return id.toString() + " = " + init.toString();
        } else {
            return id.toString();
        }
    } 
}