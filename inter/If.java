package inter;

public class If extends Stmt {
    private final Expr cond;
    private final Stmt thenStmt, elseStmt;

    public If(Expr c, Stmt t, Stmt e) {
        cond = c; thenStmt = t; elseStmt = e;
    }

    @Override
    public void exec() {
        Object c = cond.eval();
        if (!(c instanceof Boolean)) {
            error("condição do if deve ser booleana");
        }
        if ((Boolean) c) thenStmt.exec();
        else if (elseStmt != null) elseStmt.exec();
    }
}
