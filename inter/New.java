package inter;

import lexer.*;
import symbols.*;
import java.util.List;

public class New extends Expr {
    private final String className;
    private final List<Expr> args;

    public New(String className, List<Expr> args) {
        super(Word.newWord, Type.voidWord); // chamada ao construtor de Expr
        this.className = className;
        this.args = args;
    }

    @Override
    public Object eval() {
        switch (className) {
            case "CChannel":
                return new CChannel();
            default:
                throw new RuntimeException("Classe não suportada: " + className);
        }
    }
}