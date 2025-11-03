package inter;

import symbols.Env;

public class For extends Stmt {
    private final Stmt init;
    private final Expr cond;
    private final Stmt update;
    private final Stmt body;

    public For(Stmt init, Expr cond, Stmt update, Stmt body) {
        this.init = init;
        this.cond = cond;
        this.update = update;
        this.body = body;
    }

    @Override
    public void exec(Env env) {
        for (init.exec(env); (Boolean) cond.eval(); update.exec(env)) {
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