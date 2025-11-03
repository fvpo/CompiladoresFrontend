package inter;

import lexer.*;
import symbols.*;

public class Unary extends Op {
    public Expr expr;

    public Unary(Token tok, Expr e) {
        super(tok, e.type);  // Op recebe o token e tipo
        expr = e;
    }

    public Object eval() {
        Object val = expr.eval();

        switch (op.tag) {
            case MINUS:  // operador unário negativo
                if (!(val instanceof Number)) {
                    error("operand must be numeric");
                    return null;
                }
                return -((Number) val).doubleValue();

            case NOT:    // operador lógico NOT
                if (!(val instanceof Boolean)) {
                    error("operand must be boolean");
                    return null;
                }
                return !((Boolean) val);

            default:
                error("unknown unary operator: " + op);
                return null;
        }
    }
}
