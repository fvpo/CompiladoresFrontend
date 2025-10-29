package inter;

import lexer.*;
import symbols.*;

public class Text extends Expr {
    public final String value;

    public Text(String value) {
        super(new Word(value, Tag.TEXT), Type.stringWord);
        this.value = value;
    }

    @Override
    public Object eval() {
        return value;
    }
}