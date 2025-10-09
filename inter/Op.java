package inter;

import lexer.*;
import symbols.*;

public class Op extends Expr {

    public Op(Token tok, Type p) {
        super(tok, p);  // Token e tipo resultante
    }

    // Método de avaliação genérico — deve ser sobrescrito por subclasses
    @Override
    public Object eval() {
        error("cannot evaluate generic Op");
        return null;
    }
}
