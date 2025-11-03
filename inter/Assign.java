package inter;

import symbols.Env;

/** Assignment statement. The left-hand side can be an Id (variable) or an Access (field). */
public class Assign extends Stmt {
    private final Expr target;
    private final Expr expr;

    public Assign(Expr target, Expr expr) {
        this.target = target;
        this.expr = expr;
    }

    @Override
    public void exec(Env env) {
        Object val = expr.eval();

        if (target instanceof Id) {
            // variable assignment
            Id id = (Id) target;
            env.setValue(id.getName(), val);
            return;
        }

        if (target instanceof Index) {
            Index ind = (Index) target;
            Object arrObj = ind.target.eval();
            Object idxObj = ind.index.eval();
            if (!(idxObj instanceof Integer)) {
                throw new RuntimeException("Índice não é inteiro: " + idxObj);
            }
            int idx = (Integer) idxObj;
            if (arrObj instanceof Object[]) {
                Object[] a = (Object[]) arrObj;
                if (idx < 0 || idx >= a.length) throw new RuntimeException("Index out of range: " + idx);
                a[idx] = val;
                return;
            }
            throw new RuntimeException("Tentativa de indexar não-array: " + arrObj);
        }

        if (target instanceof Access) {
            Access a = (Access) target;
            Object obj = a.target.eval();
            if (obj instanceof ClassInstance) {
                ((ClassInstance) obj).setField(a.name, val);
                return;
            }
            throw new RuntimeException("Atribuição inválida: alvo não é um objeto de classe");
        }

        throw new RuntimeException("Atribuição inválida: alvo não é atribuível");
    }
}
