package symbols;
import lexer.Tag;

/**
 * Represents a user-declared class type at the symbol level.
 * This is a lightweight Type wrapper identified by name.
 */
public class ClassType extends Type {
    public ClassType(String name) {
        super(name, Tag.CLASS);
    }
}
