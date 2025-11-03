package symbols;

import lexer.*;

public class Type extends Word{
    public int width = 0;

    public Type(String s, Tag tag, int w){
        super(s, tag);
        width = w;
    }

    public Type(String s, Tag tag){
        super(s, tag);
        width = -1;
    }

    public static final Type
            voidWord    = new Type("void", Tag.BASIC, 0),
            intWord     = new Type("int", Tag.BASIC, 4),
            boolWord     = new Type("bool", Tag.BASIC, 1),
            floatWord   = new Type("float", Tag.BASIC, 8),
            charWord    = new Type("char", Tag.BASIC, 1),
            stringWord  = new Type("string", Tag.STRING),
            cchannelWord = new Type("c_channel", Tag.BASIC, 4);

    public static boolean numeric(Type p) {
        return (p == Type.charWord || p == Type.intWord || p == Type.floatWord);
    }

    public static Type max(Type p1, Type p2) {
        if (!numeric(p1) || !numeric(p2)) return null;
        else if (p1 == Type.floatWord || p2 == Type.floatWord) return Type.floatWord;
        else if (p1 == Type.intWord   || p2 == Type.intWord)   return Type.intWord;
        else return Type.charWord;
    }
}
