package lexer;

public enum Tag {
    IF,
    ELSE,
    WHILE,
    DO,
    BREAK,

    TRUE,
    FALSE,

    BASIC,      // tipo básico (int, float, bool, etc.)
    ID,         // identificador (variável, função, classe, etc.)
    INDEX,      // acesso a array ou objeto
    TEMP,       // variável temporária usada internamente

    AND,        // &&
    OR,         // ||
    NOT,        // !

    EQ,         // ==
    NE,         // !=
    LE,         // <=
    GE,         // >=
    LT,         // <
    GT,         // >

    PLUS,       // +
    MINUS,      // -
    MULT,       // *
    DIV,        // /

    NUM,        // número inteiro
    REAL,       // número real (float, double)

    ASSIGN,     // =
    SEMICOLON,  // ;
    COMMA,      // ,
    DOT,        // .
    LPAREN,     // (
    RPAREN,     // )
    LBRACE,     // {
    RBRACE,     // }
    LBRACKET,   // [
    RBRACKET,   // ]

    EOF,        // fim de arquivo
    UNKNOWN     // token não reconhecido
}
