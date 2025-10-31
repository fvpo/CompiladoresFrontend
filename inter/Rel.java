package inter;

import lexer.Token;
import symbols.Type;

public class Rel extends Expr {
    public final Expr expr1, expr2;

    public Rel(Token op, Expr e1, Expr e2) {
        super(op, Type.boolWord);
        this.expr1 = e1;
        this.expr2 = e2;
    }

    @Override
    public Object eval() {
        Object v1 = expr1.eval();
        Object v2 = expr2.eval();

        if (v1 instanceof Number && v2 instanceof Number) {
            double n1 = ((Number) v1).doubleValue();
            double n2 = ((Number) v2).doubleValue();

            switch (op.tag) {
                case LT:  return n1 < n2;
                case LE:  return n1 <= n2;
                case GT:  return n1 > n2;
                case GE:  return n1 >= n2;
                case EQ:  return n1 == n2;
                case NE:  return n1 != n2;
            }
        }

        // Comparação genérica como fallback
        switch (op.tag) {
            case EQ:  return v1.equals(v2);
            case NE:  return !v1.equals(v2);
        }

        throw new RuntimeException("Operador relacional inválido: " + op);
    }
}