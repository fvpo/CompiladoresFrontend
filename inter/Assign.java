package inter;

public class Assign extends Stmt {
    private final Id id;
    private final Expr expr;

    public Assign(Id i, Expr e) {
        id = i;
        expr = e;
    }

    @Override
    public void exec() {
        Object val = expr.eval();
        id.assign(val);
    }
}
