package inter;

import symbols.Env;

public class While extends Stmt {
    private final Expr cond;
    private final Stmt body;

    public While(Expr cond, Stmt body) {
        this.cond = cond;
        this.body = body;
    }

    @Override
    public void exec(Env env) {
        while (true) {
            Object c = cond.eval();
            if (!(c instanceof Boolean b))
                throw new RuntimeException("Condição do while não é booleana");
            if (!b) break;

            try {
                body.exec(env);
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                continue;
            }
        }
    }
}