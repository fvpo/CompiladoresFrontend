package inter;

import symbols.Env;

public class Send extends Stmt {
    private final Id channel;
    private final Expr expr;

    public Send(Id channel, Expr expr) {
        this.channel = channel;
        this.expr = expr;
    }
    @Override
    public void exec() {
        throw new UnsupportedOperationException("Use exec(Env env) instead.");
    }
    public void exec(Env env) {
        Object ch = env.getValue(channel.toString());
        if (!(ch instanceof CChannel)) {
            throw new RuntimeException("Variável não é um canal: " + channel);
        }
        ((CChannel) ch).send(expr.eval());
    }
}