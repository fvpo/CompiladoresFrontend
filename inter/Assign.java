package inter;

import symbols.*;

public class Assign extends Stmt {
    private final Id id;
    private final Expr expr;

    public Assign(Id i, Expr e) {
        id = i;
        expr = e;
    }

    @Override
    public void exec(Env env) {
        Object val = expr.eval(env);
        env.setValue(id.toString(), val);
    }
}
