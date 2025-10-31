package inter;

/**
 * Internal unchecked exception used to implement 'return' semantics in the interpreter.
 * Carries the return value from a method/function body up to the call site.
 */
public class ReturnException extends RuntimeException {
    private final Object value;

    public ReturnException(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
