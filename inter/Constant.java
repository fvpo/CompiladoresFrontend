package inter;

import symbols.*;
import lexer.*;

public class Constant extends Expr {
    private final Object value; // o valor literal

    public Constant(Object val, Type t) {
        super(null, t); // não precisa de Token real para constantes
        value = val;
    }

    @Override
    public Object eval() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    // Métodos auxiliares para criar constantes numéricas ou booleanas
    public static Constant makeNum(int v) {
        return new Constant(v, Type.intWord);
    }

    public static Constant makeReal(double v) {
        return new Constant(v, Type.floatWord);
    }

    public static Constant makeBool(boolean b) {
        return new Constant(b, Type.boolWord);
    }

    public static Constant makeString(String s) {
        return new Constant(s, Type.stringWord);
    }
}
