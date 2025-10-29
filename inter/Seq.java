package inter;

import symbols.Env;

import java.util.List;

public class Seq extends Stmt {
    private final List<Stmt> stmts;

    public Seq(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    @Override
    public void exec(Env env) {
        for (Stmt s : stmts) {
            if (s != null) s.exec(env);
        }
    }
}
