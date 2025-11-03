package inter;

import java.util.List;

/** Array literal expression, e.g. {1,2,3} */
public class ArrayLiteral extends Expr {
    private final List<Expr> elems;

    public ArrayLiteral(List<Expr> elems) {
        super(null, null);
        this.elems = elems;
    }

    @Override
    public Object eval() {
        Object[] values = new Object[elems.size()];
        for (int i = 0; i < elems.size(); i++) {
            values[i] = elems.get(i).eval();
        }
        return values;
    }
}
