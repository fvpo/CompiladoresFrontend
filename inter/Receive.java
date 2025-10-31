package inter;

import symbols.Env;

public class Receive extends Stmt {
    private final Id channel;
    private final Id target; // variável onde armazenar o valor recebido

    public Receive(Id channel, Id target) {
        this.channel = channel;
        this.target = target;
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
        Object value = ((CChannel) ch).receive();
        env.put(target.toString(), value);
    }
}