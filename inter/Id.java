package inter;

import lexer.*;
import symbols.*;

public class Id extends Expr {
    public int offset;   // compatibilidade com o livro
    public Type type;    // tipo do identificador (pode ser primitivo ou Array)

    public Id(Word id, Type p, int b) {
        super(id, p);
        this.type = p;
        this.offset = b;
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

    // Verifica se este identificador é um array
    public boolean isArray() {
        return type instanceof Array;
    }

    // Retorna o tipo array, se for
    public Array asArray() {
        if (isArray()) {
            return (Array) type;
        }
        throw new IllegalStateException("Identificador não é um array: " + getName());
    }

    @Override
    public String toString() {
        if (isArray()) {
            Array arr = (Array) type;
            return "Id(nome=" + getName() +
                   ", tipoBase=" + arr.of +
                   ", tamanho=" + arr.size +
                   ", offset=" + offset + ")";
        } else {
            return "Id(nome=" + getName() +
                   ", tipo=" + type +
                   ", offset=" + offset + ")";
        }
    }
}
