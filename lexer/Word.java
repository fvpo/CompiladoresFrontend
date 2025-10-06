package lexer;

public class Word extends Token {
    public final String lexeme;

    public Word(String s, Tag tag) {
        super(tag);
        lexeme = s;
    }

    @Override
    public String toString() {
        return lexeme;
    }


}
