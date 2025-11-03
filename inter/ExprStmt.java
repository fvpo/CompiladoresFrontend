package inter;

import symbols.Env;

/** Statement wrapper for expressions used as statements, e.g. obj.method(); */
public class ExprStmt extends Stmt {
    private final Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void exec(Env env) {
        expr.eval(); // evaluate and ignore the result
    }
}
