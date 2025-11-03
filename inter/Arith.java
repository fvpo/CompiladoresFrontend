package inter;

import lexer.*;
import symbols.*;

public class Arith extends Op {
    public Expr expr1, expr2;

    public Arith(Token tok, Expr x1, Expr x2) {
        super(tok, Type.max(x1.type, x2.type));  // tipo resultante
        expr1 = x1;
        expr2 = x2;
        if (type == null) {
            String t1 = (x1.type == null) ? "null" : x1.type.toString();
            String t2 = (x2.type == null) ? "null" : x2.type.toString();
            error("incompatible types in arithmetic operation: " + t1 + " and " + t2 + " (token=" + tok + ")");
        }
    }

    @Override
    public Object eval() {
        Object v1 = expr1.eval();
        Object v2 = expr2.eval();

        // Se ambos são inteiros, faz operação inteira
        if (v1 instanceof Integer && v2 instanceof Integer) {
            int a = (Integer) v1;
            int b = (Integer) v2;

            switch (op.tag) {
                case PLUS:  return a + b;
                case MINUS: return a - b;
                case MULT:  return a * b;
                case DIV:   return a / b; // divisão inteira
                case MOD:   return a % b;
                default:
                    error("unknown arithmetic operator: " + op);
                    return null;
            }
        }

        // Caso contrário, promove para double
        double a = ((Number)v1).doubleValue();
        double b = ((Number)v2).doubleValue();

        switch (op.tag) {
            case PLUS:  return a + b;
            case MINUS: return a - b;
            case MULT:  return a * b;
            case DIV:   return a / b;
            case MOD:   return a % b;
            default:
                error("unknown arithmetic operator: " + op);
                return null;
        }
    }
}
