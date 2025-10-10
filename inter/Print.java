package inter;

public class Print extends Stmt {
    private final Expr expr;

    public Print(Expr e) {
        expr = e;
    }

    @Override
    public void exec() {
        Object val = expr.eval();
        System.out.println(val);
    }
}
