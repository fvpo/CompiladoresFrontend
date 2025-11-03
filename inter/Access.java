package inter;

/**
 * Access (dot) expression: target.name
 */
public class Access extends Expr {
    public final Expr target;
    public final String name;

    public Access(Expr target, String name) {
        // We cannot always statically determine the field type here during parsing.
        // Use a permissive numeric default so member accesses can participate in
        // arithmetic expressions. At runtime the actual values will determine
        // the precise arithmetic path.
        super(null, symbols.Type.floatWord);
        this.target = target;
        this.name = name;
    }

    @Override
    public Object eval() {
        Object obj = target.eval();
        if (obj instanceof ClassInstance) {
            return ((ClassInstance) obj).getField(name);
        }
        // For runtime objects (like CChannel) we could try reflection, but keep simple
        throw new RuntimeException("Acesso inválido: objeto não possui campo '" + name + "'");
    }
}
