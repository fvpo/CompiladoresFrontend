package inter;

import lexer.*;
import symbols.*;

public class Id extends Expr {
    public int offset; // ainda pode existir se quiser manter compatibilidade com código do livro

    public Id(Word id, Type p, int b) {
        super(id, p);
        offset = b;
    }

    // Retorna o nome da variável
    public String getName() {
        return ((Word) op).lexeme;
    }

    // Avalia o valor da variável no ambiente de execução
    @Override
    public Object eval() {
        Object val = Env.get(getName());
        if (val == null) {
            error("Variável não declarada: " + getName());
        }
        return val;
    }

    // Define um novo valor para a variável (usado em atribuições)
    public void assign(Object value) {
        Env.put(getName(), value);
    }

    @Override
    public String toString() {
        return getName();
    }
}
