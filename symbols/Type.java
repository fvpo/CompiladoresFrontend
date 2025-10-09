package symbols;

import lexer.*;

public class Type extends Word{
    public int width = 0;

    public Type(String s, Tag tag, int w){
        super(s, tag);
    }

    public static final Type
            voidWord    = new Type("void", Tag.BASIC, 0),
            intWord     = new Type("int", Tag.BASIC, 4),
            boolWord     = new Type("bool", Tag.BASIC, 1),
            floatWord   = new Type("float", Tag.BASIC, 8),
            charWord = new Type("char", Tag.BASIC, 1),
            cchannelWord = new Type("c_channel", Tag.BASIC, 8),
            stringWord  = new Type("string", Tag.BASIC, 8);
}
