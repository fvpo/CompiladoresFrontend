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

    // 🔹 Palavras reservadas e operadores conhecidos
    public static final Word

            // ==============================
            // 🔸 Estruturas de definição
            // ==============================
            classWord   = new Word("class", Tag.CLASS),
            newWord     = new Word("new", Tag.NEW),

    // ==============================
    // 🔸 Palavras-chave de controle de fluxo
    // ==============================
    ifWord      = new Word("if", Tag.IF),
            elseWord    = new Word("else", Tag.ELSE),
            whileWord   = new Word("while", Tag.WHILE),
            forWord     = new Word("for", Tag.FOR),
            breakWord   = new Word("break", Tag.BREAK),
            continueWord= new Word("continue", Tag.CONTINUE),
            returnWord  = new Word("return", Tag.RETURN),
            printWord    = new Word("print", Tag.PRINT),

    // ==============================
    // 🔸 Blocos de execução e concorrência
    // ==============================
    seqWord     = new Word("seq", Tag.SEQ),
            parWord     = new Word("par", Tag.PAR),

    // ==============================
    // 🔸 Tipos de dados
    // ==============================
            stringWord  = new Word("string", Tag.STRING),
            cchannelWord = new Word("c_channel", Tag.C_CHANNEL),

    // ==============================
    // 🔸 Literais
    // ==============================
    trueWord    = new Word("true", Tag.TRUE),
            falseWord   = new Word("false", Tag.FALSE),

    // ==============================
    // 🔸 Operadores lógicos
    // ==============================
    andWord     = new Word("and", Tag.AND),
            orWord      = new Word("or", Tag.OR),
            notWord     = new Word("not", Tag.NOT),

    // ==============================
    // 🔸 Operadores relacionais
    // ==============================
    eqWord      = new Word("==", Tag.EQ),
            neWord      = new Word("!=", Tag.NE),
            ltWord      = new Word("<", Tag.LT),
            leWord      = new Word("<=", Tag.LE),
            gtWord      = new Word(">", Tag.GT),
            geWord      = new Word(">=", Tag.GE),

    // ==============================
    // 🔸 Operadores aritméticos
    // ==============================
    plusWord    = new Word("+", Tag.PLUS),
            minusWord   = new Word("-", Tag.MINUS),
            multWord    = new Word("*", Tag.MULT),
            divWord     = new Word("/", Tag.DIV),
            modWord     = new Word("%", Tag.MOD),

    // ==============================
    // 🔸 Atribuição e variações
    // ==============================
    assignWord      = new Word("=", Tag.ASSIGN),
            plusAssignWord  = new Word("+=", Tag.PLUS_ASSIGN),
            minusAssignWord = new Word("-=", Tag.MINUS_ASSIGN),
            multAssignWord  = new Word("*=", Tag.MULT_ASSIGN),
            divAssignWord   = new Word("/=", Tag.DIV_ASSIGN),

    // ==============================
    // 🔸 Incremento e decremento
    // ==============================
    incWord     = new Word("++", Tag.INC),
            decWord     = new Word("--", Tag.DEC),

    // ==============================
    // 🔸 Símbolos e pontuação
    // ==============================
    lparenWord  = new Word("(", Tag.LPAREN),
            rparenWord  = new Word(")", Tag.RPAREN),
            lbraceWord  = new Word("{", Tag.LBRACE),
            rbraceWord  = new Word("}", Tag.RBRACE),
            lbracketWord= new Word("[", Tag.LBRACKET),
            rbracketWord= new Word("]", Tag.RBRACKET),
            dotWord     = new Word(".", Tag.DOT),
            commaWord   = new Word(",", Tag.COMMA),
            semicolonWord = new Word(";", Tag.SEMICOLON),
            colonWord   = new Word(":", Tag.COLON),

    // ==============================
    // 🔸 Estrutura de blocos
    // ==============================
    indentWord  = new Word("<INDENT>", Tag.INDENT),
            dedentWord  = new Word("<DEDENT>", Tag.DEDENT),
            newlineWord = new Word("<NEWLINE>", Tag.NEWLINE),

    // ==============================
    // 🔸 Comentário
    // ==============================
    commentWord = new Word("#", Tag.COMMENT),

    // ==============================
    // 🔸 Literais e identificadores genéricos
    // ==============================
    numWord     = new Word("<NUM>", Tag.NUM),
            realWord    = new Word("<REAL>", Tag.REAL),
            textWord    = new Word("<TEXT>", Tag.TEXT),
            idWord      = new Word("<ID>", Tag.ID);
}
