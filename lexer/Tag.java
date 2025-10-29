package lexer;

public enum Tag {

    // 🔹 Fim de arquivo e erros
    EOF,            // fim do código-fonte
    UNKNOWN,        // token inválido
    EXTENDS,

    // 🔹 Estruturas de definição
    CLASS,          // 'class'
    NEW,            // 'new'

    // 🔹 Palavras-chave de controle de fluxo
    IF, ELSE, WHILE, FOR,
    BREAK, CONTINUE, RETURN,

    // 🔹 Blocos de execução e concorrência
    SEQ,            // execução sequencial
    PAR,            // execução paralela (threads ou blocos simultâneos)

    BASIC,

    // 🔹 Tipos não básicos
    STRING,
    C_CHANNEL,

    // 🔹 Comunicação (novos tokens)
    SEND,           // 'send' - envio de dados via canal/socket
    RECEIVE,        // 'receive' - recepção de dados via canal/socket

    // 🔹 Literais e identificadores
    TRUE, FALSE,    // booleanos
    NUM,            // número inteiro
    REAL,           // número real (float)
    TEXT,           // texto entre aspas (string literal)
    ID,             // identificador genérico

    // 🔹 Operadores lógicos
    AND, OR, NOT,

    // 🔹 Operadores relacionais
    EQ, NE, LT, LE, GT, GE,

    // 🔹 Operadores aritméticos
    PLUS, MINUS, MULT, DIV, MOD,

    // 🔹 Atribuição e operadores compostos
    ASSIGN,         // =
    PLUS_ASSIGN,    // +=
    MINUS_ASSIGN,   // -=
    MULT_ASSIGN,    // *=
    DIV_ASSIGN,     // /=

    // 🔹 Incremento/decremento
    INC,            // ++
    DEC,            // --

    // 🔹 Símbolos e pontuação
    LPAREN, RPAREN,         // ( )
    LBRACE, RBRACE,         // { }
    LBRACKET, RBRACKET,     // [ ]
    DOT,                    // .
    COMMA,                  // ,
    SEMICOLON,              // ;
    COLON,                  // :

    // 🔹 Estrutura de blocos por indentação
    INDENT,     // início de um novo bloco (tabulação)
    DEDENT,     // fim de bloco anterior (redução de indentação)
    NEWLINE,    // quebra de linha

    // 🔹 Comentários
    COMMENT,    // inicia com #, //, ou bloco de comentário

    // 🔹 Auxiliares internos
    TEMP,       // variável temporária interna
    INDEX,      // acesso a vetor/objeto

    // 🔹 Comando de saída
    PRINT
}
