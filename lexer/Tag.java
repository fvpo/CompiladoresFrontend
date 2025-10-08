package lexer;

public enum Tag {

    // 🔹 Fim de arquivo e erros
    EOF,            // fim do código-fonte
    UNKNOWN,        // token inválido

    // 🔹 Palavras-chave de controle de fluxo
    IF, ELSE, WHILE, FOR,
    BREAK, CONTINUE, RETURN,

    // 🔹 Blocos de execução
    SEQ,            // execução sequencial
    PAR,            // execução paralela (Thread)

    C_CHANNEL,      // tipo de variável canal de comunicação

    STRING,         // tipo string

    // 🔹 Literais
    TRUE, FALSE,    // booleanos
    NUM,            // número inteiro
    REAL,           // número real (float)

    // 🔹 Operadores lógicos
    AND, OR, NOT,

    // 🔹 Operadores relacionais
    EQ, NE, LT, LE, GT, GE,

    // 🔹 Operadores aritméticos
    PLUS, MINUS, MULT, DIV, MOD,

    // 🔹 Atribuição e outros operadores
    ASSIGN,         // =
    PLUS_ASSIGN,    // +=
    MINUS_ASSIGN,   // -=
    MULT_ASSIGN,    // *=
    DIV_ASSIGN,     // /=

    // 🔹 Operadores de incremento/decremento
    INC,            // ++
    DEC,            // --

    // 🔹 Símbolos e pontuação
    LPAREN, RPAREN,         // ( )
    LBRACE, RBRACE,         // { }
    LBRACKET, RBRACKET,     // [ ]

    // 🔹 Identificadores e auxiliares
    ID,             // identificador genérico
    TEMP,           // variável temporária interna
    INDEX,          // acesso a vetor/objeto
    COMMENT,        // comentário (inicia com #)

    INDENT,     // início de um novo bloco
    DEDENT,     // fim de um bloco anterior
    NEWLINE,    // quebra de linha
}
