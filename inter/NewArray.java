package inter;

import symbols.Type;
import symbols.Array;
import java.util.List;

/** Runtime creation of an array with a dynamic size expression. */
public class NewArray extends Expr {
    private final Type elemType;
    private final Expr sizeExpr;

    public NewArray(Type elemType, Expr sizeExpr) {
        super(null, null);
        this.elemType = elemType;
        this.sizeExpr = sizeExpr;
    }

    @Override
    public Object eval() {
        Object szv = sizeExpr.eval();
        if (!(szv instanceof Number)) {
            throw new RuntimeException("Tamanho do array não é inteiro: " + szv);
        }
        int size = ((Number) szv).intValue();
        if (size < 0) throw new RuntimeException("Tamanho de array negativo: " + size);

        Object[] a = new Object[size];
        for (int i = 0; i < size; i++) {
            if (elemType == symbols.Type.intWord) a[i] = 0;
            else if (elemType == symbols.Type.floatWord) a[i] = 0.0;
            else a[i] = null;
        }
        return a;
    }
}
