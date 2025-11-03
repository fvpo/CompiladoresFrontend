package symbols;
import lexer.Tag;
/**
 * Represents an array type at the symbol level.
 * An array type is defined by its element type and size.
 */

public class Array extends Type {
    public Type of;   // tipo dos elementos (ex: int, float, etc.)
    public int size;  // número de elementos

    public Array(int size, Type of) {
        // "array" é apenas um nome genérico, Tag.INDEX pode ser um token reservado
        super("array", Tag.INDEX, size * of.width);
        this.size = size;
        this.of = of;
    }
    public Array(Type of, int size) {
        this(size, of);
    }
    @Override
    public String toString() {
        return "[" + size + "] " + of.toString();
    }
}