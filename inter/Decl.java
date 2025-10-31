package inter;

import symbols.Env;

public class Decl extends Stmt {
    public final Id id;
    public final Expr init;

    public Decl(Id id, Expr init) {
        this.id = id;
        this.init = init;
    }

    @Override
    public void exec(Env env) {
        Object value = (init != null) ? init.eval() : null;
        env.put(id.getName(), value); // assume que Env tem método put para registrar variáveis
    }
}