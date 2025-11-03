package inter;

import symbols.Env;

/**
 * Return statement for methods/functions. Throws a ReturnException to be
 * caught by the call site and returned to the caller.
 */
public class Return extends Stmt {
    private final Expr expr;

    public Return(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void exec(Env env) {
        Object val = (expr != null) ? expr.eval() : null;
        throw new ReturnException(val);
    }
}
