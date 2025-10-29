package inter;

import lexer.*;
import symbols.*;

public class Expr extends Node {
    public Token op;
    public Type type;

    public Expr(Token tok, Type p) {
        op = tok;
        type = p;
    }

    // Gera uma versão simplificada da expressão (em interpretadores simples, pode ser apenas "this")
    public Expr gen() {
        return this;
    }

    // Em compiladores, isso "reduz" expressões temporárias — em interpretadores, é opcional
    public Expr reduce() {
        return this;
    }

    // Para interpretadores: método principal de avaliação
    public Object eval() {
        // Por padrão, não há valor direto — subclasses (como Constant, Arith, Logical...) implementam isso
        error("eval() não implementado para essa expressão.");
        return null;
    }

    @Override
    public String toString() {
        return op.toString();
    }
}
