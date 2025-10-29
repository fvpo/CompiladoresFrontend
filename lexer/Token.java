package lexer;

public class Token {
    public final Tag tag;

    public Token(Tag t){
        tag = t;
    }

    @Override
    public String toString() {
        return "<" + tag + ">";
    }
}
