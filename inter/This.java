package inter;

/** Represents the 'this' expression (reference to the current class instance at runtime). */
public class This extends Expr {
    public This() {
        super(null, null);
    }

    @Override
    public Object eval() {
        Object val = symbols.Env.getStatic("this");
        if (val == null) {
            error("'this' não está definido no ambiente atual");
        }
        return val;
    }

    @Override
    public String toString() {
        return "this";
    }
}
