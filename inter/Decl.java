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
        Object value;

        if (init != null) {
            value = init.eval();
            if (value == null && id.type == symbols.Type.cchannelWord) {
                // For channel type, create a new channel
                value = new java.util.concurrent.LinkedBlockingQueue<>();
            }
        } else if (id.type instanceof symbols.Array arrType) {
            // Inicializa o array com tamanho e tipo padrão
            int size = arrType.size;
            Object[] elems = new Object[size];
            for (int i = 0; i < size; i++) {
                // valor padrão do tipo base (ex: 0 para int, 0.0 para float)
                if (arrType.of == symbols.Type.intWord) {
                    elems[i] = 0;
                } else if (arrType.of == symbols.Type.floatWord && arrType.of != null) {
                    elems[i] = 0.0;
                } else {
                    elems[i] = null;
                }
            }
            value = elems;
        } else {
            // Valor padrão para tipos primitivos
            if (id.type == symbols.Type.intWord) {
                value = 0;
            } else if (id.type == symbols.Type.floatWord) {
                value = 0.0;
            } else if (id.type == symbols.Type.cchannelWord) {
                value = new java.util.concurrent.LinkedBlockingQueue<>();
            } else {
                value = null;
            }
        }
        
        Env.put(id.getName(), value);

        env.put(id.getName(), value);
    }
}