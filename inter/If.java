package inter;

import symbols.Env;

public class If extends Stmt {
    private final Expr cond;
    private final Stmt thenStmt, elseStmt;

    public If(Expr c, Stmt t, Stmt e) {
        cond = c; thenStmt = t; elseStmt = e;
    }

    @Override
    public void exec(Env env) {
        Object c = cond.eval();
        if (!(c instanceof Boolean)) {
            error("condição do if deve ser booleana");
        }
        if ((Boolean) c) thenStmt.exec(env);
        else if (elseStmt != null) elseStmt.exec(env);
    }
}
