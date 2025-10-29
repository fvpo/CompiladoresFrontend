package inter;

import symbols.*;

public class Constant extends Expr {
    private final Object value; // valor literal

    public Constant(Object val, Type t) {
        super(null, t);
        value = val;
    }

    @Override
    public Object eval() {
        return value;
    }

    @Override
    public String toString() {
        if (value == null) return "null";
        return value.toString();
    }

    // helpers (opcionais)
    public static Constant fromInt(int v) { return new Constant(v, Type.intWord); }
    public static Constant fromDouble(double v) { return new Constant(v, Type.floatWord); }
    public static Constant fromBool(boolean b) { return new Constant(b, Type.boolWord); }
    public static Constant fromString(String s) { return new Constant(s, Type.stringWord); }
}