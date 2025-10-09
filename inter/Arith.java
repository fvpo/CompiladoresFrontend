package inter;

import lexer.*;
import symbols.*;

public class Arith extends Op {
    public Expr expr1, expr2;

    public Arith(Token tok, Expr x1, Expr x2) {
        super(tok, Type.max(x1.type, x2.type));  // tipo resultante
        expr1 = x1;
        expr2 = x2;
        if (type == null)
            error("incompatible types in arithmetic operation");
    }

    @Override
    public Object eval() {
        Object v1 = expr1.eval();
        Object v2 = expr2.eval();

        switch (op.tag) {
            case Tag.PLUS:  return ((Number)v1).doubleValue() + ((Number)v2).doubleValue();
            case Tag.MINUS: return ((Number)v1).doubleValue() - ((Number)v2).doubleValue();
            case Tag.MULT:  return ((Number)v1).doubleValue() * ((Number)v2).doubleValue();
            case Tag.DIV:   return ((Number)v1).doubleValue() / ((Number)v2).doubleValue();
            case Tag.MOD:   return ((Number)v1).doubleValue() % ((Number)v2).doubleValue();
            default:
                error("unknown arithmetic operator: " + op);
                return null;
        }
    }
}
