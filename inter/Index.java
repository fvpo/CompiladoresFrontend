package inter;

/** Index expression: target[index] */
public class Index extends Expr {
    public final Expr target;
    public final Expr index;

    public Index(Expr target, Expr index) {
    // Determine element type from the target's type if available
    super(target != null ? target.op : null,
        // if target has an array type, extract its element type; otherwise leave null
        (target != null && target.type != null && target.type instanceof symbols.Array) ? ((symbols.Array) target.type).of
            : (target != null && target.type != null && target.type instanceof symbols.Arrays) ? ((symbols.Arrays) target.type).of
            : null);
    this.target = target;
    this.index = index;
    }

    @Override
    public Object eval() {
        Object arr = target.eval();
        Object idxObj = index.eval();
        if (!(idxObj instanceof Integer)) {
            throw new RuntimeException("Índice não é inteiro: " + idxObj);
        }
        int idx = (Integer) idxObj;
        if (arr instanceof Object[]) {
            Object[] a = (Object[]) arr;
            if (idx < 0 || idx >= a.length) throw new RuntimeException("Index out of range: " + idx);
            return a[idx];
        }
        throw new RuntimeException("Tentativa de indexar não-array: " + arr);
    }
}
